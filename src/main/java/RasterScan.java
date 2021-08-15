import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class RasterScan implements GLEventListener {
    private Scene scene;

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(Scene.SIZE.WIDTH, Scene.SIZE.HEIGHT);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        GLCanvas canvas = new GLCanvas();
        canvas.addGLEventListener(new RasterScan());
        canvas.setBounds(0, 0, Scene.SIZE.WIDTH, Scene.SIZE.HEIGHT);

        canvas.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
                    INPUT.MOUSE.RELEASED.EVENTS.add(mouseEvent);
                } else if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
                    INPUT.MOUSE.RELEASED.EVENTS.clear();
                }
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
            }
        });
        frame.add(canvas);
        final Animator animator = new Animator(canvas);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                new Thread(() -> {
                    animator.stop();
                    System.exit(0);
                }).start();
            }
        });
        animator.start();
    }

    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrthof(0, Scene.SIZE.WIDTH, Scene.SIZE.HEIGHT, 0, 0, 1);
        gl.glClearColor(Scene.COLOR.R, Scene.COLOR.G, Scene.COLOR.B, Scene.COLOR.A);

        scene = new Scene();
        scene.drawables.add(new Grid());
        scene.drawables.add(new Polygon());
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    }

    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

        for (Drawable d : scene.drawables) {
            d.draw(drawable);
        }


    }

    public void dispose(GLAutoDrawable glAutoDrawable) {
    }
}
