import com.sun.imageio.plugins.common.LZWStringTable;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.*;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */

    /** map vertex id to list of itself as the first item and its adj vertices */
    private Map<Long, List<Vertex>> adj = new HashMap<>();

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputFile, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        for (Long v : vertices()) {
            if (adj.get(v).size() <= 1) {
                adj.remove(v);
            }
        }
    }

    /** Returns an iterable of all vertex IDs in the graph. */
    Iterable<Long> vertices() {
        List<Long> vertices = new ArrayList<>();
        vertices.addAll(adj.keySet());
        return vertices;
    }

    /** Returns ids of all vertices adjacent to v. */
    Iterable<Long> adjacent(long v) {
        if (!adj.containsKey(v)) {
            return null;
        }
        List<Long> vertexAdj = new ArrayList<>();
        List<Vertex> vList = adj.get(v);
        int vListSize = vList.size();
        for (int i = 1; i < vListSize; i++) {
            vertexAdj.add(vList.get(i).id);
        }
        return vertexAdj;
    }

    /** Returns the Euclidean distance between vertices v and w, where Euclidean distance
     *  is defined as sqrt( (lonV - lonW)^2 + (latV - latW)^2 ). */
    double distance(long v, long w) {
        double deltaLon = lon(v) - lon(w);
        double deltaLat = lat(v) - lat(w);
        return Math.sqrt(deltaLon * deltaLon + deltaLat * deltaLat);
    }

    /** Returns the vertex id closest to the given longitude and latitude. */
    long closest(double lon, double lat) {
        double minDist = Double.MAX_VALUE;
        long closestVID = 0;
        for (Long v : adj.keySet()) {
            double deltaLon = lon(v) - lon;
            double deltaLat = lat(v) - lat;
            double dist = Math.sqrt(deltaLon * deltaLon + deltaLat * deltaLat);
            if (dist < minDist) {
                closestVID = v;
                minDist = dist;
            }
        }
        return closestVID;
    }

    /** Longitude of vertex v. */
    double lon(long v) {
        return adj.get(v).get(0).lon;
    }

    /** Latitude of vertex v. */
    double lat(long v) {
        return adj.get(v).get(0).lat;
    }

    void addVertex(Vertex v) {
        if (adj.containsKey(v.id)) {
            return;
        }
        List<Vertex> vertexList = new ArrayList<>();
        vertexList.add(v);
        adj.put(v.id, vertexList);
    }

    Vertex getVertex(long v) {
        return adj.get(v).get(0);
    }

    private void addEdge(long v, long w) {
        adj.get(v).add(new Vertex(w, lon(w), lat(w)));
        adj.get(w).add(new Vertex(v, lon(v), lat(v)));
    }

    void addEdges(List<Long> way) {
        for (int i = 0; i < way.size() - 1; i++) {
            addEdge(way.get(i), way.get(i + 1));
        }
    }

    static class Vertex implements Comparable<Vertex> {
        long id;
        double lat, lon;
        String name;
        double priority;

        Vertex(long id, double lon, double lat) {
            this.id = id;
            this.lon = lon;
            this.lat = lat;
        }

        void setName(String name) {
            this.name = name;
        }

        void setPriority(double priority) {
            this.priority = priority;
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append("Node id: " + id + ", " + "lat: " + lat + ", " + "lon: " + lon);
            if (name != null) {
                s.append(", name: " + name + "\n");
            }
            return s.toString();
        }

        @Override
        public int compareTo(Vertex o) {
            return Double.compare(this.priority, o.priority);
        }
    }

}
