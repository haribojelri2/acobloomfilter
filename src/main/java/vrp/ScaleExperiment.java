package vrp;

import java.util.*;
import java.io.*;

public class ScaleExperiment {
    static final double ALPHA = 1.0, BETA = 2.0, RHO = 0.1, Q = 100.0;
    // === Solomon + Homberger (RUNS=30, ANTS=30, ITER=100) ===
    static final int RUNS = 30;
    static final int ANTS = 30, ITER = 300;
    static final String[][] INSTANCES = {
        {"R101","solomon_benchmark/R101.txt"},
        {"R201","solomon_benchmark/R201.txt"},
        {"RC101","solomon_benchmark/RC101.txt"},
        {"RC201","solomon_benchmark/RC201.txt"},
        // {"C1_10_1",  "homberger_benchmark/1000/C1_10_1.TXT"},
        // {"C2_10_1",  "homberger_benchmark/1000/C2_10_1.TXT"},
        // {"R1_10_1",  "homberger_benchmark/1000/R1_10_1.TXT"},
        // {"R2_10_1",  "homberger_benchmark/1000/R2_10_1.TXT"},
        // {"RC1_10_1", "homberger_benchmark/1000/RC1_10_1.TXT"},
        // {"RC2_10_1", "homberger_benchmark/1000/RC2_10_1.TXT"},
        // {"XL-n3007",  "cvrp_benchmark/XL-n3007-k658.vrp",   "cvrp"},
        // {"XL-n4063", "cvrp_benchmark/XL-n4063-k347.vrp", "cvrp"},
    };


    static final String[] ALGOS = {
        "ACO+Tabu",
        "ACO+TabuLikeBF@0.05"
        // "ACO+TabuLikeBF@0.01",
        // "ACO+TabuLikeBF@0.03",
        // "ACO+TabuLikeBF@0.05",
        // "ACO+TabuLikeBF@0.10",
        // "ACO+TabuLikeBF@0.20",
    };

    public static void main(String[] args) throws Exception {
        new File("out").mkdirs();
        for (String[] entry : INSTANCES) {
            String sizeLabel = entry[0];
            String path = entry[1];
            boolean isCvrp = entry.length > 2 && entry[2].equals("cvrp");
            VrpParser.ParsedInstance inst = isCvrp ? VrpParser.parseCvrp(path) : VrpParser.parse(path);
            VrpProblem p = inst.problem;
            int opt = inst.optimalValue;
            System.out.printf("%n=== size~%s  name=%s  N=%d  opt=%s ===%n",
                sizeLabel, inst.name, p.size(), opt < 0 ? "N/A" : String.valueOf(opt));
            System.out.printf("%-22s %9s %8s %9s %10s %10s %8s %10s%n",
                "algo", "avg_cost", "best_cost", "avg_sec", "gc_delta_kb", "tabu_bytes", "avg_veh", "peak_heap_mb");
            System.out.println("-".repeat(97));

            for (String algo : ALGOS) {
                runAlgo(algo, sizeLabel, p, opt);
            }
        }
        System.out.println("\nDone. Results saved to out/");
    }

    static void runAlgo(String algo, String sizeLabel, VrpProblem p, int opt) throws Exception {
        double sumCost = 0;
        double bestCost = Double.MAX_VALUE;
        double sumTime = 0;
        double sumGcDelta = 0;
        double sumHeapMb = 0;
        int sumVehicles = 0;
        double[] seedBests = new double[RUNS];
        double[] seedTimes = new double[RUNS];
        List<double[]> bestRunLog = null;

        for (int r = 0; r < RUNS; r++) {
            AcoEngine engine = makeEngine(algo, p);
            engine.rng = new Random(r);

            // GC-based memory delta
            System.gc(); Thread.sleep(50);
            long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            VrpSolution sol = engine.solve();
            System.gc(); Thread.sleep(50);
            long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long gcDelta = Math.max(0, memAfter - memBefore);
            sumGcDelta += gcDelta / 1024.0; // KB

            if (!VrpSolution.isFeasible(sol.routes, p))
                throw new RuntimeException("INFEASIBLE solution: algo=" + algo + " run=" + r);
            double cost = sol.totalDistance;
            sumCost += cost;
            sumVehicles += sol.numVehicles;
            seedBests[r] = cost;
            if (cost < bestCost) {
                bestCost = cost;
                bestRunLog = engine.getConvergenceLog();
            }

            List<double[]> log = engine.getConvergenceLog();
            double t = log.get(log.size() - 1)[2];
            double peakHeap = log.stream().mapToDouble(e -> e[3]).max().orElse(0);
            sumTime += t;
            sumHeapMb += peakHeap;
            seedTimes[r] = t;
        }

        long theoBytes = theoreticalTabuBytes(algo, p.size());
        System.out.printf("%-22s %9.2f %8.2f %9.2f %10.1f %10d %8.1f %10.1f%n",
            algo, sumCost / RUNS, bestCost, sumTime / RUNS, sumGcDelta / RUNS,
            theoBytes, (double) sumVehicles / RUNS, sumHeapMb / RUNS);

        String tag = sizeLabel + "_" + algo.replace("+", "_").replace("@", "_").replace(".", "p");
        saveBestRunConvergence(tag, bestRunLog);
        saveSeedBests(tag, seedBests);
        saveSeedTimes(tag, seedTimes);
    }

