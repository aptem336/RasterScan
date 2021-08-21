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

    private final float[] borderColor = new float[]{1.0F, 0.0F, 0.0F, 1.0F};//цвет границы
    private final float[] fillColor = new float[]{0.0F, 0.0F, 1.0F, 1.0F};//цвет заливки
    private final float[] clearColor = new float[]{0.0F, 0.0F, 0.0F, 1.0F};//цвет фона
    private final List<Point> points = new ArrayList<>();//массив точек
    private float[] pixels;//массив цветовых компонент всех пикселей

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
        GL2 gl = drawable.getGL().getGL2();
        //включение смешивания и его функции (для прозрачности)
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        //инициализация массива цветовых компонент пикселей при изменении размеров окна в т.ч. при инициализации
        pixels = new float[drawable.getSurfaceWidth() * drawable.getSurfaceHeight() * 4];
    }

    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        //очистка цветового буффера
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        //очистка массва цветовых компонент пискселей цветом фона
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = clearColor[i % 4];
        }
        //вычисление среднего x для всех точек
        float avgX = 0;
        for (Point point : points) {
            avgX += point.x;
        }
        avgX /= points.size();
        //для всех точек
        for (int i = 0; i < points.size(); i++) {
            Point a = points.get(i);
            //замыкание массива точек - последняя + 1 = первая
            Point b = points.get((i + 1) % points.size());
            //обмен точек, чтобы всегда идти от нижней к верхней
            if (a.y > b.y) {
                a = points.get((i + 1) % points.size());
                b = points.get(i);
            }
            for (int y = a.y; y < b.y; y++) {
                //интерполяцмя положения x
                int x = a.x + ((y - a.y) * (b.x - a.x)) / (b.y - a.y);
                //собственно алгоритм по ребрам с перегородкой
                //Дополнить цвет пикселей, расположенных правее точки пересечения сканирующей строки с ребром многоугольника, но левее перегородки, если пересечение расположено левее перегородки.
                if (x < avgX) {
                    for (int j = x; j < avgX; j++) {
                        invertColor((drawable.getSurfaceHeight() - y) * drawable.getSurfaceWidth() * 4 + j * 4);
                    }
                    //Если точка пересечения сканирующей строки с ребром многоугольника расположена правее перегородки, то дополнить все пиксели, расположенные левее точки пересечения, но правее перегородки.
                } else {
                    for (int j = x; j >= avgX; j--) {
                        invertColor((drawable.getSurfaceHeight() - y) * drawable.getSurfaceWidth() * 4 + j * 4);
                    }
                }
            }
        }
        //Брезенхем в устранение стпуенчатости
        for (int i = 0; i < points.size(); i++) {
            Point a = points.get(i);
            Point b = points.get((i + 1) % points.size());
            int x0 = a.x;
            int y0 = a.y;
            int x1 = b.x;
            int y1 = b.y;
            //смещения по модулю, сохраняем знак
            int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
            int dy = Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
            int err = dx - dy, e2, x2;
            //если смешения по координатам нет, то длина = 1, иначе по теореме пифагора
            int ed = dx + dy == 0 ? 1 : (int) Math.sqrt(dx * dx + dy * dy);
            //останваливаться будет при достижении конечного значения одной из компонени
            while (true) {
                //меняем альфа-компонету пикселя
                borderColor[3] = 1 - (float) Math.abs(err - dx + dy) / ed;
                //красим (тут положение пискеля в одномерном массиве вычисляется на основе размеров экрана и кол-ва цветовых компонент
                fillPixel((drawable.getSurfaceHeight() - y0) * drawable.getSurfaceWidth() * 4 + x0 * 4, borderColor);
                //сохраняем начальные значения x и ошибки
                e2 = err;
                x2 = x0;
                if (2 * e2 >= -dx) {
                    //вылетаем
                    if (x0 == x1) break;
                    //начальная ошибка + смещение по y < длины
                    if (e2 + dy < ed) {
                        //меняем альфа-компонету пикселя
                        borderColor[3] = 1 - (float) (e2 + dy) / ed;
                        //красим
                        fillPixel((drawable.getSurfaceHeight() - (y0 + sy)) * drawable.getSurfaceWidth() * 4 + x0 * 4, borderColor);
                    }
                    //уменьшаем ошибку
                    err -= dy;
                    //сдвигаемся по x
                    x0 += sx;
                }
                if (2 * e2 <= dy) {
                    //вылетаем
                    if (y0 == y1) break;
                    //смещение по x - начальная ошибка < длины
                    if (dx - e2 < ed) {
                        //меняем альфа-компонету пикселя
                        borderColor[3] = 1 - (float) (dx - e2) / ed;
                        //красим
                        fillPixel((drawable.getSurfaceHeight() - y0) * drawable.getSurfaceWidth() * 4 + (x2 + sx) * 4, borderColor);
                    }
                    //увеличиваем ошибку
                    err += dx;
                    //сдвигаемся по y
                    y0 += sy;
                }
            }
            gl.glDrawPixels(drawable.getSurfaceWidth(), drawable.getSurfaceHeight(), GL2.GL_RGBA, GL2.GL_FLOAT, FloatBuffer.wrap(pixels));
        }
    }


    public void dispose(GLAutoDrawable glAutoDrawable) {
    }

    private void invertColor(int offset) {
        //справниваем все цветовые компоненты пикселя
        if (pixels[offset] == fillColor[0]
                && pixels[offset + 1] == fillColor[1]
                && pixels[offset + 2] == fillColor[2]
                && pixels[offset + 3] == fillColor[3]) {
            //заполняем цветом фона
            fillPixel(offset, clearColor);
        } else {
            //заполняем цветом заливки
            fillPixel(offset, fillColor);
        }
    }

    private void fillPixel(int offset, float[] color) {
        //копируем массив цветовых компонент определённого пикселя
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
