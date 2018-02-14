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
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.syntax.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.amelia.lang.SandboxSecurityException;

/**
 * @author Cedric Champeau
 * @author Guillaume Laforge
 * @author Hamlet D'Arcy
 * @author Chiori Greene
 * @since 1.8.0
 */
@SuppressWarnings( "rawtypes" )
public class GroovySandbox extends CompilationCustomizer
{
	private boolean isPackageAllowed = true;
	private boolean isMethodDefinitionAllowed = true;
	private boolean isClosuresAllowed = true;
	
	// imports
	private List<String> importsWhitelist;
	private List<String> importsBlacklist;
	
	// static imports
	private List<String> staticImportsWhitelist;
	private List<String> staticImportsBlacklist;
	
	// star imports
	private List<String> starImportsWhitelist;
	private List<String> starImportsBlacklist;
	
	// static star imports
	private List<String> staticStarImportsWhitelist;
	private List<String> staticStarImportsBlacklist;
	
	
	// indirect import checks
	// if set to true, then security rules on imports will also be applied on class nodes.
	// Direct instantiation of classes without imports will therefore also fail if this option is enabled
	private boolean isIndirectImportCheckEnabled;
	
	// statements
	private List<Class<? extends Statement>> statementsWhitelist;
	private List<Class<? extends Statement>> statementsBlacklist;
	private final List<StatementChecker> statementCheckers = new LinkedList<StatementChecker>();
	
	// expressions
	private List<Class<? extends Expression>> expressionsWhitelist;
	private List<Class<? extends Expression>> expressionsBlacklist;
	private final List<ExpressionChecker> expressionCheckers = new LinkedList<ExpressionChecker>();
	
	// tokens from Types
	private List<Integer> tokensWhitelist;
	private List<Integer> tokensBlacklist;
	
	// constant types
	private List<String> constantTypesWhiteList;
	private List<String> constantTypesBlackList;
	
	// receivers
	private List<String> receiversWhiteList;
	private List<String> receiversBlackList;
	
	public GroovySandbox()
	{
		super( CompilePhase.CANONICALIZATION );
	}
	
	public boolean isMethodDefinitionAllowed()
	{
		return isMethodDefinitionAllowed;
	}
	
	public void setMethodDefinitionAllowed( final boolean methodDefinitionAllowed )
	{
		isMethodDefinitionAllowed = methodDefinitionAllowed;
	}
	
	public boolean isPackageAllowed()
	{
		return isPackageAllowed;
	}
	
	public boolean isClosuresAllowed()
	{
		return isClosuresAllowed;
	}
	
	public void setClosuresAllowed( final boolean closuresAllowed )
	{
		isClosuresAllowed = closuresAllowed;
	}
	
	public void setPackageAllowed( final boolean packageAllowed )
	{
		isPackageAllowed = packageAllowed;
	}
	
	public List<String> getImportsBlacklist()
	{
		return importsBlacklist;
	}
	
	public void setImportsBlacklist( final List<String> importsBlacklist )
	{
		if ( importsWhitelist != null || starImportsWhitelist != null )
		{
			throw new IllegalArgumentException( "You are not allowed to set both whitelist and blacklist" );
		}
		this.importsBlacklist = importsBlacklist;
	}
	
	public List<String> getImportsWhitelist()
	{
		return importsWhitelist;
	}
	
	public void setImportsWhitelist( final List<String> importsWhitelist )
	{
		if ( importsBlacklist != null || starImportsBlacklist != null )
		{
			throw new IllegalArgumentException( "You are not allowed to set both whitelist and blacklist" );
		}
		this.importsWhitelist = importsWhitelist;
	}
	
	public List<String> getStarImportsBlacklist()
	{
		return starImportsBlacklist;
	}
	
	public void setStarImportsBlacklist( final List<String> starImportsBlacklist )
	{
		if ( importsWhitelist != null || starImportsWhitelist != null )
		{
			throw new IllegalArgumentException( "You are not allowed to set both whitelist and blacklist" );
		}
		this.starImportsBlacklist = normalizeStarImports( starImportsBlacklist );
		if ( this.importsBlacklist == null )
			importsBlacklist = Collections.emptyList();
	}
	
	public List<String> getStarImportsWhitelist()
	{
		return starImportsWhitelist;
	}
	
	public void setStarImportsWhitelist( final List<String> starImportsWhitelist )
	{
		if ( importsBlacklist != null || starImportsBlacklist != null )
		{
			throw new IllegalArgumentException( "You are not allowed to set both whitelist and blacklist" );
		}
		this.starImportsWhitelist = normalizeStarImports( starImportsWhitelist );
		if ( this.importsWhitelist == null )
			importsWhitelist = Collections.emptyList();
	}
	
	/**
	 * Ensures that every star import ends with .* as this is the expected syntax in import checks.
	 */
	private static List<String> normalizeStarImports( List<String> starImports )
	{
		List<String> result = new ArrayList<String>( starImports.size() );
		for ( String starImport : starImports )
		{
			if ( starImport.endsWith( ".*" ) )
			{
				result.add( starImport );
			}
			else if ( starImport.endsWith( "." ) )
			{
				result.add( starImport + "*" );
			}
			else
			{
				result.add( starImport + ".*" );
			}
		}
		return Collections.unmodifiableList( result );
	}
	
