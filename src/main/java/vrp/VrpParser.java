package vrp;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class VrpParser {

    public static class ParsedInstance {
        public final VrpProblem problem;
        public final String name;
        public final int optimalValue;

        public ParsedInstance(VrpProblem problem, String name, int optimalValue) {
            this.problem = problem;
            this.name = name;
            this.optimalValue = optimalValue;
        }
    }

    // Solomon VRPTW format:
    // Line 1: instance name
    // VEHICLE section: NUMBER  CAPACITY
    // CUSTOMER section: CUST_NO  X  Y  DEMAND  READY_TIME  DUE_DATE  SERVICE_TIME
    public static ParsedInstance parse(String path) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path));

        String name = "";
        int maxVehicles = 0, capacity = 0;
        List<Node> nodes = new ArrayList<>();

        boolean inVehicle = false, inCustomer = false;
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;

            if (name.isEmpty() && !line.toUpperCase().startsWith("VEHICLE")
                               && !line.toUpperCase().startsWith("CUSTOMER")) {
                name = line;
                continue;
            }
            if (line.toUpperCase().startsWith("VEHICLE")) { inVehicle = true; inCustomer = false; continue; }
            if (line.toUpperCase().startsWith("CUSTOMER")) { inCustomer = true; inVehicle = false; continue; }
            // skip header lines (non-numeric first token)
            if (!Character.isDigit(line.charAt(0))) continue;

            String[] p = line.split("\\s+");
            if (inVehicle && maxVehicles == 0) {
                maxVehicles = Integer.parseInt(p[0]);
                capacity    = Integer.parseInt(p[1]);
                inVehicle = false;
            } else if (inCustomer) {
                double x           = Double.parseDouble(p[1]);
                double y           = Double.parseDouble(p[2]);
                int    demand      = Integer.parseInt(p[3]);
                double readyTime   = Double.parseDouble(p[4]);
                double dueDate     = Double.parseDouble(p[5]);
                double serviceTime = Double.parseDouble(p[6]);
                nodes.add(new Node(x, y, demand, readyTime, dueDate, serviceTime));
            }
        }

        return new ParsedInstance(new VrpProblem(nodes, capacity, maxVehicles), name, -1);
    }

    // TSPLIB CVRP format (.vrp)
    // Sections: NODE_COORD_SECTION, DEMAND_SECTION, DEPOT_SECTION
    // No time windows → readyTime=0, dueDate=1e9, serviceTime=0
    public static ParsedInstance parseCvrp(String path) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path));

        String name = "";
        int capacity = 0, dimension = 0, optimalValue = -1;
        double[][] coords = null;
        int[] demands = null;
        int depotId = 1; // 1-indexed

        String section = "";
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.equals("EOF")) continue;

            if (line.startsWith("NAME")) {
                name = line.contains(":") ? line.split(":", 2)[1].trim() : line.split("\\s+", 2)[1].trim();
            } else if (line.startsWith("COMMENT")) {
                String comment = line.contains(":") ? line.split(":", 2)[1].trim() : "";
                for (String pattern : new String[]{"Optimal value:", "Best value:", "optimal value:", "best value:"}) {
                    int idx = comment.indexOf(pattern);
                    if (idx >= 0) {
                        String rest = comment.substring(idx + pattern.length()).trim().split("[^0-9]")[0];
                        if (!rest.isEmpty()) { optimalValue = Integer.parseInt(rest); break; }
                    }
                }
            } else if (line.startsWith("DIMENSION")) {
                dimension = Integer.parseInt(line.split("[:\\s]+")[1].trim());
                coords = new double[dimension + 1][2];
                demands = new int[dimension + 1];
            } else if (line.startsWith("CAPACITY")) {
                capacity = Integer.parseInt(line.split("[:\\s]+")[1].trim());
            } else if (line.startsWith("NODE_COORD_SECTION")) {
                section = "COORD";
            } else if (line.startsWith("DEMAND_SECTION")) {
                section = "DEMAND";
            } else if (line.startsWith("DEPOT_SECTION")) {
                section = "DEPOT";
            } else if (!line.isEmpty() && Character.isDigit(line.charAt(0))) {
                String[] p = line.split("\\s+");
                int id = Integer.parseInt(p[0]);
                if (section.equals("COORD") && coords != null) {
                    coords[id][0] = Double.parseDouble(p[1]);
                    coords[id][1] = Double.parseDouble(p[2]);
                } else if (section.equals("DEMAND") && demands != null) {
                    demands[id] = Integer.parseInt(p[1]);
                } else if (section.equals("DEPOT")) {
                    if (id > 0) depotId = id;
                }
            }
        }

        // build nodes: depot first (index 0), then customers
        List<Node> nodes = new ArrayList<>();
        double bigT = 1e9;
        nodes.add(new Node(coords[depotId][0], coords[depotId][1], 0, 0, bigT, 0));
        for (int id = 1; id <= dimension; id++) {
            if (id == depotId) continue;
            nodes.add(new Node(coords[id][0], coords[id][1], demands[id], 0, bigT, 0));
        }

        return new ParsedInstance(new VrpProblem(nodes, capacity, dimension), name, optimalValue);
    }
}
