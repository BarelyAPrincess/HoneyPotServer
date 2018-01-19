package io.amelia.foundation.binding;

public class Facades
{
	/**
	 * Queries for a facade registration. This will return if no facade has been registered.
	 *
	 * @param <T>    The facade interface
	 * @param facade The facade interface
	 *
	 * @return provider registration or null
	 /
	@SuppressWarnings( "unchecked" )
	public static <T extends FacadeService> Optional<RegisteredFacade<? extends FacadeService>> getFacadeRegistration( @Nonnull Class<T> facade )
	{
		synchronized ( providers )
		{
			return providers.computeIfAbsent( facade, ( k ) -> new ArrayList<>() ).stream().findFirst();
		}
	}

	/**
	 * Get registrations for facade class.
	 *
	 * @param <T>    The facade
	 * @param facade The facade class
	 *
	 * @return a stream of registrations
	 /
	@SuppressWarnings( "unchecked" )
	public static <T extends FacadeService> Stream<RegisteredFacade<T>> getFacadeRegistrations( @Nonnull Class<T> facade )
	{
		synchronized ( providers )
		{
			return providers.computeIfAbsent( facade, ( k ) -> new ArrayList<>() ).stream().map( r -> ( RegisteredFacade<T> ) r );
		}
	}

	@SuppressWarnings( "unchecked" )
	public static <T extends FacadeService> Stream<T> getFacades( @Nonnull Class<?> service ) throws ClassCastException
	{
		synchronized ( providers )
		{
			return providers.entrySet().stream().filter( e -> e.getKey().isAssignableFrom( service ) ).flatMap( e -> e.getValue().stream() ).map( p -> ( T ) p.getInstance() );
		}
	}

	public static Set<Class<? extends FacadeService>> getKnownFacades()
	{
		return providers.keySet();
	}

	public static <R, T extends FacadeService> R ifFacadePresent( Class<T> serviceClass, Function<T, R> consumer )
	{
		return getFacadeRegistration( serviceClass ).map( registeredFacade -> consumer.apply( ( T ) registeredFacade.getInstance() ) ).orElse( null );
	}

	public static void init()
	{
		// Load Builtin Facades First
		// registerFacadeBinding( FacadeService.class, FacadePriority.STRICT, FacadeService::new );

		// Load Facades from Config
		ConfigMap facades = ConfigRegistry.getChild( Foundation.ConfigKeys.BINDINGS_FACADES );

		facades.getChildren().forEach( c -> {
			if ( c.hasChild( "class" ) )
			{
				Class<FacadeService> facadeClass = c.getStringAsClass( "class", FacadeService.class ).orElse( null );
				FacadePriority priority = c.getEnum( "priority", FacadePriority.class ).orElse( FacadePriority.NORMAL );

				if ( facadeClass != null )
					registerFacadeBinding( facadeClass, priority, () -> Objs.initClass( facadeClass ) );
				else
					Foundation.L.warning( "We found malformed arguments in the facade config for key -> " + c.getName() );
			}
			else
				Foundation.L.warning( "We found malformed arguments in the facade config for key -> " + c.getName() );
		} );
	}

	/**
	 * Returns whether a facade has been registered
	 *
	 * @param <T>     facade
	 * @param service facade to check
	 *
	 * @return true if and only if the facade is registered
	 /
	public static <T> boolean isFacadeRegistered( @Nonnull Class<T> service )
	{
		synchronized ( providers )
		{
			return providers.containsKey( service );
		}
	}

	public static <T extends FacadeService> void registerFacadeBinding( Class<T> facadeClass, FacadePriority priority, Supplier<T> serviceSupplier )
	{
		RegisteredFacade<T> registeredFacade = new RegisteredFacade<>( facadeClass, priority, serviceSupplier );

		synchronized ( providers )
		{
			List<RegisteredFacade<? extends FacadeService>> priorityList = providers.computeIfAbsent( facadeClass, ( k ) -> new ArrayList<>() );

			// Insert the provider into the collection, much more efficient big O than sort
			int position = Collections.binarySearch( priorityList, registeredFacade );
			if ( position < 0 )
				priorityList.add( -( position + 1 ), registeredFacade );
			else
				priorityList.add( position, registeredFacade );
		}

		Events.callEvent( new FacadeRegisterEvent<T>( registeredFacade ) );
	}

	public static class RegisteredFacade<T extends FacadeService> implements Comparable<RegisteredFacade<?>>
	{
		private final Class<T> facadeClass;
		private final FacadePriority priority;
		private final Supplier<T> supplier;
		private T instance = null;

		public RegisteredFacade( @Nonnull Class<T> facadeClass, @Nonnull FacadePriority priority, @Nonnull Supplier<T> supplier )
		{
			this.facadeClass = facadeClass;
			this.supplier = supplier;
			this.priority = priority;
		}

		@Override
		public int compareTo( RegisteredFacade<?> o )
		{
			if ( priority.ordinal() == o.getPriority().ordinal() )
				return 0;
			else
				return priority.ordinal() < o.getPriority().ordinal() ? 1 : -1;
		}

		public void destoryInstance() throws ApplicationException.Error
		{
			// TODO Run exception through exception handler
			if ( instance != null )
				instance.destroy();
			instance = null;
		}

		public T getInstance()
		{
			if ( instance == null )
				instance = supplier.get();
			return instance;
		}

		public FacadePriority getPriority()
		{
			return priority;
		}

		public Class<T> getBindingClass()
		{
			return facadeClass;
		}
	}*/
}