	public List<String> getStaticImportsBlacklist()
	{
		return staticImportsBlacklist;
	}
	
	public void setStaticImportsBlacklist( final List<String> staticImportsBlacklist )
	{
		if ( staticImportsWhitelist != null || staticStarImportsWhitelist != null )
		{
			throw new IllegalArgumentException( "You are not allowed to set both whitelist and blacklist" );
		}
		this.staticImportsBlacklist = staticImportsBlacklist;
	}
	
	public List<String> getStaticImportsWhitelist()
	{
		return staticImportsWhitelist;
	}
	
	public void setStaticImportsWhitelist( final List<String> staticImportsWhitelist )
	{
		if ( staticImportsBlacklist != null || staticStarImportsBlacklist != null )
		{
			throw new IllegalArgumentException( "You are not allowed to set both whitelist and blacklist" );
		}
		this.staticImportsWhitelist = staticImportsWhitelist;
	}
	
	public List<String> getStaticStarImportsBlacklist()
	{
		return staticStarImportsBlacklist;
	}
	
	public void setStaticStarImportsBlacklist( final List<String> staticStarImportsBlacklist )
	{
		if ( staticImportsWhitelist != null || staticStarImportsWhitelist != null )
		{
			throw new IllegalArgumentException( "You are not allowed to set both whitelist and blacklist" );
		}
		this.staticStarImportsBlacklist = normalizeStarImports( staticStarImportsBlacklist );
		if ( this.staticImportsBlacklist == null )
			this.staticImportsBlacklist = Collections.emptyList();
	}
	
	public List<String> getStaticStarImportsWhitelist()
	{
		return staticStarImportsWhitelist;
	}
	
	public void setStaticStarImportsWhitelist( final List<String> staticStarImportsWhitelist )
	{
		if ( staticImportsBlacklist != null || staticStarImportsBlacklist != null )
		{
			throw new IllegalArgumentException( "You are not allowed to set both whitelist and blacklist" );
		}
		this.staticStarImportsWhitelist = normalizeStarImports( staticStarImportsWhitelist );
		if ( this.staticImportsWhitelist == null )
			this.staticImportsWhitelist = Collections.emptyList();
	}
	
	public List<Class<? extends Expression>> getExpressionsBlacklist()
	{
		return expressionsBlacklist;
	}
	
	public void setExpressionsBlacklist( final List<Class<? extends Expression>> expressionsBlacklist )
	{
		if ( expressionsWhitelist != null )
		{
			throw new IllegalArgumentException( "You are not allowed to set both whitelist and blacklist" );
		}
		this.expressionsBlacklist = expressionsBlacklist;
	}
	
	public List<Class<? extends Expression>> getExpressionsWhitelist()
	{
		return expressionsWhitelist;
	}
	
	public void setExpressionsWhitelist( final List<Class<? extends Expression>> expressionsWhitelist )
	{
		if ( expressionsBlacklist != null )
		{
			throw new IllegalArgumentException( "You are not allowed to set both whitelist and blacklist" );
		}
		this.expressionsWhitelist = expressionsWhitelist;
	}
	
	public List<Class<? extends Statement>> getStatementsBlacklist()
	{
		return statementsBlacklist;
	}
	
	public void setStatementsBlacklist( final List<Class<? extends Statement>> statementsBlacklist )
	{
		if ( statementsWhitelist != null )
		{
			throw new IllegalArgumentException( "You are not allowed to set both whitelist and blacklist" );
		}
		this.statementsBlacklist = statementsBlacklist;
	}
	
	public List<Class<? extends Statement>> getStatementsWhitelist()
	{
		return statementsWhitelist;
	}
	
	public void setStatementsWhitelist( final List<Class<? extends Statement>> statementsWhitelist )
	{
		if ( statementsBlacklist != null )
		{
			throw new IllegalArgumentException( "You are not allowed to set both whitelist and blacklist" );
		}
		this.statementsWhitelist = statementsWhitelist;
	}
	
	public List<Integer> getTokensBlacklist()
	{
		return tokensBlacklist;
	}
	
	public boolean isIndirectImportCheckEnabled()
	{
		return isIndirectImportCheckEnabled;
	}
	
	/**
	 * Set this option to true if you want your import rules to be checked against every class node. This means that if
	 * someone uses a fully qualified class name, then it will also be checked against the import rules, preventing, for
	 * example, instantiation of classes without imports thanks to FQCN.
	 *
	 * @param indirectImportCheckEnabled
	 *            set to true to enable indirect checks
	 */
	public void setIndirectImportCheckEnabled( final boolean indirectImportCheckEnabled )
	{
		isIndirectImportCheckEnabled = indirectImportCheckEnabled;
	}
	
	/**
	 * Sets the list of tokens which are blacklisted.
	 *
	 * @param tokensBlacklist
	 *            the tokens. The values of the tokens must be those of {@link org.codehaus.groovy.syntax.Types}
	 */
	public void setTokensBlacklist( final List<Integer> tokensBlacklist )
	{
		if ( tokensWhitelist != null )
		{
			throw new IllegalArgumentException( "You are not allowed to set both whitelist and blacklist" );
		}
		this.tokensBlacklist = tokensBlacklist;
	}
	
