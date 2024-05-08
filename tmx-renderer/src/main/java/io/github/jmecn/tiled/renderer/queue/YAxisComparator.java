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
        int ret = Float.compare(v1.y, v2.y);
        if (ret == 0) {
            return Float.compare(v1.z, v2.z);
        } else {
            return ret;
        }
    }
    @Override
    public void setCamera(Camera cam) {
        // nothing
    }
}
