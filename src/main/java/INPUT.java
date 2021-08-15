import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public interface INPUT {
    interface MOUSE {
        interface RELEASED {
            List<MouseEvent> EVENTS = new ArrayList<>();
        }
    }
}
