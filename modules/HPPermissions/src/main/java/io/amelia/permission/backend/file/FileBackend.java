/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permission.backend.file;

import io.amelia.config.ConfigRegistry;
import io.amelia.permission.PermissibleEntity;
import io.amelia.permission.PermissibleGroup;
import io.amelia.permission.Permission;
import io.amelia.permission.PermissionBackend;
import io.amelia.permission.PermissionDefault;
import io.amelia.permission.PermissionGuard;
import io.amelia.permission.PermissionModelValue;
import io.amelia.permission.PermissionType;
import io.amelia.permission.References;
import io.amelia.permission.lang.PermissionBackendException;
import io.amelia.permission.lang.PermissionException;
import io.amelia.support.LibIO;
import io.amelia.support.Namespace;
import io.amelia.support.Strs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Provides the File Permission Backend
 */
public class FileBackend extends PermissionBackend
{
	private static FileBackend backend;

	public static FileBackend getBackend()
	{
		return backend;
	}

	public FileConfiguration permissions;

	public File permissionsFile;

	public FileBackend()
	{
		super();
		backend = this;
	}

	@Override
	public void commit()
	{
		try
		{
			permissions.save( permissionsFile );
		}
		catch ( IOException e )
		{
			Logger.getLogger( "" ).severe( "[Permissions] Error during saving permissions file: " + e.getMessage() );
		}
	}

	@Override
	public PermissibleGroup getDefaultGroup( References refs )
	{
		ConfigurationSection groups = permissions.getConfigurationSection( "groups" );

		if ( groups == null )
			throw new RuntimeException( "No groups defined. Check your permissions file." );

		String defaultGroupProperty = "default";
		for ( String ref : refs )
		{
			defaultGroupProperty = UtilIO.buildPath( "refs", ref, defaultGroupProperty );

			for ( Map.Entry<String, Object> entry : groups.getValues( false ).entrySet() )
				if ( entry.getValue() instanceof ConfigurationSection )
				{
					ConfigurationSection groupSection = ( ConfigurationSection ) entry.getValue();

					if ( groupSection.getBoolean( defaultGroupProperty, false ) )
						return PermissionGuard.i().getGroup( entry.getKey() );
				}
		}

		if ( refs.isEmpty() )
			throw new RuntimeException( "Default user group is not defined. Please select one using the \"default: true\" property" );

		return null;
	}

	@Override
	public PermissibleEntity getEntity( String id )
	{
		return new FileEntity( id );
	}

	@Override
	public Collection<String> getEntityNames()
	{
		return getEntityNames( 0 );
	}

	@Override
	public Collection<String> getEntityNames( int type )
	{
		ConfigurationSection section = permissions.getConfigurationSection( type == 1 ? "groups" : "entities" );

		if ( section == null )
			return new HashSet<>();

		return section.getKeys( false );
	}

	@Override
	public PermissibleGroup getGroup( String groupName )
	{
		return new FileGroup( groupName );
	}

	@Override
	public Collection<String> getGroupNames()
	{
		return getEntityNames( 1 );
	}

	@Override
	public void initialize() throws PermissionBackendException
	{
		String permissionFilename = ConfigRegistry.getString( "permissions.file" );

		if ( permissionFilename == null )
		{
			permissionFilename = "permissions.yaml";
			ConfigRegistry.i().set( "permissions.file", "permissions.yaml" );
		}

		permissionsFile = LibIO.isAbsolute( permissionFilename ) ? new File( permissionFilename ) : new File( ConfigRegistry.i().getDirectory(), permissionFilename );

		FileConfiguration newPermissions = new YamlConfiguration();
		try
		{
			newPermissions.load( permissionsFile );
			PermissionGuard.L.info( "Permissions file successfully loaded" );
			permissions = newPermissions;
		}
		catch ( FileNotFoundException e )
		{
			if ( permissions == null )
			{
				// First load, load even if the file doesn't exist
				permissions = newPermissions;
				initNewConfiguration();
			}
		}
		catch ( Throwable e )
		{
			throw new PermissionBackendException( "Error loading permissions file!", e );
		}
	}

	@Override
	public void loadEntities() throws PermissionBackendException
	{
		ConfigurationSection section = permissions.getConfigurationSection( "entities" );

		if ( section != null )
			for ( String s : section.getKeys( false ) )
				PermissionGuard.getEntity( s );
	}

	@Override
	public void loadGroups() throws PermissionBackendException
	{
		ConfigurationSection section = permissions.getConfigurationSection( "groups" );

		if ( section != null )
			for ( String s : section.getKeys( false ) )
				PermissionGuard.getGroup( s );
	}

