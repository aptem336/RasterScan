import com.jogamp.opengl.GL;
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

    private final float[] borderColor = new float[]{1.0F, 0.0F, 0.0F, 1.0F};
    private final float[] fillColor = new float[]{0.0F, 0.0F, 1.0F, 1.0F};
    private final float[] clearColor = new float[]{0.0F, 0.0F, 0.0F, 1.0F};
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
        GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        pixels = new float[drawable.getSurfaceWidth() * drawable.getSurfaceHeight() * 4];
    }

    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = clearColor[i % 4];
        }
        float avgX = 0;
        for (Point point : points) {
            avgX += point.x;
        }
        avgX /= points.size();
        for (int i = 0; i < points.size(); i++) {
            Point a = points.get(i);
            Point b = points.get((i + 1) % points.size());
            if (a.y > b.y) {
                a = points.get((i + 1) % points.size());
                b = points.get(i);
            }
            for (int y = a.y; y < b.y; y++) {
                int x = a.x + ((y - a.y) * (b.x - a.x)) / (b.y - a.y);
                if (x < avgX) {
                    for (int j = x; j < avgX; j++) {
                        invertColor((drawable.getSurfaceHeight() - y) * drawable.getSurfaceWidth() * 4 + j * 4);
                    }
                } else {
                    for (int j = x; j >= avgX; j--) {
                        invertColor((drawable.getSurfaceHeight() - y) * drawable.getSurfaceWidth() * 4 + j * 4);
                    }
                }
            }
        }
        for (int i = 0; i < points.size(); i++) {
            Point a = points.get(i);
            Point b = points.get((i + 1) % points.size());
            int x0 = a.x;
            int y0 = a.y;
            int x1 = b.x;
            int y1 = b.y;
            int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
            int dy = Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
            int err = dx - dy, e2, x2;
            int ed = dx + dy == 0 ? 1 : (int) Math.sqrt(dx * dx + dy * dy);
            while (true) {
                borderColor[3] = 1 - (float) Math.abs(err - dx + dy) / ed;
                fillPixel((drawable.getSurfaceHeight() - y0) * drawable.getSurfaceWidth() * 4 + x0 * 4, borderColor);
                e2 = err;
                x2 = x0;
                if (2 * e2 >= -dx) {
                    if (x0 == x1) break;
                    if (e2 + dy < ed) {
                        borderColor[3] = 1 - (float) (e2 + dy) / ed;
                        fillPixel((drawable.getSurfaceHeight() - (y0 + sy)) * drawable.getSurfaceWidth() * 4 + x0 * 4, borderColor);
                    }
                    err -= dy;
                    x0 += sx;
                }
                if (2 * e2 <= dy) {
                    if (y0 == y1) break;
                    if (dx - e2 < ed) {
                        borderColor[3] = 1 - (float) (dx - e2) / ed;
                        fillPixel((drawable.getSurfaceHeight() - y0) * drawable.getSurfaceWidth() * 4 + (x2 + sx) * 4, borderColor);
                    }
                    err += dx;
                    y0 += sy;
                }
            }
            gl.glDrawPixels(drawable.getSurfaceWidth(), drawable.getSurfaceHeight(), GL2.GL_RGBA, GL2.GL_FLOAT, FloatBuffer.wrap(pixels));
        }

    }


    public void dispose(GLAutoDrawable glAutoDrawable) {
    }

    private void invertColor(int offset) {
        if (pixels[offset] == fillColor[0]
                && pixels[offset + 1] == fillColor[1]
                && pixels[offset + 2] == fillColor[2]
                && pixels[offset + 3] == fillColor[3]) {
            fillPixel(offset, clearColor);
        } else {
            fillPixel(offset, fillColor);
        }
    }

    private void fillPixel(int offset, float[] color) {
        System.arraycopy(color, 0, pixels, offset, 4);
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
