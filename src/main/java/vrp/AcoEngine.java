package vrp;

import java.util.*;
import java.util.stream.*;
import java.lang.management.*;

public class AcoEngine {
    private static final int CANDIDATE_K_MIN = 20;
    private static final int CANDIDATE_K_MAX = 100;
    protected static final int ELITE_K = 3;

    protected final VrpProblem problem;
    protected double[][] pheromone;
    protected int[][] candidateList;
    protected final int numAnts;
    protected final int maxIter;
    protected final double alpha, beta, rho, q;
    protected Random rng = new Random();

    protected static final double VEHICLE_PENALTY = 1000.0;

    protected List<List<Integer>> bestRoutes;
    protected double bestDist = Double.MAX_VALUE;
    protected int bestVehicles = 0;
    protected final List<double[]> convergenceLog = new ArrayList<>(); // [iter_best, global_best, time_sec, mem_mb,
                                                                       // iter_vehicles, global_vehicles]
    protected long solveStartTime;
    protected int actualIters = 0;

    public List<double[]> getConvergenceLog() { return convergenceLog; }
    public int getActualIters() { return actualIters; }

    public int ealystop = 1000;

    protected void logConvergence(int iter, double iterBestCost, int iterBestVehicles) {
        if (iter % 5 == 0 || iter == maxIter - 1) {
            double timeSec = (System.nanoTime() - solveStartTime) / 1_000_000_000.0;
            double memMb = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / (1024.0 * 1024.0);
            convergenceLog.add(new double[] { iterBestCost, bestDist, timeSec, memMb, iterBestVehicles, bestVehicles });
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
        int k = Math.min(CANDIDATE_K_MAX, Math.max(CANDIDATE_K_MIN, (problem.size() - 1) / 5));
        buildCandidateList(k);
    }

    protected void initPheromone() {
        int n = problem.size();
        pheromone = new double[n][n];
        for (double[] row : pheromone)
            Arrays.fill(row, 1.0);
    }

    private void buildCandidateList(int k) {
        int n = problem.size();
        candidateList = new int[n][];
        for (int i = 0; i < n; i++) {
            List<Integer> nodes = new ArrayList<>();
            for (int j = 1; j < n; j++)
                if (j != i)
                    nodes.add(j);
            final int fi = i;
            nodes.sort(Comparator.comparingDouble(j -> problem.distMatrix[fi][j]));
            int size = Math.min(k, nodes.size());
            candidateList[i] = new int[size];
            for (int x = 0; x < size; x++)
                candidateList[i][x] = nodes.get(x);
        }
    }

    public VrpSolution solve() {
        solveStartTime = System.nanoTime();
        int noImprovCount = 0;
        for (int iter = 0; iter < maxIter; iter++) {
            long[] antSeeds = new long[numAnts];
            for (int a = 0; a < numAnts; a++) antSeeds[a] = rng.nextLong();
            List<List<List<Integer>>> allRoutes = IntStream.range(0, numAnts)
                    .parallel()
                    .mapToObj(a -> constructSolution(new Random(antSeeds[a])))
                    .collect(Collectors.toList());

            double[] costs = new double[numAnts];
            double iterBestCost = Double.MAX_VALUE;
            int iterBestVehicles = 0;
            double prevBest = bestDist;
            for (int a = 0; a < numAnts; a++) {
                double dist = VrpSolution.calcDistance(allRoutes.get(a), problem);
                int numV = allRoutes.get(a).size();
                double cost = dist + VEHICLE_PENALTY * numV;
                costs[a] = cost;
                if (cost < iterBestCost) {
                    iterBestCost = cost;
                    iterBestVehicles = numV;
                }
                if (cost < bestDist) {
                    bestDist = cost;
                    bestVehicles = numV;
                    bestRoutes = allRoutes.get(a);
                }
            }
            noImprovCount = (bestDist < prevBest - 1e-10) ? 0 : noImprovCount + 1;
            updatePheromone(allRoutes, costs);
            logConvergence(iter, iterBestCost, iterBestVehicles);
            actualIters = iter + 1;
            if (noImprovCount >= ealystop)
                break;
        }
        return new VrpSolution(bestRoutes, bestVehicles,
                bestDist - VEHICLE_PENALTY * bestVehicles);
    }

    protected List<List<Integer>> constructSolution(Random rng) {
        int n = problem.size();
        boolean[] visited = new boolean[n];
        visited[0] = true;
        int unvisited = n - 1;
        List<List<Integer>> routes = new ArrayList<>();

        while (true) {
            List<Integer> route = new ArrayList<>();
            int current = 0;
            int load = 0;
            double currentTime = 0;
            while (true) {
                int next = selectNext(current, visited, load, currentTime, rng);
                if (next == -1)
                    break;
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
                    if (!visited[i])
                        routes.add(new ArrayList<>(Collections.singletonList(i)));
                break;
            }
            routes.add(route);
            if (unvisited == 0)
                break;
        }
        return routes;
    }

    protected int selectNext(int current, boolean[] visited, int load, double currentTime, Random rng) {
        int n = problem.size();
        double[] prob = new double[n];
        double sum = 0;

        for (int i : candidateList[current]) {
            if (visited[i])
                continue;
            if (load + problem.nodes.get(i).demand > problem.vehicleCapacity)
                continue;
            if (!twFeasible(current, i, currentTime))
                continue;
            double tau = Math.pow(pheromone[current][i], alpha);
            double eta = Math.pow(1.0 / (problem.distMatrix[current][i] + 1e-10), beta);
            prob[i] = tau * eta;
            sum += prob[i];
        }

        if (sum == 0) {
            for (int i = 1; i < n; i++) {
                if (visited[i])
                    continue;
                if (load + problem.nodes.get(i).demand > problem.vehicleCapacity)
                    continue;
                if (!twFeasible(current, i, currentTime))
                    continue;
                double tau = Math.pow(pheromone[current][i], alpha);
                double eta = Math.pow(1.0 / (problem.distMatrix[current][i] + 1e-10), beta);
                prob[i] = tau * eta;
                sum += prob[i];
            }
        }

        if (sum == 0)
            return -1;
        double r = rng.nextDouble() * sum;
        double cumul = 0;
        for (int i = 1; i < n; i++) {
            cumul += prob[i];
            if (cumul >= r)
                return i;
        }
        return -1;
    }

    // Returns true if visiting node i next is time-window feasible and allows
    // return to depot.
    protected boolean twFeasible(int current, int i, double currentTime) {
        Node node = problem.nodes.get(i);
        double arrival = currentTime + problem.distMatrix[current][i];
        if (arrival > node.dueDate)
            return false;
        double departure = Math.max(arrival, node.readyTime) + node.serviceTime;
        return departure + problem.distMatrix[i][0] <= problem.nodes.get(0).dueDate;
    }

    protected int[] eliteOrder(double[] costs) {
        Integer[] idx = new Integer[numAnts];
        for (int i = 0; i < numAnts; i++) idx[i] = i;
        Arrays.sort(idx, Comparator.comparingDouble(i -> costs[i]));
        int[] order = new int[numAnts];
        for (int i = 0; i < numAnts; i++) order[i] = idx[i];
        return order;
    }

    protected void updatePheromone(List<List<List<Integer>>> allRoutes, double[] costs) {
        int n = problem.size();
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                pheromone[i][j] *= (1 - rho);

        int[] order = eliteOrder(costs);
        int eliteK = Math.min(ELITE_K, numAnts);
        for (int r = 0; r < eliteK; r++)
            depositPheromone(allRoutes.get(order[r]), q / costs[order[r]]);
    }



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
