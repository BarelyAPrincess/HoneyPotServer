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
 *     private int mData;
 *
 *     public int describeContents() {
 *         return 0;
 *     }
 *
 *     public void writeToParcel(Parcel out, int flags) {
 *         out.writeInt(mData);
 *     }
 *
 *     public static final Parcelable.Creator&lt;MyParcelable&gt; CREATOR
 *             = new Parcelable.Creator&lt;MyParcelable&gt;() {
 *         public MyParcelable createFromParcel(Parcel in) {
 *             return new MyParcelable(in);
 *         }
 *
 *         public MyParcelable[] newArray(int size) {
 *             return new MyParcelable[size];
 *         }
 *     };
 *
 *     private MyParcelable(Parcel in) {
 *         mData = in.readInt();
 *     }
 * }</pre>
 */
public interface Parcelable
{
	/**
	 * Flatten this object in to a Parcel.
	 *
	 * @param dest The Parcel in which the object should be written.
	 */
	void writeToParcel( Parcel dest );

	/**
	 * Specialization of {@link Creator} that allows you to receive the
	 * ClassLoader the object is being created in.
	 */
	interface ClassLoaderCreator<T> extends Creator<T>
	{
		/**
		 * Create a new instance of the Parcelable class, instantiating it
		 * from the given Parcel whose data had previously been written by
		 * {@link Parcelable#writeToParcel Parcelable.writeToParcel()} and
		 * using the given ClassLoader.
		 *
		 * @param source The Parcel to read the object's data from.
		 * @param loader The ClassLoader that this object is being created in.
		 * @return Returns a new instance of the Parcelable class.
		 */
		T createFromParcel( Parcel source, ClassLoader loader );
	}

	/**
	 * Interface that must be implemented and provided as a public CREATOR
	 * field that generates instances of your Parcelable class from a Parcel.
	 */
	interface Creator<T>
	{
		/**
		 * Create a new instance of the Parcelable class, instantiating it
		 * from the given Parcel whose data had previously been written by
		 * {@link Parcelable#writeToParcel Parcelable.writeToParcel()}.
		 *
		 * @param source The Parcel to read the object's data from.
		 * @return Returns a new instance of the Parcelable class.
		 */
		T createFromParcel( Parcel source );

		/**
		 * Create a new array of the Parcelable class.
		 *
		 * @param size Size of the array.
		 * @return Returns an array of the Parcelable class, with every entry
		 * initialized to null.
		 */
		T[] newArray( int size );
	}
}
