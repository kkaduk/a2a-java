# a2a-java Library

This library provides Spring Components under the `net.kaduk.a2a` package.

## Usage with Spring Boot (No `scanBasePackages` Required)

To use this library without manually specifying `scanBasePackages`:

1. **Add this library as a dependency** in your application's `pom.xml` or `build.gradle`.

2. **Project Structure**:  
   Make sure your application's `@SpringBootApplication` class is located in a package **above** or **at the same level as** `net.kaduk.a2a` in your directory hierarchy. The default component scanning of Spring will then automatically find all beans provided by this library.

   ### Example Project Structure

  ```
   com/
     example/
       yourapp/
         Application.java       // @SpringBootApplication here
   net/
     kaduk/
       a2a/
         // library classes
   ```
3. **No Extra Configuration Needed**:  
   With the setup above, you do **not** need to use
  ```java
   @ComponentScan(basePackages = { "net.kaduk.a2a" })
   ```
   in your code.

## Provided Components

All the beans in this library are annotated with Spring stereotypes and are automatically discovered by default component scanning.

## Troubleshooting

If you place your main application class in a sub-package that does **not** cover `net.kaduk.a2a`, Spring will not find these beans automatically. Either move your main class to a parent package or, as a last resort, use `@ComponentScan`.

## License

[Specify your license here]
