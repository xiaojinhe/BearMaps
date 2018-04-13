import java.util.*;

public class Quadtree {
    Tile root;
    final String imgAddr;

    public Quadtree(double ullon, double ullat, double lrlon, double lrlat, int maxDepth, String imgAddr) {
        this.imgAddr = imgAddr;
        this.root = new Tile(0, ullon, ullat, lrlon, lrlat, imgAddr, 0);
        this.root = buildQuadtree(this.root, 0, maxDepth);
    }

    /**
     * Recursively constructs the Quadtree from the root node and depth provided, by adding 4 children
     * northwestern (index 1), northeastern (index 2), southwestern (index 3), and southeastern (index 4).
     * @param x the starting Tile
     * @param depth the depth of the Quadtree
     * @return the root node of the Quadtree
     */
    private Tile buildQuadtree(Tile x, int depth, int maxDepth) {
        if (depth == maxDepth) {
            return x;
        }

        double midLon = (x.ullon + x.lrlon) / 2;
        double midLat = (x.ullat + x.lrlat) / 2;
        // add northwestern child
        Tile nwTile = new Tile(x.index * 10 + 1, x.ullon, x.ullat, midLon, midLat, this.imgAddr,depth + 1);
        x.nw = buildQuadtree(nwTile, depth + 1, maxDepth);

        // add northeastern child
        Tile neTile = new Tile(x.index * 10 + 2, midLon, x.ullat, x.lrlon, midLat, this.imgAddr,depth + 1);
        x.ne = buildQuadtree(neTile, depth + 1, maxDepth);

        // add southwestern child
        Tile swTile = new Tile(x.index * 10 + 3, x.ullon, midLat, midLon, x.lrlat, this.imgAddr,depth + 1);
        x.sw = buildQuadtree(swTile, depth + 1, maxDepth);

        // add southeastern child
        Tile seTile = new Tile(x.index * 10 + 4, midLon, midLat, x.lrlon, x.lrlat, this.imgAddr,depth + 1);
        x.se = buildQuadtree(seTile, depth + 1, maxDepth);

        return x;
    }

    public boolean intersectWithNode(double ullon, double ullat, double lrlon, double lrlat, Tile x) {
        return !((ullon > x.lrlon) || (ullat < x.lrlat) || (lrlon < x.ullon) || (lrlat > x.ullat));
    }

    public boolean lonDPPLessThanQueryOrIsLeaf (Tile x, double queryLonDPP) {
        return (x.depth == 7 || x.lonDPP < queryLonDPP);
    }

    public void queryIntersection(double qUllon, double qUllat, double qLrlon, double qLrlat, double qLonDPP,
                                 Map<String, Object> results, Map<Double, List<String>> intersectTiles) {
        intersectionTiles(qUllon, qUllat, qLrlon, qLrlat, qLonDPP, this.root, results, intersectTiles);
    }

    private void intersectionTiles(double qUllon, double qUllat, double qLrlon, double qLrlat, double qLonDPP,
                                   Tile x, Map<String, Object> results, Map<Double, List<String>> intersectTiles) {
        if (x == null) {
            return;
        }

        if (!intersectWithNode(qUllon, qUllat, qLrlon, qLrlat, x)) {
            return;
        }

        if (!lonDPPLessThanQueryOrIsLeaf(x, qLonDPP)) {
            if (qUllon < x.lrlon && qUllat > x.lrlat) {
                intersectionTiles(qUllon, qUllat, qLrlon, qLrlat, qLonDPP, x.nw, results, intersectTiles);
            }
            if (qLrlon > x.ullon && qUllat > x.lrlat) {
                intersectionTiles(qUllon, qUllat, qLrlon, qLrlat, qLonDPP, x.ne, results, intersectTiles);
            }
            if (qUllon < x.lrlon && qLrlat < x.ullat) {
                intersectionTiles(qUllon, qUllat, qLrlon, qLrlat, qLonDPP, x.sw, results, intersectTiles);
            }
            if (qLrlon > x.ullon && qLrlat < x.ullat) {
                intersectionTiles(qUllon, qUllat, qLrlon, qLrlat, qLonDPP, x.se, results, intersectTiles);
            }
        } else {
            if (x.ullon < (double) results.get("raster_ul_lon")) {
                results.replace("raster_ul_lon", x.ullon);
            }
            if (x.ullat > (double) results.get("raster_ul_lat")) {
                results.replace("raster_ul_lat", x.ullat);
            }
            if (x.lrlon > (double) results.get("raster_lr_lon")) {
                results.replace("raster_lr_lon", x.lrlon);
            }
            if (x.lrlat < (double) results.get("raster_lr_lat")) {
                results.replace("raster_lr_lat", x.lrlat);
            }
            if (x.depth > (int) results.get("depth")) {
                results.replace("depth", x.depth);
            }
            arrangeTiles(x.ullat, x.imgName, intersectTiles);
        }
    }

