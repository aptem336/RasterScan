import com.jogamp.nativewindow.NativeSurface;
import com.jogamp.opengl.*;
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
import java.util.Stack;

public class RasterScan implements GLEventListener, MouseListener {

    private final float[] borderColor = new float[]{1.0F, 0.0F, 0.0F, 1.0F};//цвет границы
    private final float[] fillColor = new float[]{0.0F, 0.0F, 0.0F, 1.0F};//цвет фона
    private final float[] clearColor = new float[]{0.0F, 0.0F, 1.0F, 1.0F};//цвет заливки
    private final List<Point> points = new ArrayList<>();//массив точек
    private float[] pixels;//массив цветовых компонент всех пикселей
    private GLDrawable drawable;

    public static void main(String[] args) {
        //инициализация фрейма
        JFrame frame = new JFrame();
        frame.setSize(1000, 1000);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        //инициализация канвы
        GLCanvas canvas = new GLCanvas();
        canvas.setSize(frame.getSize());
        canvas.setLocation(0, 0);
        //доабвление слушателей GL и мыщи - интанса самого класса
        RasterScan rasterScan = new RasterScan();
        canvas.addGLEventListener(rasterScan);
        canvas.addMouseListener(rasterScan);
        //добавление канвы на фрейм
        frame.add(canvas);
        final Animator animator = new Animator(canvas);
        //добавление аниматора
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                new Thread(() -> {
                    animator.stop();
                    System.exit(0);
                }).start();
            }
        });
        //запуск аниматора
        animator.start();
    }

    public void init(GLAutoDrawable drawable) {
        this.drawable = drawable;
        GL2 gl = drawable.getGL().getGL2();
        //включение смешивания и его функции (для прозрачности)
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        this.drawable = drawable;
        //инициализация массива цветовых компонент пикселей при изменении размеров окна в т.ч. при инициализации
        pixels = new float[(drawable.getSurfaceWidth() + 1) * (drawable.getSurfaceHeight() + 1) * 4];
    }

    public void display(GLAutoDrawable drawable) {
        this.drawable = drawable;
        GL2 gl = drawable.getGL().getGL2();
        //очистка цветового буффера
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        //очистка массва цветовых компонент пискселей цветом фона
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = clearColor[i % 4];
        }
        for (int i = 0; i < points.size(); i++) {
            bresenham(points.get(i).x, points.get(i).y,
                    points.get((i + 1) % points.size()).x, points.get((i + 1) % points.size()).y);
        }
        if (points.size() > 2) {
            fill(points.get(0));
        }
        for (int x = 1; x < drawable.getSurfaceWidth() - 1; x++) {
            for (int y = 1; y < drawable.getSurfaceHeight() - 1; y++) {
                float[] color = sumColor(new float[]{pixels[toOffset(x - 1, y + 1)], pixels[toOffset(x - 1, y + 1) + 1], pixels[toOffset(x - 1, y + 1) + 2], pixels[toOffset(x - 1, y + 1) + 3]},
                        sumColor(new float[]{pixels[toOffset(x, y + 1)], pixels[toOffset(x, y + 1) + 1], pixels[toOffset(x, y + 1) + 2], pixels[toOffset(x, y + 1) + 3]},
                                sumColor(new float[]{pixels[toOffset(x + 1, y + 1)], pixels[toOffset(x + 1, y + 1) + 1], pixels[toOffset(x + 1, y + 1) + 2], pixels[toOffset(x + 1, y + 1) + 3]},
                                        sumColor(new float[]{pixels[toOffset(x, y)], pixels[toOffset(x, y) + 1], pixels[toOffset(x, y) + 2], pixels[toOffset(x, y) + 3]},
                                                sumColor(new float[]{pixels[toOffset(x - 1, y)], pixels[toOffset(x - 1, y) + 1], pixels[toOffset(x - 1, y) + 2], pixels[toOffset(x - 1, y) + 3]},
                                                        sumColor(new float[]{pixels[toOffset(x - 1, y - 1)], pixels[toOffset(x - 1, y - 1) + 1], pixels[toOffset(x - 1, y - 1) + 2], pixels[toOffset(x - 1, y - 1) + 3]},
                                                                sumColor(new float[]{pixels[toOffset(x, y - 1)], pixels[toOffset(x, y - 1) + 1], pixels[toOffset(x, y - 1) + 2], pixels[toOffset(x, y - 1) + 3]},
                                                                        sumColor(new float[]{pixels[toOffset(x + 1, y - 1)], pixels[toOffset(x + 1, y - 1) + 1], pixels[toOffset(x + 1, y - 1) + 2], pixels[toOffset(x + 1, y - 1) + 3]},
                                                                                new float[]{pixels[toOffset(x + 1, y)], pixels[toOffset(x + 1, y) + 1], pixels[toOffset(x + 1, y) + 2], pixels[toOffset(x + 1, y) + 3]}))))))));
                color[0] /= 9;
                color[1] /= 9;
                color[2] /= 9;
                color[3] /= 9;
                fillPixel(x, y, color);
            }
        }
        gl.glDrawPixels(drawable.getSurfaceWidth(), drawable.getSurfaceHeight(), GL2.GL_RGBA, GL2.GL_FLOAT, FloatBuffer.wrap(pixels));
    }

    private float[] sumColor(float[] a, float[] b) {
        return new float[]{a[0] + b[0], a[1] + b[1], a[2] + b[2], a[3] + b[3]};
    }

    private void fill(Point start) {
        Stack<Point> stack = new Stack<>();
        stack.push(start);
        while (!stack.empty()) {
            Point pixel = stack.pop();
            fillPixel(pixel.x, pixel.y, fillColor);
            int tmp_x = pixel.x;
            int x = pixel.x + 1, y = pixel.y;
            while (x < drawable.getSurfaceWidth() && !is_countour(x, y)) {
                fillPixel(x, y, fillColor);
                x++;
            }

            int right_x = x - 1;
            x = tmp_x;
            x--;
            while (x >= 0 && !is_countour(x, y)) {
                fillPixel(x, y, fillColor);
                x--;
            }
            int left_x = ++x;

            y++;
            if (y < drawable.getSurfaceHeight()) {
                while (x <= right_x) {
                    boolean flag = false;
                    while (!is_countour(x, y) && !is_inner(x, y) && x < right_x) {
                        if (!flag) {
                            flag = true;
                        }
                        x++;
                    }

                    if (flag) {
                        if (x == right_x && !is_countour(x, y) && !is_inner(x, y)) {
                            stack.push(new Point(x, y));
                        } else {
                            stack.push(new Point(x - 1, y));
                        }
                        flag = false;
                    }

                    float in_x = x;
                    while ((is_countour(x, y) || is_inner(x, y)) && x < right_x) {
                        x++;
                    }

                    if (in_x == x)
                        x++;
                }
            }
            x = left_x;
            y -= 2;
            if (y >= 0) {
                while (x <= right_x) {
                    boolean flag = false;
                    while (!is_countour(x, y) && !is_inner(x, y) && x < right_x) {
                        if (!flag) {
                            flag = true;
                        }
                        x++;
                    }

                    if (flag) {
                        if (x == right_x && !is_countour(x, y) && !is_inner(x, y)) {
                            stack.push(new Point(x, y));
                        } else {
                            stack.push(new Point(x - 1, y));
                        }
                        flag = false;
                    }

                    int in_x = x;
                    while ((is_countour(x, y) || is_inner(x, y)) && x < right_x) {
                        x++;
                    }

                    if (in_x == x)
                        x++;
                }
            }
        }
    }

    private boolean is_inner(int x, int y) {
        return pixels[toOffset(x, y)] == fillColor[0]
                && pixels[toOffset(x, y) + 1] == fillColor[1]
                && pixels[toOffset(x, y) + 2] == fillColor[2]
                && pixels[toOffset(x, y) + 3] == fillColor[3];
    }

    private boolean is_countour(int x, int y) {
        return pixels[toOffset(x, y)] == borderColor[0]
                && pixels[toOffset(x, y) + 1] == borderColor[1]
                && pixels[toOffset(x, y) + 2] == borderColor[2]
                && pixels[toOffset(x, y) + 3] == borderColor[3];
    }

    private void bresenham(int x1, int y1,
                           int x2, int y2) {
        int x = x1, y = y1,
                dx = Math.abs(x2 - x1),
                dy = Math.abs(y2 - y1),
                s1 = x1 < x2 ? 1 : -1,
                s2 = y1 < y2 ? 1 : -1;

        boolean changed = true;
        if (dy > dx) {
            int temp = dx;
            dx = dy;
            dy = temp;
        } else {
            changed = false;
        }

        int e = 2 * dy - dx;
        for (int i = 1; i <= dx; i++) {
            fillPixel(x, y, borderColor);
            while (e >= 0) {
                if (changed) {
                    x += s1;
                } else {
                    y += s2;
                }
                e -= 2 * dx;
            }
            if (changed) {
                y += s2;
            } else {
                x += s1;
            }
            e += 2 * dy;
        }
    }

    private void fillPixel(int x, int y, float[] color) {
        //копируем массив цветовых компонент определённого пикселя
        System.arraycopy(color, 0, pixels, toOffset(x, y), 4);
    }

    private int toOffset(int x, int y) {
        return ((drawable.getSurfaceHeight() - y) * drawable.getSurfaceWidth() + x) * 4;
    }


    public void dispose(GLAutoDrawable glAutoDrawable) {
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        //ЛКМ - добавляем точку
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            points.add(mouseEvent.getPoint());
            //ПКМ - очищаем массив
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
