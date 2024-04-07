package com.jme3.tmx.util;

import java.util.ArrayList;
import java.util.List;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.tmx.math2d.Triangulation;
import com.jme3.util.BufferUtils;

/**
 * Create mesh for the visual part of an ObjectNode.
 * Rectangle, Ellipse, Polygon, Polyline
 * 
 * @author yanmaoyuan
 *
 */
public final class ObjectMesh {

    private ObjectMesh() {}

    /**
     * Make an ellipse mesh.
     * 
     * 
     * @param width the ellipse width
     * @param height the ellipse height
     * @param count how many points you need?
     * @return the ellipse meth
     */
    public static Mesh makeEllipse(double width, double height, int count) {
        // the uv center
        float uc = 0.5f;
        float vc = 0.5f;
        
        // the ellipse center
        float xc = (float) (width * uc);
        float yc = (float) (height * vc);
        
        // add two for center vertex and last triangle
        Vector3f[] vertex = new Vector3f[count+2];
        Vector3f[] normal = new Vector3f[count+2];
        Vector2f[] texCoord = new Vector2f[count+2];
        int[] index = new int[count+2];

        // the center
        vertex[0] = new Vector3f(xc, 0, yc);
        normal[0] = new Vector3f(0, 1, 0);
        texCoord[0] = new Vector2f(uc, vc);
        index[0] = 0;
        
        float radian = FastMath.TWO_PI / count;
        float r = 0;
        for(int i=0; i<count; i++) {
            float x = FastMath.sin(r);
            float y = FastMath.cos(r);
            r += radian;
            
            vertex[i+1] = new Vector3f(x*xc+xc, 0, y*yc+yc);
            normal[i+1] = new Vector3f(0, 1, 0);
            texCoord[i+1] = new Vector2f(x*uc+uc, y*vc+vc);
            index[i+1] = i+1;
        }
        vertex[count+1] = vertex[1];
        normal[count+1] = normal[1];
        texCoord[count+1] = texCoord[1];
        index[count+1] = count+1;
        
        Mesh mesh = new Mesh();
        mesh.setMode(Mode.TriangleFan);
        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertex));
        mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normal));
        mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
        mesh.setBuffer(Type.Index, 3, index);
        mesh.setStatic();
        mesh.updateBound();
        mesh.updateCounts();
        
        return mesh;
    }
    
    /**
     * Make a border for Ellipse.
     * 
     * 
     * @param width the ellipse width
     * @param height the ellipse height
     * @param count How many points you need?
     * @return the ellipse border mesh
     */
    public static Mesh makeEllipseBorder(double width, double height, int count) {
        float xc = (float) (width * 0.5);
        float yc = (float) (height * 0.5);
        
        List<Vector2f> points = new ArrayList<>(count);
        float radian = FastMath.TWO_PI / count;
        float r = 0;
        for(int i=0; i<count; i++) {
            float x = FastMath.sin(r) * xc + xc;
            float y = FastMath.cos(r) * yc + yc;
            points.add(new Vector2f(x, y));
            
            r += radian;
        }
        
        return makePolyline(points, true);
    }
    
    public static Mesh makeRectangle(double width, double height) {
        float w = (float) width;
        float h = (float) height;
        
        Mesh mesh = new Mesh();
        mesh.setBuffer(Type.Position, 3, new float[] {
                0, 0, h,
                w, 0, h,
                w, 0, 0,
                0, 0, 0
        });
        mesh.setBuffer(Type.Normal, 3, new float[] {
                0, 1, 0,
                0, 1, 0,
                0, 1, 0,
                0, 1, 0
        });
        mesh.setBuffer(Type.TexCoord, 2, new float[] {
                0, 0,
                1, 0,
                1, 1,
                0, 1
        });
        mesh.setBuffer(Type.Index, 3, new short[] {
                0, 1, 2,
                0, 2, 3
        });
        mesh.setStatic();
        mesh.updateBound();
        
        return mesh;
    }
    
    public static Mesh makeRectangleBorder(double width, double height) {
        List<Vector2f> points = new ArrayList<>();
        points.add(new Vector2f(0,0));
        points.add(new Vector2f((float) width,0));
        points.add(new Vector2f((float)width, (float)height));
        points.add(new Vector2f(0, (float) height));
        
        return makePolyline(points, true);
    }
    
    public static Mesh makePolygon(List<Vector2f> points) {

        int len = points.size();
        List<Integer> result = new ArrayList<>(len);

        Triangulation.Process(points, result);
        
        Vector3f[] vertex = new Vector3f[len];
        Vector3f[] normal = new Vector3f[len];
        Vector2f[] texCoord = new Vector2f[len];
        for(int i=0; i<len; i++) {
            Vector2f p = points.get(i);
            vertex[i] = new Vector3f(p.x, 0f, p.y);
            normal[i] = new Vector3f(0, 1, 0);
            texCoord[i] = new Vector2f(0, 0);
        }
        
        len = result.size();
        int[] index = new int[len];
        for(int i=0; i<len; i++) {
            index[i] = result.get(i);
        }
        
        Mesh mesh = new Mesh();
        mesh.setMode(Mode.Triangles);
        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertex));
        mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normal));
        mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
        mesh.setBuffer(Type.Index, 3, index);
        mesh.setStatic();
        mesh.updateBound();
        mesh.updateCounts();
        
        return mesh;
    }
    
    public static Mesh makePolyline(List<Vector2f> points, boolean closePath) {
        int len = points.size();
        if (len < 2) {
            throw new IllegalArgumentException("An polygon must have 2 points at least.");
        }
        
        Vector3f[] vertex = new Vector3f[len];
        Vector3f[] normal = new Vector3f[len];
        short[] index = new short[closePath?len+1:len];
        
        // first one
        Vector2f point = new Vector2f();
        for(int i=0; i<len; i++) {
            point.set(points.get(i));
            vertex[i] = new Vector3f(point.x, 0, point.y);
            normal[i] = new Vector3f(0f, 1f, 0f);
            index[i] = (short) i;
        }
        
        if (closePath) {
            index[len] = 0;
        }
        
        Mesh mesh = new Mesh();
        mesh.setMode(Mode.LineStrip);
        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertex));
        mesh.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normal));
        mesh.setBuffer(Type.Index, 2, index);
        mesh.setStatic();
        mesh.updateBound();
        mesh.updateCounts();
        
        return mesh;
    }

    /**
     * Make a border for map marker. It's a half circle on the top and a triangle on the bottom, point to (0,0).
     * @param radius the radius of the marker
     * @param count how many points you need?
     * @return the mesh
     */
    public static Mesh makeMarkerBorder(float radius, int count) {
        List<Vector2f> points = new ArrayList<>(count + 1);
        points.add(new Vector2f(0, 0));

        float baseHeight = radius * -2f;
        float baseRadian = FastMath.PI / 3f;
        float radian = (FastMath.PI + baseRadian) / count;
        float r = 0;
        for(int i=0; i<count; i++) {
            float x = FastMath.sin(r + baseRadian) * radius;
            float y = FastMath.cos(r + baseRadian) * radius + baseHeight;
            points.add(new Vector2f(x, y));

            r += radian;
        }

        return makePolyline(points, true);
    }


    /**
     * Make a border for map marker. It's a half circle on the top and a triangle on the bottom, point to (0,0).
     * @param radius the radius of the marker
     * @param count how many points you need?
     * @return the mesh
     */
    public static Mesh makeMarker(float radius, int count) {
        float baseHeight = radius * -2f;
        float baseRadian = FastMath.PI / 3f;

        List<Vector2f> points = new ArrayList<>(count + 2);
        points.add(new Vector2f(0, 0));

        float radian = (FastMath.PI + baseRadian) / count;
        float r = 0;
        for(int i=0; i<count; i++) {
            float x = FastMath.sin(r + baseRadian) * radius;
            float y = FastMath.cos(r + baseRadian) * radius + baseHeight;
            points.add(new Vector2f(x, y));

            r += radian;
        }

        return makePolygon(points);
    }
}
