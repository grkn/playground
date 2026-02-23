package inheritence;

import java.io.IOException;

public interface Interface2<T extends Number> {

  T method() throws IOException;
}
