package io.github.jmecn.tiled.render.shape;

import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.TempVars;
import io.github.jmecn.tiled.math2d.Point;

/**
 * This is the mesh of a tile.
 *
 * @author yanmaoyuan
 */
public class TileMesh extends Mesh {

    private Point coord;
    private Point size;
    private Point offset;
    private Point origin;

    /**
     * Create a tile mesh.
     *
     * @param coord the coordinate of the tile
     * @param size the size of the tile
     * @param offset the offset of the tile
     * @param origin the origin of the tile
     */
    public TileMesh(Point coord, Point size, Point offset, Point origin) {
        this.coord = coord;
        this.size = size;
        this.offset = offset;
        this.origin = origin;

        int x = coord.getX();
        int y = coord.getY();

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
            vertices[i] = vertices[i] * size.getX() + origin.getX() + offset.getX();
            vertices[i + 2] = vertices[i + 2] * size.getY() + origin.getY() + offset.getY();
        }
        return vertices;
    }

    public TileMesh(TileMesh mesh, boolean isFlipHorizontally, boolean isFlipVertically, boolean isFlipAntiDiagonally) {
        this(mesh.coord, mesh.size, mesh.offset, mesh.origin);

        float[] texCoords = new float[]{0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f};

        if (isFlipHorizontally) {
            swap(texCoords, 0, 1, 2);
            swap(texCoords, 2, 3, 2);
        }
        if (isFlipVertically) {
            swap(texCoords, 0, 3, 2);
            swap(texCoords, 1, 2, 2);
        }
        if (isFlipAntiDiagonally) {
            swap(texCoords, 0, 2, 2);
        }

        this.setBuffer(VertexBuffer.Type.TexCoord, 2, texCoords);
    }

    public TileMesh(TileMesh mesh, boolean isFlipHorizontally, boolean isFlipVertically, boolean isFlipAntiDiagonally, boolean isRotatedHexagonal120) {
        this(mesh.coord, mesh.size, mesh.offset, mesh.origin);

        float[] texCoords = new float[]{0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f};

        if (isFlipHorizontally) {
            swap(texCoords, 0, 1, 2);
            swap(texCoords, 2, 3, 2);
        }
        if (isFlipVertically) {
            swap(texCoords, 0, 3, 2);
            swap(texCoords, 1, 2, 2);
        }

        float rotate = 0f;
        if (isFlipAntiDiagonally) {
            rotate += FastMath.PI / 3f;// 60
        }
        if (isRotatedHexagonal120) {
            rotate += FastMath.TWO_PI / 3f;// 120
        }
        if (rotate != 0f) {
            float[] vertices = getPositions(size, offset, origin);

            TempVars vars = TempVars.get();
            Matrix3f mat3 = vars.tempMat3;
            mat3.fromAngleAxis(-rotate, Vector3f.UNIT_Y);

            Vector3f center = mesh.getBound().getCenter();
            for (int i = 0; i < 4; i++) {
                Vector3f pos = vars.vect1;
                pos.set(vertices[i * 3], vertices[i * 3 + 1], vertices[i * 3 + 2]);
                pos.subtractLocal(center);
                mat3.mult(pos, pos);
                pos.addLocal(center);
                vertices[i * 3] = pos.x;
                vertices[i * 3 + 1] = pos.y;
                vertices[i * 3 + 2] = pos.z;
            }
            vars.release();

            this.setBuffer(VertexBuffer.Type.Position, 3, vertices);
        }
        this.setBuffer(VertexBuffer.Type.TexCoord, 2, texCoords);
    }

    private void swap(float[] buf, int i, int j, int component) {
        for (int k = 0; k < component; k++) {
            int a = i * component + k;
            int b = j * component + k;
            float tmp = buf[b];
            buf[b] = buf[a];
            buf[a] = tmp;
        }
    }
}
