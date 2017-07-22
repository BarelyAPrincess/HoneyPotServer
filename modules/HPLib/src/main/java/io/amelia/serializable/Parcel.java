package io.amelia.serializable;

import io.amelia.lang.BadParcelableException;

import java.util.Map;

public interface Parcel
{
	static Parcel obtain()
	{
		return null;
	}

	void appendFrom( Parcel parcel, int offset, int length );

	int dataPosition();

	int dataSize();

	void readHashMapInternal( Map<String, Object> map, int n, ClassLoader mClassLoader ) throws BadParcelableException;

	int readInt();

	PersistableBundle readPersistableBundle();

	void recycle();

	void setDataPosition( int i );

	void writeHashMapInternal( Map<String, Object> mMap );

	void writeInt( int i );
}
