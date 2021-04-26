import java.io.IOException;
import java.util.List;

public interface File {
    List<String> readFile(String filePath) throws IOException;

    void write(String content, String filePath) throws IOException;

}
