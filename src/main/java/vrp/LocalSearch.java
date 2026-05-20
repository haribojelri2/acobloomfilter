package vrp;

import java.util.*;

public class LocalSearch {

    public static List<Integer> twoOpt(List<Integer> route, VrpProblem problem) {
        // route는 depot 제외 노드 목록. 전후에 depot(0) 암묵적으로 존재.
        List<Integer> best = new ArrayList<>(route);
        boolean improved = true;
        while (improved) {
            improved = false;
            int sz = best.size();
            for (int i = 0; i < sz - 1; i++) {
                for (int j = i + 2; j < sz; j++) {
                    int a = (i == 0) ? 0 : best.get(i - 1);
                    int b = best.get(i);
                    int c = best.get(j);
                    int d = (j + 1 < sz) ? best.get(j + 1) : 0;
                    double before = problem.distMatrix[a][b] + problem.distMatrix[c][d];
                    double after  = problem.distMatrix[a][c] + problem.distMatrix[b][d];
                    if (after < before - 1e-10) {
                        Collections.reverse(best.subList(i, j + 1));
                        improved = true;
                    }
                }
            }
        }
        return best;
    }

    public static List<Integer> tabu(List<Integer> route, VrpProblem problem, int maxIter, int tabuTenure) {
        List<Integer> best = new ArrayList<>(route);
        List<Integer> current = new ArrayList<>(route);
        double bestDist = routeDist(best, problem);
        Set<Long> tabuList = new LinkedHashSet<>();

        for (int iter = 0; iter < maxIter; iter++) {
            List<Integer> bestNeighbor = null;
            double bestNeighborDist = Double.MAX_VALUE;
            long bestMove = -1;

            double currentDist = routeDist(current, problem);

            for (int i = 0; i < current.size() - 1; i++) {
                for (int j = i + 1; j < current.size(); j++) {
                    long move = ((long) current.get(i) << 20) | current.get(j);

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

                    if (tabuList.contains(move) && neighborDist >= bestDist) {
                        continue;
                    }

                    if (neighborDist < bestNeighborDist) {
                        bestNeighborDist = neighborDist;
                        bestMove = move;
                        bestNeighbor = new ArrayList<>(current);
                        Collections.swap(bestNeighbor, i, j);
                    }
                }
            }
            if (bestNeighbor == null) break;
            current = bestNeighbor;
            tabuList.add(bestMove);
            if (tabuList.size() > tabuTenure) {
                Iterator<Long> it = tabuList.iterator();
                it.next(); it.remove();
            }
            if (bestNeighborDist < bestDist - 1e-10) {
                bestDist = bestNeighborDist;
                best = new ArrayList<>(current);
            }
        }
        return best;
    }

    static double routeDist(List<Integer> route, VrpProblem problem) {
        double d = problem.distMatrix[0][route.get(0)];
        for (int i = 0; i < route.size() - 1; i++)
            d += problem.distMatrix[route.get(i)][route.get(i + 1)];
        d += problem.distMatrix[route.get(route.size() - 1)][0];
        return d;
    }

    public static List<List<Integer>> applyTwoOpt(List<List<Integer>> routes, VrpProblem problem) {
        List<List<Integer>> result = new ArrayList<>();
        for (List<Integer> route : routes)
            result.add(route.size() < 2 ? route : twoOpt(route, problem));
        return result;
    }

    public static List<List<Integer>> applyTabu(List<List<Integer>> routes, VrpProblem problem) {
        List<List<Integer>> result = new ArrayList<>();
        for (List<Integer> route : routes) {
            int tenure = (int) Math.floor(Math.sqrt(route.size()));
            result.add(route.size() < 2 ? route : tabu(route, problem, 50, tenure));
        }
        return result;
    }
}
