package inheritence;

public class IBase<T extends Number> extends IAbstractBase implements Interface1<T>, Interface2<T> {

  public IBase(Integer value1, Integer value2) {
    super(value1, value2);
  }

  /**
   * What happened to exceptions? We have two methods with the same signature but different
   * exceptions. The compiler will not allow this because it cannot determine which method to call.
   * In this case, we can either choose to throw a common exception or handle the exceptions in the
   * method body.
   *
   * @return
   */
  @Override
  public T method() {
    Integer sum = 1 + 1;
    /**
     * You need to case because of type erasure.
     * The compiler will not know what type T is at runtime, so it will treat it as an Object.
     * Therefore, you need to cast the result to T before returning it.
     */
    return (T) sum;
  }
}
