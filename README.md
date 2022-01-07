###To run the xlang script
1. Create a shadow jar with `gradle shadowJar`
2. Run the shadow jar with `java -jar build/libs/autoxlang-0.1-all.jar 9090 --javaClassLookupAllowlistFile=src/main/resources/config.yml`
3. Download and run Docker
4. Run `python example.py --runner DirectRunner`

You will probably need to go through a lot of the core beam setup for your example.py to run

###To run the auto transformer, just run SchemaExtractor

## Big Todos
1. Support for Generic Types
2. Support for Collection Types
3. Testing generation on more complex (i.e. existing) PTransforms