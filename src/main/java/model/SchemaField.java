package model;

import java.lang.reflect.Type;

public class SchemaField {
  public String name;
  public Class<?> type;
  public String renderAs;

  public String getName() {
    return name;
  }

  public Class<?> getType() {
    return type;
  }

  public String getRenderAs() {
    return renderAs;
  }
}
