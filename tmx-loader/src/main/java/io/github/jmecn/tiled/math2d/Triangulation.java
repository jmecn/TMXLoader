package io.github.jmecn.tiled.math2d;

import java.util.List;

import com.jme3.math.Vector2f;

//COTD Entry submitted by John W. Ratcliff [jratcliff@verant.com]

//** THIS IS A CODE SNIPPET WHICH WILL EFFICIEINTLY TRIANGULATE ANY
//** POLYGON/CONTOUR (without holes) AS A STATIC CLASS.  THIS SNIPPET
//** IS COMPRISED OF 3 FILES, TRIANGULATE.H, THE HEADER FILE FOR THE
//** TRIANGULATE BASE CLASS, TRIANGULATE.CPP, THE IMPLEMENTATION OF
//** THE TRIANGULATE BASE CLASS, AND TEST.CPP, A SMALL TEST PROGRAM
//** DEMONSTRATING THE USAGE OF THE TRIANGULATOR.  THE TRIANGULATE
//** BASE CLASS ALSO PROVIDES TWO USEFUL HELPER METHODS, ONE WHICH
//** COMPUTES THE AREA OF A POLYGON, AND ANOTHER WHICH DOES AN EFFICENT
//** POINT IN A TRIANGLE TEST.
//** SUBMITTED BY JOHN W. RATCLIFF (jratcliff@verant.com) July 22, 2000

/*****************************************************************/
/** Static class to triangulate any contour/polygon efficiently **/
/** You should replace Vector2d with whatever your own Vector   **/
/** class might be.  Does not support polygons with holes.      **/
/** Uses STL vectors to represent a dynamic array of vertices.  **/
/** This code snippet was submitted to FlipCode.com by          **/
/** John W. Ratcliff (jratcliff@verant.com) on July 22, 2000    **/
/** I did not write the original code/algorithm for this        **/
/** this triangulator, in fact, I can't even remember where I   **/
/** found it in the first place.  However, I did rework it into **/
/** the following black-box static class so you can make easy   **/
/** use of it in your own code.  Simply replace Vector2d with   **/
/** whatever your own Vector implementation might be.           **/
/*****************************************************************/

/**
 * <p>
 * <a href=
 * "http://www.flipcode.com/archives/Efficient_Polygon_Triangulation.shtml"
 * >Efficient Polygon Triangulation</a>, by John W. Ratcliff on flipcode
 * </p>
 * 
 * I rewrite an Java version of Efficient_Polygon_Triangulation.
 * 
 * @author yanmaoyuan
 * 
 */
public class Triangulation {
    private static final float EPSILON = 0.0000000001f;

    public static float area(final List<Vector2f> contour) {

        int n = contour.size();

        float A = 0.0f;

        for (int p = n - 1, q = 0; q < n; p = q++) {
            Vector2f vp = contour.get(p);
            Vector2f vq = contour.get(q);
            A += vp.determinant(vq);
        }
        return A * 0.5f;
    }

    /*
     * InsideTriangle decides if a point P is inside of the triangle defined by
     * A, B, C.
     */
    public static boolean InsideTriangle(Vector2f A, Vector2f B, Vector2f C, Vector2f P)

    {
        Vector2f a = C.subtract(B);
        Vector2f b = A.subtract(C);
        Vector2f c = B.subtract(A);
        
        Vector2f ap = P.subtract(A);
        Vector2f bp = P.subtract(B);
        Vector2f cp = P.subtract(C);
        
        return ((a.determinant(bp) >= 0.0f) && (b.determinant(cp) >= 0.0f) && (c.determinant(ap) >= 0.0f));
    }

    public static boolean Snip(final List<Vector2f> contour, int u, int v,
            int w, int n, int[] V) {
        Vector2f A = contour.get(V[u]);
        Vector2f B = contour.get(V[v]);
        Vector2f C = contour.get(V[w]);
        
        if (EPSILON > (((B.x - A.x) * (C.y - A.y)) - ((B.y - A.y) * (C.x - A.x))))
            return false;

        for (int p = 0; p < n; p++) {
            if ((p == u) || (p == v) || (p == w))
                continue;
            Vector2f P = contour.get(V[p]);
            if (InsideTriangle(A, B, C, P))
                return false;
        }

        return true;
    }

    public static boolean Process(final List<Vector2f> contour,
            List<Integer> index) {
        /* allocate and initialize list of Vertices in polygon */

        int n = contour.size();
        if (n < 3)
            return false;

        int[] V = new int[n];

        /* we want a counter-clockwise polygon in V */

        if (0.0f < area(contour))
            for (int v = 0; v < n; v++)
                V[v] = v;
        else
            for (int v = 0; v < n; v++)
                V[v] = (n - 1) - v;

        int nv = n;

        /* remove nv-2 Vertices, creating 1 triangle every time */
        int count = 2 * nv; /* error detection */

        for (int v = nv - 1; nv > 2;) {
            /* if we loop, it is probably a non-simple polygon */
            if (0 >= (count--)) {
                // ** Triangulate: ERROR - probable bad polygon!
                return false;
            }

            /* three consecutive vertices in current polygon, <u,v,w> */
            int u = v;
            if (nv <= u)
                u = 0; /* previous */
            v = u + 1;
            if (nv <= v)
                v = 0; /* new v */
            int w = v + 1;
            if (nv <= w)
                w = 0; /* next */

            if (Snip(contour, u, v, w, nv, V)) {
                int a, b, c, s, t;

                /* true names of the vertices */
                a = V[u];
                b = V[v];
                c = V[w];

                /* output Triangle */
                index.add(a);
                index.add(b);
                index.add(c);

                /* remove v from remaining polygon */
                for (s = v, t = v + 1; t < nv; s++, t++)
                    V[s] = V[t];
                nv--;

                /* resest error detection counter */
                count = 2 * nv;
            }
        }

        return true;
    }
}
