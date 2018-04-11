import java.util.*;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {

    private Quadtree quadtree;

    /** imgRoot is the name of the directory containing the images.
     *  You may not actually need this for your class. */
    public Rasterer(String imgRoot) {
        quadtree = new Quadtree(MapServer.ROOT_ULLON, MapServer.ROOT_ULLAT, MapServer.ROOT_LRLON,
                MapServer.ROOT_LRLAT, 7, imgRoot);
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

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     * </p>
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified:
     * "render_grid"   -> String[][], the files to display
     * "raster_ul_lon" -> Number, the bounding upper left longitude of the rastered image <br>
     * "raster_ul_lat" -> Number, the bounding upper left latitude of the rastered image <br>
     * "raster_lr_lon" -> Number, the bounding lower right longitude of the rastered image <br>
     * "raster_lr_lat" -> Number, the bounding lower right latitude of the rastered image <br>
     * "depth"         -> Number, the 1-indexed quadtree depth of the nodes of the rastered image.
     *                    Can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "query_success" -> Boolean, whether the query was able to successfully complete. Don't
     *                    forget to set this to true! <br>
     * //@see #REQUIRED_RASTER_REQUEST_PARAMS
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        //System.out.println(params);
        Map<String, Object> results = new HashMap<>();
        //System.out.println("Since you haven't implemented getMapRaster, nothing is displayed in "
         //                  + "your browser.");
        results.put("render_grid", null);
        results.put("raster_ul_lon", Double.MAX_VALUE);
        results.put("raster_ul_lat", Double.NEGATIVE_INFINITY);
        results.put("raster_lr_lon", Double.NEGATIVE_INFINITY);
        results.put("raster_lr_lat", Double.MAX_VALUE);
        results.put("depth", 0);
        results.put("query_success", false);
        results.put("render_grid", null);

        double qUllon = params.get("ullon");
        double qUllat = params.get("ullat");
        double qLrlon = params.get("lrlon");
        double qLrlat = params.get("lrlat");

        if ( qUllon > qLrlon || qUllat < qLrlat) {
            return results;
        }

        double qLonDPP = (qLrlon - qUllon) / params.get("w");

        Map<Double, List<String>> intersectTiles = new TreeMap<>();
        quadtree.queryIntersection(qUllon, qUllat, qLrlon, qLrlat, qLonDPP, results, intersectTiles);
        if (!intersectTiles.isEmpty()) {
            results.replace("query_success", true);
            results.replace("render_grid", renderGrid(intersectTiles));
        }

        return results;
    }
}
