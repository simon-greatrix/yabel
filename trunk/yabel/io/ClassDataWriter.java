package yabel.io;

import java.io.IOException;
import java.util.List;

public interface ClassDataWriter {

    void startClassData(String name) throws IOException;

    void endClassData() throws IOException;

    <T> void writeList(String k, Class<T> clss, List<T> list) throws IOException;

    void write(String k, Object v) throws IOException;

    void start() throws IOException;

    void finish() throws IOException;

}
