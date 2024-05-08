package io.github.jmecn.tiled.renderer.queue;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.GeometryComparator;
import com.jme3.scene.Geometry;
import io.github.jmecn.tiled.enums.RenderOrder;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class RenderOrderComparator implements GeometryComparator {
    private final RenderOrder renderOrder;

    public RenderOrderComparator(RenderOrder renderOrder) {
        this.renderOrder = renderOrder;
    }

    @Override
    public int compare(Geometry o1, Geometry o2) {
        Vector3f v1 = o1.getWorldTranslation();
        Vector3f v2 = o2.getWorldTranslation();
        // sorted by layer
        int layer = Float.compare(v1.y, v2.y);
        if (layer != 0) {
            return layer;
        }

        int up = Float.compare(v1.z, v2.z);

        if (up != 0) {
            switch (renderOrder) {
                case LEFT_UP:
                case RIGHT_UP:
                    return -up;
                case LEFT_DOWN:
                case RIGHT_DOWN:
                    return up;
            }
            return up;
        }

        int right = Float.compare(v1.x, v2.x);
        if (right == 0) {
            return right;
        } else {
            switch (renderOrder) {
                case RIGHT_DOWN:
                case RIGHT_UP:
                    return right;
                case LEFT_DOWN:
                case LEFT_UP:
                    return -right;
            }
        }
        return right;
    }

    @Override
    public void setCamera(Camera cam) {
        // nothing
    }
}
