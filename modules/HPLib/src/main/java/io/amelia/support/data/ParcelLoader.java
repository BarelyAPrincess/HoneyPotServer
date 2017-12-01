package io.amelia.support.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.amelia.support.Encrypt;
import io.amelia.support.IO;
import io.amelia.support.Maps;
import io.amelia.support.data.yaml.YamlConstructor;
import io.amelia.support.data.yaml.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class ParcelLoader
{
	private static final Gson gson = new GsonBuilder().serializeNulls().setLenient().create();
	private static final DumperOptions yamlOptions = new DumperOptions();
	private static final Representer yamlRepresenter = new YamlRepresenter();
	private static final Yaml yaml = new Yaml( new YamlConstructor(), yamlRepresenter, yamlOptions );

	public static StackerWithValue<?, Object> decodeJson( String jsonEncoded )
	{
		return decodeMap( decodeJsonToMap( jsonEncoded ) );
	}

	public static StackerWithValue<?, Object> decodeJson( File file ) throws IOException
	{
		return decodeJson( IO.readFileToString( file ) );
	}

	public static StackerWithValue<?, Object> decodeJson( InputStream inputStream ) throws IOException
	{
		return decodeJson( IO.readStreamToString( inputStream ) );
	}

	public static Map<String, Object> decodeJsonToMap( String jsonEncoded )
	{
		return Maps.builder().putAll( ( Map<?, ?> ) gson.fromJson( jsonEncoded, Map.class ) ).castTo( String.class, Object.class ).hashMap();
	}

	public static Map<String, Object> decodeJsonToMap( File file ) throws IOException
	{
		return Maps.builder().putAll( ( Map<?, ?> ) gson.fromJson( IO.readFileToString( file ), Map.class ) ).castTo( String.class, Object.class ).hashMap();
	}

	public static Map<String, Object> decodeJsonToMap( InputStream inputStream ) throws IOException
	{
		return Maps.builder().putAll( ( Map<?, ?> ) gson.fromJson( IO.readStreamToString( inputStream ), Map.class ) ).castTo( String.class, Object.class ).hashMap();
	}

	public static Map<String, Object> decodeListToMap( InputStream inputStream )
	{
		return decodeListToMap( IO.readStreamToLines( inputStream, "#" ) );
	}

	public static Map<String, Object> decodeListToMap( List<String> encodedList )
	{
		return Maps.builder().increment( encodedList ).castTo( String.class, Object.class ).hashMap();
	}

	public static Map<String, Object> decodeListToMap( File file ) throws FileNotFoundException
	{
		return decodeListToMap( IO.readFileToLines( file, "#" ) );
	}

	public static StackerWithValue<?, Object> decodeMap( Map<String, Object> mapEncoded )
	{
		Parcel dataMap = new Parcel();
		decodeMap( mapEncoded, dataMap );
		return dataMap;
	}

	/* public static StackerWithValue<?, Object> decodeXml( String xml )
	{
		TODO Implement
	} */

	public static void decodeMap( Map<String, Object> mapEncoded, StackerWithValue<?, Object> root )
	{
		for ( Map.Entry<String, Object> entry : mapEncoded.entrySet() )
		{
			if ( entry.getKey().equals( "__value" ) )
				root.setValue( entry.getValue() );
			else
			{
				StackerWithValue child = root.getChildOrCreate( entry.getKey() );

				if ( entry.getValue() instanceof Map )
					decodeMap( ( Map<String, Object> ) entry.getValue(), child );
				else
					child.setValue( entry.getValue() );
			}
		}
	}

	public static Map<String, Object> decodePropToMap( String propEncoded ) throws IOException
	{
		Properties prop = new Properties();
		prop.load( new StringReader( propEncoded ) );
		return Maps.builder( prop ).castTo( String.class, Object.class ).hashMap();
	}

	public static Map<String, Object> decodePropToMap( InputStream inputStream ) throws IOException
	{
		Properties prop = new Properties();
		prop.load( inputStream );
		return Maps.builder( prop ).castTo( String.class, Object.class ).hashMap();
	}

	public static Map<String, Object> decodePropToMap( File file ) throws IOException
	{
		Properties prop = new Properties();
		prop.load( new FileReader( file ) );
		return Maps.builder( prop ).castTo( String.class, Object.class ).hashMap();
	}

	public static StackerWithValue<?, Object> decodeYaml( File file ) throws IOException
	{
		return decodeYaml( IO.readFileToString( file ) );
	}

	public static StackerWithValue<?, Object> decodeYaml( InputStream inputStream ) throws IOException
	{
		return decodeYaml( IO.readStreamToString( inputStream ) );
	}

	public static StackerWithValue<?, Object> decodeYaml( String yamlEncoded )
	{
		return decodeMap( decodeYamlToMap( yamlEncoded ) );
	}

	public static Map<String, Object> decodeYamlToMap( String yamlEncoded )
	{
		return Maps.builder().putAll( ( Map<?, ?> ) yaml.load( yamlEncoded ) ).castTo( String.class, Object.class ).hashMap();
	}

	public static Map<String, Object> decodeYamlToMap( File file ) throws IOException
	{
		return Maps.builder().putAll( ( Map<?, ?> ) yaml.load( IO.readFileToString( file ) ) ).castTo( String.class, Object.class ).hashMap();
	}

	public static Map<String, Object> decodeYamlToMap( InputStream inputStream ) throws IOException
	{
		return Maps.builder().putAll( ( Map<?, ?> ) yaml.load( IO.readStreamToString( inputStream ) ) ).castTo( String.class, Object.class ).hashMap();
	}

	/* public static void encodeXml( StackerWithValue<?, Object> encoded )
	{
		TODO Implement
	} */

	public static String encodeJson( StackerWithValue<?, Object> encoded )
	{
		return gson.toJson( encodeMap( encoded ) );
	}

	public static Map<String, Object> encodeMap( StackerWithValue<?, Object> encoded )
	{
		Map<String, Object> map = new HashMap<>();

		for ( StackerWithValue<?, Object> child : encoded.children )
		{
			Optional<Object> value = child.getValue();

			if ( child.hasChildren() )
			{
				map.put( child.getName(), encodeMap( child ) );
				value.ifPresent( o -> map.put( "__value", o ) );
			}
			else
				value.ifPresent( o -> map.put( child.getName(), o ) );
		}

		return map;
	}

	public static String encodeYaml( StackerWithValue<?, Object> encoded )
	{
		return yaml.dump( encodeMap( encoded ) );
	}

	public static String hashObject( Object obj )
	{
		// yaml.dump( obj ) OR gson.toJson( obj )?
		return obj == null ? null : Encrypt.md5Hex( obj instanceof String ? ( String ) obj : gson.toJson( obj ) );
	}

	private ParcelLoader()
	{
		// Static Class
	}
}
