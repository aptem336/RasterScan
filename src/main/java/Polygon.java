import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Polygon implements Drawable {
    public static final List<Point> POINTS = new ArrayList<>();
    public static float[] colors;
    private final Scene scene;

    public Polygon(GLAutoDrawable drawable, Scene scene) {
        this.scene = scene;
        colors = new float[drawable.getSurfaceWidth() * drawable.getSurfaceHeight() * 4];
    }

    private float[] getColor(int offset) {
        float[] color = new float[4];
        System.arraycopy(colors, offset, color, 0, 4);
        return color;
    }

    public void setColor(float[] color, int offset) {
        System.arraycopy(color, 0, colors, offset, 4);
    }

    @Override
    public void draw(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        Arrays.fill(Polygon.colors, 0.0F);
        for (int i = 0; i < POINTS.size(); i++) {
            Point a = POINTS.get(i);
            Point b = POINTS.get((i + 1) % POINTS.size());
            if (a.y > b.y) {
                a = POINTS.get((i + 1) % POINTS.size());
                b = POINTS.get(i);
            }
            for (int y = a.y; y < b.y; y++) {
                int startX = a.x + ((y - a.y) * (b.x - a.x)) / (b.y - a.y);
                for (int x = startX; x < drawable.getSurfaceWidth(); x++) {
                    int offset = (drawable.getSurfaceHeight() - y) * drawable.getSurfaceWidth() * 4 + x * 4;
                    float[] color = getColor(offset);
                    if (color[0] == getColor()[0]
                            && color[1] == getColor()[1]
                            && color[2] == getColor()[2]
                            && color[3] == getColor()[3]) {
                        setColor(new float[]{0.0F, 0.0F, 0.0F, 0.0F}, offset);
                    } else {
                        setColor(getColor(), offset);
                    }
                }
            }
        }

        gl.glDrawPixels(drawable.getSurfaceWidth(), drawable.getSurfaceHeight(), GL2.GL_RGBA, GL2.GL_FLOAT, FloatBuffer.wrap(colors));
    }

    @Override
    public float[] getColor() {
        return new float[]{1.0F, 1.0F, 1.0F, 1.0F};
    }
}
