import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.cloud.Tuple;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Argument;
import model.Configurer;
import model.Schema;
import model.SchemaField;
import org.apache.beam.repackaged.core.org.apache.commons.lang3.ClassUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaExtractor {

  private static final Logger LOG = LoggerFactory.getLogger(SchemaExtractor.class);

  public static void main(String[] args) throws IOException, ClassNotFoundException{
    Class<?> clazz = Class.forName(args[0]);
    List<Constructor<?>> constructors = Arrays.asList(clazz.getConstructors());
    List<Method> staticGenerators = new ArrayList<>();
    List<Method> configurers = new ArrayList<>();
    Arrays.asList(clazz.getMethods())
        .forEach(method -> {
          if (method.getReturnType().equals(clazz) && Modifier.isPublic(method.getModifiers())){
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

  //TODO form a DAG for generated type to ensure they get printed in the right order
  private static void writeGeneratedPython(final Schema schema) throws IOException{
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.writeValue(new File("schema.yml"),schema);

    preprocessSchemaForPython(schema);

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

  private static void preprocessSchemaForPython(final Schema schema){
    //Convert variables to pythonic underscore
    Converter<String,String> camelCaseConverter = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE);
    schema.constructorArgs.forEach(argument -> argument.argumentName = camelCaseConverter.convert(argument.argumentName));
    schema.flattenedConfigurerArgs = new HashMap<String,Argument>();
    schema.configurers.forEach(configurer ->
        configurer.arguments.forEach(argument -> {
          argument.argumentName = camelCaseConverter.convert(argument.argumentName);
          if (!schema.flattenedConfigurerArgs.containsKey(argument.argumentName)){
            schema.flattenedConfigurerArgs.put(argument.argumentName,argument);
          } else if(schema.flattenedConfigurerArgs.get(argument.getArgumentName()).type != argument.type) {
            throw new RuntimeException("Two or more configureres have the same parameter, but with difffernt types. This is not supported");
          }
        })
    );

    schema.flattenedConfigurerArgs = new HashMap<String,Argument>();


    //Generate python renderings
    schema.types.forEach((key, value) -> value.forEach(sf -> {
      if (sf.type == String.class) {
        sf.renderAs = "str";
      } else if (sf.type == byte.class || sf.type == Byte.class){
        sf.renderAs = "numpy.int8";
      } else if (sf.type == int.class || sf.type == Integer.class) {
        sf.renderAs = "numpy.int32";
      } else if (sf.type == long.class || sf.type == Long.class){
        sf.renderAs = "numpy.int64";
      } else if (sf.type == float.class || sf.type == Float.class) {
        sf.renderAs = "numpy.float32";
      } else if (sf.type == double.class || sf.type == Double.class) {
        sf.renderAs = "numpy.float64";
      } else if (sf.type == boolean.class || sf.type == Boolean.class) {
        sf.renderAs = "bool";
      } else {
        sf.renderAs = sf.type.getSimpleName();
      }
    }));

    schema.constructorArgs.forEach(SchemaExtractor::renderArgument);
    schema.configurers.forEach(configurer -> configurer.arguments.forEach(SchemaExtractor::renderArgument));
  }

  private static void renderArgument(final Argument argument) {
      if (argument.type == byte.class || argument.type == Byte.class){
        argument.renderAs = "numpy.int8(self." + argument.argumentName + ")";
      } else if (argument.type == int.class || argument.type == Integer.class) {
        argument.renderAs = "numpy.int32(self." + argument.argumentName + ")";
      } else if (argument.type == long.class || argument.type == Long.class){
        argument.renderAs = "numpy.int64(self." + argument.argumentName + ")";
      } else if (argument.type == float.class || argument.type == Float.class) {
        argument.renderAs = "numpy.float32(self." + argument.argumentName + ")";
      } else if (argument.type == double.class || argument.type == Double.class) {
        argument.renderAs = "numpy.float64(self." + argument.argumentName + ")";
      } else {
        argument.renderAs = "self."+argument.argumentName;
      }
  }

  private static Schema generateSchema(final Class<?> clazz,final List<Constructor<?>> constructors, final List<Method> staticGenerators, final List<Method> configurers){
    Schema schema = new Schema();
    schema.types = new HashMap<>();
    schema.className = clazz.getSimpleName();
    schema.classPath = clazz.getName();
    Executable constructor = chooseConstructor(constructors, staticGenerators);
    schema.isConstructorStatic = constructor instanceof Method;
    schema.constructorName = constructor.getName();
    List<Parameter> constructorParameters = Arrays.asList(constructor.getParameters());
    generateTypes(schema.types,constructorParameters);
    schema.constructorArgs = new ArrayList<>();
    constructorParameters.forEach(parameter -> {
      Argument a = new Argument();
      a.argumentName = parameter.getName();
      a.type = parameter.getType();
      schema.constructorArgs.add(a);
    });

    schema.configurers = new ArrayList<>();
    configurers.forEach(configurer -> {
      Configurer c = new Configurer();
      c.configurerName = configurer.getName();
      c.arguments = new ArrayList<>();
      for (Parameter parameter: configurer.getParameters()){
        Argument a = new Argument();
        a.argumentName = parameter.getName();
        a.type = parameter.getType();
        c.arguments.add(a);
        generateType(schema.types, Tuple.of(parameter.getName(), parameter.getType()));
      }
      schema.configurers.add(c);
    });

    return schema;
  }

  private static void generateTypes(final Map<Class<?>, List<SchemaField>> types, final List<Parameter> parameters){
    for (Parameter p: parameters) {
      generateType(types, Tuple.of(p.getName(),p.getType()));
    }
  }

  private static void generateType(final Map<Class<?>, List<SchemaField>> types,
      final Tuple<String, Class<?>> namedVariable){
    Class<?> c = namedVariable.y();
    if (Collection.class.isAssignableFrom(c)){
      // throw new RuntimeException("Collections are not supported");
    }
    if (ClassUtils.isPrimitiveOrWrapper(c) || c == String.class || c == Object.class || types.containsKey(c)){
      return;
    } else {
      List<SchemaField> fields = new ArrayList<>();
      for (Field f: c.getDeclaredFields()) {
        SchemaField sf = new SchemaField();
        sf.name = f.getName();
        sf.type = f.getType();
        fields.add(sf);
      }
      types.put(c,fields);
      for (SchemaField field: fields){
        generateType(types, Tuple.of(field.name,field.type));
      }
    }
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
