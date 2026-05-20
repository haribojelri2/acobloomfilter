package vrp;

import java.util.*;
import java.util.BitSet;

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
            int tenure = (int) Math.floor(Math.sqrt(route.size()));
            result.add(route.size() < 2 ? route : tabuWithBF(route, problem, 50, tenure, fpr));
        }
        return result;
    }

    private static List<Integer> tabuWithBF(List<Integer> route, VrpProblem problem, int maxIter, int tabuTenure, double fpr) {
        List<Integer> best = new ArrayList<>(route);
        List<Integer> current = new ArrayList<>(route);
        double bestDist = LocalSearch.routeDist(best, problem);

        // double-buffer BF: 두 BitSet을 번갈아 사용해 decay 대신 rotation으로 eviction
        // double-buffer BF: 두 BitSet을 번갈아 사용해 decay 대신 rotation으로 eviction
        int bitSize = BloomFilter.optimalBitSetSize(tabuTenure * 2, fpr);
        int numHash = BloomFilter.optimalNumHashes(bitSize, tabuTenure * 2);
        BitSet[] bufs = { new BitSet(bitSize), new BitSet(bitSize) };
        int active = 0;
        int addCount = 0;

        for (int iter = 0; iter < maxIter; iter++) {
            List<Integer> bestNeighbor = null;
            double bestNeighborDist = Double.MAX_VALUE;
            long bestMoveKey = -1;

            double currentDist = LocalSearch.routeDist(current, problem);

            for (int i = 0; i < current.size() - 1; i++) {
                for (int j = i + 1; j < current.size(); j++) {
                    long moveKey = ((long) current.get(i) << 20) | current.get(j);

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

                    boolean isTabu = dbMightContain(bufs, numHash, bitSize, moveKey);
                    if (isTabu && neighborDist >= bestDist) continue;

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
            dbAdd(bufs[active], numHash, bitSize, bestMoveKey);
            if (++addCount >= tabuTenure) {
                active = 1 - active;
                bufs[active].clear();
                addCount = 0;
            }
            if (bestNeighborDist < bestDist - 1e-10) {
                bestDist = bestNeighborDist;
                best = new ArrayList<>(current);
            }
        }
        return best;
    }

    private static void dbAdd(BitSet buf, int numHash, int bitSize, long key) {
        for (int i = 0; i < numHash; i++) {
            long h = (key ^ (i * 2654435761L)) * 6364136223846793005L + 1442695040888963407L;
            buf.set((int) ((h & Long.MAX_VALUE) % bitSize));
        }
    }

    private static boolean dbMightContain(BitSet[] bufs, int numHash, int bitSize, long key) {
        for (int i = 0; i < numHash; i++) {
            long h = (key ^ (i * 2654435761L)) * 6364136223846793005L + 1442695040888963407L;
            int pos = (int) ((h & Long.MAX_VALUE) % bitSize);
            if (!bufs[0].get(pos) && !bufs[1].get(pos)) return false;
        }
        return true;
    }
}
