import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Polygon implements Drawable {
    private static final float POINT_SIZE = 5.0F;
    public static final List<Point> POINTS = new ArrayList<>();

    @Override
    public void draw(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glColor4f(COLOR.R, COLOR.G, COLOR.B, COLOR.A);
        gl.glBegin(GL2.GL_LINE_STRIP);
        for (Point point : POINTS) {
            gl.glVertex2i(point.x, point.y);
        }
        gl.glEnd();

        if (!POINTS.isEmpty()) {
            gl.glPointSize(POINT_SIZE);
            gl.glBegin(GL2.GL_POINTS);
            gl.glVertex2i(POINTS.get(POINTS.size() - 1).x,
                    POINTS.get(POINTS.size() - 1).y);
            gl.glEnd();
        }
    }

    private static class COLOR {
        private static final float R = 1.0F;
        private static final float G = 1.0F;
        private static final float B = 1.0F;
        private static final float A = 1.0F;
    }
}
