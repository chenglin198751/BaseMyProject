package com.wcl.test.view.polygon.shapes;

/**
 * Regular com.wcl.test.view.polygon shape implementation
 *
 */
public class RegularPolygonShape extends BasePolygonShape {

    @Override
    protected void addEffect(float currentX, float currentY, float nextX, float nextY) {
        getPath().lineTo(nextX, nextY);
    }
}
