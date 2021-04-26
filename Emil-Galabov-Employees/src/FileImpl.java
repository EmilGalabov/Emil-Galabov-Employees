import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileImpl implements File {
    @Override
    public List<String> readFile(String filePath) throws IOException {
        List<String> lines = null;
        try (Stream<String> stream = Files.lines(Paths.get("resources/employees"))) {
            lines = stream
                    .skip(1)
                    .filter(line -> !line.isEmpty())
                    .map(line -> line.replace(", ", " "))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return lines;
    }

    @Override
    public void write(String content, String filePath) throws IOException {
        Files.write(Paths.get(filePath), Collections.singleton(content), StandardCharsets.UTF_8);
    }
}
