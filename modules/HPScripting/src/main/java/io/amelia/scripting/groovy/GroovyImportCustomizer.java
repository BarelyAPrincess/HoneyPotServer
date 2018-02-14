/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting.groovy;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;

import java.util.LinkedList;
import java.util.List;

/**
 * This compilation customizer allows adding various types of imports to the compilation unit. Supports adding :
 * <ul>
 * <li>standard imports thanks to {@link #addImport(String)}, {@link #addImport(String, String)} or {@link #addImports(String...)}</li>
 * <li>star imports thanks to {@link #addStarImport(String)} or {@link #addStarImports(String...)}</li>
 * <li>static imports thanks to {@link #addStaticImport(String, String)} or {@link #addStaticImport(String, String, String)}</li>
 * <li>static star imports thanks to {@link #addStaticStar(String)} or {@link #addStaticStars(String...)}</li>
 * </ul>
 *
 * @author Chiori Greene, a.k.a. Chiori-chan {@literal <me@chiorichan.com>}
 */
public class GroovyImportCustomizer extends CompilationCustomizer
{
	/**
	 * Represents imports which are possibly aliased.
	 */
	private static class Import
	{
		final ImportType type;
		final ClassNode classNode;
		final String alias;
		final String field;
		final String star; // only used for star imports
		
		private Import( final ImportType type, final String star )
		{
			this.type = type;
			this.star = star;
			alias = null;
			classNode = null;
			field = null;
		}
		
		private Import( final ImportType type, final String alias, final ClassNode classNode )
		{
			this.alias = alias;
			this.classNode = classNode;
			this.type = type;
			field = null;
			star = null;
		}
		
		private Import( final ImportType type, final String alias, final ClassNode classNode, final String field )
		{
			this.alias = alias;
			this.classNode = classNode;
			this.field = field;
			this.type = type;
			star = null;
		}
	}
	
	private enum ImportType
	{
		regular, staticImport, staticStar, star
	}
	
	private final List<Import> imports = new LinkedList<Import>();
	
	public GroovyImportCustomizer()
	{
		super( CompilePhase.CONVERSION );
	}
	
	private void addImport( final Class<?> clz )
	{
		final ClassNode node = ClassHelper.make( clz );
		imports.add( new Import( ImportType.regular, node.getNameWithoutPackage(), node ) );
	}
	
	private void addImport( final String className )
	{
		final ClassNode node = ClassHelper.make( className );
		imports.add( new Import( ImportType.regular, node.getNameWithoutPackage(), node ) );
	}
	
	public GroovyImportCustomizer addImport( final String alias, final String className )
	{
		imports.add( new Import( ImportType.regular, alias, ClassHelper.make( className ) ) );
		return this;
	}
	
	public GroovyImportCustomizer addImports( final Class<?>... imports )
	{
		for ( Class<?> anImport : imports )
			addImport( anImport );
		return this;
	}
	
	public GroovyImportCustomizer addImports( final String... imports )
	{
		for ( String anImport : imports )
			addImport( anImport );
		return this;
	}
	
	private void addStarImport( final String packageName )
	{
		final String packageNameEndingWithDot = packageName.endsWith( "." ) ? packageName : packageName + '.';
		imports.add( new Import( ImportType.star, packageNameEndingWithDot ) );
	}
	
	public GroovyImportCustomizer addStarImports( final String... packageNames )
	{
		for ( String packageName : packageNames )
			addStarImport( packageName );
		return this;
	}
	
	public GroovyImportCustomizer addStaticImport( final String className, final String fieldName )
	{
		final ClassNode node = ClassHelper.make( className );
		imports.add( new Import( ImportType.staticImport, fieldName, node, fieldName ) );
		return this;
	}
	
	public GroovyImportCustomizer addStaticImport( final String alias, final String className, final String fieldName )
	{
		imports.add( new Import( GroovyImportCustomizer.ImportType.staticImport, alias, ClassHelper.make( className ), fieldName ) );
		return this;
	}
	
	private void addStaticStar( final Class<?> clz )
	{
		final ClassNode node = ClassHelper.make( clz );
		imports.add( new Import( ImportType.staticStar, node.getNameWithoutPackage(), node ) );
	}
	
	private void addStaticStar( final String className )
	{
		imports.add( new Import( ImportType.staticStar, className, ClassHelper.make( className ) ) );
	}
	
	public GroovyImportCustomizer addStaticStars( final Class<?>... classNames )
	{
		for ( Class<?> className : classNames )
			addStaticStar( className );
		return this;
	}
	
	// -------------------- Helper classes -------------------------
	
	public GroovyImportCustomizer addStaticStars( final String... classNames )
	{
		for ( String className : classNames )
			addStaticStar( className );
		return this;
	}
	
	@Override
	public void call( final SourceUnit source, final GeneratorContext context, final ClassNode classNode ) throws CompilationFailedException
	{
		final ModuleNode ast = source.getAST();
		for ( Import anImport : imports )
			switch ( anImport.type )
			{
				case regular:
					ast.addImport( anImport.alias, anImport.classNode );
					break;
				case staticImport:
					ast.addStaticImport( anImport.classNode, anImport.field, anImport.alias );
					break;
				case staticStar:
					ast.addStaticStarImport( anImport.alias, anImport.classNode );
					break;
				case star:
					ast.addStarImport( anImport.star );
					break;
			}
	}
}
