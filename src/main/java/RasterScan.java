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
    private final float[] fillColor = new float[]{1.0F, 0.0F, 0.0F, 1.0F};
    private final float[] clearColor = new float[]{0.15F, 0.15F, 0.15F, 1.0F};
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
                    invertColor((drawable.getSurfaceHeight() - y) * drawable.getSurfaceWidth() * 4 + j * 4);
                }
            }
        }
        for (int i = 0; i < points.size(); i++) {
            Point a = points.get(i);
            Point b = points.get((i + 1) % points.size());
            int I = 16;
            int x = b.x;
            int y = b.y;
            int dx = a.x - b.x;
            int dy = a.y - b.y;
            int sx = Integer.compare(dx, 0);
            int sy = Integer.compare(dy, 0);
            dx = Math.abs(dx);
            dy = Math.abs(dy);
            float m = (float) dy / dx;
            boolean flag = false;
            if (m > 1) {
                int temp = dx;
                dx = dy;
                dy = temp;
                m = 1.0F / m;
                flag = true;
            }
            m *= I;
            float f = I / 2.0F;
            float w = I - m;
            borderColor[3] = f / I;
            fillPixel((drawable.getSurfaceHeight() - y) * drawable.getSurfaceWidth() * 4 + x * 4, borderColor);
            for (int j = 1; j < dx; j++) {
                if (f < w) {
                    if (flag) {
                        y = y + sy;
                    } else {
                        x = x + sx;
                    }
                    f = f + m;
                } else {
                    x = x + sx;
                    y = y + sy;
                    f = f - w;
                }
                borderColor[3] = f / I;
                fillPixel((drawable.getSurfaceHeight() - y) * drawable.getSurfaceWidth() * 4 + x * 4, borderColor);
            }
        }
        gl.glDrawPixels(drawable.getSurfaceWidth(), drawable.getSurfaceHeight(), GL2.GL_RGBA, GL2.GL_FLOAT, FloatBuffer.wrap(pixels));
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
