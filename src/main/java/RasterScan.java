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
import java.util.List;

public class RasterScan implements GLEventListener, MouseListener {

    private final float[] borderColor = new float[]{1.0F, 0.0F, 0.0F};
    private final float[] fillColor = new float[]{1.0F, 1.0F, 1.0F};
    private final float[] clearColor = new float[]{0.15F, 0.15F, 0.15F};
    private final List<Point> points = new ArrayList<>();
    private float[] pixels;

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(1000, 1000);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        GLCanvas canvas = new GLCanvas();
        canvas.setSize(frame.getSize());
        canvas.setLocation(0, 0);

        RasterScan rasterScan = new RasterScan();
        canvas.addGLEventListener(rasterScan);
        canvas.addMouseListener(rasterScan);

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
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = clearColor[i % 3];
        }
        for (int i = 0; i < points.size(); i++) {
            Point a = points.get(i);
            Point b = points.get((i + 1) % points.size());
            if (a.y > b.y) {
                a = points.get((i + 1) % points.size());
                b = points.get(i);
            }
            for (int y = a.y; y < b.y; y++) {
                int x = a.x + ((y - a.y) * (b.x - a.x)) / (b.y - a.y);
                for (int j = x; j < drawable.getSurfaceWidth(); j++) {
                    invertColor((drawable.getSurfaceHeight() - y) * drawable.getSurfaceWidth() * 3 + j * 3);
                }
            }
            int dx = b.x - a.x;
            int dy = b.y - a.y;
            int signX = Integer.compare(dx, 0);
            int signY = Integer.compare(dy, 0);
            if (dx < 0) dx = -dx;
            if (dy < 0) dy = -dy;
            int pdx;
            int pdy;
            int es;
            int el;
            if (dx > dy) {
                pdx = signX;
                pdy = 0;
                es = dy;
                el = dx;
            } else {
                pdx = 0;
                pdy = signY;
                es = dx;
                el = dy;
            }
            int x = a.x;
            int y = a.y;
            int err = el / 2;
            fillPixel((drawable.getSurfaceHeight() - y) * drawable.getSurfaceWidth() * 3 + x * 3, borderColor);
            for (int t = 0; t < el; t++) {
                err -= es;
                if (err < 0) {
                    err += el;
                    x += signX;
                    y += signY;
                } else {
                    x += pdx;
                    y += pdy;
                }
                fillPixel((drawable.getSurfaceHeight() - y) * drawable.getSurfaceWidth() * 3 + x * 3, borderColor);
            }
        }
        GL2 gl = drawable.getGL().getGL2();
        gl.glDrawPixels(drawable.getSurfaceWidth(), drawable.getSurfaceHeight(), GL2.GL_RGB, GL2.GL_FLOAT, FloatBuffer.wrap(pixels));
    }


    public void dispose(GLAutoDrawable glAutoDrawable) {
    }

    private void invertColor(int offset) {
        if (pixels[offset] == fillColor[1]
                && pixels[offset + 1] == fillColor[1]
                && pixels[offset + 2] == fillColor[2]) {
            fillPixel(offset, clearColor);
        } else {
            fillPixel(offset, fillColor);
        }
    }

    private void fillPixel(int offset, float[] color) {
        System.arraycopy(color, 0, pixels, offset, 3);
    }

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
}