	@Override
	public void loadPermissions() throws PermissionBackendException
	{
		ConfigurationSection section = permissions.getConfigurationSection( "permissions" );
		if ( section == null )
			return;

		try
		{
			Set<String> keys = section.getKeys( false );
			for ( String s : keys )
			{
				ConfigurationSection node = section.getConfigurationSection( s );
				Namespace ns = Namespace.parseString( s.replaceAll( "/", "." ) );

				if ( !ns.containsOnlyValidChars() )
				{
					PermissionGuard.L.warning( String.format( "The permission '%s' contains invalid characters, namespaces can only contain the characters a-z, 0-9, and _, this will be fixed automatically.", ns ) );
					ns.fixInvalidChars();
					section.set( s, null );
					section.set( ns.getLocalName(), node );
				}

				Permission perm = new Permission( ns, PermissionType.valueOf( section.getString( "type" ) ) );
				PermissionModelValue model = perm.getModel();

				if ( section.get( "value" ) != null && !section.isConfigurationSection( "value" ) )
					model.setValue( section.get( "value" ) );

				if ( section.get( "default" ) != null && !section.isConfigurationSection( "default" ) )
					model.setValueDefault( section.get( "default" ) );

				if ( perm.getType().hasMax() )
					model.setMaxLen( Math.min( section.getInt( "max" ), perm.getType().maxValue() ) );

				if ( perm.getType() == PermissionType.ENUM )
					model.setEnums( Strs.split( section.getString( "enum" ), "|" ).collect( Collectors.toSet() ) );

				model.setDescription( section.getString( "description" ) );
			}
		}
		catch ( PermissionException e )
		{
			e.printStackTrace();
			PermissionGuard.getLogger().warning( e.getMessage() );
		}
	}

	@Override
	public void reloadBackend() throws PermissionBackendException
	{
		try
		{
			permissions.load( permissionsFile );
		}
		catch ( IOException | InvalidConfigurationException e )
		{
			throw new PermissionBackendException( e );
		}
	}

	@Override
	public void setDefaultGroup( String group, References ref )
	{
		String refs = ref.join();

		ConfigurationSection groups = permissions.getConfigurationSection( "groups", true );

		String defaultGroupProperty = "default";
		if ( refs != null )
			defaultGroupProperty = UtilIO.buildPath( "refs", refs, defaultGroupProperty );

		boolean success = false;

		for ( Map.Entry<String, Object> entry : groups.getValues( false ).entrySet() )
			if ( entry.getValue() instanceof ConfigurationSection )
			{
				ConfigurationSection groupSection = ( ConfigurationSection ) entry.getValue();

				groupSection.set( defaultGroupProperty, false );

				if ( !groupSection.getName().equals( group ) )
					groupSection.set( defaultGroupProperty, null );
				else
				{
					groupSection.set( defaultGroupProperty, true );
					success = true;
				}
			}

		if ( !success )
		{
			PermissibleGroup pGroup = PermissionGuard.i().getGroup( group );
			pGroup.setDefault( true );
			pGroup.save();
		}

		commit();
	}

	/**
	 * This method is called when the permissions config file does not exist
	 * and needs to be created, this also adds the defaults.
	 */
	private void initNewConfiguration() throws PermissionBackendException
	{
		if ( !permissionsFile.exists() )
			try
			{
				permissionsFile.createNewFile();

				setDefaultGroup( "default", References.format( "" ) );

				List<String> defaultPermissions = new LinkedList<String>();
				defaultPermissions.add( "com.chiorichan.*" );

				permissions.set( "groups/default/permissions", defaultPermissions );

				commit();
			}
			catch ( IOException e )
			{
				throw new PermissionBackendException( e );
			}
	}

	@Override
	public void nodeCommit( Permission perm )
	{
		if ( PermissionDefault.isDefault( perm ) )
			return;

		if ( perm.getType() == PermissionType.DEFAULT && perm.hasChildren() && !perm.getModel().hasDescription() )
			return;

		PermissionModelValue model = perm.getModel();
		ConfigurationSection permission = permissions.getConfigurationSection( "permissions." + perm.getNamespace().replaceAll( "\\.", "/" ), true );

		permission.set( "type", perm.getType().name() );

		permission.set( "value", perm.getType() == PermissionType.DEFAULT ? null : model.getValue() );
		permission.set( "default", perm.getType() == PermissionType.DEFAULT ? null : model.getValueDefault() );

		permission.set( "max", perm.getType().hasMax() ? model.getMaxLen() : null );
		permission.set( "min", perm.getType().hasMin() ? 0 : null );
		permission.set( "enum", perm.getType() == PermissionType.ENUM ? model.getEnumsString() : null );
		permission.set( "description", model.hasDescription() ? model.getDescription() : null );

		commit();
	}

	@Override
	public void nodeDestroy( Permission perm )
	{
		ConfigurationSection permissionsSection = permissions.getConfigurationSection( "permissions", true );
		permissionsSection.set( perm.getNamespace(), null );
	}

	@Override
	public void nodeReload( Permission perm )
	{

	}
}