	public List<Integer> getTokensWhitelist()
	{
		return tokensWhitelist;
	}
	
	/**
	 * Sets the list of tokens which are whitelisted.
	 *
	 * @param tokensWhitelist
	 *            the tokens. The values of the tokens must be those of {@link org.codehaus.groovy.syntax.Types}
	 */
	public void setTokensWhitelist( final List<Integer> tokensWhitelist )
	{
		if ( tokensBlacklist != null )
		{
			throw new IllegalArgumentException( "You are not allowed to set both whitelist and blacklist" );
		}
		this.tokensWhitelist = tokensWhitelist;
	}
	
	public void addStatementCheckers( StatementChecker... checkers )
	{
		statementCheckers.addAll( Arrays.asList( checkers ) );
	}
	
	public void addExpressionCheckers( ExpressionChecker... checkers )
	{
		expressionCheckers.addAll( Arrays.asList( checkers ) );
	}
	
	public List<String> getConstantTypesBlackList()
	{
		return constantTypesBlackList;
	}
	
	public void setConstantTypesBlackList( final List<String> constantTypesBlackList )
	{
		if ( constantTypesWhiteList != null )
		{
			throw new IllegalArgumentException( "You are not allowed to set both whitelist and blacklist" );
		}
		this.constantTypesBlackList = constantTypesBlackList;
	}
	
	public List<String> getConstantTypesWhiteList()
	{
		return constantTypesWhiteList;
	}
	
	public void setConstantTypesWhiteList( final List<String> constantTypesWhiteList )
	{
		if ( constantTypesBlackList != null )
		{
			throw new IllegalArgumentException( "You are not allowed to set both whitelist and blacklist" );
		}
		this.constantTypesWhiteList = constantTypesWhiteList;
	}
	
	/**
	 * An alternative way of setting constant types.
	 *
	 * @param constantTypesWhiteList
	 *            a list of classes.
	 */
	public void setConstantTypesClassesWhiteList( final List<Class> constantTypesWhiteList )
	{
		List<String> values = new LinkedList<String>();
		for ( Class aClass : constantTypesWhiteList )
		{
			values.add( aClass.getName() );
		}
		setConstantTypesWhiteList( values );
	}
	
	/**
	 * An alternative way of setting constant types.
	 *
	 * @param constantTypesBlackList
	 *            a list of classes.
	 */
	public void setConstantTypesClassesBlackList( final List<Class> constantTypesBlackList )
	{
		List<String> values = new LinkedList<String>();
		for ( Class aClass : constantTypesBlackList )
		{
			values.add( aClass.getName() );
		}
		setConstantTypesBlackList( values );
	}
	
	public List<String> getReceiversBlackList()
	{
		return receiversBlackList;
	}
	
	/**
	 * Sets the list of classes which deny method calls.
	 * 
	 * Please note that since Groovy is a dynamic language, and
	 * this class performs a static type check, it will be reletively
	 * simple to bypass any blacklist unless the receivers blacklist contains, at
	 * a minimum, Object, Script, GroovyShell, and Eval. Additionally,
	 * it is necessary to also blacklist MethodPointerExpression in the
	 * expressions blacklist for the receivers blacklist to function
	 * as a security check.
	 *
	 * @param receiversBlackList
	 *            the list of refused classes, as fully qualified names
	 */
	public void setReceiversBlackList( final List<String> receiversBlackList )
	{
		if ( receiversWhiteList != null )
		{
			throw new IllegalArgumentException( "You are not allowed to set both whitelist and blacklist" );
		}
		this.receiversBlackList = receiversBlackList;
	}
	
	/**
	 * An alternative way of setting {@link #setReceiversBlackList(List) receiver classes}.
	 *
	 * @param receiversBlacklist
	 *            a list of classes.
	 */
	public void setReceiversClassesBlackList( final List<Class> receiversBlacklist )
	{
		List<String> values = new LinkedList<String>();
		for ( Class aClass : receiversBlacklist )
		{
			values.add( aClass.getName() );
		}
		setReceiversBlackList( values );
	}

	public List<String> getReceiversWhiteList()
	{
		return receiversWhiteList;
	}

	/**
	 * Sets the list of classes which may accept method calls.
	 *
	 * @param receiversWhiteList
	 *            the list of accepted classes, as fully qualified names
	 */
	public void setReceiversWhiteList( final List<String> receiversWhiteList )
	{
		if ( receiversBlackList != null )
		{
			throw new IllegalArgumentException( "You are not allowed to set both whitelist and blacklist" );
		}
		this.receiversWhiteList = receiversWhiteList;
	}

	/**
	 * An alternative way of setting {@link #setReceiversWhiteList(List) receiver classes}.
	 *
	 * @param receiversWhitelist
	 *            a list of classes.
	 */
	public void setReceiversClassesWhiteList( final List<Class> receiversWhitelist )
	{
		List<String> values = new LinkedList<String>();
		for ( Class aClass : receiversWhitelist )
		{
			values.add( aClass.getName() );
		}
		setReceiversWhiteList( values );
	}
	
