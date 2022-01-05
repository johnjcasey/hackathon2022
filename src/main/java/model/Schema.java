package model;

import java.util.List;

public class Schema {
  public String className;
  public String classPath;
  public String constructorName;
  public boolean isConstructorStatic;
  public List<Argument> constructorArgs;
  public List<Configurer> configurers;

  public String getClassName() {
    return className;
  }

  public String getClassPath() {
    return classPath;
  }

  public String getConstructorName() {
    return constructorName;
  }

  public List<Argument> getConstructorArgs() {
    return constructorArgs;
  }

  public List<Configurer> getConfigurers() {
    return configurers;
  }
}


