import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import java.nio.FloatBuffer;

public class Grid implements Drawable {
    private static final int X_STEP = 20;
    private static final int Y_STEP = 20;

    public Grid(GLAutoDrawable drawable) {
    }

    @Override
    public void draw(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glColor4fv(FloatBuffer.wrap(getColor()));
        gl.glBegin(GL2.GL_LINES);
        for (int X = 0; X < drawable.getSurfaceWidth(); X += X_STEP) {
            gl.glVertex2i(X, 0);
            gl.glVertex2i(X, drawable.getSurfaceHeight());
        }
        for (int Y = 0; Y < drawable.getSurfaceHeight(); Y += Y_STEP) {
            gl.glVertex2i(0, Y);
            gl.glVertex2i(drawable.getSurfaceWidth(), Y);
        }
        gl.glEnd();
    }

    @Override
    public float[] getColor() {
        return new float[]{0.25F, 0.25F, 0.25F, 0.25F};
    }
}
