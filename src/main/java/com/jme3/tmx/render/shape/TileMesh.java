package com.jme3.tmx.render.shape;

import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.tmx.math2d.Point;

/**
 * This is the mesh of a tile.
 *
 * @author yanmaoyuan
 */
public class TileMesh extends Mesh {

    public TileMesh(int x, int y, int width, int height, int imageWidth, int imageHeight, Point offset, Point origin) {

        float[] texCoords = getTexCoords(x, y, width, height, imageWidth, imageHeight);

        float[] vertices = getPositions(width, height, offset, origin);

        short[] indexes = new short[]{3, 2, 1, 3, 1, 0};

        float[] normals = new float[]{0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0};

        // use TexCoord2 to store the tile's position in the image.
        // this is useful when we want to get the tile's position in the image.
        float[] texCoord2 = new float[]{x, y, x, y, x, y, x, y};
        this.setBuffer(VertexBuffer.Type.Position, 3, vertices);
        this.setBuffer(VertexBuffer.Type.TexCoord, 2, texCoords);
        this.setBuffer(VertexBuffer.Type.TexCoord2, 2, texCoord2);
        this.setBuffer(VertexBuffer.Type.Normal, 3, normals);
        this.setBuffer(VertexBuffer.Type.Index, 3, indexes);
        this.updateBound();
        this.setStatic();
    }

    /**
     * Calculate the vertices' position of this tile.
     *
     * <pre>
     * 0          1
     * *----------*
     * |        * |
     * |      *   |
     * |    *     |
     * |  *       |
     * *----------*
     * 3          2
     * </pre>
     * @param width  the width of the tile
     * @param height the height of the tile
     * @param offset the offset of the tile
     * @param origin the origin of the tile
     */
    private float[] getPositions(int width, int height, Point offset, Point origin) {
        float[] vertices = new float[]{
                0,     0, -height,
                width, 0, -height,
                width, 0, 0,
                0,     0, 0};

        // I know this can be down in vertex shader, but it will cause view frustum culling problem.
        for (int i = 0; i < vertices.length; i += 3) {
            vertices[i] += origin.x + offset.x;
            vertices[i + 2] += origin.y + offset.y;
        }
        return vertices;
    }

    /**
     * Calculate the texCoord of this tile in an Image.
     *
     * <pre>
     * (u0,v0)    (u1,v0)
     * *----------*
     * |        * |
     * |      *   |
     * |    *     |
     * |  *       |
     * *----------*
     * (u0,v1)    (u1,v1)
     * </pre>
     */
    private float[] getTexCoords(float x, float y, float width, float height, float imageWidth, float imageHeight) {
//        float u0 = x / imageWidth;
//        float v0 = 1f - y / imageHeight;
//        float u1 = (x + width) / imageWidth;
//        float v1 = 1f - (y + height) / imageHeight;
        float u0 = 0f;
        float v0 = 0f;
        float u1 = 1f;
        float v1 = 1f;
        return new float[]{u0, v0, u1, v0, u1, v1, u0, v1};
    }
}
