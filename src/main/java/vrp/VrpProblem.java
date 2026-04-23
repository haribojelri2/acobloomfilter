package vrp;

import java.util.List;

public class VrpProblem {
    public final List<Node> nodes;
    public final int vehicleCapacity;
    public final double[][] distMatrix;

    public VrpProblem(List<Node> nodes, int vehicleCapacity) {
        this.nodes = nodes;
        this.vehicleCapacity = vehicleCapacity;
        int n = nodes.size();
        this.distMatrix = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                distMatrix[i][j] = nodes.get(i).distanceTo(nodes.get(j));
    }

    public int size() { return nodes.size(); }
}
