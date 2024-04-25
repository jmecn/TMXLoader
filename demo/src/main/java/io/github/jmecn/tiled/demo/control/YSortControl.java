package io.github.jmecn.tiled.demo.control;

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import io.github.jmecn.tiled.renderer.MapRenderer;

/**
 * @author yanmaoyuan
 */
public class YSortControl extends AbstractControl {
    private final MapRenderer mapRenderer;
    private final int layerIndex;// layer index

    public YSortControl(MapRenderer mapRenderer, int layerIndex) {
        this.mapRenderer = mapRenderer;
        this.layerIndex = layerIndex;
    }

    @Override
    protected void controlUpdate(float tpf) {
        Vector3f position = spatial.getLocalTranslation();
        float y = mapRenderer.getLayerBaseHeight(layerIndex) + mapRenderer.getObjectTopDownHeight(position.z);
        spatial.setLocalTranslation(position.x, y, position.z);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        // nothing
    }
}
