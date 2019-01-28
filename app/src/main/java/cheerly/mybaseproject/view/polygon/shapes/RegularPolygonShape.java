package cheerly.mybaseproject.view.polygon.shapes;

/**
 * Regular cheerly.mybaseproject.view.polygon shape implementation
 *
 */
public class RegularPolygonShape extends BasePolygonShape {

    @Override
    protected void addEffect(float currentX, float currentY, float nextX, float nextY) {
        getPath().lineTo(nextX, nextY);
    }
}