# Java Notes

## Polymorphism via interfaces.

Don't want separate code paths to create `DataCrime`, and `Data311` instances,
when they are (up to now) virtually the same.

Even `DataCrimeRepository` and `Data311Repository` are the same, except for the
different table name.

Motivated to reduce code repetition by leveraging common interfaces.

### Repository

`IDataEntityRepository:findAllBySourceIdAndReportNumIn`:

* For `findAllBy...` method, inherit the declaration (`IDataEntityRepository`),
  while `CrudRepository` automatically provides implementation.
  * Assign both repositories to compatible `IDataEntityRepository` by interface
    polymorphism.
    * `Data311Repository extends IDataEntityRepository<Data311>,
      CrudRepository<Data311, Integer>`
    * `DataCrimeRepository extends IDataEntityRepository<DataCrime>,
      CrudRepository<DataCrime, Integer>`
* `List<IDataEntity> findAllBySourceIdAndReportNumIn`: query method defined in
  IDataEntityRepository (called in `DataService`.)
  * `Data311Repository`, `DataCrimeRepository` inherit the method declaration.
  * At runtime, Spring Data JPA will provide the implementation via
    `CrudRepository`.

`IDataEntityRepository:saveAllEntities`:

* This method is a wrapper around `saveAll` - which is a default method
  implemented by the classes.
* This is not a query method implemented by Spring Data JPA at runtime, so we
  have to decorate the actual implementation with our own default method
  `saveAllEntities`.

```
    default Iterable<IDataEntity> saveAllEntities(Iterable<IDataEntity> entities) {
        return ((CrudRepository<IDataEntity, Integer>) this).saveAll(entities);
    }

```
* Cast `this` - the object implementing the interface - to `CrudRepository` and
  use that `saveAll()`
* Allows `IDataEntityRepository` to have an class agnostic equivalent
  `saveAll()` method by leveraging `CrudRepository`'s` default implementation.

### Models

`IDataEntity`: polymorphic single instance of `DataCrime` and `Data311`

* In `DataService`, create a variable `private Class<? extends IDataEntity> dataEntityClass;`
  * `Class`: an object of type class - e.g. `Data311.class`.
  * `Class<? extends IDataEntity>`: Class inheritance. `?` is a generic.
    * We need this because we cannot cast class literals
      * `(IDataEntity.class) DataCrime.class` - does not work. We have to use compatible polymoprhism.
* In `DataService:setTypes()`: we assign the `dataEntityClass`.
* The `dataEntityClass` gets instantiated in `buildDataEntity`:
  * `IDataEntity dataEntity = dataEntityClass.getConstructor(Source.class).newInstance(source);`
  * This is reflection: based on dynamically obtained constructor of (dynamically assigned) class.

### Function type - passing functions

Function Parameter: `public void test(Function<String, ?> randomFn)`

* 1st parameter: input (`String`) - only single input allowed in `Function`
* 2nd parameter: return type (`?`) - `?` indicates wildcard any output.

How to pass the function?: `Class::method` - using the `::` method reference
operator.

How to call: `fn.apply(input)`, take the input type (here is `String`, but could be
anything) as defined in the parameter.


```
public void test(Function<String, ?> randomMethod) {
    String input = "abc";
    randomMethod.apply(input)
}

// call
// where MyClass::randomMethod is defined somewhere; randomMethod(String) returning wildcard.

test(MyClass::randomMethod)

```


### Functional Interfaces

Annotated, typically is the signature used to describe the lambda being passed.


```
@FunctionalInterface
public interface BaseParser {
  String parse(Object source, MappingField result);
}


public String test(String hello, Source source, BaseParser baseParser) {
    ...
    baseParser.parse(source, result)  // we call the lambda function passed in
    ...
}

```

When calling the above function `test()`, `BaseParser` expects a function, looks
like `(Object source, MappingField result) -> { ...}` : as defined in functional
interface

```
return test("hi", map, (source, result) -> {
    result.something();
    source.something();
    return result;
})
```

Use case: refactoring a common block but maybe need to use different data types;
we can define a different, concrete data type or operation in the lambdas, which
then get executed and returned in the uniform block (return value of
`baseParser.parse`, in this case).


### Template / Generics


##### `<T>`

* `<T>` is a type parameter used in generic classes, interfaces. Provides a
  "placeholder" type safety on any data type.
  * `<T>` sets / locks a consistent type throughout the class or method.
  * Typically found in interfaces, declarations - with a concrete type in its actual usage.
  * can use `<E>`, `<K>` - arbitrary.

* Generic Type `<T>` class:

```
public class Box<T> {
    private T content;

    public T getContent() {
        return content;
    }
}

// Usage

Box<String> stringBox = new Box<>();
Box<Integer> stringBox = new Box<>();

```

Generic Methods: `<T>` prefix return type

```
public <T> void printArray(T[] array) {
        for (T element : array) {
            System.out.println(element);
        }
    }
```

##### `?`

Wildcard `<?>` represents an unknown type - want some type but not entirely sure
(e.g. at runtime).

* can be in method parameters: `List<?> list` - just any old data type.
* can also bind a wildcard type: `? extends MyClass` - to reduce set of
  compatible types.


#### IDataEntity and IDataEntityRepository<T> Generic Usage

Try to "genericize" between crime and 311 types at runtime.

* `DataCrime` and `Data311` implements `IDataEntity` - this is the common
  "generic" interface

Repository relations:

* `Data311Repository extends IDataEntityRepository<Data311>`
* `DataCrimeRepository extends IDataEntityRepository<DataCrime>`

Common `IDataEntityRepository` interface:

* `interface IDataEntityRepository<T extends IDataEntity>`:
  * declarations indicate it takes a bound parameter type: `<T extends IDataEntity>`.
    * `Iterable<IDataEntity> saveAllEntities(...)`
    * `List<T> findAllBySourceIdAndReportNumIn(...)`: place holder T, where the
      concrete implementation of `<T>` in `Data311Repository` /
      `DataCrimeRepository` - the `T` represents specific type, `Data311`, and
      `DataCrime`.

Usage in `DataParser` relies on wildcard:

* Variable type declaration: `IDataEntityRepository<? extends IDataEntity> dataEntityRepository`
  * remember `T` is typically used for placeholder declarations; esp in interface.
* method declaration: `List<T> findAllBySourceIdAndReportNumIn(...)` -> where
  `T` is represented with a wildcard;
  * points to a compatible subclass of `IDataEntity` (either `Data311`, `DataCrime`):
* Usage in `DataParser.java`:
  * `List<? extends IDataEntity> existing =
    dataEntityRepository.findAllBySourceIdAndReportNumIn(...)`
  * where this is `List<Data311>` or `List<DataCrime>` - that matches the
      wildcard type definition of `<? extends IDataEntity>`.

