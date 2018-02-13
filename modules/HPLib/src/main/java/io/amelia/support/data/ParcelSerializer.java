/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support.data;

import io.amelia.lang.ParcelableException;

/**
 * Interface for implementing an object serializer.
 * <p>
 * You can register the serializer by either calling {@link Parcel.Factory#registerClassSerializer(Class, ParcelSerializer)}
 * or by adding the {@link Parcelable} annotation to any class to be serialized.
 * <p>
 * Ex:
 * <pre>
 *   &at;Parcelable( ExampleClass.Serializer.class )
 *   public class ExampleClass
 *   {
 *     private int someSerializableField;
 *
 *     public static class Serializer implements ParcelSerializer&lt;ExampleClass&gt;
 *     {
 *       &at;Override
 *       public ExampleClass readFromParcel( Parcel src ) throws ParcelableException.Error
 *       {
 *         ExampleClass obj = new ExampleClass();
 *         obj.someSerializableField = src.getValue( "someSerializableField" );
 *         return obj;
 *       }
 *
 *       &at;Override
 *       public void writeToParcel( ExampleClass obj, Parcel dest ) throws ParcelableException.Error
 *       {
 *         dest.setValue( "someSerializableField", obj.someSerializableField );
 *       }
 *     }
 *   }
 * </pre>
 */
public interface ParcelSerializer<ParcelObject>
{
	/**
	 * Create a new instance of the Parcelable class, instantiating it
	 * from the given Parcel whose data had previously been written by
	 * {@link ParcelSerializer#writeToParcel Parcelable.writeToParcel()}.
	 *
	 * @param src The Parcel to read the object's data from.
	 *
	 * @return Returns a new instance of the Parcelable class.
	 */
	ParcelObject readFromParcel( Parcel src ) throws ParcelableException.Error;

	/**
	 * Flatten the Parcelable object to a Parcel.
	 *
	 * @param obj  The Parcelable object to be flattened.
	 * @param dest The Parcel in which the object should be written.
	 */
	void writeToParcel( ParcelObject obj, Parcel dest ) throws ParcelableException.Error;
}
