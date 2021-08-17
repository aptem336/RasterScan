import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import java.util.ArrayList;
import java.util.List;

public class Scene implements Drawable {
    final List<Drawable> drawables = new ArrayList<>();

    public Scene(GLAutoDrawable drawable) {
        drawables.add(new Polygon(drawable));
        drawables.add(new Grid(drawable));
    }

    @Override
    public void draw(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        float[] clearColor = getColor();
        gl.glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
        for (Drawable d : drawables) {
            d.draw(drawable);
        }
    }

    @Override
    public float[] getColor() {
        return new float[]{0.15F, 0.15F, 0.15F, 0.15F};
    }
}
