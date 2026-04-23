package vrp;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.*;

public class AcoEngine {
    protected static final int CANDIDATE_K = 20;

    protected final VrpProblem problem;
    protected double[][] pheromone;
    protected int[][] candidateList;
    protected final int numAnts;
    protected final int maxIter;
    protected final double alpha, beta, rho, q;
    protected final Random rng = new Random();

    protected List<List<Integer>> bestRoutes;
    protected double bestDist = Double.MAX_VALUE;
    protected final List<double[]> convergenceLog = new ArrayList<>(); // [iter_best, global_best, time_ms, mem_bytes]
    protected long solveStartTime;

    public List<double[]> getConvergenceLog() { return convergenceLog; }

    protected void logConvergence(int iter, double iterBestCost) {
        if (iter % 5 == 0 || iter == maxIter - 1) {
            double timeSec = (System.nanoTime() - solveStartTime) / 1_000_000_000.0;
            double memMb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024.0 * 1024.0);
            convergenceLog.add(new double[]{iterBestCost, bestDist, timeSec, memMb});
        }
    }

    public AcoEngine(VrpProblem problem, int numAnts, int maxIter,
            double alpha, double beta, double rho, double q) {
        this.problem = problem;
        this.numAnts = numAnts;
        this.maxIter = maxIter;
        this.alpha = alpha;
        this.beta = beta;
        this.rho = rho;
        this.q = q;
        initPheromone();
        buildCandidateList(CANDIDATE_K);
    }

    protected void initPheromone() {
        int n = problem.size();
        pheromone = new double[n][n];
        double nnDist = nearestNeighborDist();
        double tau0 = (nnDist > 0) ? numAnts / nnDist : 1.0;
        for (double[] row : pheromone)
            Arrays.fill(row, tau0);
    }

    private void buildCandidateList(int k) {
        int n = problem.size();
        candidateList = new int[n][];
        for (int i = 0; i < n; i++) {
            List<Integer> nodes = new ArrayList<>();
            for (int j = 1; j < n; j++) if (j != i) nodes.add(j);
            final int fi = i;
            nodes.sort(Comparator.comparingDouble(j -> problem.distMatrix[fi][j]));
            int size = Math.min(k, nodes.size());
            candidateList[i] = new int[size];
            for (int x = 0; x < size; x++) candidateList[i][x] = nodes.get(x);
        }
    }

    private double nearestNeighborDist() {
        int n = problem.size();
        boolean[] visited = new boolean[n];
        visited[0] = true;
        double dist = 0;
        int current = 0;
        for (int step = 1; step < n; step++) {
            double minD = Double.MAX_VALUE;
            int nearest = -1;
            for (int i = 1; i < n; i++) {
                if (!visited[i] && problem.distMatrix[current][i] < minD) {
                    minD = problem.distMatrix[current][i];
                    nearest = i;
                }
            }
            if (nearest == -1) break;
            dist += minD;
            visited[nearest] = true;
            current = nearest;
        }
        dist += problem.distMatrix[current][0];
        return dist;
    }

    public VrpSolution solve() {
        solveStartTime = System.nanoTime();
        for (int iter = 0; iter < maxIter; iter++) {
            List<List<List<Integer>>> allRoutes = IntStream.range(0, numAnts)
                .parallel()
                .mapToObj(a -> constructSolution())
                .collect(Collectors.toList());
            double iterBestDist = Double.MAX_VALUE;
            for (List<List<Integer>> routes : allRoutes) {
                double dist = VrpSolution.calcDistance(routes, problem);
                if (dist < iterBestDist) iterBestDist = dist;
                if (dist < bestDist) { bestDist = dist; bestRoutes = routes; }
            }
            updatePheromone(allRoutes);
            logConvergence(iter, iterBestDist);
        }
        return new VrpSolution(bestRoutes, bestDist);
    }

    protected List<List<Integer>> constructSolution() {
        int n = problem.size();
        boolean[] visited = new boolean[n];
        visited[0] = true;
        List<List<Integer>> routes = new ArrayList<>();

        while (true) {
            List<Integer> route = new ArrayList<>();
            int current = 0;
            int load = 0;
            boolean added = true;
            while (added) {
                added = false;
                int next = selectNext(current, visited, load);
                if (next == -1)
                    break;
                visited[next] = true;
                route.add(next);
                load += problem.nodes.get(next).demand;
                current = next;
                added = true;
            }
            if (route.isEmpty()) {
                for (int i = 1; i < n; i++)
                    if (!visited[i]) routes.add(new ArrayList<>(Collections.singletonList(i)));
                break;
            }
            routes.add(route);
            boolean allVisited = true;
            for (int i = 1; i < n; i++)
                if (!visited[i]) {
                    allVisited = false;
                    break;
                }
            if (allVisited)
                break;
        }
        return routes;
    }

    protected int selectNext(int current, boolean[] visited, int load) {
        int n = problem.size();
        double[] prob = new double[n];
        double sum = 0;

        for (int i : candidateList[current]) {
            if (visited[i]) continue;
            if (load + problem.nodes.get(i).demand > problem.vehicleCapacity) continue;
            double tau = Math.pow(pheromone[current][i], alpha);
            double eta = Math.pow(1.0 / (problem.distMatrix[current][i] + 1e-10), beta);
            prob[i] = tau * eta;
            sum += prob[i];
        }

        if (sum == 0) {
            for (int i = 1; i < n; i++) {
                if (visited[i]) continue;
                if (load + problem.nodes.get(i).demand > problem.vehicleCapacity) continue;
                double tau = Math.pow(pheromone[current][i], alpha);
                double eta = Math.pow(1.0 / (problem.distMatrix[current][i] + 1e-10), beta);
                prob[i] = tau * eta;
                sum += prob[i];
            }
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

    protected void updatePheromone(List<List<List<Integer>>> allRoutes) {
        int n = problem.size();
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                pheromone[i][j] *= (1 - rho);

        for (List<List<Integer>> routes : allRoutes) {
            double dist = VrpSolution.calcDistance(routes, problem);
            double delta = q / dist;
            depositPheromone(routes, delta);
        }
    }

    public double getBfHitRate() { return Double.NaN; }

    protected void depositPheromone(List<List<Integer>> routes, double delta) {
        for (List<Integer> route : routes) {
            if (route.isEmpty())
                continue;
            pheromone[0][route.get(0)] += delta;
            for (int i = 0; i < route.size() - 1; i++)
                pheromone[route.get(i)][route.get(i + 1)] += delta;
            pheromone[route.get(route.size() - 1)][0] += delta;
        }
    }
}
