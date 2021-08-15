import java.util.ArrayList;
import java.util.List;

public class Scene {
    final List<Drawable> drawables = new ArrayList<>();

    interface SIZE {
        int WIDTH = 1000;
        int HEIGHT = 1000;
    }

    interface COLOR {
        float R = 0.15F;
        float G = 0.15F;
        float B = 0.15F;
        float A = 0.15F;
    }
}
