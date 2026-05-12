package vrp;

import java.util.*;
import java.util.stream.*;

public class AcoWithBloomFilter extends AcoEngine {

    private static final double BLOOM_BONUS = 0.5;
    private static final double BF_DECAY = 0.8;
    private final BloomFilter bloomFilter;
    private final boolean useTwoOpt;
    private final boolean useTabu;
    private final boolean useTabuLikeBF;
    private final int maxSubPath;
    private double totalHitRate = 0.0;
    private int hitRateCount = 0;

    public AcoWithBloomFilter(VrpProblem problem, int numAnts, int maxIter,
            double alpha, double beta, double rho, double q,
                               boolean useTwoOpt) {
        this(problem, numAnts, maxIter, alpha, beta, rho, q, useTwoOpt, false, false);
    }

    public AcoWithBloomFilter(VrpProblem problem, int numAnts, int maxIter,
            double alpha, double beta, double rho, double q,
                               boolean useTwoOpt, boolean useTabu) {
        this(problem, numAnts, maxIter, alpha, beta, rho, q, useTwoOpt, useTabu, false);
    }

    public AcoWithBloomFilter(VrpProblem problem, int numAnts, int maxIter,
            double alpha, double beta, double rho, double q,
                               boolean useTwoOpt, boolean useTabu, boolean useTabuLikeBF) {
        super(problem, numAnts, maxIter, alpha, beta, rho, q);
        int totalDemand = problem.nodes.stream().mapToInt(nd -> nd.demand).sum();
        int estVehicles = (int) Math.ceil((double) totalDemand / problem.vehicleCapacity);
        int avgRouteLen = Math.max(2, (problem.size() - 1) / Math.max(1, estVehicles));
        this.maxSubPath = Math.max(2, avgRouteLen / 2);
        int expectedEdges = (int) Math.ceil(numAnts * problem.size() / (1.0 - BF_DECAY));
        int bitSize = BloomFilter.optimalBitSetSize(expectedEdges, 0.01);
        int numHash = BloomFilter.optimalNumHashes(bitSize, expectedEdges);
        this.bloomFilter = new BloomFilter(bitSize, numHash);
        this.useTwoOpt = useTwoOpt;
        this.useTabu = useTabu;
        this.useTabuLikeBF = useTabuLikeBF;
    }

    @Override
    protected List<List<Integer>> constructSolution(Random rng) {
        int n = problem.size();
        boolean[] visited = new boolean[n];
        visited[0] = true;
        int unvisited = n - 1;
        List<List<Integer>> routes = new ArrayList<>();
        int[] hist = new int[maxSubPath - 1];
        int[] window = new int[maxSubPath];

        while (true) {
            List<Integer> route = new ArrayList<>();
            int current = 0;
            int load = 0;
            double currentTime = 0;
            int histSize = 0;
            while (true) {
                int next = selectNextBf(hist, histSize, current, visited, load, currentTime, window, rng);
                if (next == -1) break;
                if (histSize < maxSubPath - 1) {
                    hist[histSize++] = current;
                } else {
                    System.arraycopy(hist, 1, hist, 0, histSize - 1);
                    hist[histSize - 1] = current;
                }
                visited[next] = true;
                unvisited--;
                route.add(next);
                load += problem.nodes.get(next).demand;
                double arrival = currentTime + problem.distMatrix[current][next];
                currentTime = Math.max(arrival, problem.nodes.get(next).readyTime)
                              + problem.nodes.get(next).serviceTime;
                current = next;
            }
            if (route.isEmpty()) {
                for (int i = 1; i < n; i++)
                    if (!visited[i]) routes.add(new ArrayList<>(Collections.singletonList(i)));
                break;
            }
            routes.add(route);
            if (unvisited == 0) break;
        }
        return routes;
    }

    private int selectNextBf(int[] hist, int histSize, int current, boolean[] visited, int load, double currentTime, int[] window, Random rng) {
        int n = problem.size();
        double[] prob = new double[n];
        double sum = fillBfProb(hist, histSize, current, visited, load, currentTime, prob, window, candidateList[current]);
        if (sum == 0) {
            int[] all = new int[n - 1];
            for (int i = 1; i < n; i++) all[i - 1] = i;
            sum = fillBfProb(hist, histSize, current, visited, load, currentTime, prob, window, all);
        }
        if (sum == 0) return -1;
        double r = rng.nextDouble() * sum;
        double cumul = 0;
        for (int i = 1; i < n; i++) {
            cumul += prob[i];
            if (cumul >= r) return i;
        }
        return -1;
    }

