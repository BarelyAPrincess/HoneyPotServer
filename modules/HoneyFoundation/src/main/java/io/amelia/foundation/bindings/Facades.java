/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.binding;

public class Facades
{
	/*public static Set<Class<? extends FacadeService>> getKnownFacades()
	{
		return providers.keySet();
	}

	public static <R, T extends FacadeService> R ifFacadePresent( Class<T> serviceClass, Function<T, R> consumer )
	{
		return getFacadeRegistration( serviceClass ).map( registeredFacade -> consumer.apply( ( T ) registeredFacade.getInstance() ) ).orElse( null );
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

	 @Override public int compareTo( RegisteredFacade<?> o )
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
