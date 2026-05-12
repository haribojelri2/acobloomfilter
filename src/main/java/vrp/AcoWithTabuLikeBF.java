package vrp;

import java.util.*;
import java.util.stream.*;

public class AcoWithTabuLikeBF extends AcoEngine {

    public enum TabuMode { CUST, NARROW, SUBPATH, CUST_2OPT }

    private final boolean useTwoOpt;
    private final double fpr;
    private final TabuMode tabuMode;

    public AcoWithTabuLikeBF(VrpProblem problem, int numAnts, int maxIter,
                              double alpha, double beta, double rho, double q,
                              boolean useTwoOpt) {
        this(problem, numAnts, maxIter, alpha, beta, rho, q, useTwoOpt, 0.05, TabuMode.CUST);
    }

    public AcoWithTabuLikeBF(VrpProblem problem, int numAnts, int maxIter,
                              double alpha, double beta, double rho, double q,
                              boolean useTwoOpt, double fpr) {
        this(problem, numAnts, maxIter, alpha, beta, rho, q, useTwoOpt, fpr, TabuMode.CUST);
    }

    public AcoWithTabuLikeBF(VrpProblem problem, int numAnts, int maxIter,
                              double alpha, double beta, double rho, double q,
                              boolean useTwoOpt, double fpr, TabuMode tabuMode) {
        super(problem, numAnts, maxIter, alpha, beta, rho, q);
        this.useTwoOpt = useTwoOpt;
        this.fpr = fpr;
        this.tabuMode = tabuMode;
    }