    // Theoretical memory: bytes used by tabu data structure per search invocation
    static long theoreticalTabuBytes(String algo, int N) {
        int tenureMin = Math.max(3, (int) Math.ceil(Math.sqrt(N - 1) / 2));
        int tenureMax = Math.max(10, (int) Math.ceil(Math.sqrt(N - 1) * 2));
        int tenureAvg = (tenureMin + tenureMax) / 2;
        if (algo.equals("ACO") || algo.equals("ACO+BF")) return 0;
        if (algo.equals("ACO+Tabu") || algo.equals("ACO+TabuNarrow") ||
            algo.equals("ACO+TabuSubpath") || algo.equals("ACO+Tabu2opt"))
            return tenureAvg * 88L + 128;
        if (algo.startsWith("ACO+TabuLikeBF")) {
            double fpr = algo.contains("@")
                ? Double.parseDouble(algo.substring(algo.indexOf('@') + 1))
                : 0.05;
            int bitSize = BloomFilter.optimalBitSetSize(tenureAvg, fpr);
            int roundedBitSize = Integer.highestOneBit(bitSize - 1) << 1;
            long arrayBytes = (long)((roundedBitSize + 63) / 64) * 8;
            return arrayBytes * 2 + 48;
        }
        return 0;
    }

    static AcoEngine makeEngine(String algo, VrpProblem p) {
        if (algo.startsWith("ACO+TabuLikeBF@")) {
            double fpr = Double.parseDouble(algo.substring(algo.indexOf('@') + 1));
            return new AcoWithTabuLikeBF(p, ANTS, ITER, ALPHA, BETA, RHO, Q, false, fpr);
        }
        return switch (algo) {
            case "ACO"                   -> new AcoEngine(p, ANTS, ITER, ALPHA, BETA, RHO, Q);
            case "ACO+Tabu"              -> new AcoWithLocalSearch(p, ANTS, ITER, ALPHA, BETA, RHO, Q, AcoWithLocalSearch.Mode.TABU);
            case "ACO+TabuNarrow"        -> new AcoWithLocalSearch(p, ANTS, ITER, ALPHA, BETA, RHO, Q, AcoWithLocalSearch.Mode.TABU_NARROW);
            case "ACO+TabuSubpath"       -> new AcoWithLocalSearch(p, ANTS, ITER, ALPHA, BETA, RHO, Q, AcoWithLocalSearch.Mode.TABU_SUBPATH);
            case "ACO+TabuLikeBF"        -> new AcoWithTabuLikeBF(p, ANTS, ITER, ALPHA, BETA, RHO, Q, false, 0.05);
            case "ACO+TabuLikeBFNarrow"  -> new AcoWithTabuLikeBF(p, ANTS, ITER, ALPHA, BETA, RHO, Q, false, 0.05, AcoWithTabuLikeBF.TabuMode.NARROW);
            case "ACO+TabuLikeBFSubpath" -> new AcoWithTabuLikeBF(p, ANTS, ITER, ALPHA, BETA, RHO, Q, false, 0.05, AcoWithTabuLikeBF.TabuMode.SUBPATH);
            case "ACO+Tabu2opt"          -> new AcoWithLocalSearch(p, ANTS, ITER, ALPHA, BETA, RHO, Q, AcoWithLocalSearch.Mode.TABU_2OPT_RELOCATE);
            case "ACO+TabuLikeBF2opt"    -> new AcoWithTabuLikeBF(p, ANTS, ITER, ALPHA, BETA, RHO, Q, false, 0.05, AcoWithTabuLikeBF.TabuMode.CUST_2OPT);
            default -> throw new IllegalArgumentException(algo);
        };
    }

    // static void saveConvergence(String tag, double[] logSum) throws Exception {
    //     int checkpoints = logSum.length / 6;
    //     try (PrintWriter pw = new PrintWriter(new FileWriter("out/conv_" + tag + ".csv"))) {
    //         pw.println("iter,iter_best_cost,global_best_cost,time_sec,mem_mb,iter_best_vehicles,global_best_vehicles");
    //         for (int i = 0; i < checkpoints; i++)
    //             pw.printf("%d,%.4f,%.4f,%.4f,%.2f,%.1f,%.1f%n",
    //                 i * 5,
    //                 logSum[i*6+0] / RUNS,
    //                 logSum[i*6+1] / RUNS,
    //                 logSum[i*6+2] / RUNS,
    //                 logSum[i*6+3] / RUNS,
    //                 logSum[i*6+4] / RUNS,
    //                 logSum[i*6+5] / RUNS);
    //     }
    // }

    static void saveBestRunConvergence(String tag, List<double[]> log) throws Exception {
        try (PrintWriter pw = new PrintWriter(new FileWriter("out/best_conv_" + tag + ".csv"))) {
            pw.println("iter,global_best_cost,global_best_vehicles,heap_mb");
            for (int i = 0; i < log.size(); i++) {
                double actualCost = log.get(i)[1] - AcoEngine.VEHICLE_PENALTY * log.get(i)[5];
                pw.printf("%d,%.4f,%.0f,%.2f%n", i * 5, actualCost, log.get(i)[5], log.get(i)[3]);
            }
        }
    }

    static void saveSeedBests(String tag, double[] seedBests) throws Exception {
        try (PrintWriter pw = new PrintWriter(new FileWriter("out/seeds_" + tag + ".csv"))) {
            pw.println("best_cost");
            for (double v : seedBests)
                pw.printf("%.4f%n", v);
        }
    }

    static void saveSeedTimes(String tag, double[] seedTimes) throws Exception {
        try (PrintWriter pw = new PrintWriter(new FileWriter("out/times_" + tag + ".csv"))) {
            pw.println("time_sec");
            for (double v : seedTimes)
                pw.printf("%.4f%n", v);
        }
    }
}
