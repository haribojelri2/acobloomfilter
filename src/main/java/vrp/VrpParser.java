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

    public static ParsedInstance parse(String vrpPath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(vrpPath));

        String name = "";
        int dimension = 0, capacity = 0, optimalValue = -1;
        Map<Integer, int[]> coords = new LinkedHashMap<>();
        Map<Integer, Integer> demands = new LinkedHashMap<>();

        String section = "";
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.equals("EOF")) continue;

            if (line.startsWith("NAME")) {
                name = line.split(":")[1].trim();
            } else if (line.startsWith("COMMENT")) {
                String comment = line.contains(":") ? line.split(":", 2)[1].trim() : line.trim();
                // "Optimal value: 784" 또는 "Best value: 1071"
                for (String tag : new String[]{"Optimal value:", "Best value:"}) {
                    int idx = comment.indexOf(tag);
                    if (idx >= 0) {
                        String rest = comment.substring(idx + tag.length()).trim().replaceAll("[^0-9].*", "");
                        if (!rest.isEmpty()) { optimalValue = Integer.parseInt(rest); break; }
                    }
                }
                // Golden 스타일: COMMENT 전체가 순수 숫자(실수)
                if (optimalValue < 0) {
                    try { optimalValue = (int) Math.round(Double.parseDouble(comment)); } catch (NumberFormatException ignored) {}
                }
            } else if (line.startsWith("DIMENSION")) {
                dimension = Integer.parseInt(line.split(":")[1].trim());
            } else if (line.startsWith("CAPACITY")) {
                capacity = Integer.parseInt(line.split(":")[1].trim());
            } else if (line.startsWith("NODE_COORD_SECTION")) {
                section = "COORD";
            } else if (line.startsWith("DEMAND_SECTION")) {
                section = "DEMAND";
            } else if (line.startsWith("DEPOT_SECTION")) {
                section = "DEPOT";
            } else if (section.equals("COORD")) {
                String[] parts = line.split("\\s+");
                int id = Integer.parseInt(parts[0]);
                coords.put(id, new int[]{(int) Math.round(Double.parseDouble(parts[1])),
                                         (int) Math.round(Double.parseDouble(parts[2]))});
            } else if (section.equals("DEMAND")) {
                String[] parts = line.split("\\s+");
                int id = Integer.parseInt(parts[0]);
                demands.put(id, Integer.parseInt(parts[1]));
            }
        }

        // node 0 = depot (id 1 in file), rest = customers
        List<Node> nodes = new ArrayList<>();
        for (int id = 1; id <= dimension; id++) {
            int[] xy = coords.get(id);
            int demand = demands.getOrDefault(id, 0);
            nodes.add(new Node(id - 1, xy[0], xy[1], demand));
        }

        return new ParsedInstance(new VrpProblem(nodes, capacity), name, optimalValue);
    }
}
