package io.amelia.networking;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.amelia.lang.NetworkException;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.networking.udp.UDPWorker;
import io.amelia.support.Objs;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class NetworkLoader
{
	public static final Logger L = LogBuilder.get( NetworkLoader.class );
	public static final EventLoopGroup IO_LOOP_GROUP = new NioEventLoopGroup( 0, Executors.newCachedThreadPool( new ThreadFactoryBuilder().setNameFormat( "Netty Client IO #%d" ).setDaemon( true ).build() ) );
	private static final Map<Class<? extends NetworkWorker>, NetworkWorker> networkWorkers = new ConcurrentHashMap<>();

	static
	{
		if ( Security.getProvider( "BC" ) == null )
			Security.addProvider( new BouncyCastleProvider() );
	}

	public static UDPWorker UDP()
	{
		return getWorker( UDPWorker.class, UDPWorker::new );
	}

	/*
	public static HTTPWorker HTTP()
	{
		return getWorker( HTTPWorker.class, HTTPWorker::new )
	}
	*/

	public static void disposeWorker( Class<? extends NetworkWorker> workerClass ) throws NetworkException
	{
		Objs.ifPresent( networkWorkers.remove( workerClass ), NetworkWorker::stop );
	}

	@SuppressWarnings( "unchecked" )
	public static <N extends NetworkWorker> N getWorker( Class<N> workerClass, Supplier<N> workerSupplier )
	{
		return ( N ) networkWorkers.computeIfAbsent( workerClass, c -> workerSupplier.get() );
	}

	@SuppressWarnings( "unchecked" )
	public static <N extends NetworkWorker> N getWorker( Class<N> workerClass )
	{
		return ( N ) networkWorkers.computeIfAbsent( workerClass, NetworkLoader::initWorker );
	}

	public static void heartbeat()
	{
		for ( NetworkWorker worker : networkWorkers.values() )
			if ( worker.isStarted() )
				worker.heartbeat();
	}

	public static <N extends NetworkWorker> N initWorker( Class<N> workerClass )
	{
		return Objs.initClass( workerClass );
	}

	private NetworkLoader()
	{
		// Static Class
	}
}