    /**
     * Arrange the tiles that intersect with the query box by mapping the ullats of the tiles to their imgName.
     * The imgName of the tiles sharing the same ullat will be put into the same List.
     * @param ullat
     * @param imgName the intersect Tile's imgName
     * @param intersectTiles
     */
    private void arrangeTiles(double ullat, String imgName, Map<Double, List<String>> intersectTiles) {
        if (intersectTiles.containsKey(ullat)) {
            intersectTiles.get(ullat).add(imgName);
        } else {
            List<String> row = new LinkedList<>();
            row.add(imgName);
            intersectTiles.put(ullat, row);
        }
    }

    private String[][] renderGrid(Map<Double, List<String>> intersectTiles) {
        String[][] res = new String[intersectTiles.size()][];

        int row = intersectTiles.size() - 1;
        for (Map.Entry<Double, List<String>> e : intersectTiles.entrySet()) {
            res[row] = new String[e.getValue().size()];
            for (int t = 0; t < res[row].length; t++) {
                res[row][t] = e.getValue().get(t);
            }
            row--;
        }
        return res;
    }

    public static void main(String[] args) {
        Quadtree qt = new Quadtree(MapServer.ROOT_ULLON, MapServer.ROOT_ULLAT, MapServer.ROOT_LRLON,
                MapServer.ROOT_LRLAT, 7, "img/");
        Tile x = qt.root;
        while (x.depth != 7) {
            x = x.ne;
        }
        System.out.println(x.lonDPP);
        System.out.println();

        Map<String, Object> results = new HashMap<>();
        results.put("render_grid", null);
        results.put("raster_ul_lon", Double.MAX_VALUE);
        results.put("raster_ul_lat", Double.NEGATIVE_INFINITY);
        results.put("raster_lr_lon", Double.NEGATIVE_INFINITY);
        results.put("raster_lr_lat", Double.MAX_VALUE);
        results.put("depth", 0);
        results.put("query_success", false);

        double qUllon = -122.23995662778569;
        double qUllat = 37.877266154010954;
        double qLrlon = -122.22275132672245;
        double qLrlat = 37.85829260830337;
        double qLonDPP = (qLrlon - qUllon) / 613;

        Map<Double, List<String>> intersectTiles = new TreeMap<>();
        qt.intersectionTiles(qUllon, qUllat, qLrlon, qLrlat, qLonDPP, qt.root, results, intersectTiles);
        if (!intersectTiles.isEmpty()) {
            results.put("render_grid", qt.renderGrid(intersectTiles));
            results.replace("query_success", true);
        }

        String[][] a = (String[][]) results.get("render_grid");
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                System.out.print(a[i][j] + " ");
            }
            System.out.println();
        }
    }
}

/**
 * A class represents a tile object. It contains all the information about the tile object. And four links
 * to its northwestern, northeastern, southwestern, and southeastern children.
 */
class Tile {
    public static final int TILE_SIZE = 256;
    double ullon;
    double ullat;
    double lrlon;
    double lrlat;
    double lonDPP;
    String imgName;
    int index;
    int depth;
    Tile nw, ne, sw, se;

    public Tile(int index, double ullon, double ullat, double lrlon, double lrlat, String imgDir, int depth) {
        if (index == 0) {
            this.imgName = imgDir + "root.png";
        } else {
            this.imgName = imgDir + index + ".png";
        }
        this.ullon = ullon;
        this.ullat = ullat;
        this.lrlon = lrlon;
        this.lrlat = lrlat;
        this.lonDPP = (this.lrlon - this.ullon) / TILE_SIZE;
        this.index = index;
        this.depth = depth;
    }

}
