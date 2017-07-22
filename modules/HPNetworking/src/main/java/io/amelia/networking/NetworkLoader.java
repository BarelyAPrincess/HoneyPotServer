package io.amelia.networking;

import io.amelia.config.ConfigRegistry;
import io.amelia.foundation.RegistrarBase;
import io.amelia.lang.NetworkException;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.networking.udp.UDPWorker;
import io.amelia.support.Objs;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;

public class NetworkLoader
{
	public static final RegistrarBase R = new RegistrarBase( NetworkLoader.class );
	public static final Logger L = LogBuilder.get( NetworkLoader.class );
	public static final EventLoopGroup IO_LOOP_GROUP = new NioEventLoopGroup( 250 );
	private static final Map<Class<? extends NetworkWorker>, Worker<? extends NetworkWorker>> workerRefs = new HashMap<>();

	static
	{
		if ( Security.getProvider( "BC" ) == null )
			Security.addProvider( new BouncyCastleProvider() );
	}

	@SuppressWarnings( "unchecked" )
	public static Worker<UDPWorker> UDP()
	{
		return ( Worker<UDPWorker> ) workerRefs.compute( UDPWorker.class, ( k, v ) -> v == null ? initWorker( UDPWorker.class ) : v );
	}

	public static <N extends NetworkWorker> Worker<N> initWorker( Class<N> workerClass )
	{
		return new Worker<>( workerClass );
	}

	public void heartbeat()
	{
		// TODO Heartbeat from task manager

		for ( Worker<? extends NetworkWorker> worker : workerRefs.values() )
			if ( worker.isStarted() )
				worker.get().heartbeat();
	}

	private NetworkLoader()
	{

	}

	public static class Worker<N extends NetworkWorker>
	{
		private N i;

		public Worker( Class<N> workerClass )
		{
			i = Objs.initClass( workerClass );
			NetworkLoader.workerRefs.put( workerClass, this );
		}

		public N get()
		{
			return i;
		}

		public String getId()
		{
			return i.getId();
		}

		public boolean isStarted()
		{
			return i.isStarted();
		}

		protected void shutdown() throws NetworkException
		{
			i.stop();
		}

		public Worker<N> start() throws NetworkException
		{
			i.start( ConfigRegistry.getChild( "config.network." + getId() ) );
			return this;
		}

		public Worker<N> stop() throws NetworkException
		{
			i.stop();
			return this;
		}
	}
}
