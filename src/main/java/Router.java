import java.util.*;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map, using A* algorithm. It uses Euclidean distance as heuristic.
 */
public class Router {
    /**
     * Return a LinkedList of <code>Long</code>s representing the shortest path from st to dest, 
     * where the longs are node IDs.
     */
    public static LinkedList<Long> shortestPath(GraphDB g, double stlon, double stlat, double destlon, double destlat) {
        long startID = g.closest(stlon, stlat);
        GraphDB.Node start = g.getNode(startID);
        long destID = g.closest(destlon, destlat);

        LinkedList<Long> sp = new LinkedList<>();
        Map<Long, Double> distTo = new HashMap<>(); // store the distance from startID to node n
        for (Long v : g.vertices()) {
            distTo.put(v, Double.MAX_VALUE);        // set all of the distance value to max
        }
        Map<Long, Long> edgeTo = new HashMap<>();   // track the previous node id

        PriorityQueue<GraphDB.Node> fringe = new PriorityQueue<>();  // priority queue to determine optimal node to add
        start.setPriority(0.0);
        distTo.put(startID, 0.0);
        fringe.add(start);

        while (!fringe.isEmpty() && fringe.peek().id != destID) {
            long v = fringe.poll().id;
            for (Long wid : g.adjacent(v)) {
                double dist = distTo.get(v) + g.distance(v, wid);
                if (dist < distTo.get(wid)) {
                    distTo.put(wid, dist);
                    edgeTo.put(wid, v);
                    GraphDB.Node w = g.getNode(wid);
                    w.setPriority(dist + g.distance(wid, destID));
                    if (fringe.contains(w)) {
                        fringe.remove(w);
                        fringe.offer(w);
                    } else {
                        fringe.offer(w);
                    }
                }
            }
        }

        if (fringe.isEmpty()) {
            return sp;
        }

        long cur = destID;
        while (edgeTo.containsKey(cur)) {
            sp.addFirst(cur);
            cur = edgeTo.get(cur);
        }
        sp.addFirst(cur);
        return sp;
    }
}
