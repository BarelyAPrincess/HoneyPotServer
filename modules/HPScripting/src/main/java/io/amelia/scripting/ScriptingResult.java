package io.amelia.scripting;

import java.nio.charset.Charset;

import groovy.lang.Script;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.ReportingLevel;
import io.amelia.support.Objs;
import io.netty.buffer.ByteBuf;

/**
 * Contains the end result of {@link ScriptingFactory#eval(ScriptingContext)}
 */
public class ScriptingResult extends ExceptionReport
{
	private final ScriptingContext context;
	private ByteBuf content;
	private Object obj = null;
	private String reason = null;
	private Script script = null;
	private boolean success = false;

	ScriptingResult( ScriptingContext context, ByteBuf content )
	{
		this.context = context;
		this.content = content;
	}

	@Override
	public ScriptingResult addException( IException exception )
	{
		IException.check( exception );
		if ( exception != null )
			if ( exception instanceof ScriptingException )
			{
				// If this EvalException never had it's script trace populated, we handle it here
				if ( !( ( ScriptingException ) exception ).hasScriptTrace() )
					if ( context.factory() != null )
						( ( ScriptingException ) exception ).populateScriptTrace( context.factory().stack() );
					else if ( context.request() != null )
						( ( ScriptingException ) exception ).populateScriptTrace( context.request().getScriptingFactory().stack() );
				caughtExceptions.add( exception );
			}
			else
				super.addException( exception );
		return this;
	}

	@Override
	public ScriptingResult addException( ReportingLevel level, Throwable throwable )
	{
		if ( throwable != null )
			if ( throwable instanceof ScriptingException )
			{
				// If this EvalException never had it's script trace populated, we handle it here
				if ( !( ( ScriptingException ) throwable ).hasScriptTrace() )
					if ( context.factory() != null )
						( ( ScriptingException ) throwable ).populateScriptTrace( context.factory().stack() );
					else if ( context.request() != null )
						( ( ScriptingException ) throwable ).populateScriptTrace( context.request().getScriptingFactory().stack() );
				caughtExceptions.add( ( ScriptingException ) throwable );
			}
			else
				caughtExceptions.add( new ScriptingException( level, throwable ).populateScriptTrace( context.factory().stack() ) );
		return this;
	}

	public ByteBuf content()
	{
		return content;
	}

	public ScriptingContext context()
	{
		return context;
	}

	public IException[] getExceptions()
	{
		return caughtExceptions.toArray( new IException[0] );
	}

	public Object getObject()
	{
		return obj;
	}

	public void setObject( Object obj )
	{
		this.obj = obj;
	}

	public String getReason()
	{
		if ( reason == null || reason.isEmpty() )
			reason = "There was no available result reason at this time.";
		return reason;
	}

	public ScriptingResult setReason( String reason )
	{
		this.reason = reason;
		return this;
	}

	public Script getScript()
	{
		return script;
	}

	public void setScript( Script script )
	{
		this.script = script;
	}

	public String getString()
	{
		return getString( false );
	}

	public String getString( boolean includeObj )
	{
		return ( content == null ? "" : content.toString( Charset.defaultCharset() ) ) + ( includeObj ? Objs.castToString( obj ) : "" );
	}

	public boolean isSuccessful()
	{
		return success;
	}

	public ScriptingResult success( boolean success )
	{
		this.success = success;
		return this;
	}

	@Override
	public String toString()
	{
		return String.format( "EvalFactoryResult{success=%s,reason=%s,size=%s,obj=%s,script=%s,context=%s}", success, reason, content.writerIndex(), obj, script, context );
	}
}
