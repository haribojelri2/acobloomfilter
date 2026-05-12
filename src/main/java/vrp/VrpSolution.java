package vrp;

import java.util.*;

public class VrpSolution {
    public final List<List<Integer>> routes;
    public final int numVehicles;
    public final double totalDistance;

    public VrpSolution(List<List<Integer>> routes, int numVehicles, double totalDistance) {
        this.routes = routes;
        this.numVehicles = numVehicles;
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

    public static boolean isFeasible(List<List<Integer>> routes, VrpProblem problem) {
        boolean[] served = new boolean[problem.size()];
        for (List<Integer> route : routes) {
            double time = 0;
            int load = 0;
            int prev = 0;
            for (int node : route) {
                time += problem.distMatrix[prev][node];
                Node nd = problem.nodes.get(node);
                if (time > nd.dueDate) return false;
                time = Math.max(time, nd.readyTime) + nd.serviceTime;
                load += nd.demand;
                if (load > problem.vehicleCapacity) return false;
                if (served[node]) return false;
                served[node] = true;
                prev = node;
            }
            if (time + problem.distMatrix[prev][0] > problem.nodes.get(0).dueDate) return false;
        }
        for (int i = 1; i < problem.size(); i++) if (!served[i]) return false;
        long usedVehicles = routes.stream().filter(r -> !r.isEmpty()).count();
        if (problem.maxVehicles > 0 && usedVehicles > problem.maxVehicles) return false;
        return true;
    }
}
