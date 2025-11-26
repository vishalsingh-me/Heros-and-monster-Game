import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface DataLoader<T> {
    List<T> load(Path path) throws IOException;
}