    private List<List<Integer>> applyTabuBF(List<List<Integer>> routes) {
        List<List<Integer>> r = useTwoOpt ? LocalSearch.applyTwoOpt(routes, problem, candidateList) : routes;
        return switch (tabuMode) {
            case CUST      -> tabuWithBF(r, problem, 50, fpr, candidateList, rng);
            case NARROW    -> tabuWithBFNarrow(r, problem, 50, fpr, candidateList, rng);
            case SUBPATH   -> tabuWithBFSubpath(r, problem, 50, fpr, candidateList, rng);
            case CUST_2OPT -> tabuWithBF(tabuWithBF2opt(r, problem, 50, fpr, candidateList, rng), problem, 50, fpr, candidateList, rng);
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

            int[] order = eliteOrder(costs);
            int eliteK = Math.min(ELITE_K, numAnts);
            for (int r = 0; r < eliteK; r++) {
                int a = order[r];
                allRoutes.set(a, applyTabuBF(allRoutes.get(a)));
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

    static List<List<Integer>> tabuWithBF(List<List<Integer>> routes, VrpProblem problem,
                                          int maxIter, double fpr, int[][] candidateList, Random rng) {
        int sz = problem.size() - 1;
        int tenureMin = Math.max(3, (int) Math.ceil(Math.sqrt(sz) / 2));
        int tenureMax = Math.max(10, (int) Math.ceil(Math.sqrt(sz) * 2));
        int tenureAvg = (tenureMin + tenureMax) / 2;
        List<List<Integer>> best = LocalSearch.deepCopy(routes);
        List<List<Integer>> current = LocalSearch.deepCopy(routes);
        double bestCost = LocalSearch.totalCost(best, problem);
        double[][] dist = problem.distMatrix;
        int capacity = problem.vehicleCapacity;

        int bitSize = PingPongBloomFilter.optimalBitSize(tenureAvg, fpr);
        int numHash = PingPongBloomFilter.optimalNumHashes(bitSize, tenureAvg);
        PingPongBloomFilter tabuBF = new PingPongBloomFilter(bitSize, numHash, tenureMin, tenureMax, rng);

        for (int iter = 0; iter < maxIter; iter++) {
            double currentCost = LocalSearch.totalCost(current, problem);

            int[] routeLoad = new int[current.size()];
            int[] custToRoute = new int[problem.size()];
            for (int i = 0; i < current.size(); i++)
                for (int node : current.get(i)) {
                    routeLoad[i] += problem.nodes.get(node).demand;
                    custToRoute[node] = i;
                }

            int[] fiOrder = randomPerm(current.size(), rng);
            int bCust = -1, bFi = -1, bFp = -1, bTi = -1, bTp = -1;
            double bMoveCost = Double.MAX_VALUE;

            outer:
            for (int fi : fiOrder) {
                List<Integer> from = current.get(fi);
                int[] fpOrder = randomPerm(from.size(), rng);
                for (int fp : fpOrder) {
                    int cust = from.get(fp);
                    int custDemand = problem.nodes.get(cust).demand;
                    boolean isTabu = tabuBF.mightContain((long) cust);

                    double removeDistDelta = LocalSearch.distDeltaRemove(from, fp, dist);
                    boolean fiSolo = (from.size() == 1);
                    boolean fiWithoutFeasible = fiSolo || LocalSearch.isTwFeasibleWithout(from, fp, problem);

                    boolean[] isTarget = new boolean[current.size()];
                    isTarget[fi] = true;
                    for (int nb : candidateList[cust])
                        isTarget[custToRoute[nb]] = true;

                    for (int ti = 0; ti < current.size(); ti++) {
                        if (!isTarget[ti]) continue;
                        List<Integer> to = current.get(ti);
                        int maxTp = (ti == fi) ? from.size() - 1 : to.size();

                        for (int tp = 0; tp <= maxTp; tp++) {
                            if (ti == fi && tp == fp) continue;
                            if (ti != fi && routeLoad[ti] + custDemand > capacity) continue;

                            double distDelta = (ti != fi)
                                ? removeDistDelta + LocalSearch.distDeltaInsert(to, tp, cust, dist)
                                : LocalSearch.distDeltaIntraRelocate(from, fp, tp, cust, dist);
                            double vehicleDelta = (ti != fi && fiSolo) ? -AcoEngine.VEHICLE_PENALTY : 0;
                            double moveCost = currentCost + distDelta + vehicleDelta;

                            if (isTabu && moveCost >= bestCost) continue;
                            if (!isTabu && moveCost >= currentCost - 1e-10) continue;

                            if (ti != fi) {
                                if (!fiWithoutFeasible) continue;
                                if (!LocalSearch.isTwFeasibleWith(to, tp, cust, problem)) continue;
                            } else {
                                if (!LocalSearch.isTwFeasibleWithRemoveInsert(from, fp, tp, cust, problem)) continue;
                            }

                            bMoveCost = moveCost; bCust = cust; bFi = fi; bFp = fp; bTi = ti; bTp = tp;
                            break outer;
                        }
                    }
                }
            }

            if (bCust == -1) break;

            current.get(bFi).remove(bFp);
            List<Integer> toRoute = (bTi == bFi) ? current.get(bFi) : current.get(bTi);
            toRoute.add(bTp, bCust);
            current.removeIf(List::isEmpty);

            tabuBF.add((long) bCust);
            tabuBF.onMove();

            if (bMoveCost < bestCost - 1e-10) {
                bestCost = bMoveCost;
                best = LocalSearch.deepCopy(current);
            }
        }
        return best;
    }

    static List<List<Integer>> tabuWithBFNarrow(List<List<Integer>> routes, VrpProblem problem,
                                               int maxIter, double fpr, int[][] candidateList, Random rng) {
        int N = problem.size();
        int tenureMin = Math.max(3, (int) Math.ceil(Math.sqrt(N - 1) / 2));
        int tenureMax = Math.max(10, (int) Math.ceil(Math.sqrt(N - 1) * 2));
        int tenureAvg = (tenureMin + tenureMax) / 2;
        List<List<Integer>> best = LocalSearch.deepCopy(routes);
        List<List<Integer>> current = LocalSearch.deepCopy(routes);
        double bestCost = LocalSearch.totalCost(best, problem);
        double[][] dist = problem.distMatrix;
        int capacity = problem.vehicleCapacity;

        int bitSize = PingPongBloomFilter.optimalBitSize(tenureAvg, fpr);
        int numHash = PingPongBloomFilter.optimalNumHashes(bitSize, tenureAvg);
        PingPongBloomFilter tabuBF = new PingPongBloomFilter(bitSize, numHash, tenureMin, tenureMax, rng);

        for (int iter = 0; iter < maxIter; iter++) {
            double currentCost = LocalSearch.totalCost(current, problem);
            int[] routeLoad = new int[current.size()];
            int[] custToRoute = new int[N];
            for (int i = 0; i < current.size(); i++)
                for (int node : current.get(i)) { routeLoad[i] += problem.nodes.get(node).demand; custToRoute[node] = i; }

            int[] fiOrder = randomPerm(current.size(), rng);
            int bCust = -1, bFi = -1, bFp = -1, bTi = -1, bTp = -1;
            double bMoveCost = Double.MAX_VALUE;

            outer:
            for (int fi : fiOrder) {
                List<Integer> from = current.get(fi);
                int[] fpOrder = randomPerm(from.size(), rng);
                for (int fp : fpOrder) {
                    int cust = from.get(fp);
                    int custDemand = problem.nodes.get(cust).demand;
                    double removeDistDelta = LocalSearch.distDeltaRemove(from, fp, dist);
                    boolean fiSolo = (from.size() == 1);
                    boolean fiWithoutFeasible = fiSolo || LocalSearch.isTwFeasibleWithout(from, fp, problem);

                    boolean[] isTarget = new boolean[current.size()];
                    isTarget[fi] = true;
                    for (int nb : candidateList[cust]) isTarget[custToRoute[nb]] = true;

                    for (int ti = 0; ti < current.size(); ti++) {
                        if (!isTarget[ti]) continue;
                        List<Integer> to = current.get(ti);
                        int maxTp = (ti == fi) ? from.size() - 1 : to.size();
                        boolean intra = (ti == fi);

                        for (int tp = 0; tp <= maxTp; tp++) {
                            if (intra && tp == fp) continue;
                            if (!intra && routeLoad[ti] + custDemand > capacity) continue;

                            int insPred = LocalSearch.computeInsPred(from, fp, to, intra, tp);
                            boolean isTabu = tabuBF.mightContain((long) insPred * N + cust);

                            double distDelta = !intra
                                ? removeDistDelta + LocalSearch.distDeltaInsert(to, tp, cust, dist)
                                : LocalSearch.distDeltaIntraRelocate(from, fp, tp, cust, dist);
                            double vehicleDelta = (!intra && fiSolo) ? -AcoEngine.VEHICLE_PENALTY : 0;
                            double moveCost = currentCost + distDelta + vehicleDelta;

                            if (isTabu && moveCost >= bestCost) continue;
                            if (!isTabu && moveCost >= currentCost - 1e-10) continue;

                            if (!intra) {
                                if (!fiWithoutFeasible) continue;
                                if (!LocalSearch.isTwFeasibleWith(to, tp, cust, problem)) continue;
                            } else {
                                if (!LocalSearch.isTwFeasibleWithRemoveInsert(from, fp, tp, cust, problem)) continue;
                            }

                            bMoveCost = moveCost; bCust = cust; bFi = fi; bFp = fp; bTi = ti; bTp = tp;
                            break outer;
                        }
                    }
                }
            }

            if (bCust == -1) break;

            List<Integer> bFrom = current.get(bFi);
            int bCustPred = (bFp == 0) ? 0 : bFrom.get(bFp - 1);
            tabuBF.add((long) bCustPred * N + bCust);
            tabuBF.onMove();

            bFrom.remove(bFp);
            List<Integer> toRoute = (bTi == bFi) ? bFrom : current.get(bTi);
            toRoute.add(bTp, bCust);
            current.removeIf(List::isEmpty);

            if (bMoveCost < bestCost - 1e-10) { bestCost = bMoveCost; best = LocalSearch.deepCopy(current); }
        }
        return best;
    }

    static List<List<Integer>> tabuWithBFSubpath(List<List<Integer>> routes, VrpProblem problem,
                                                 int maxIter, double fpr, int[][] candidateList, Random rng) {
        int N = problem.size();
        int tenureMin = Math.max(3, (int) Math.ceil(Math.sqrt(N - 1) / 2));
        int tenureMax = Math.max(10, (int) Math.ceil(Math.sqrt(N - 1) * 2));
        int tenureAvg = (tenureMin + tenureMax) / 2;
        List<List<Integer>> best = LocalSearch.deepCopy(routes);
        List<List<Integer>> current = LocalSearch.deepCopy(routes);
        double bestCost = LocalSearch.totalCost(best, problem);
        double[][] dist = problem.distMatrix;
        int capacity = problem.vehicleCapacity;

        int bitSize = PingPongBloomFilter.optimalBitSize(tenureAvg, fpr);
        int numHash = PingPongBloomFilter.optimalNumHashes(bitSize, tenureAvg);
        PingPongBloomFilter tabuBF = new PingPongBloomFilter(bitSize, numHash, tenureMin, tenureMax, rng);

        for (int iter = 0; iter < maxIter; iter++) {
            double currentCost = LocalSearch.totalCost(current, problem);
            int[] routeLoad = new int[current.size()];
            int[] custToRoute = new int[N];
            for (int i = 0; i < current.size(); i++)
                for (int node : current.get(i)) { routeLoad[i] += problem.nodes.get(node).demand; custToRoute[node] = i; }

            int[] fiOrder = randomPerm(current.size(), rng);
            int bCust = -1, bFi = -1, bFp = -1, bTi = -1, bTp = -1;
            double bMoveCost = Double.MAX_VALUE;

            outer:
            for (int fi : fiOrder) {
                List<Integer> from = current.get(fi);
                int[] fpOrder = randomPerm(from.size(), rng);
                for (int fp : fpOrder) {
                    int cust = from.get(fp);
                    int custDemand = problem.nodes.get(cust).demand;
                    double removeDistDelta = LocalSearch.distDeltaRemove(from, fp, dist);
                    boolean fiSolo = (from.size() == 1);
                    boolean fiWithoutFeasible = fiSolo || LocalSearch.isTwFeasibleWithout(from, fp, problem);

                    boolean[] isTarget = new boolean[current.size()];
                    isTarget[fi] = true;
                    for (int nb : candidateList[cust]) isTarget[custToRoute[nb]] = true;

                    for (int ti = 0; ti < current.size(); ti++) {
                        if (!isTarget[ti]) continue;
                        List<Integer> to = current.get(ti);
                        int maxTp = (ti == fi) ? from.size() - 1 : to.size();
                        boolean intra = (ti == fi);

                        for (int tp = 0; tp <= maxTp; tp++) {
                            if (intra && tp == fp) continue;
                            if (!intra && routeLoad[ti] + custDemand > capacity) continue;

                            int insPred = LocalSearch.computeInsPred(from, fp, to, intra, tp);
                            int insSucc = LocalSearch.computeInsSucc(from, fp, to, intra, tp);
                            boolean isTabu = tabuBF.mightContain(((long) insPred * N + cust) * N + insSucc);

                            double distDelta = !intra
                                ? removeDistDelta + LocalSearch.distDeltaInsert(to, tp, cust, dist)
                                : LocalSearch.distDeltaIntraRelocate(from, fp, tp, cust, dist);
                            double vehicleDelta = (!intra && fiSolo) ? -AcoEngine.VEHICLE_PENALTY : 0;
                            double moveCost = currentCost + distDelta + vehicleDelta;

                            if (isTabu && moveCost >= bestCost) continue;
                            if (!isTabu && moveCost >= currentCost - 1e-10) continue;

                            if (!intra) {
                                if (!fiWithoutFeasible) continue;
                                if (!LocalSearch.isTwFeasibleWith(to, tp, cust, problem)) continue;
                            } else {
                                if (!LocalSearch.isTwFeasibleWithRemoveInsert(from, fp, tp, cust, problem)) continue;
                            }

                            bMoveCost = moveCost; bCust = cust; bFi = fi; bFp = fp; bTi = ti; bTp = tp;
                            break outer;
                        }
                    }
                }
            }

            if (bCust == -1) break;

            List<Integer> bFrom = current.get(bFi);
            int bCustPred = (bFp == 0) ? 0 : bFrom.get(bFp - 1);
            int bCustSucc = (bFp == bFrom.size() - 1) ? 0 : bFrom.get(bFp + 1);

            tabuBF.add(((long) bCustPred * N + bCust) * N + bCustSucc);
            tabuBF.onMove();

            bFrom.remove(bFp);
            List<Integer> toRoute = (bTi == bFi) ? bFrom : current.get(bTi);
            toRoute.add(bTp, bCust);
            current.removeIf(List::isEmpty);

            if (bMoveCost < bestCost - 1e-10) { bestCost = bMoveCost; best = LocalSearch.deepCopy(current); }
        }
        return best;
    }

    static List<List<Integer>> tabuWithBF2opt(List<List<Integer>> routes, VrpProblem problem,
                                              int maxIter, double fpr, int[][] candidateList, Random rng) {
        int N = problem.size();
        int tenureMin = Math.max(3, (int) Math.ceil(Math.sqrt(N - 1) / 2));
        int tenureMax = Math.max(10, (int) Math.ceil(Math.sqrt(N - 1) * 2));
        int tenureAvg = (tenureMin + tenureMax) / 2;
        List<List<Integer>> best = LocalSearch.deepCopy(routes);
        List<List<Integer>> current = LocalSearch.deepCopy(routes);
        double bestCost = LocalSearch.totalCost(best, problem);
        double[][] dist = problem.distMatrix;

        int bitSize = PingPongBloomFilter.optimalBitSize(tenureAvg, fpr);
        int numHash = PingPongBloomFilter.optimalNumHashes(bitSize, tenureAvg);
        PingPongBloomFilter tabuBF = new PingPongBloomFilter(bitSize, numHash, tenureMin, tenureMax, rng);

        for (int iter = 0; iter < maxIter; iter++) {
            double currentCost = LocalSearch.totalCost(current, problem);
            int bRi = -1, bI = -1, bJ = -1;
            double bMoveCost = Double.MAX_VALUE;

            outer:
            for (int ri = 0; ri < current.size(); ri++) {
                List<Integer> route = current.get(ri);
                int sz = route.size();
                if (sz < 2) continue;
                int[] pos = new int[N];
                for (int k = 0; k < sz; k++) pos[route.get(k)] = k;

                for (int i = 0; i < sz - 1; i++) {
                    int b = route.get(i);
                    int a = (i == 0) ? 0 : route.get(i - 1);
                    for (int nb : candidateList[b]) {
                        int j = pos[nb];
                        if (j <= i + 1) continue;
                        int c = route.get(j);
                        int d = (j + 1 < sz) ? route.get(j + 1) : 0;
                        double delta = dist[a][c] + dist[b][d] - dist[a][b] - dist[c][d];
                        double moveCost = currentCost + delta;
                        long tabuKey = (long) a * N + b;
                        boolean isTabu = tabuBF.mightContain(tabuKey);
                        if (isTabu && moveCost >= bestCost) continue;
                        if (!isTabu && moveCost >= currentCost - 1e-10) continue;
                        Collections.reverse(route.subList(i, j + 1));
                        boolean feasible = LocalSearch.isTwFeasible(route, problem);
                        Collections.reverse(route.subList(i, j + 1));
                        if (!feasible) continue;
                        bMoveCost = moveCost; bRi = ri; bI = i; bJ = j;
                        break outer;
                    }
                }
            }

            if (bRi == -1) break;
            List<Integer> route = current.get(bRi);
            int a = (bI == 0) ? 0 : route.get(bI - 1);
            long tabuKey = (long) a * N + route.get(bI);
            Collections.reverse(route.subList(bI, bJ + 1));
            tabuBF.add(tabuKey);
            tabuBF.onMove();
            if (bMoveCost < bestCost - 1e-10) { bestCost = bMoveCost; best = LocalSearch.deepCopy(current); }
        }
        return best;
    }

    private static int[] randomPerm(int n, Random rng) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
        }
        return arr;
    }
}
