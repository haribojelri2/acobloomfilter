package vrp;

import java.util.*;
import java.io.*;

public class ScaleExperiment {
    static final double ALPHA = 1.0, BETA = 2.0, RHO = 0.1, Q = 100.0;
    static final int RUNS = 30;
    static final int ANTS = 30, ITER = 100;

    static final String[][] INSTANCES = {
        {"50",   "C:/Users/user/Downloads/B/B/B-n50-k7.vrp"},
        {"100",  "C:/Users/user/Downloads/E/E/E-n101-k8.vrp"},
        {"200",  "C:/Users/user/Downloads/Golden/Golden/Golden_5.vrp"},
        {"500",  "C:/Users/user/Downloads/Golden/Golden/Golden_12.vrp"},
        {"1000", "C:/Users/user/Downloads/XL/XL/XL-n1048-k237.vrp"},
        {"2000", "C:/Users/user/Downloads/XL/XL/XL-n2028-k617.vrp"},
        // {"4000", "C:/Users/user/Downloads/XL/XL/XL-n3975-k687.vrp"},
        // {"6000", "C:/Users/user/Downloads/XL/XL/XL-n6168-k1922.vrp"},
        // {"8000", "C:/Users/user/Downloads/XL/XL/XL-n8028-k294.vrp"},
        // {"10000","C:/Users/user/Downloads/XL/XL/XL-n10001-k1570.vrp"},
    };

   static final String[] ALGOS = {"ACO", "ACO+Tabu", "TabuLikeBF+fpr=0.001", "TabuLikeBF+fpr=0.01", "TabuLikeBF+fpr=0.05", "TabuLikeBF+fpr=0.20"};
    // static final String[] ALGOS = {"ACO+Tabu", "TabuLikeBF+fpr=0.001", "TabuLikeBF+fpr=0.01", "TabuLikeBF+fpr=0.05", "TabuLikeBF+fpr=0.20"};
    public static void main(String[] args) throws Exception {
        new File("out").mkdirs();
        for (String[] entry : INSTANCES) {
            String sizeLabel = entry[0];
            String path = entry[1];
            VrpParser.ParsedInstance inst = VrpParser.parse(path);
            VrpProblem p = inst.problem;
            int opt = inst.optimalValue;
            System.out.printf("%n=== size~%s  name=%s  N=%d  opt=%s ===%n",
                sizeLabel, inst.name, p.size(), opt < 0 ? "N/A" : String.valueOf(opt));
            System.out.printf("%-16s %9s %8s %9s%n", "algo", "avg_cost", "best_cost", "avg_time");
            System.out.println("-".repeat(48));

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
        double[] logSum = null;
        double[] seedBests = new double[RUNS];
        double[] seedTimes = new double[RUNS];

        for (int r = 0; r < RUNS; r++) {
            AcoEngine engine = makeEngine(algo, p);
            engine.rng = new Random(r);
            long t0 = System.nanoTime();
            VrpSolution sol = engine.solve();
            double elapsed = (System.nanoTime() - t0) / 1e9;
            sumTime += elapsed;
            seedTimes[r] = elapsed;
            double cost = sol.totalDistance;
            sumCost += cost;
            if (cost < bestCost) bestCost = cost;
            seedBests[r] = cost;

            List<double[]> log = engine.getConvergenceLog();
            if (logSum == null) logSum = new double[log.size() * 4];
            for (int i = 0; i < log.size(); i++) {
                logSum[i*4+0] += log.get(i)[0]; // iter_best
                logSum[i*4+1] += log.get(i)[1]; // global_best
                logSum[i*4+2] += log.get(i)[2]; // time_sec
                logSum[i*4+3] += log.get(i)[3]; // mem_mb
            }
        }

        double avgCost = sumCost / RUNS;
        double avgTime = sumTime / RUNS;
        System.out.printf("%-16s %9.2f %8.2f %8.2fs%n", algo, avgCost, bestCost, avgTime);

        String tag = sizeLabel + "_" + algo.replace("+", "_");
        saveConvergence(tag, logSum);
        saveSeedBests(tag, seedBests, seedTimes);
    }

    static AcoEngine makeEngine(String algo, VrpProblem p) {
        return switch (algo) {
            case "ACO"          -> new AcoEngine(p, ANTS, ITER, ALPHA, BETA, RHO, Q);
            case "ACO+2opt"      -> new AcoWithLocalSearch(p, ANTS, ITER, ALPHA, BETA, RHO, Q, AcoWithLocalSearch.Mode.TWO_OPT);
            case "ACO+Tabu"     -> new AcoWithLocalSearch(p, ANTS, ITER, ALPHA, BETA, RHO, Q, AcoWithLocalSearch.Mode.TABU);
            case "ACO+2opt+Tabu"-> new AcoWithLocalSearch(p, ANTS, ITER, ALPHA, BETA, RHO, Q, AcoWithLocalSearch.Mode.TWO_OPT_TABU);
            case "ACO+BF"       -> new AcoWithBloomFilter(p, ANTS, ITER, ALPHA, BETA, RHO, Q, false);
            case "ACO+2opt+BF"       -> new AcoWithBloomFilter(p, ANTS, ITER, ALPHA, BETA, RHO, Q, true);
            case "ACO+TabuLikeBF"       -> new AcoWithTabuLikeBF(p, ANTS, ITER, ALPHA, BETA, RHO, Q, false);
            case "ACO+2opt+TabuLikeBF"  -> new AcoWithTabuLikeBF(p, ANTS, ITER, ALPHA, BETA, RHO, Q, true);
            case "TabuLikeBF+fpr=0.001" -> new AcoWithTabuLikeBF(p, ANTS, ITER, ALPHA, BETA, RHO, Q, true, 0.001);
            case "TabuLikeBF+fpr=0.01"  -> new AcoWithTabuLikeBF(p, ANTS, ITER, ALPHA, BETA, RHO, Q, true, 0.01);
            case "TabuLikeBF+fpr=0.05"  -> new AcoWithTabuLikeBF(p, ANTS, ITER, ALPHA, BETA, RHO, Q, true, 0.05);
            case "TabuLikeBF+fpr=0.20"  -> new AcoWithTabuLikeBF(p, ANTS, ITER, ALPHA, BETA, RHO, Q, true, 0.20);
            default -> throw new IllegalArgumentException(algo);
        };
    }

    static void saveConvergence(String tag, double[] logSum) throws Exception {
        int checkpoints = logSum.length / 4;
        try (PrintWriter pw = new PrintWriter(new FileWriter("out/conv_" + tag + ".csv"))) {
            pw.println("iter,iter_best_cost,global_best_cost,time_sec,mem_mb");
            for (int i = 0; i < checkpoints; i++)
                pw.printf("%d,%.4f,%.4f,%.4f,%.2f%n",
                    i * 5,
                    logSum[i*4+0] / RUNS,
                    logSum[i*4+1] / RUNS,
                    logSum[i*4+2] / RUNS,
                    logSum[i*4+3] / RUNS);
        }
    }

    static void saveSeedBests(String tag, double[] seedBests, double[] seedTimes) throws Exception {
        try (PrintWriter pw = new PrintWriter(new FileWriter("out/seeds_" + tag + ".csv"))) {
            pw.println("seed,best_cost,time_sec");
            for (int r = 0; r < seedBests.length; r++)
                pw.printf("%d,%.4f,%.6f%n", r, seedBests[r], seedTimes[r]);
        }
    }
}