	@Override
	public void call( final SourceUnit source, final GeneratorContext context, final ClassNode classNode ) throws CompilationFailedException
	{
		final ModuleNode ast = source.getAST();
		if ( !isPackageAllowed && ast.getPackage() != null )
		{
			throw new SandboxSecurityException( "Package definitions are not allowed" );
		}
		checkMethodDefinitionAllowed( classNode );
		
		// verify imports
		if ( importsBlacklist != null || importsWhitelist != null || starImportsBlacklist != null || starImportsWhitelist != null )
		{
			for ( ImportNode importNode : ast.getImports() )
			{
				final String className = importNode.getClassName();
				assertImportIsAllowed( className );
			}
			for ( ImportNode importNode : ast.getStarImports() )
			{
				final String className = importNode.getPackageName();
				assertStarImportIsAllowed( className + "*" );
			}
		}
		
		// verify static imports
		if ( staticImportsBlacklist != null || staticImportsWhitelist != null || staticStarImportsBlacklist != null || staticStarImportsWhitelist != null )
		{
			for ( Map.Entry<String, ImportNode> entry : ast.getStaticImports().entrySet() )
			{
				final String className = entry.getValue().getClassName();
				assertStaticImportIsAllowed( entry.getKey(), className );
			}
			for ( Map.Entry<String, ImportNode> entry : ast.getStaticStarImports().entrySet() )
			{
				final String className = entry.getValue().getClassName();
				assertStaticImportIsAllowed( entry.getKey(), className );
			}
		}
		
		final SecuringCodeVisitor visitor = new SecuringCodeVisitor();
		ast.getStatementBlock().visit( visitor );
		for ( ClassNode clNode : ast.getClasses() )
		{
			if ( clNode != classNode )
			{
				checkMethodDefinitionAllowed( clNode );
				for ( MethodNode methodNode : clNode.getMethods() )
				{
					if ( !methodNode.isSynthetic() )
					{
						methodNode.getCode().visit( visitor );
					}
				}
			}
		}
		
		List<MethodNode> methods = filterMethods( classNode );
		if ( isMethodDefinitionAllowed )
		{
			for ( MethodNode method : methods )
			{
				if ( method.getDeclaringClass() == classNode )
					method.getCode().visit( visitor );
			}
		}
	}
	
	private void checkMethodDefinitionAllowed( ClassNode owner )
	{
		if ( isMethodDefinitionAllowed )
			return;
		List<MethodNode> methods = filterMethods( owner );
		if ( !methods.isEmpty() )
			throw new SandboxSecurityException( "Method definitions are not allowed" );
	}
	
	private List<MethodNode> filterMethods( ClassNode owner )
	{
		List<MethodNode> result = new LinkedList<MethodNode>();
		List<MethodNode> methods = owner.getMethods();
		for ( MethodNode method : methods )
		{
			if ( method.getDeclaringClass() == owner && !method.isSynthetic() )
			{
				if ( "main".equals( method.getName() ) || "run".equals( method.getName() ) && owner.isScriptBody() )
					continue;
				result.add( method );
			}
		}
		return result;
	}
	
	private void assertStarImportIsAllowed( final String packageName )
	{
		if ( starImportsWhitelist != null && !starImportsWhitelist.contains( packageName ) )
		{
			throw new SandboxSecurityException( "Importing [" + packageName + "] is not allowed" );
		}
		if ( starImportsBlacklist != null && starImportsBlacklist.contains( packageName ) )
		{
			throw new SandboxSecurityException( "Importing [" + packageName + "] is not allowed" );
		}
	}
	
	private void assertImportIsAllowed( final String className )
	{
		if ( importsWhitelist != null && !importsWhitelist.contains( className ) )
		{
			if ( starImportsWhitelist != null )
			{
				// we should now check if the import is in the star imports
				ClassNode node = ClassHelper.make( className );
				final String packageName = node.getPackageName();
				if ( !starImportsWhitelist.contains( packageName + ".*" ) )
				{
					throw new SandboxSecurityException( "Importing [" + className + "] is not allowed" );
				}
			}
			else
			{
				throw new SandboxSecurityException( "Importing [" + className + "] is not allowed" );
			}
		}
		if ( importsBlacklist != null && importsBlacklist.contains( className ) )
		{
			throw new SandboxSecurityException( "Importing [" + className + "] is not allowed" );
		}
		// check that there's no star import blacklist
		if ( starImportsBlacklist != null )
		{
			ClassNode node = ClassHelper.make( className );
			final String packageName = node.getPackageName();
			if ( starImportsBlacklist.contains( packageName + ".*" ) )
			{
				throw new SandboxSecurityException( "Importing [" + className + "] is not allowed" );
			}
		}
	}
	
