---
description: The Most Essential Classes You'll Need To Know About
---

# Essential Support Classes



## io.amelia.data.parcel.Parcel

For those who've developed for Android, the Parcel class provides much the same functionality. Think of it like a HashMap except super-sized so that information can be easily serialized and carried not just within the application but also across processes and networks. While parcels can carry nearly any type of value, they are built with the purpose of containing built-in Java types and for serializing more complex classes.

Creating a new empty instance is super easy and error free.

```java
Parcel.empty();
```

To serialize and deserialize an object to and from a Parcel is easily done using the `io.amelia.data.parcel.Parcel.Factory` inner-class.

Serialize:

```java
Parcel destParcel = Parcel.Factory.serialize( myObjectInstance );
```

```java
Parcel destParcel = Parcel.empty();
Parcel.Factory.serialize( myObjectInstance, destParcel );
```

Deserialize:

```java
MyObject myObjectInstance = Parcel.Factory.deserialize( srcParcel, MyObject.class );
```

```java
MyObject myObjectInstance = Parcel.Factory.deserialize( srcParcel );
```

{% hint style="info" %}
Both examples above theoretically do the exact same thing. The first will forcefully use the specified deserializer, while the ladder relies on the `$class` string value having been defined. Both will throw a `ParcelableException.Error` if either the specified or `$class` class can't be deserialized.
{% endhint %}

Classes that are not already implemented must implement our `io.amelia.data.parcel.ParcelSerializer` in ensure error-free transmission. How a Parcel is queued to be transmitted will be covered elsewhere.

```java
@Parcelable( ExampleClass.Serializer.class )
public class ExampleClass
{
  private int someSerializableField;

  public static class Serializer implements ParcelSerializer<ExampleClass>;
  {
    @Override
    public ExampleClass readFromParcel( Parcel src ) throws ParcelableException.Error {
      ExampleClass obj = new ExampleClass();
      obj.someSerializableField = src.getValue( "someSerializableField" );
      return obj;
    }

    @Override
    public void writeToParcel( ExampleClass obj, Parcel dest ) throws ParcelableException.Error {
      dest.setValue( "someSerializableField", obj.someSerializableField );
    }
  }
}
```

{% hint style="info" %}
When deserializing a class, the `Parcel.Factory` will additionally check if the specified class implements a constructor that takes `Parcel` as an argument. This is a great way to write simple classes without the need to maintain complex serializers, just keep in mind that you'll need to serialize using alternative methods and this process can't be automated.
{% endhint %}

If you plan on making your serializer external from the class being serialized, \(e.g., The class was shipped with a library outside your control.\) be sure to register your serializer using:

```java
Parcel.Factory.registerClassSerialized( ExternalClass.class, new ParcelSerializer<ExternalClass> {
    @Override
    public ExternalClass readFromParcel( Parcel src ) throws ParcelableException.Error {
      ExampleClass obj = new ExampleClass();
      obj.someSerializableField = src.getValue( "someSerializableField" );
      return obj;
    }

    @Override
    public void writeToParcel( ExternalClass obj, Parcel dest ) throws ParcelableException.Error {
      dest.setValue( "someSerializableField", obj.someSerializableField );
    }
  } );
```

You can also check if an object can be serialized and prevent the possibility of throwing an exception using:

```java
Parcel.Factory.isSerializable( myObjectInstance );
```

{% hint style="info" %}
See `io.amelia.support.KeyValueTypeTrait` for information on how to retrieve values from `Parcel`.
{% endhint %}

## io.amelia.foundation.ConfigData

Similar to Parcel, except lacks a mechanism for serializing its contents. Additionally only a limited set of types can be specified. The intend of this class is to read and write configuration from configurations data sources - as if that didn't need to be said.

Creating a new empty instance is identical to Parcel and also error free:

```java
ConfigData.empty();
```

