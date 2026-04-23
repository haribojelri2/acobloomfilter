package vrp;

import java.util.*;

public class AcoWithTabuLikeBF extends AcoEngine {

    private final boolean useTwoOpt;

    private final double fpr;

    public AcoWithTabuLikeBF(VrpProblem problem, int numAnts, int maxIter,
                              double alpha, double beta, double rho, double q,
                              boolean useTwoOpt) {
        this(problem, numAnts, maxIter, alpha, beta, rho, q, useTwoOpt, 0.05);
    }

    public AcoWithTabuLikeBF(VrpProblem problem, int numAnts, int maxIter,
                              double alpha, double beta, double rho, double q,
                              boolean useTwoOpt, double fpr) {
        super(problem, numAnts, maxIter, alpha, beta, rho, q);
        this.useTwoOpt = useTwoOpt;
        this.fpr = fpr;
    }

    @Override
    protected List<List<Integer>> constructSolution() {
        List<List<Integer>> routes = super.constructSolution();
        if (useTwoOpt) routes = LocalSearch.applyTwoOpt(routes, problem);
        return applyTabuBF(routes);
    }

    private List<List<Integer>> applyTabuBF(List<List<Integer>> routes) {
        List<List<Integer>> result = new ArrayList<>();
        for (List<Integer> route : routes) {
            int tenure = Math.max(5, route.size() / 3);
            result.add(route.size() < 2 ? route : tabuWithBF(route, problem, 50, tenure, fpr));
        }
        return result;
    }

    private static List<Integer> tabuWithBF(List<Integer> route, VrpProblem problem, int maxIter, int tabuTenure, double fpr) {
        List<Integer> best = new ArrayList<>(route);
        List<Integer> current = new ArrayList<>(route);
        double bestDist = routeDist(best, problem);

        int bitSize = BloomFilter.optimalBitSetSize(tabuTenure, fpr);
        int numHash = BloomFilter.optimalNumHashes(bitSize, tabuTenure);
        BloomFilter tabuBF = new BloomFilter(bitSize, numHash);
        // tenure 후 약 50% 생존 확률로 decay
        double keepProb = Math.pow(0.5, 1.0 / tabuTenure);
        Random rng = new Random();

        for (int iter = 0; iter < maxIter; iter++) {
            List<Integer> bestNeighbor = null;
            double bestNeighborDist = Double.MAX_VALUE;
            EdgeKey bestMoveKey = null;

            double currentDist = routeDist(current, problem);

            for (int i = 0; i < current.size() - 1; i++) {
                for (int j = i + 1; j < current.size(); j++) {
                    EdgeKey moveKey = new EdgeKey(current.get(i), current.get(j));

                    int a = (i == 0) ? 0 : current.get(i - 1);
                    int b = current.get(i);
                    int c = (i == current.size() - 1) ? 0 : current.get(i + 1);
                    int e = current.get(j);
                    int f = (j == current.size() - 1) ? 0 : current.get(j + 1);

                    double before, after;
                    if (j == i + 1) {
                        before = problem.distMatrix[a][b] + problem.distMatrix[b][e] + problem.distMatrix[e][f];
                        after  = problem.distMatrix[a][e] + problem.distMatrix[e][b] + problem.distMatrix[b][f];
                    } else {
                        int d = current.get(j - 1);
                        before = problem.distMatrix[a][b] + problem.distMatrix[b][c] + problem.distMatrix[d][e] + problem.distMatrix[e][f];
                        after  = problem.distMatrix[a][e] + problem.distMatrix[e][c] + problem.distMatrix[d][b] + problem.distMatrix[b][f];
                    }
                    double neighborDist = currentDist + (after - before);

                    // Aspiration criterion
                    if (tabuBF.mightContain(moveKey) && neighborDist >= bestDist) continue;

                    if (neighborDist < bestNeighborDist) {
                        bestNeighborDist = neighborDist;
                        bestMoveKey = moveKey;
                        bestNeighbor = new ArrayList<>(current);
                        Collections.swap(bestNeighbor, i, j);
                    }
                }
            }
            if (bestNeighbor == null) break;
            current = bestNeighbor;
            tabuBF.add(bestMoveKey);
            tabuBF.decay(keepProb, rng);
            if (bestNeighborDist < bestDist - 1e-10) {
                bestDist = bestNeighborDist;
                best = new ArrayList<>(current);
            }
        }
        return best;
    }

    private static double routeDist(List<Integer> route, VrpProblem problem) {
        double d = problem.distMatrix[0][route.get(0)];
        for (int i = 0; i < route.size() - 1; i++)
            d += problem.distMatrix[route.get(i)][route.get(i + 1)];
        d += problem.distMatrix[route.get(route.size() - 1)][0];
        return d;
    }
}