	private void assertStaticImportIsAllowed( final String member, final String className )
	{
		final String fqn = member.equals( className ) ? member : className + "." + member;
		if ( staticImportsWhitelist != null && !staticImportsWhitelist.contains( fqn ) )
		{
			if ( staticStarImportsWhitelist != null )
			{
				// we should now check if the import is in the star imports
				if ( !staticStarImportsWhitelist.contains( className + ".*" ) )
				{
					throw new SandboxSecurityException( "Importing [" + fqn + "] is not allowed" );
				}
			}
			else
			{
				throw new SandboxSecurityException( "Importing [" + fqn + "] is not allowed" );
			}
		}
		if ( staticImportsBlacklist != null && staticImportsBlacklist.contains( fqn ) )
		{
			throw new SandboxSecurityException( "Importing [" + fqn + "] is not allowed" );
		}
		// check that there's no star import blacklist
		if ( staticStarImportsBlacklist != null )
		{
			if ( staticStarImportsBlacklist.contains( className + ".*" ) )
			{
				throw new SandboxSecurityException( "Importing [" + fqn + "] is not allowed" );
			}
		}
	}
	
	/**
	 * This visitor directly implements the {@link GroovyCodeVisitor} interface instead of using the CodeVisitorSupport class to make sure that future features of the language gets managed by this visitor. Thus,
	 * adding a new feature would result in a compilation error if this visitor is not updated.
	 */
	private class SecuringCodeVisitor implements GroovyCodeVisitor
	{
		
		/**
		 * Checks that a given statement is either in the whitelist or not in the blacklist.
		 *
		 * @param statement
		 *            the statement to be checked
		 * @throws SandboxSecurityException
		 *             if usage of this statement class is forbidden
		 */
		private void assertStatementAuthorized( final Statement statement ) throws SandboxSecurityException
		{
			final Class<? extends Statement> clazz = statement.getClass();
			if ( statementsBlacklist != null && statementsBlacklist.contains( clazz ) )
			{
				throw new SandboxSecurityException( clazz.getSimpleName() + "s are not allowed" );
			}
			else if ( statementsWhitelist != null && !statementsWhitelist.contains( clazz ) )
			{
				throw new SandboxSecurityException( clazz.getSimpleName() + "s are not allowed" );
			}
			for ( StatementChecker statementChecker : statementCheckers )
			{
				if ( !statementChecker.isAuthorized( statement ) )
				{
					throw new SandboxSecurityException( "Statement [" + clazz.getSimpleName() + "] is not allowed" );
				}
			}
		}
		
		/**
		 * Checks that a given expression is either in the whitelist or not in the blacklist.
		 *
		 * @param expression
		 *            the expression to be checked
		 * @throws SandboxSecurityException
		 *             if usage of this expression class is forbidden
		 */
		private void assertExpressionAuthorized( final Expression expression ) throws SandboxSecurityException
		{
			final Class<? extends Expression> clazz = expression.getClass();
			if ( expressionsBlacklist != null && expressionsBlacklist.contains( clazz ) )
			{
				throw new SandboxSecurityException( clazz.getSimpleName() + "s are not allowed: " + expression.getText() );
			}
			else if ( expressionsWhitelist != null && !expressionsWhitelist.contains( clazz ) )
			{
				throw new SandboxSecurityException( clazz.getSimpleName() + "s are not allowed: " + expression.getText() );
			}
			for ( ExpressionChecker expressionChecker : expressionCheckers )
			{
				if ( !expressionChecker.isAuthorized( expression ) )
				{
					throw new SandboxSecurityException( "Expression [" + clazz.getSimpleName() + "] is not allowed: " + expression.getText() );
				}
			}
			if ( isIndirectImportCheckEnabled )
			{
				try
				{
					if ( expression instanceof ConstructorCallExpression )
					{
						assertImportIsAllowed( expression.getType().getName() );
					}
					else if ( expression instanceof MethodCallExpression )
					{
						MethodCallExpression expr = ( MethodCallExpression ) expression;
						final String typename = expr.getObjectExpression().getType().getName();
						assertImportIsAllowed( typename );
						assertStaticImportIsAllowed( expr.getMethodAsString(), typename );
					}
					else if ( expression instanceof StaticMethodCallExpression )
					{
						StaticMethodCallExpression expr = ( StaticMethodCallExpression ) expression;
						final String typename = expr.getOwnerType().getName();
						assertImportIsAllowed( typename );
						assertStaticImportIsAllowed( expr.getMethod(), typename );
					}
					else if ( expression instanceof MethodPointerExpression )
					{
						MethodPointerExpression expr = ( MethodPointerExpression ) expression;
						final String typename = expr.getType().getName();
						assertImportIsAllowed( typename );
						assertStaticImportIsAllowed( expr.getText(), typename );
					}
				}
				catch ( SandboxSecurityException e )
				{
					throw new SandboxSecurityException( "Indirect import checks prevents usage of expression", e );
				}
			}
		}
		
		/**
		 * Checks that a given token is either in the whitelist or not in the blacklist.
		 *
		 * @param token
		 *            the token to be checked
		 * @throws SandboxSecurityException
		 *             if usage of this token is forbidden
		 */
		private void assertTokenAuthorized( final Token token ) throws SandboxSecurityException
		{
			final int value = token.getType();
			if ( tokensBlacklist != null && tokensBlacklist.contains( value ) )
			{
				throw new SandboxSecurityException( "Token " + token + " is not allowed" );
			}
			else if ( tokensWhitelist != null && !tokensWhitelist.contains( value ) )
			{
				throw new SandboxSecurityException( "Token " + token + " is not allowed" );
			}
		}
		
