package com.jme3.tmx.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * desc:
 *
 * @author yanmaoyuan
 * @date 2023/9/26
 */
public class GroupLayer extends Layer {

    private final List<Layer> layers;
    private final Map<String, Layer> layerMap;
    public GroupLayer( ) {
        layers = new ArrayList<>();
        layerMap = new HashMap<>();
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public void addLayer(Layer layer) {
        layer.setMap(getMap());
        layer.setParent(this);
        layers.add(layer);
        layerMap.put(layer.getName(), layer);
    }
}
