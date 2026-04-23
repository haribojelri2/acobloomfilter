package vrp;

import java.util.*;

public class VrpSolution {
    public final List<List<Integer>> routes;
    public final double totalDistance;

    public VrpSolution(List<List<Integer>> routes, double totalDistance) {
        this.routes = routes;
        this.totalDistance = totalDistance;
    }

    public static double calcDistance(List<List<Integer>> routes, VrpProblem problem) {
        double total = 0;
        for (List<Integer> route : routes) {
            if (route.isEmpty()) continue;
            total += problem.distMatrix[0][route.get(0)];
            for (int i = 0; i < route.size() - 1; i++)
                total += problem.distMatrix[route.get(i)][route.get(i + 1)];
            total += problem.distMatrix[route.get(route.size() - 1)][0];
        }
        return total;
    }
}