		@Override
		public void visitBlockStatement( final BlockStatement block )
		{
			assertStatementAuthorized( block );
			for ( Statement statement : block.getStatements() )
			{
				statement.visit( this );
			}
		}
		
		
		@Override
		public void visitForLoop( final ForStatement forLoop )
		{
			assertStatementAuthorized( forLoop );
			forLoop.getCollectionExpression().visit( this );
			forLoop.getLoopBlock().visit( this );
		}
		
		@Override
		public void visitWhileLoop( final WhileStatement loop )
		{
			assertStatementAuthorized( loop );
			loop.getBooleanExpression().visit( this );
			loop.getLoopBlock().visit( this );
		}
		
		@Override
		public void visitDoWhileLoop( final DoWhileStatement loop )
		{
			assertStatementAuthorized( loop );
			loop.getBooleanExpression().visit( this );
			loop.getLoopBlock().visit( this );
		}
		
		@Override
		public void visitIfElse( final IfStatement ifElse )
		{
			assertStatementAuthorized( ifElse );
			ifElse.getBooleanExpression().visit( this );
			ifElse.getIfBlock().visit( this );
			
			Statement elseBlock = ifElse.getElseBlock();
			if ( elseBlock instanceof EmptyStatement )
			{
				// dispatching to EmptyStatement will not call back visitor,
				// must call our visitEmptyStatement explicitly
				visitEmptyStatement( ( EmptyStatement ) elseBlock );
			}
			else
			{
				elseBlock.visit( this );
			}
		}
		
		@Override
		public void visitExpressionStatement( final ExpressionStatement statement )
		{
			assertStatementAuthorized( statement );
			statement.getExpression().visit( this );
		}
		
		@Override
		public void visitReturnStatement( final ReturnStatement statement )
		{
			assertStatementAuthorized( statement );
			statement.getExpression().visit( this );
		}
		
		@Override
		public void visitAssertStatement( final AssertStatement statement )
		{
			assertStatementAuthorized( statement );
			statement.getBooleanExpression().visit( this );
			statement.getMessageExpression().visit( this );
		}
		
		@Override
		public void visitTryCatchFinally( final TryCatchStatement statement )
		{
			assertStatementAuthorized( statement );
			statement.getTryStatement().visit( this );
			for ( CatchStatement catchStatement : statement.getCatchStatements() )
			{
				catchStatement.visit( this );
			}
			Statement finallyStatement = statement.getFinallyStatement();
			if ( finallyStatement instanceof EmptyStatement )
			{
				// dispatching to EmptyStatement will not call back visitor,
				// must call our visitEmptyStatement explicitly
				visitEmptyStatement( ( EmptyStatement ) finallyStatement );
			}
			else
			{
				finallyStatement.visit( this );
			}
		}
		
		protected void visitEmptyStatement( EmptyStatement statement )
		{
			// noop
		}
		
		@Override
		public void visitSwitch( final SwitchStatement statement )
		{
			assertStatementAuthorized( statement );
			statement.getExpression().visit( this );
			for ( CaseStatement caseStatement : statement.getCaseStatements() )
			{
				caseStatement.visit( this );
			}
			statement.getDefaultStatement().visit( this );
		}
		
		@Override
		public void visitCaseStatement( final CaseStatement statement )
		{
			assertStatementAuthorized( statement );
			statement.getExpression().visit( this );
			statement.getCode().visit( this );
		}
		
		@Override
		public void visitBreakStatement( final BreakStatement statement )
		{
			assertStatementAuthorized( statement );
		}
		
		@Override
		public void visitContinueStatement( final ContinueStatement statement )
		{
			assertStatementAuthorized( statement );
		}
		
		@Override
		public void visitThrowStatement( final ThrowStatement statement )
		{
			assertStatementAuthorized( statement );
			statement.getExpression().visit( this );
		}
		
		@Override
		public void visitSynchronizedStatement( final SynchronizedStatement statement )
		{
			assertStatementAuthorized( statement );
			statement.getExpression().visit( this );
			statement.getCode().visit( this );
		}
		
		@Override
		public void visitCatchStatement( final CatchStatement statement )
		{
			assertStatementAuthorized( statement );
			statement.getCode().visit( this );
		}
		
		@Override
		public void visitMethodCallExpression( final MethodCallExpression call )
		{
			try
			{
				assertExpressionAuthorized( call );
				Expression receiver = call.getObjectExpression();
				final String typeName = receiver.getType().getName();
				if ( receiversWhiteList != null && !receiversWhiteList.contains( typeName ) )
				{
					throw new SandboxSecurityException( "Method calls not allowed on [" + typeName + "]" );
				}
				else if ( receiversBlackList != null && receiversBlackList.contains( typeName ) )
				{
					throw new SandboxSecurityException( "Method calls not allowed on [" + typeName + "]" );
				}
				receiver.visit( this );
				final Expression method = call.getMethod();
				checkConstantTypeIfNotMethodNameOrProperty( method );
				
				call.getArguments().visit( this );
			}
			catch ( SandboxSecurityException e )
			{
				if ( e.getLineNumber() < 0 )
				{
					e.setLineNumber( call.getArguments().getLineNumber(), call.getArguments().getColumnNumber() );
					e.setMethodName( call.getMethodAsString() );
				}
				throw e;
			}
		}
		
