package io.github.jmecn.tiled.renderer.queue;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.GeometryComparator;
import com.jme3.scene.Geometry;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class YAxisComparator implements GeometryComparator {

    @Override
    public int compare(Geometry o1, Geometry o2) {
        Vector3f v1 = o1.getWorldTranslation();
        Vector3f v2 = o2.getWorldTranslation();
        return Float.compare(v1.y, v2.y);
    }

    @Override
    public void setCamera(Camera cam) {
        // nothing
    }
}
