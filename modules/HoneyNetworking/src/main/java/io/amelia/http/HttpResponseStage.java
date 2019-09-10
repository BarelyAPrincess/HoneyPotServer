/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http;

public enum HttpResponseStage
{
	READING( 0 ),
	WRITING( 1 ),
	WRITTEN( 2 ),
	CLOSED( 3 ),
	MULTIPART( 4 );

	private final int stageId;

	HttpResponseStage( int id )
	{
		stageId = id;
	}

	public int getId()
	{
		return stageId;
	}
}