{% hint style="warning" %}
Keep in mind that ConfigData prevents values from being set on the root and top-level keys. And a minimum depth of two nodes is enforced to help prevent value collisions.

#### Contributors

The reverse domain `io.amelia` is preserved for storing internal _Honey Pot Server_ config values and is read-only.

#### Developers

For plugins and custom config values, you would ideally use a unique reverse domain, such as, `com.example` or `jp.co.nikko`.
{% endhint %}

{% hint style="info" %}
See `io.amelia.support.KeyValueTypeTrait` for information on how to retrieve values from `ConfigData`.
{% endhint %}

## io.amelia.support.KeyValueTypesTrait

This interface is implemented on both `Parcel` and `ConfigData` to provide value translation for the implementing developer. Returned values are casted using methods provided by the `Objs` support class. The support class `Voluntary` is always returned and may contain a type cast exception. It's an interface so it can't be directly instigated. The following methods are implemented:

```java
VoluntaryBoolean getBoolean()
VoluntaryBoolean getBoolean( String key )
Voluntary<Color> getColor()
Voluntary<Color> getColor( String key )
OptionalDouble getDouble() // API might change
OptionalDouble getDouble( String key )
Voluntary<Enum> getEnum( Class<Enum> enumClass )
Voluntary<Enum> getEnum( String key, Class<Enum> enumClass )
OptionalInteger getInteger() // API might change
OptionalInteger getInteger( String key )
Voluntary<List<T>> getList()
void getList( List<T> list )
void getList( String key, List<T> list )
Voluntary<List<T>> getList( String key )
Voluntary<List<T>> getList( Class<T> expectedObjectClass )
Voluntary<List<T>> getList( String key, Class<T> expectedObjectClass )
VoluntaryLong getLong()
VoluntaryLong getLong( String key )
Voluntary<String> GetString()
Voluntary<String> GetString( String key )
Voluntary<Class<T>> getStringAsClass()
Voluntary<Class<T>> getStringAsClass( String key )
Voluntary<Class<T>> getStringAsClass( String key, Class<T> expectedClass ) // Prevents ClassCastException.
Voluntary<File> getStringAsFile( File rel )
Voluntary<File> getStringAsFile( String key, File rel )
Voluntary<File> getStringAsFile( String key )
Voluntary<File> getStringAsFile()
Voluntary<Path> getStringAsPath( File rel )
Voluntary<Path> getStringAsPath( String key, File rel )
Voluntary<Path> getStringAsPath( String key )
Voluntary<Path> getStringAsPath()
Voluntary<List<String>> getStringList() // Split string to list unless already a list.
Voluntary<List<String>> getStringList( String key )
Voluntary<List<String>> getStringList( String key, String delimiter )
```

In the event that you would like to check if a value is of a particular type or state use the following:

```java
boolean isColor()
boolean isColor( String key )
boolean isEmpty()
boolean isEmpty( String key )
boolean isList()
boolean isList( String key )
boolean isLong()
boolean isLong( String key )
boolean isNull()
boolean isNull( String key )
boolean isSet()
boolean isSet( String key )
boolean isTrue()
boolean isTrue( boolean def )
boolean isTrue( String key )
boolean isTrue( String key, boolean def )
boolean isType( String key, Class<?> type )
```

{% hint style="info" %}
The above will not try casting the value because type checking, e.g., String value "614344000" with `getLong().filter( l -> l instanceof Long ).orElse( false )` will return `true`, while `isLong()` will return `false`. 

Much the same, `getValue().filter( l -> l instanceof Long ).orElse( false )` will also return false.
{% endhint %}

{% hint style="info" %}
#### For Contributors

`KeyValueTypesTrait` utilizes the default interface method feature and handles all internal translation for you. Hence why it's called a trait, taken after the class trait feature found within PHP 7. You are only expected to implement the non-default `getValue()` and `getValue( String key )` method.

Additionally, if you would like to specify a default key, implement the default method `getDefaultKey()`. Presently this value will be used for all methods that have no key argument.
{% endhint %}

