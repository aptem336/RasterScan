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
    private static float[] pixels;

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(1000, 100);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        GLCanvas canvas = new GLCanvas();
        canvas.addGLEventListener(new RasterScan());
        canvas.setSize(frame.getSize());
        canvas.setLocation(0, 0);

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
                } else if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
                    points.clear();
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
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        pixels = new float[drawable.getSurfaceWidth() * drawable.getSurfaceHeight() * 3];
    }

    public void display(GLAutoDrawable drawable) {
        //Q framebuffer?
        Arrays.fill(pixels, 0.15F);
        for (int i = 0; i < points.size(); i++) {
            Point a = points.get(i);
            Point b = points.get((i + 1) % points.size());
            //Q
            if (a.y > b.y) {
                a = points.get((i + 1) % points.size());
                b = points.get(i);
            }
            for (int y = a.y; y < b.y; y++) {
                int startX = a.x + ((y - a.y) * (b.x - a.x)) / (b.y - a.y);
                for (int x = startX; x < drawable.getSurfaceWidth(); x++) {
                    int offset = (drawable.getSurfaceHeight() - y) * drawable.getSurfaceWidth() * 3 + x * 3;
                    float[] color = getPixel(offset);
                    if (color[0] == 1.0F && color[1] == 1.0F && color[2] == 1.0F) {
                        setPixel(new float[]{0.15F, 0.15F, 0.15F}, offset);
                    } else {
                        setPixel(new float[]{1.0F, 1.0F, 1.0F}, offset);
                    }
                }
            }
        }
        GL2 gl = drawable.getGL().getGL2();
        gl.glDrawPixels(drawable.getSurfaceWidth(), drawable.getSurfaceHeight(), GL2.GL_RGB, GL2.GL_FLOAT, FloatBuffer.wrap(pixels));
    }


    public void dispose(GLAutoDrawable glAutoDrawable) {
    }

    private float[] getPixel(int offset) {
        float[] color = new float[3];
        System.arraycopy(pixels, offset, color, 0, 3);
        return color;
    }

    public void setPixel(float[] color, int offset) {
        System.arraycopy(color, 0, pixels, offset, 3);
    }

}
