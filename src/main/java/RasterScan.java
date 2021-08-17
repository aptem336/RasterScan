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
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RasterScan implements GLEventListener {

    private static final List<Point> points = new ArrayList<>();
    private static float[] colors;

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
                if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
                    points.add(mouseEvent.getPoint());
                    Arrays.fill(colors, 0.15F);
                } else if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
                    points.clear();
                    Arrays.fill(colors, 0.15F);
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
        colors = new float[drawable.getSurfaceWidth() * drawable.getSurfaceHeight() * 4];
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glOrthof(0, width, height, 0, 0, 1);
    }

    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        Arrays.fill(RasterScan.colors, 0.15F);
        for (int i = 0; i < points.size(); i++) {
            Point a = points.get(i);
            Point b = points.get((i + 1) % points.size());
            if (a.y > b.y) {
                a = points.get((i + 1) % points.size());
                b = points.get(i);
            }
            for (int y = a.y; y < b.y; y++) {
                int startX = a.x + ((y - a.y) * (b.x - a.x)) / (b.y - a.y);
                for (int x = startX; x < drawable.getSurfaceWidth(); x++) {
                    int offset = (drawable.getSurfaceHeight() - y) * drawable.getSurfaceWidth() * 4 + x * 4;
                    float[] color = getColor(offset);
                    if (color[0] == 1.0F && color[1] == 1.0F && color[2] == 1.0F && color[3] == 1.0F) {
                        setColor(new float[]{0.15F, 0.15F, 0.15F, 0.15F}, offset);
                    } else {
                        setColor(new float[]{1.0F, 1.0F, 1.0F, 1.0F}, offset);
                    }
                }
            }
        }
        gl.glDrawPixels(drawable.getSurfaceWidth(), drawable.getSurfaceHeight(), GL2.GL_RGBA, GL2.GL_FLOAT, FloatBuffer.wrap(RasterScan.colors));
        if (!points.isEmpty()) {
            gl.glPointSize(10.0F);
            gl.glBegin(GL2.GL_POINTS);
            gl.glColor4f(1.0F, 0.0F, 0.0F, 1.0F);
            gl.glVertex2i(points.get(points.size() - 1).x, points.get(points.size() - 1).y);
            gl.glEnd();
        }
    }


    public void dispose(GLAutoDrawable glAutoDrawable) {
    }

    private float[] getColor(int offset) {
        float[] color = new float[4];
        System.arraycopy(RasterScan.colors, offset, color, 0, 4);
        return color;
    }

    public void setColor(float[] color, int offset) {
        System.arraycopy(color, 0, RasterScan.colors, offset, 4);
    }

}
