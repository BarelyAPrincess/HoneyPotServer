package io.amelia.march;

import com.marchnetworks.command.common.dao.GenericDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.amelia.data.parcel.Parcel;
import io.amelia.data.parcel.ParcelLoader;
import io.amelia.database.Database;
import io.amelia.database.DatabaseManager;
import io.amelia.database.elegant.ElegantQueryTable;
import io.amelia.foundation.ConfigData;
import io.amelia.foundation.Foundation;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ConfigException;
import io.amelia.lang.ParcelableException;
import io.amelia.support.Encrypt;
import io.amelia.support.Streams;

public class MarchPersistenceDAO<Type, IdType> implements GenericDAO<Type, IdType>
{
	public static final Database DATABASE_MANAGER;

	public static final Kernel.Logger L = Kernel.getLogger( MarchPersistenceDAO.class );

	static
	{
		try
		{
			ConfigData config = ConfigData.empty();
			config.setValue( "type", "sqlite" );
			config.setValue( "dbfile", "march/persistence.sql" );
			DATABASE_MANAGER = new DatabaseManager( "march", config ).getDatabase();
		}
		catch ( ConfigException.Error e )
		{
			throw new ApplicationException.Runtime( e );
		}
	}

	private final ElegantQueryTable databaseTable;
	private final Map<Type, IdType> loaded = new ConcurrentHashMap<>();
	// Wait what? When could you get the parameter type like this?
	private final Class<Type> typeClass;

	public MarchPersistenceDAO()
	{
		typeClass = ( Class ) ( ( java.lang.reflect.ParameterizedType ) getClass().getGenericSuperclass() ).getActualTypeArguments()[0];
		databaseTable = DATABASE_MANAGER.table( Encrypt.md5Hex( typeClass.getName() ) );
	}

	@Override
	public void clear()
	{
		// detach all entities
		loaded.clear();
	}

	@Override
	public void create( Type obj )
	{
		if ( loaded.containsValue( obj ) )
			throw new ApplicationException.Runtime( "The entity already exists." );
		loaded.put( obj, null );
	}

	@Override
	public void delete( Type obj )
	{
		if ( loaded.containsKey( obj ) )
		{
			L.debug( "Removing entity: " + obj );
			loaded.remove( obj );
		}
	}

	@Override
	public void deleteAll()
	{

	}

	@Override
	public boolean deleteById( IdType id )
	{
		Type foundEntity = loaded.entrySet().stream().filter( entry -> id.equals( entry.getValue() ) ).map( Map.Entry::getKey ).findFirst().orElse( null );
		if ( foundEntity != null )
		{
			L.debug( "Removing entity: " + foundEntity );
			loaded.remove( foundEntity );
			return true;
		}
		return false;
	}

	@Override
	public int deleteByIdsDetached( List<IdType> idTypeList )
	{
		if ( idTypeList == null || idTypeList.size() == 0 )
			return 0;
	}

	@Override
	public boolean deleteDetached( IdType id )
	{
		return false;
	}

	@Override
	public void evict( List<Type> types )
	{

	}

	@Override
	public List<Type> findAll()
	{
		return null;
	}

	@Override
	public List<Type> findAllDehydrated( List<String> paramList )
	{
		return null;
	}

	@Override
	public List<Type> findAllDetached()
	{
		if ( Files.notExists( persistenceFile ) )
			return new ArrayList<>();

		try
		{
			Parcel contents = ParcelLoader.decodeYaml( persistenceFile );
			Parcel serialized = contents.getChildOrCreate( "serialized" );

			List<Type> result = new ArrayList<>();
			Streams.forEachWithException( serialized.getChildren(), child -> {
				String id = child.getLocalName();
				if ( !loaded.containsKey( id ) )
				{
					Type childInstance = Parcel.Factory.deserialize( child, typeClass );
					loaded.put( id, childInstance );
					result.add( childInstance );
				}
			} );
			return result;
		}
		catch ( ParcelableException.Error | IOException e )
		{
			Foundation.L.severe( "There was a problem loading persistent objects for class \"" + typeClass.getName() + "\"", e );
			return new ArrayList<>();
		}
	}

	@Override
	public Type findById( IdType id )
	{
		return null;
	}

	@Override
	public Type findByIdDetached( IdType id )
	{
		return null;
	}

	@Override
	public List<Type> findByIds( List<IdType> idTypeList )
	{
		return null;
	}

	@Override
	public Type findFirst()
	{
		return null;
	}

	@Override
	public Object findMaxValue( String paramString )
	{
		return null;
	}

	@Override
	public Object findMinValue( String paramString )
	{
		return null;
	}

	@Override
	public void flush()
	{

	}

	@Override
	public void flushAndClear()
	{

	}

	@Override
	public IdType getLastId()
	{

	}

	@Override
	public int getRowCount()
	{
		return databaseTable.select().count();
	}

	@Override
	public void merge( Type obj )
	{
		Parcel.Factory.serialize( obj );
	}
}
