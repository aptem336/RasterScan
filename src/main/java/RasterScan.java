import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

public class RasterScan implements GLEventListener {
    private Scene scene;

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(1000, 1000);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        GLCanvas canvas = new GLCanvas();
        canvas.addGLEventListener(new RasterScan());
        canvas.setBounds(0, 0, 1000, 1000);

        canvas.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                int X_ROUNDER = 20;
                int Y_ROUNDER = 20;
                if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
                    Polygon.POINTS.add(new Point(mouseEvent.getX() - mouseEvent.getX() % X_ROUNDER + X_ROUNDER / 2,
                            mouseEvent.getY() - mouseEvent.getY() % Y_ROUNDER + Y_ROUNDER / 2));
                    Arrays.fill(Polygon.colors, 0.0F);
                } else if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
                    Polygon.POINTS.clear();
                    Arrays.fill(Polygon.colors, 0.0F);
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
        scene = new Scene(drawable);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glOrthof(0, width, height, 0, 0, 1);
    }

    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        scene.draw(drawable);
    }

    public void dispose(GLAutoDrawable glAutoDrawable) {
    }
}
