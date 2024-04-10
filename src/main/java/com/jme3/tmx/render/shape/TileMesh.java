package com.jme3.tmx.render.shape;

import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;

/**
 * This is the mesh of a tile.
 *
 * @author yanmaoyuan
 */
public class TileMesh extends Mesh {

    public TileMesh(int x, int y, int width, int height, int imageWidth, int imageHeight, float offsetX, float offsetY) {

        float[] texCoords = getTexCoords(x, y, width, height, imageWidth, imageHeight);

        /**
         * Calculate the vertices' position of this tile.
         *
         * <pre>
         * 3          2
         * *----------*
         * |        * |
         * |      *   |
         * |    *     |
         * |  *       |
         * *----------*
         * 0          1
         * </pre>
         */
        float[] vertices = new float[]{
                offsetX,         0, offsetY + height,
                offsetX + width, 0, offsetY + height,
                offsetX + width, 0, offsetY,
                offsetX,         0, offsetY};

        short[] indexes = new short[]{0, 1, 2, 0, 2, 3};

        float[] normals = new float[]{0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0};

        this.setBuffer(VertexBuffer.Type.Position, 3, vertices);
        this.setBuffer(VertexBuffer.Type.TexCoord, 2, texCoords);
        this.setBuffer(VertexBuffer.Type.Normal, 3, normals);
        this.setBuffer(VertexBuffer.Type.Index, 3, indexes);
        this.updateBound();
        this.setStatic();
    }

    /**
     * Calculate the texCoord of this tile in an Image.
     *
     * <pre>
     * (u0,v1)    (u1,v1)
     * *----------*
     * |        * |
     * |      *   |
     * |    *     |
     * |  *       |
     * *----------*
     * (u0,v0)    (u1,v0)
     * </pre>
     */
    private float[] getTexCoords(float x, float y, float width, float height, float imageWidth, float imageHeight) {
        float u0 = x / imageWidth;
        float v0 = 1f - (y + height) / imageHeight;
        float u1 = (x + width) / imageWidth;
        float v1 = 1f - y / imageHeight;
        return new float[]{u0, v0, u1, v0, u1, v1, u0, v1};
    }
}
