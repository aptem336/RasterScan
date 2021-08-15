import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

public class Grid implements Drawable {
    private static final int X_STEP = 20;
    private static final int Y_STEP = 20;

    @Override
    public void draw(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glColor4f(Grid.COLOR.R, Grid.COLOR.G, Grid.COLOR.B, Grid.COLOR.A);
        gl.glBegin(GL2.GL_LINES);
        for (int X = 0; X < Grid.SIZE.WIDTH; X += X_STEP) {
            gl.glVertex2i(X, 0);
            gl.glVertex2i(X, Grid.SIZE.HEIGHT);
        }
        for (int Y = 0; Y < Grid.SIZE.HEIGHT; Y += Y_STEP) {
            gl.glVertex2i(0, Y);
            gl.glVertex2i(Grid.SIZE.WIDTH, Y);
        }
        gl.glEnd();
    }

    private static class SIZE {
        private static final int WIDTH = 1000;
        private static final int HEIGHT = 1000;
    }

    private static class COLOR {
        private static final float R = 0.25F;
        private static final float G = 0.25F;
        private static final float B = 0.25F;
        private static final float A = 0.25F;
    }
}
