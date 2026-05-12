package vrp;

import java.util.*;
import java.util.stream.*;

public class AcoWithLocalSearch extends AcoEngine {
    public enum Mode { TABU, TWO_OPT, TWO_OPT_TABU, TABU_NARROW, TABU_SUBPATH, TABU_2OPT_RELOCATE }
    private final Mode mode;

    public AcoWithLocalSearch(VrpProblem problem, int numAnts, int maxIter,
                               double alpha, double beta, double rho, double q, Mode mode) {
        super(problem, numAnts, maxIter, alpha, beta, rho, q);
        this.mode = mode;
    }

    private List<List<Integer>> applyLS(List<List<Integer>> routes) {
        return switch (mode) {
            case TWO_OPT      -> LocalSearch.applyTwoOpt(routes, problem, candidateList);
            case TABU         -> LocalSearch.applyTabu(routes, problem, candidateList, rng);
            case TWO_OPT_TABU -> LocalSearch.applyTabu(LocalSearch.applyTwoOpt(routes, problem, candidateList), problem, candidateList, rng);
            case TABU_NARROW       -> LocalSearch.applyTabuNarrow(routes, problem, candidateList, rng);
            case TABU_SUBPATH      -> LocalSearch.applyTabuSubpath(routes, problem, candidateList, rng);
            case TABU_2OPT_RELOCATE -> LocalSearch.applyTabu2opt(routes, problem, candidateList, rng);
        };
    }

    @Override
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
            for (int a = 0; a < numAnts; a++) {
                double dist = VrpSolution.calcDistance(allRoutes.get(a), problem);
                costs[a] = dist + VEHICLE_PENALTY * allRoutes.get(a).size();
            }

            // apply LS only to elite-k ants
            int[] order = eliteOrder(costs);
            int eliteK = Math.min(ELITE_K, numAnts);
            for (int r = 0; r < eliteK; r++) {
                int a = order[r];
                allRoutes.set(a, applyLS(allRoutes.get(a)));
                double dist = VrpSolution.calcDistance(allRoutes.get(a), problem);
                costs[a] = dist + VEHICLE_PENALTY * allRoutes.get(a).size();
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
            updatePheromone(allRoutes, costs);
            logConvergence(iter, iterBestCost, iterBestVehicles);
            actualIters = iter + 1;
            if (noImprovCount >= ealystop) break;
        }
        return new VrpSolution(bestRoutes, bestVehicles, bestDist - VEHICLE_PENALTY * bestVehicles);
    }
}
