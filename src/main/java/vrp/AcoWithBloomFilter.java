package vrp;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.*;

public class AcoWithBloomFilter extends AcoEngine {

    private static final double BLOOM_PENALTY = 0.15;
    private static final double BF_DECAY = 0.95;

    private final BloomFilter bloomFilter;
    private final boolean useTwoOpt;
    private final int maxSubPath;

    public AcoWithBloomFilter(VrpProblem problem, int numAnts, int maxIter,
                               double alpha, double beta, double rho, double q,
                               boolean useTwoOpt) {
        super(problem, numAnts, maxIter, alpha, beta, rho, q);
        // 평균 route 길이(= n / 추정 차량 수)의 1/3
        int totalDemand = problem.nodes.stream().mapToInt(nd -> nd.demand).sum();
        int estVehicles = (int) Math.ceil((double) totalDemand / problem.vehicleCapacity);
        int avgRouteLen = Math.max(2, (problem.size() - 1) / Math.max(1, estVehicles));
        this.maxSubPath = Math.max(2, avgRouteLen / 2);
        // decay 기반 steady-state 크기
        int expectedEdges = (int) Math.ceil(numAnts * problem.size() / (1.0 - BF_DECAY));
        int bitSize = BloomFilter.optimalBitSetSize(expectedEdges, 0.01);
        int numHash = BloomFilter.optimalNumHashes(bitSize, expectedEdges);
        this.bloomFilter = new BloomFilter(bitSize, numHash);
        this.useTwoOpt = useTwoOpt;
    }

    @Override
    protected List<List<Integer>> constructSolution() {
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
            int histSize = 0;
            while (true) {
                int next = selectNextBf(hist, histSize, current, visited, load, window);
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
        if (useTwoOpt) routes = LocalSearch.applyTwoOpt(routes, problem);
        return routes;
    }

    private int selectNextBf(int[] hist, int histSize, int current, boolean[] visited, int load, int[] window) {
        int n = problem.size();
        double[] prob = new double[n];
        double sum = fillBfProb(hist, histSize, current, visited, load, prob, window, candidateList[current]);
        if (sum == 0) {
            int[] all = new int[n - 1];
            for (int i = 1; i < n; i++) all[i - 1] = i;
            sum = fillBfProb(hist, histSize, current, visited, load, prob, window, all);
        }
        if (sum == 0) return -1;
        double r = ThreadLocalRandom.current().nextDouble() * sum;
        double cumul = 0;
        for (int i = 1; i < n; i++) {
            cumul += prob[i];
            if (cumul >= r) return i;
        }
        return -1;
    }

    private double fillBfProb(int[] hist, int histSize, int current,
                               boolean[] visited, int load, double[] prob,
                               int[] window, int[] nodes) {
        double sum = 0;
        for (int next : nodes) {
            if (visited[next]) continue;
            if (load + problem.nodes.get(next).demand > problem.vehicleCapacity) continue;
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
                if (bloomFilter.mightContain(new SubPathKey(window, len))) { hit = true; break; }
            }
            if (hit) w *= BLOOM_PENALTY;
            prob[next] = w;
            sum += w;
        }
        return sum;
    }

    protected void updatePheromone(List<List<List<Integer>>> allRoutes, double[] costs) {
        int n = problem.size();
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                pheromone[i][j] *= (1 - rho);

        double sumCost = 0;
        for (int a = 0; a < numAnts; a++) {
            depositPheromone(allRoutes.get(a), q / costs[a]);
            sumCost += costs[a];
        }

        bloomFilter.decay(BF_DECAY, rng);

        double avgCost = sumCost / numAnts;
        List<Integer> badAnts = new ArrayList<>();
        for (int a = 0; a < numAnts; a++)
            if (costs[a] >= avgCost) badAnts.add(a);
        Collections.shuffle(badAnts, rng);
        int sampleSize = Math.max(1, badAnts.size() / 3);

        for (int idx = 0; idx < sampleSize; idx++) {
            int a = badAnts.get(idx);
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
        for (int iter = 0; iter < maxIter; iter++) {
            List<List<List<Integer>>> allRoutes = IntStream.range(0, numAnts)
                .parallel()
                .mapToObj(a -> constructSolution())
                .collect(Collectors.toList());

            double[] costs = new double[numAnts];
            double iterBestDist = Double.MAX_VALUE;
            for (int a = 0; a < numAnts; a++) {
                double dist = VrpSolution.calcDistance(allRoutes.get(a), problem);
                costs[a] = dist;
                if (dist < iterBestDist) iterBestDist = dist;
                if (dist < bestDist) { bestDist = dist; bestRoutes = allRoutes.get(a); }
            }

            updatePheromone(allRoutes, costs);
            logConvergence(iter, iterBestDist);
        }
        return new VrpSolution(bestRoutes, bestDist);
    }

}
