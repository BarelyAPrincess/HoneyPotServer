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

```java
Parcel destParcel = Parcel.Factory.serialize( myObjectInstance );
Parcel.Factory.serialize( myObjectInstance, destParcel );

MyObject myObjectInstance = Parcel.Factory.deserialize( srcParcel, MyObject.class );
MyObject myObjectInstance = Parcel.Factory.deserialize( srcParcel );
```

Classes that are not already implemented \(i.e., Built-in HoneyPotServer classes\) or built-in Java types much implement our `io.amelia.data.parcel.ParcelSerializer` in ensure error-free transmission.

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

If you plan on making your serializer external from the class being serialized, \(e.g., The class shipped with a library.\) be sure to register your serializer using:

```java
Parcel.Factory.registerClassSerialized( MyNewObject.class, new MyNewObjectSerializer<MyNewObject>() );
```

You can also check if an object can be serialized with the possibility of throwing an exception using:

```text
Parcel.Factory.isSerializable( myObjectInstance );
```

