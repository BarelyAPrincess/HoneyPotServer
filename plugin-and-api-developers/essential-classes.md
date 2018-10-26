---
description: The Most Essential Classes You'll Need To Know About
---

# Essential Classes



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

## io.amelia.foundation.ConfigData

Similar to Parcel, except lacks a mechanism for serializing its contents. Additionally only a limited set of types can be specified. The intend of this class is to read and write configuration from configurations data sources - as if that didn't need to be said.

Creating a new empty instance is identical to Parcel and also error free:

```java
ConfigData.empty();
```

{% hint style="warning" %}
Keep in mind that ConfigData prevents values from being set on the root and tld keys. Minimum is depth is two to prevent the collision of values between inner-mechanisms of the software. Ideally you would use reverse domain order to store your values, e.g., `com.example` or `jp.co.nikko`
{% endhint %}

## io.amelia.support.Voluntary

The `Voluntary` class is near identical to the Java 8 Optional feature, however, with a few key improvements. The most notable would be the ability for `Voluntary` to and/or return an `Exception`. You will find `Voluntary` used in place of Optional in a majority of _Honey Pot Server_. However, usage and conversion between the two is effortless.

### For Developers

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

Create a `Voluntary` but instead of throwing `NullPointerException` when value is null, the specified `Exception` will be set instead.

```java
Voluntary.ofElseException( getMaybeNullObject(), new CustomNullException() ); // Via Instance
Voluntary.ofElseException( getMaybeNullObject(), CustomNullException::new ); // Via Functional Interface
Voluntary.ofElseException( getMaybeNullObject(), () -> new CustomNullException(); ) // Via Supplier
```

Similar to `ofElseException`, except will specifically set cause to `NullPointerException` instead of it being thrown.

```java
Voluntary.withNullPointerException( getMaybeNullObject() );
```

Create Voluntary but don't throw NullPointerException if value is null.

```java
Voluntary.ofNullable( getMaybeNullObject() );
Voluntary.ofNullable( getMaybeNullObject(), new CustomNullException() ); // Set cause via Instance
Voluntary.ofNullable( getMaybeNullObject(), CustomNullException::new ); // Set cause via Functional Interface
Voluntary.ofNullable( getMaybeNullObject(), () -> new CustomNullException(); ) // Set cause via Supplier
```

Create Voluntary with only a cause.

```java
Voluntary.withCause( new CustomNullException() ); // Via Instance
Voluntary.withCause( CustomNullException::new ); // Via Functional Interface
Voluntary.withCause( () -> new CustomNullException(); ) // Via Supplier
```

### For Implementing

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

Removes exception from `Voluntary`.

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



