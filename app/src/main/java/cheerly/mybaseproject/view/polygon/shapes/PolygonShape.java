package cheerly.mybaseproject.view.polygon.shapes;

import android.graphics.Path;

/**
 * Interface for set a custom cheerly.mybaseproject.view.polygon shape
 */
public interface PolygonShape {

    /**
     * Return a closed valid Path
     *
     * @param polygonShapeSpec polygonal specs
     * @return a Path
     */
    Path getPolygonPath(PolygonShapeSpec polygonShapeSpec);
}