		@Override
		public void visitStaticMethodCallExpression( final StaticMethodCallExpression call )
		{
			assertExpressionAuthorized( call );
			final String typeName = call.getOwnerType().getName();
			if ( receiversWhiteList != null && !receiversWhiteList.contains( typeName ) )
			{
				throw new SandboxSecurityException( "Method calls not allowed on [" + typeName + "]" );
			}
			else if ( receiversBlackList != null && receiversBlackList.contains( typeName ) )
			{
				throw new SandboxSecurityException( "Method calls not allowed on [" + typeName + "]" );
			}
			call.getArguments().visit( this );
		}
		
		@Override
		public void visitConstructorCallExpression( final ConstructorCallExpression call )
		{
			assertExpressionAuthorized( call );
			call.getArguments().visit( this );
		}
		
		@Override
		public void visitTernaryExpression( final TernaryExpression expression )
		{
			assertExpressionAuthorized( expression );
			expression.getBooleanExpression().visit( this );
			expression.getTrueExpression().visit( this );
			expression.getFalseExpression().visit( this );
		}
		
		@Override
		public void visitShortTernaryExpression( final ElvisOperatorExpression expression )
		{
			assertExpressionAuthorized( expression );
			visitTernaryExpression( expression );
		}
		
		@Override
		public void visitBinaryExpression( final BinaryExpression expression )
		{
			assertExpressionAuthorized( expression );
			assertTokenAuthorized( expression.getOperation() );
			expression.getLeftExpression().visit( this );
			expression.getRightExpression().visit( this );
		}
		
		@Override
		public void visitPrefixExpression( final PrefixExpression expression )
		{
			assertExpressionAuthorized( expression );
			assertTokenAuthorized( expression.getOperation() );
			expression.getExpression().visit( this );
		}
		
		@Override
		public void visitPostfixExpression( final PostfixExpression expression )
		{
			assertExpressionAuthorized( expression );
			assertTokenAuthorized( expression.getOperation() );
			expression.getExpression().visit( this );
		}
		
		@Override
		public void visitBooleanExpression( final BooleanExpression expression )
		{
			assertExpressionAuthorized( expression );
			expression.getExpression().visit( this );
		}
		
		@Override
		public void visitClosureExpression( final ClosureExpression expression )
		{
			assertExpressionAuthorized( expression );
			if ( !isClosuresAllowed )
				throw new SandboxSecurityException( "Closures are not allowed" );
			expression.getCode().visit( this );
		}
		
		@Override
		public void visitTupleExpression( final TupleExpression expression )
		{
			assertExpressionAuthorized( expression );
			visitListOfExpressions( expression.getExpressions() );
		}
		
		@Override
		public void visitMapExpression( final MapExpression expression )
		{
			assertExpressionAuthorized( expression );
			visitListOfExpressions( expression.getMapEntryExpressions() );
		}
		
		@Override
		public void visitMapEntryExpression( final MapEntryExpression expression )
		{
			assertExpressionAuthorized( expression );
			expression.getKeyExpression().visit( this );
			expression.getValueExpression().visit( this );
		}
		
		@Override
		public void visitListExpression( final ListExpression expression )
		{
			assertExpressionAuthorized( expression );
			visitListOfExpressions( expression.getExpressions() );
		}
		
		@Override
		public void visitRangeExpression( final RangeExpression expression )
		{
			assertExpressionAuthorized( expression );
			expression.getFrom().visit( this );
			expression.getTo().visit( this );
		}
		
		@Override
		public void visitPropertyExpression( final PropertyExpression expression )
		{
			assertExpressionAuthorized( expression );
			Expression receiver = expression.getObjectExpression();
			final String typeName = receiver.getType().getName();
			if ( receiversWhiteList != null && !receiversWhiteList.contains( typeName ) )
			{
				throw new SandboxSecurityException( "Property access not allowed on [" + typeName + "]" );
			}
			else if ( receiversBlackList != null && receiversBlackList.contains( typeName ) )
			{
				throw new SandboxSecurityException( "Property access not allowed on [" + typeName + "]" );
			}
			receiver.visit( this );
			final Expression property = expression.getProperty();
			checkConstantTypeIfNotMethodNameOrProperty( property );
		}
		
		private void checkConstantTypeIfNotMethodNameOrProperty( final Expression expr )
		{
			if ( expr instanceof ConstantExpression )
			{
				if ( !"java.lang.String".equals( expr.getType().getName() ) )
				{
					expr.visit( this );
				}
			}
			else
			{
				expr.visit( this );
			}
		}
		
		@Override
		public void visitAttributeExpression( final AttributeExpression expression )
		{
			assertExpressionAuthorized( expression );
			Expression receiver = expression.getObjectExpression();
			final String typeName = receiver.getType().getName();
			if ( receiversWhiteList != null && !receiversWhiteList.contains( typeName ) )
			{
				throw new SandboxSecurityException( "Attribute access not allowed on [" + typeName + "]" );
			}
			else if ( receiversBlackList != null && receiversBlackList.contains( typeName ) )
			{
				throw new SandboxSecurityException( "Attribute access not allowed on [" + typeName + "]" );
			}
			receiver.visit( this );
			final Expression property = expression.getProperty();
			checkConstantTypeIfNotMethodNameOrProperty( property );
		}
		
