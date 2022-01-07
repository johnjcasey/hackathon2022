package model;

import java.util.List;
import java.util.Map;

public class Schema {
  public String className;
  public String classPath;
  public String constructorName;
  public boolean isConstructorStatic;
  public List<Argument> constructorArgs;
  public List<Configurer> configurers;
  public Map<String,Argument> flattenedConfigurerArgs;
  public Map<Class<?>, List<SchemaField>> types;

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

  public Map<String,Argument> getFlattenedConfigurerArgs() {
    return flattenedConfigurerArgs;
  }

  public boolean isConstructorStatic() {
    return isConstructorStatic;
  }

  public Map<Class<?>, List<SchemaField>> getTypes() {
    return types;
  }
}


