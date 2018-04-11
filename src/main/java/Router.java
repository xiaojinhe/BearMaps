import java.util.*;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {
    /**
     * Return a LinkedList of <code>Long</code>s representing the shortest path from st to dest, 
     * where the longs are node IDs.
     */
    public static LinkedList<Long> shortestPath(GraphDB g, double stlon, double stlat, double destlon, double destlat) {
        long startID = g.closest(stlon, stlat);
        GraphDB.Vertex start = g.getVertex(startID);
        long destID = g.closest(destlon, destlat);

        LinkedList<Long> sp = new LinkedList<>();
        Map<Long, Double> distTo = new HashMap<>();
        for (Long v : g.vertices()) {
            distTo.put(v, Double.MAX_VALUE);
        }
        Map<Long, Long> edgeTo = new HashMap<>();

        PriorityQueue<GraphDB.Vertex> fringe = new PriorityQueue<>();
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
                    GraphDB.Vertex w = g.getVertex(wid);
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
