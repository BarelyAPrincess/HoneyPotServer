/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Used for when multiple exceptions were thrown
 */
public class MultipleException extends Exception
{
	private static final long serialVersionUID = -659541886519281396L;

	private final List<ExceptionContext> exceptions = new ArrayList<>();

	public MultipleException( List<ExceptionContext> exceptions )
	{
		for ( ExceptionContext e : exceptions )
			if ( ! ( e instanceof Throwable ) )
				throw new IllegalArgumentException( "IException must be implemented on Throwables only, this is a serious programming bug!" );

		this.exceptions.addAll( exceptions );
	}

	public List<ExceptionContext> getExceptions()
	{
		return Collections.unmodifiableList( exceptions );
	}
}
