package vrp;

public class Node {
    public final double x, y;
    public final int demand;

    public Node(int id, double x, double y, int demand) {
        this.x = x;
        this.y = y;
        this.demand = demand;
    }

    public double distanceTo(Node other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
