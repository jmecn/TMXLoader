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

    public TileMesh(Point coord, Point size, Point offset, Point origin) {
        int x = coord.x;
        int y = coord.y;

        float[] vertices = getPositions(size, offset, origin);

        // use TexCoord2 to store the tile's position in the image.
        // this is useful when we want to get the tile's position in the image.
        float[] texCoord2 = new float[]{x, y, 0, x, y, 0, x, y, 0, x, y, 0};

        float[] texCoords = new float[]{0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f};
        short[] indexes = new short[]{3, 2, 1, 3, 1, 0};
        float[] normals = new float[]{0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0};

        this.setBuffer(VertexBuffer.Type.Position, 3, vertices);
        this.setBuffer(VertexBuffer.Type.TexCoord, 2, texCoords);
        this.setBuffer(VertexBuffer.Type.TexCoord2, 3, texCoord2);
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
     * @param size the size of the tile
     * @param offset the offset of the tile
     * @param origin the origin of the tile
     */
    private float[] getPositions(Point size, Point offset, Point origin) {
        float[] vertices = new float[]{
                0, 0, -1,
                1, 0, -1,
                1, 0, 0,
                0, 0, 0};

        // I know this can be down in vertex shader, but it will cause view frustum culling problem.
        for (int i = 0; i < vertices.length; i += 3) {
            vertices[i] = vertices[i] * size.x + origin.x + offset.x;
            vertices[i + 2] = vertices[i + 2] * size.y + origin.y + offset.y;
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
        return new float[]{0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f};
    }
}
