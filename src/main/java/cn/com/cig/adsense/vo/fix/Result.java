package cn.com.cig.adsense.vo.fix;


@javax.annotation.Generated(value = "com.twitter.scrooge.Compiler", date = "2014-12-06T02:04:33.859+0800")
public enum Result {
  
  SUCCESS(1),
  
  NORESULT(0),
  
  ERROE(2);

  private final int value;

  private Result(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  public static Result findByValue(int value) {
    switch(value) {
      case 1: return SUCCESS;
      case 0: return NORESULT;
      case 2: return ERROE;
      default: return null;
    }
  }
}