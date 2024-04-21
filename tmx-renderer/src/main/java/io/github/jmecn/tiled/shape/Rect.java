package io.github.jmecn.tiled.shape;

import com.jme3.math.Vector2f;
import com.jme3.scene.VertexBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class Rect extends Polyline {

    public Rect(float width, float height, boolean border) {
        if (border) {
            border(width, height);
        } else {
            fill(width, height);
        }
    }

    private void border(float w, float h) {
        List<Vector2f> points = new ArrayList<>();
        points.add(new Vector2f(0,0));
        points.add(new Vector2f(w,0));
        points.add(new Vector2f(w, h));
        points.add(new Vector2f(0, h));
        polyline(points, true);
    }

    private void fill(float w, float h) {
        this.setBuffer(VertexBuffer.Type.Position, 3, new float[] {
                0, 0, h,
                w, 0, h,
                w, 0, 0,
                0, 0, 0
        });
        this.setBuffer(VertexBuffer.Type.Normal, 3, new float[] {
                0, 1, 0,
                0, 1, 0,
                0, 1, 0,
                0, 1, 0
        });
        this.setBuffer(VertexBuffer.Type.TexCoord, 2, new float[] {
                0, 0,
                1, 0,
                1, 1,
                0, 1
        });
        this.setBuffer(VertexBuffer.Type.Index, 3, new short[] {
                0, 1, 2,
                0, 2, 3
        });
        this.setStatic();
        this.updateBound();
    }
}