### Value Templating

`KeyValueTypesTrait` also utilizes a features coined as value templating which uses the class `io.amelia.data.TypeBase` to outline default keys and  values. This feature is intended for use with the `ConfigData` class but its function can be used with any and all classes that implement `KeyValueTypesTrait`. The following example is intended to be implemented with `ConfigData` but the concept is generally the same everywhere.

```java
public class MyCustomClass {
    public MyCustomClass() {
        if ( getConfig().isTrue( Config.SAY_HELLO ) )
            getConsole().println( getConfig().getValue( Config.WELCOME_MESSAGE ) );
    }

    public static class Config {
        public static final TypeBase CUSTOM_BASE = new TypeBase( "jp.co.nikko" );
		public static final TypeBase.TypeBoolean SAY_HELLO = new TypeBase.TypeBoolean( CUSTOM_BASE, "sayHello", true );
		public static final TypeBase.TypeString WELCOME_MESSAGE = new TypeBase.TypeString( CUSTOM_BASE, "welcomeMessage", "Welcome to my custom class!" );

		private Config() {
			// Static Access
		}
    }
}
```

{% hint style="info" %}
`TypeBase` also provides the following sub-types:

* `TypeBoolean`
* `TypeString`
* `TypeColor`
* `TypeDouble`
* `TypeEnum`
* `TypeFile`
* `TypePath`
* `TypeInteger`
* `TypeLong`
* `TypeStringList`

Values are simply returned using the method `getValue( TypeBase )`, which will return the default key if not set or is null.
{% endhint %}

### More...

The class `io.amelia.support.ValueTypeTrait` is also available for type translation but intentionally lack methods that take a `key` argument.

## io.amelia.support.Voluntary

The `Voluntary` class is near identical to the Java 8 Optional feature, however, with a few key improvements. The most notable would be the ability for `Voluntary` to and/or return an `Exception`. You will find `Voluntary` used in place of Optional in a majority of _Honey Pot Server_. However, usage and conversion between the two is effortless.

{% hint style="info" %}
Voluntary does not use functional interface provided by Java 8 but instead uses custom ones provided by _Honey Pot Server_. See functional interfaces section for more information.
{% endhint %}

### For Contributors

Create an empty `Voluntary`

```java
Voluntary.empty();
```

Create a `Voluntary` using the specified value, will throw `NullPointerException` if value is null.

```java
Voluntary.of( new Object() );
```

Create a `Voluntary` using the specified value. Additionally specify a non-fatal exception. Will still throw `NullPointerException` if value is null.

```java
Voluntary.of( new Object(), new Exception() );
```

Create a `Voluntary` using an `Optional`. Won't throw `NullPointerException`.

```java
Voluntary.of( Arrays.stream( new String[]{"First", "Second", "Third"} ).findAny() );
```

Create a `Voluntary` but instead of throwing `NullPointerException` the specified `Exception` will be used when value is null.

```java
Voluntary.ofElseException( getMaybeNullObject(), new CustomNullException() ); // Via Instance
Voluntary.ofElseException( getMaybeNullObject(), CustomNullException::new ); // Via Functional Interface
Voluntary.ofElseException( getMaybeNullObject(), () -> new CustomNullException(); ) // Via Supplier
```

Similar to `ofElseException`, except `NullPointerException` will be set instead of it being thrown.

```java
Voluntary.withNullPointerException( getMaybeNullObject() );
```

Create Voluntary but don't throw NullPointerException if value is null.

```java
Voluntary.ofNullable( getMaybeNullObject() );
Voluntary.ofNullable( getMaybeNullObject(), new CustomNullException() ); // Set exception via Instance
Voluntary.ofNullable( getMaybeNullObject(), CustomNullException::new ); // Set exception via Functional Interface
Voluntary.ofNullable( getMaybeNullObject(), () -> new CustomNullException(); ) // Set exception via Supplier
```

