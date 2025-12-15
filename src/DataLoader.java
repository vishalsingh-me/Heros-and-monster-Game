import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Generic contract for loading domain objects from a file.
 * Implemented by the various loaders (WeaponLoader, Monster loaders, etc.).
 */
public interface DataLoader<T> {
    /**
     * Load objects of type T from the given path.
     *
     * @param path file to read
     * @return list of parsed objects
     * @throws IOException if reading fails
     */
    List<T> load(Path path) throws IOException;
}
