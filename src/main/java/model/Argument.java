package model;

public class Argument{
  public Class<?> type;
  public String argumentName;
  public String renderAs;

  public Class<?> getType() {
    return type;
  }

  public String getArgumentName() {
    return argumentName;
  }

  public String getRenderAs() {
    return renderAs;
  }
}