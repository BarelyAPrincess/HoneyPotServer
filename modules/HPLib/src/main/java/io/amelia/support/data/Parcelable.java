package io.amelia.support.data;

/**
 * Interface for classes whose instances can be written to
 * and restored from a {@link Parcel}.  Classes implementing the Parcelable
 * interface must also have a non-null static field called <code>CREATOR</code>
 * of a type that implements the {@link Parcelable.Creator} interface.
 * <p>
 * <p>A typical implementation of Parcelable is:</p>
 * <p>
 * <pre>
 * public class MyParcelable implements Parcelable {
 *    public static final Parcelable.Creator&lt;MyParcelable&gt; CREATOR = new Parcelable.Creator&lt;MyParcelable&gt;() {
 *       &at;Override
 *       public MyParcelable readFromParcel( Parcel in ) {
 *          return new MyParcelable( in );
 *       }
 *
 *       &at;Override
 *       public MyParcelable[] newArray( int size ) {
 *          return new MyParcelable[size];
 *       }
 *
 *       &at;Override
 *       public void writeToParcel( MyParcelable obj, Parcel out ) {
 *          out.setValue( "mData", obj.mData );
 *       }
 *    };
 *
 *    private int mData;
 *
 *    private MyParcelable( Parcel in ) {
 *       mData = in.getInteger( "mData" ).orElseThrow( () -> ApplicationException.runtime( "Key 'mData' does not exist! Are you sure this Parcel was created from MyParcelable?" ) );
 *    }
 * }
 * </pre>
 */
public interface Parcelable
{
	/**
	 * Specialization of {@link Creator} that allows you to receive the
	 * ClassLoader the object is being created in.
	 */
	interface ClassLoaderCreator<T> extends Creator<T>
	{
		/**
		 * Create a new instance of the Parcelable class, instantiating it
		 * from the given Parcel whose data had previously been written by
		 * {@link Creator#writeToParcel Parcelable.writeToParcel()} and
		 * using the given ClassLoader.
		 *
		 * @param in     The Parcel to read the object's data from.
		 * @param loader The ClassLoader that this object is being created in.
		 * @return Returns a new instance of the Parcelable class.
		 */
		T readFromParcel( Parcel in, ClassLoader loader );
	}

	/**
	 * Interface that must be implemented and provided as a public CREATOR
	 * field that generates instances of your Parcelable class from a Parcel.
	 */
	interface Creator<T>
	{
		/**
		 * Create a new array of the Parcelable class.
		 *
		 * @param size Size of the array.
		 * @return Returns an array of the Parcelable class, with every entry
		 * initialized to null.
		 */
		T[] newArray( int size );

		/**
		 * Create a new instance of the Parcelable class, instantiating it
		 * from the given Parcel whose data had previously been written by
		 * {@link Creator#writeToParcel Parcelable.writeToParcel()}.
		 *
		 * @param in The Parcel to read the object's data from.
		 * @return Returns a new instance of the Parcelable class.
		 */
		T readFromParcel( Parcel in );

		/**
		 * Flatten the Parcelable object to a Parcel.
		 *
		 * @param obj The Parcelable object to be flattened.
		 * @param out The Parcel in which the object should be written.
		 */
		void writeToParcel( T obj, Parcel out );
	}
}