Create Voluntary with only a cause.

```java
Voluntary.withCause( new CustomNullException() ); // Via Instance
Voluntary.withCause( CustomNullException::new ); // Via Functional Interface
Voluntary.withCause( () -> new CustomNullException(); ) // Via Supplier
```

### For Developers

Same as methods with the same name found in `Optional`.

```java
getVoluntary().get();
getVoluntary().map( value -> value.newValue() );
getVoluntary().filter( Objs::isNotEmpty );
getVoluntary().ifPresent( (value, cause) -> {} );
getVoluntary().flatMap( value -> value.getAnotherVoluntary() );
getVoluntary().flatMapCompatible( value -> value.getOptional() ); // Will convert returned Optional into an Voluntary.
getVoluntary().orElse( new CustomValue() );
getVoluntary().orElseGet( CustomValue::new );
```

Remove exception from `Voluntary`.

```java
getVoluntaryAlwaysHaveCause().removeException();
```

Calls supplied functional interface if value is present. Will catch exceptions returned by supplied functional interface.

```java
getVoluntary().ifPresentCatchException( value -> throw new IllegalStateException() );
```

Calls supplied functional interface if no cause was returned. Returns new voluntary with returned object.

```java
getVoluntary().hasNotErrored( value -> new AltValueObject() );
```

Same as above except forwards returned `Voluntary`.

```java
getVoluntary().hasNotErroredFlat( value -> value.getAnotherVoluntary() );
```

Similar to `Optional.map()` exception catches any thrown exceptions.

```java
getVoluntary().mapCatchException( value -> {
    if ( value.condition() )
        return value.newValue();
    throw new CustomException(); } );
```

Was an error returned?

```java
getVoluntary().hasErrored();
```

Was no error returned and value non-null?

```java
getVoluntary().hasSucceeded();
```

Return value, if present, otherwise throw supplied exception.

```java
getVoluntary().orElseThrow( () -> new CustomThrowableException() );
```

Return value, if present, otherwise throw returned exception.

```java
getVoluntary().orElseThrowCause( cause -> new IllegalStateException( "Problem!", cause ) );
```

### More...

The classes `VoluntaryBoolean` and `VoluntaryLong` are also implemented and provide much the same methods and function as `OptionalBoolean` and `OptionalLong`.

```java
// Allows for a tri-state boolean, i.e., true, false, and null.
io.amelia.support.VoluntaryBoolean
// Voluntary that may or may not contain a long value.
io.amelia.support.VoluntaryLong
```

## Functional Interfaces

Along with the functional interfaces provided by Java 8, _Honey Pot Server_ also implements an array of custom implementations that allow for exceptions to be caught from within lambda statements, as well as the occasional other feature. Java 8 provides to following basic implementations:

```java
java.util.function.Function<T, R>
java.util.function.BiFunction<T, U, R>
java.util.function.Consumer<T>
java.util.function.Predicate<T>
java.util.function.Supplier<T>
// ... And More
```

_Honey Pot Server_ implements the following basic implementations:

```java
io.amelia.support.TriFunction<T, Y, U, R>
io.amelia.support.QuadFunction<T, Y, U, W, R>
```

The followings allows for exceptions to be thrown from within an lambda and rethrown to calling statement.

```java
io.amelia.support.Callback<E extends Exception> // No argument callback with Exception
io.amelia.support.NonnullFunction<T, R> // Throws NullPointerException if T or R is null
io.amelia.support.FunctionWithException<T, R, E extends Exception>
io.amelia.support.BiFunctionWithException<T, U, R, E extends Exception>
io.amelia.support.TriFunctionWithException<T, Y, U, R, E extends Exception>
io.amelia.support.QuadFunctionWithException<T, Y, U, W, R, E extends Exception>
io.amelia.support.ConsumerWithException<T, E extends Exception>
io.amelia.support.SupplierWithException<T, E extends Exception>
```

