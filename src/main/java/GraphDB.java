
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.*;

/**
 * Graph for storing all of the intersection (node) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** map node id to list of itself as the first item and its adj vertices */
    private Map<Long, List<Node>> adj = new HashMap<>();
    /** a 256-way trie structure to represents a symbol table for implementing an autocomplete system
     * where a user types in a partial query string, and return a list of location names that have the
     * query string as a prefix.
     */
    private TrieST trie = new TrieST();
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
            if (adj.get(v).size() == 1) {
                adj.remove(v);
            }
        }
    }

    /** Returns an iterable of all node IDs in the graph. */
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
        List<Long> nodeAdj = new ArrayList<>();
        List<Node> vList = adj.get(v);
        int vListSize = vList.size();
        for (int i = 1; i < vListSize; i++) {
            nodeAdj.add(vList.get(i).id);
        }
        return nodeAdj;
    }

    /** Returns the Euclidean distance between vertices v and w, where Euclidean distance
     *  is defined as sqrt( (lonV - lonW)^2 + (latV - latW)^2 ). */
    double distance(long v, long w) {
        double deltaLon = lon(v) - lon(w);
        double deltaLat = lat(v) - lat(w);
        return Math.sqrt(deltaLon * deltaLon + deltaLat * deltaLat);
    }

    /** Returns the node id closest to the given longitude and latitude. */
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

    /** Longitude of node v. */
    double lon(long v) {
        return adj.get(v).get(0).lon;
    }

    /** Latitude of node v. */
    double lat(long v) {
        return adj.get(v).get(0).lat;
    }

    void addNode(Node v) {
        if (adj.containsKey(v.id)) {
            return;
        }
        List<Node> nodeList = new ArrayList<>();
        nodeList.add(v);
        adj.put(v.id, nodeList);
    }

    Node getNode(long v) {
        return adj.get(v).get(0);
    }

    private void addEdge(long v, long w) {
        adj.get(v).add(new Node(w, lon(w), lat(w)));
        adj.get(w).add(new Node(v, lon(v), lat(v)));
    }

    /**
     * Add all edges along the way in this graph
     * @param way a list of
     */
    void addEdges(List<Long> way) {
        for (int i = 0; i < way.size() - 1; i++) {
            addEdge(way.get(i), way.get(i + 1));
        }
    }

    /**
     * Inserts a key (searchName) with locationName and corresponding node into the trie
     * @param searchName cleaned location name, lowercase
     * @param locationName actual location Name from xml file
     * @param node a node object
     */
    public void putLocNameToTrie(String searchName, String locationName, Node node) {
        trie.put(searchName, locationName, node);
    }

    /**
     * Return a list of valid location names that associated with vertices according to the given prefix.
     * @param prefix
     * @return a list of valid location names
     */
    public List<String> getLocationsByPrefix(String prefix) {
        return trie.keysWithPrefix(cleanString(prefix));
    }

    /**
     * Returns a list of Map objects if the given locationName exists, otherwise an empty list.
     * Each Map object contains the information of the node whose cleaned name is the same as cleaned
     * locationName.
     * @param locationName
     * @return a list of Map objects or an empty list
     */
    public List<Map<String, Object>> getLocationsByName(String locationName) {
        List<Map<String, Object>> res = new LinkedList<>();
        List<Node> vertices = trie.get(cleanString(locationName));

        for (Node v : vertices) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", v.id);
            map.put("lon", v.lon);
            map.put("lat", v.lat);
            map.put("name", v.name);
            res.add(map);
        }
        return res;
    }

    /**
     * Represents a node object in the graph, which is comparable by its priority.
     * Each node object has an id, a location represents by latitude and longitude. The name and priority
     * can be optional and can be set using setName and setPriority methods.
     */
    static class Node implements Comparable<Node> {
        long id;
        double lat, lon;
        String name;
        double priority;

        Node(long id, double lon, double lat) {
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
            s.append("Tile id: " + id + ", " + "lat: " + lat + ", " + "lon: " + lon);
            if (name != null) {
                s.append(", name: " + name + "\n");
            }
            return s.toString();
        }

        @Override
        public int compareTo(Node o) {
            return Double.compare(this.priority, o.priority);
        }
    }

}
