import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class Polygon implements Drawable {
    public static final List<Point> POINTS = new ArrayList<>();
    private static float[] colors;

    public Polygon(GLAutoDrawable drawable) {
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
        for (int i = 0; i < colors.length; i += 4) {
            setColor(getColor(), i);
        }

        gl.glDrawPixels(drawable.getSurfaceWidth(), drawable.getSurfaceHeight(), GL2.GL_RGBA, GL2.GL_FLOAT, FloatBuffer.wrap(colors));
    }

    @Override
    public float[] getColor() {
        return new float[]{1.0F, 0.0F, 0.0F, 1.0F};
    }

    public static class COLOR {
        private static final float R = 1.0F;
        private static final float G = 1.0F;
        private static final float B = 1.0F;
        private static final float A = 1.0F;
    }
}
