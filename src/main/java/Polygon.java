import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class Polygon implements Drawable {
    private static final int X_ROUNDER = 20;
    private static final int Y_ROUNDER = 20;
    private static final float POINT_SIZE = 5.0F;

    private List<Point> getPoints() {
        return INPUT.MOUSE.RELEASED.EVENTS.stream().map(mouseEvent ->
                new Point(mouseEvent.getX() - mouseEvent.getX() % X_ROUNDER + X_ROUNDER / 2,
                        mouseEvent.getY() - mouseEvent.getY() % Y_ROUNDER + Y_ROUNDER / 2)
        ).collect(Collectors.toList());
    }

    @Override
    public void draw(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glColor4f(COLOR.R, COLOR.G, COLOR.B, COLOR.A);
        gl.glBegin(GL2.GL_LINE_STRIP);
        for (Point point : getPoints()) {
            gl.glVertex2i(point.x, point.y);
        }
        gl.glEnd();

        if (!getPoints().isEmpty()) {
            gl.glPointSize(POINT_SIZE);
            gl.glBegin(GL2.GL_POINTS);
            gl.glVertex2i(getPoints().get(getPoints().size() - 1).x,
                    getPoints().get(getPoints().size() - 1).y);
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
