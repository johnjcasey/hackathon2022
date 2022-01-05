import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import model.Argument;
import model.Configurer;
import model.Schema;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sampletransform.CrossLanguageTransform;

public class SchemaExtractor {

  private static final Logger LOG = LoggerFactory.getLogger(SchemaExtractor.class);

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    Class<?> clazz = Class.forName("sampletransform.CrossLanguageTransform");
    List<Constructor<?>> constructors = Arrays.asList(clazz.getConstructors());
    List<Method> staticGenerators = new ArrayList<>();
    List<Method> configurers = new ArrayList<>();
    //todo filter for public methods
    Arrays.asList(clazz.getMethods())
        .forEach(method -> {
          if (method.getReturnType().equals(clazz)){
            if(Modifier.isStatic(method.getModifiers())){
              staticGenerators.add(method);
            } else {
              configurers.add(method);
            }
          }
        });

    Schema schema = generateSchema(clazz,constructors,staticGenerators,configurers);

    writeGeneratedPython(schema);
  }

  private static void writeGeneratedPython(Schema schema) throws IOException{
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.writeValue(new File("schema.yml"),schema);

    VelocityEngine engine = new VelocityEngine();
    engine.init();

    Template t = engine.getTemplate("src/main/resources/python.vm");

    VelocityContext context = new VelocityContext();
    context.put("schema", schema);

    StringWriter writer = new StringWriter();
    t.merge(context,writer);

    FileWriter fw = new FileWriter("generated.py");
    fw.write(writer.toString());
    fw.close();
  }

  private static Schema generateSchema(Class<?> clazz,List<Constructor<?>> constructors, List<Method> staticGenerators, List<Method> configurers){
    Schema schema = new Schema();
    schema.className = clazz.getSimpleName();
    schema.classPath = clazz.getName();
    Executable constructor = chooseConstructor(constructors, staticGenerators);
    schema.isConstructorStatic = constructor instanceof Method;
    schema.constructorName = constructor.getName();
    List<Parameter> constructorParameters = Arrays.asList(constructor.getParameters());
    schema.constructorArgs = new ArrayList<>();
    constructorParameters.forEach(parameter -> {
      Argument a = new Argument();
      a.argumentName = parameter.getName();
      a.type = parameter.getType().toString();
      schema.constructorArgs.add(a);
    });

    schema.configurers = new ArrayList<>();
    configurers.forEach(configurer -> {
      Configurer c = new Configurer();
      c.configurerName = configurer.getName();
      Argument a = new Argument();
      Parameter parameter = configurer.getParameters()[0];
      a.argumentName = parameter.getName();
      a.type = parameter.getType().toString();
      c.argument = a;
      schema.configurers.add(c);
    });

    return schema;
  }

  //todo figure out better method for choosing constructor
  private static Executable chooseConstructor(final List<Constructor<?>> constructors, final List<Method> staticGenerators){
    if(constructors.size() + staticGenerators.size() < 1){
      throw new RuntimeException("No constructor or static generator available, invalid PTransform");
    }
    if(constructors.size() + staticGenerators.size() > 1) {
      LOG.warn("Multiple eligible constructors found, choosing on best efforts basis");
    }

    if(!constructors.isEmpty()){
      return constructors.get(0);
    }
    return staticGenerators.get(0);
  }

}
