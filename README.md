###To run the xlang script
1. Create a shadow jar with `gradle shadowJar`
2. Run the shadow jar with `java -jar build/libs/autoxlang-0.1-all.jar 9090 --javaClassLookupAllowlistFile=src/main/resources/config.yml`
3. Download and run Docker
4. Run `python example.py --runner DirectRunner`

You will probably need to go through a lot of the core beam setup for your example.py to run

###To run the auto transformer, just run SchemaExtractor

## Big Todos
1. Support for complex types
   1. See https://beam.apache.org/documentation/programming-guide/#create-x-lang-transforms for how to feed a typed struct into JavaExternalTransform
2. Better formatting for generated python file
3. Testing  generated python file
4. Testing generation on more complex (i.e. existing) PTransforms