    private double fillBfProb(int[] hist, int histSize, int current,
                               boolean[] visited, int load, double currentTime, double[] prob,
                               int[] window, int[] nodes) {
        double sum = 0;
        for (int next : nodes) {
            if (visited[next]) continue;
            if (load + problem.nodes.get(next).demand > problem.vehicleCapacity) continue;
            if (!twFeasible(current, next, currentTime)) continue;
            double tau = Math.pow(pheromone[current][next], alpha);
            double eta = Math.pow(1.0 / (problem.distMatrix[current][next] + 1e-10), beta);
            double w = tau * eta;
            boolean hit = false;
            for (int len = 2; len <= histSize + 2 && len <= maxSubPath; len++) {
                int hNeeded = len - 2;
                int hStart = histSize - hNeeded;
                for (int k = 0; k < hNeeded; k++) window[k] = hist[hStart + k];
                window[hNeeded] = current;
                window[hNeeded + 1] = next;
                if (bloomFilter.mightContainPath(window, len)) { hit = true; break; }
            }
            if (hit) w *= BLOOM_BONUS;
            prob[next] = w;
            sum += w;
        }
        return sum;
    }

    @Override
    protected void updatePheromone(List<List<List<Integer>>> allRoutes, double[] costs) {
        int n = problem.size();
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                pheromone[i][j] *= (1 - rho);

        int[] order = eliteOrder(costs);
        int eliteK = Math.min(ELITE_K, numAnts);
        for (int r = 0; r < eliteK; r++)
            depositPheromone(allRoutes.get(order[r]), q / costs[order[r]]);

        bloomFilter.decay(BF_DECAY, rng);

        int sampleSize = Math.max(1, eliteK / 3);
        for (int r = 0; r < sampleSize; r++) {
            int a = order[numAnts - 1 - r];
            for (List<Integer> route : allRoutes.get(a)) {
                if (route.size() < 2) continue;
                int maxLen = Math.min(route.size(), maxSubPath);
                int len = (maxLen == 2) ? 2 : (2 + rng.nextInt(maxLen - 1));
                int start = rng.nextInt(route.size() - len + 1);
                int[] subPath = new int[len];
                for (int k = 0; k < len; k++) subPath[k] = route.get(start + k);
                bloomFilter.add(new SubPathKey(subPath, len));
            }
        }

    }

    @Override
    public VrpSolution solve() {
        solveStartTime = System.nanoTime();
        int noImprovCount = 0;
        for (int iter = 0; iter < maxIter; iter++) {
            bloomFilter.resetStats();

            long[] antSeeds = new long[numAnts];
            for (int a = 0; a < numAnts; a++) antSeeds[a] = rng.nextLong();
            List<List<List<Integer>>> allRoutes = IntStream.range(0, numAnts)
                .parallel()
                .mapToObj(a -> constructSolution(new Random(antSeeds[a])))
                .collect(Collectors.toList());


            double[] costs = new double[numAnts];
            for (int a = 0; a < numAnts; a++) {
                double dist = VrpSolution.calcDistance(allRoutes.get(a), problem);
                costs[a] = dist + VEHICLE_PENALTY * allRoutes.get(a).size();
            }

            if (useTwoOpt || useTabu || useTabuLikeBF) {
                int[] order = eliteOrder(costs);
                int eliteK = Math.min(ELITE_K, numAnts);
                for (int r = 0; r < eliteK; r++) {
                    int a = order[r];
                    List<List<Integer>> improved = allRoutes.get(a);
                    if (useTwoOpt)    improved = LocalSearch.applyTwoOpt(improved, problem, candidateList);
                    if (useTabu)      improved = LocalSearch.applyTabu(improved, problem, candidateList, rng);
                    if (useTabuLikeBF) improved = AcoWithTabuLikeBF.tabuWithBF(improved, problem, 50, 0.05, candidateList, rng);
                    allRoutes.set(a, improved);
                    double dist = VrpSolution.calcDistance(allRoutes.get(a), problem);
                    costs[a] = dist + VEHICLE_PENALTY * allRoutes.get(a).size();
                }
            }

            double iterBestCost = Double.MAX_VALUE;
            int iterBestVehicles = 0;
            double prevBest = bestDist;
            for (int a = 0; a < numAnts; a++) {
                int numV = allRoutes.get(a).size();
                if (costs[a] < iterBestCost) { iterBestCost = costs[a]; iterBestVehicles = numV; }
                if (costs[a] < bestDist) { bestDist = costs[a]; bestVehicles = numV; bestRoutes = allRoutes.get(a); }
            }
            noImprovCount = (bestDist < prevBest - 1e-10) ? 0 : noImprovCount + 1;
            actualIters = iter + 1;

            if (bloomFilter.getQueryCount() > 0) {
                totalHitRate += bloomFilter.getHitRate();
                hitRateCount++;
            }

            updatePheromone(allRoutes, costs);
            logConvergence(iter, iterBestCost, iterBestVehicles);
            if (noImprovCount >= ealystop) break;
        }
        return new VrpSolution(bestRoutes, bestVehicles,
            bestDist - VEHICLE_PENALTY * bestVehicles);
    }

    public double getBfHitRate() {
        return hitRateCount > 0 ? totalHitRate / hitRateCount : Double.NaN;
    }
}
