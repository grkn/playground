package inheritence;

import java.io.IOException;

public abstract class IAbstractBase {

  Integer value1;
  Integer value2;

  public IAbstractBase(Integer value1, Integer value2) {
    this.value1 = value1;
    this.value2 = value2;
  }

  abstract Number method() throws NullPointerException, IOException;
}