		@Override
		public void visitFieldExpression( final FieldExpression expression )
		{
			assertExpressionAuthorized( expression );
		}
		
		@Override
		public void visitMethodPointerExpression( final MethodPointerExpression expression )
		{
			assertExpressionAuthorized( expression );
			expression.getExpression().visit( this );
			expression.getMethodName().visit( this );
		}
		
		@Override
		public void visitConstantExpression( final ConstantExpression expression )
		{
			assertExpressionAuthorized( expression );
			final String type = expression.getType().getName();
			if ( constantTypesWhiteList != null && !constantTypesWhiteList.contains( type ) )
			{
				throw new SandboxSecurityException( "Constant expression type [" + type + "] is not allowed" );
			}
			if ( constantTypesBlackList != null && constantTypesBlackList.contains( type ) )
			{
				throw new SandboxSecurityException( "Constant expression type [" + type + "] is not allowed" );
			}
		}
		
		@Override
		public void visitClassExpression( final ClassExpression expression )
		{
			assertExpressionAuthorized( expression );
		}
		
		@Override
		public void visitVariableExpression( final VariableExpression expression )
		{
			assertExpressionAuthorized( expression );
			final String type = expression.getType().getName();
			if ( constantTypesWhiteList != null && !constantTypesWhiteList.contains( type ) )
			{
				throw new SandboxSecurityException( "Usage of variables of type [" + type + "] is not allowed" );
			}
			if ( constantTypesBlackList != null && constantTypesBlackList.contains( type ) )
			{
				throw new SandboxSecurityException( "Usage of variables of type [" + type + "] is not allowed" );
			}
		}
		
		@Override
		public void visitDeclarationExpression( final DeclarationExpression expression )
		{
			assertExpressionAuthorized( expression );
			visitBinaryExpression( expression );
		}
		
		protected void visitListOfExpressions( List<? extends Expression> list )
		{
			if ( list == null )
				return;
			for ( Expression expression : list )
			{
				if ( expression instanceof SpreadExpression )
				{
					Expression spread = ( ( SpreadExpression ) expression ).getExpression();
					spread.visit( this );
				}
				else
				{
					expression.visit( this );
				}
			}
		}
		
		@Override
		public void visitGStringExpression( final GStringExpression expression )
		{
			assertExpressionAuthorized( expression );
			visitListOfExpressions( expression.getStrings() );
			visitListOfExpressions( expression.getValues() );
		}
		
		@Override
		public void visitArrayExpression( final ArrayExpression expression )
		{
			assertExpressionAuthorized( expression );
			visitListOfExpressions( expression.getExpressions() );
			visitListOfExpressions( expression.getSizeExpression() );
		}
		
		@Override
		public void visitSpreadExpression( final SpreadExpression expression )
		{
			assertExpressionAuthorized( expression );
			expression.getExpression().visit( this );
		}
		
		@Override
		public void visitSpreadMapExpression( final SpreadMapExpression expression )
		{
			assertExpressionAuthorized( expression );
			expression.getExpression().visit( this );
		}
		
		@Override
		public void visitNotExpression( final NotExpression expression )
		{
			assertExpressionAuthorized( expression );
			expression.getExpression().visit( this );
		}
		
		@Override
		public void visitUnaryMinusExpression( final UnaryMinusExpression expression )
		{
			assertExpressionAuthorized( expression );
			expression.getExpression().visit( this );
		}
		
		@Override
		public void visitUnaryPlusExpression( final UnaryPlusExpression expression )
		{
			assertExpressionAuthorized( expression );
			expression.getExpression().visit( this );
		}
		
		@Override
		public void visitBitwiseNegationExpression( final BitwiseNegationExpression expression )
		{
			assertExpressionAuthorized( expression );
			expression.getExpression().visit( this );
		}
		
		@Override
		public void visitCastExpression( final CastExpression expression )
		{
			assertExpressionAuthorized( expression );
			expression.getExpression().visit( this );
		}
		
		@Override
		public void visitArgumentlistExpression( final ArgumentListExpression expression )
		{
			assertExpressionAuthorized( expression );
			visitTupleExpression( expression );
		}
		
		@Override
		public void visitClosureListExpression( final ClosureListExpression closureListExpression )
		{
			assertExpressionAuthorized( closureListExpression );
			if ( !isClosuresAllowed )
				throw new SandboxSecurityException( "Closures are not allowed" );
			visitListOfExpressions( closureListExpression.getExpressions() );
		}
		
		@Override
		public void visitBytecodeExpression( final BytecodeExpression expression )
		{
			assertExpressionAuthorized( expression );
		}
	}
	
	/**
	 * This interface allows the user to plugin custom expression checkers if expression blacklist or whitelist are not
	 * sufficient
	 */
	public interface ExpressionChecker
	{
		boolean isAuthorized( Expression expression );
	}
	
	/**
	 * This interface allows the user to plugin custom statement checkers if statement blacklist or whitelist are not
	 * sufficient
	 */
	public interface StatementChecker
	{
		boolean isAuthorized( Statement expression );
	}
}
