package com.marchnetworks.health.dao;

import com.marchnetworks.health.search.AlertSearchQuery;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

public class SearchQueryUtils
{
	public static void setCriteriaForQuery( Criteria criteria, AlertSearchQuery query )
	{
		if ( query.getCategories() != null )
		{
			criteria.add( Restrictions.in( "category", query.getCategories() ) );
		}

		long startTime = query.getStartTime();
		long stopTime = query.getStopTime();
		if ( query.getUseTimePeriod() )
		{
			stopTime = System.currentTimeMillis();
			startTime = stopTime - query.getLastPeriod();
		}

		if ( query.getTimeField() != null )
		{
			switch ( query.getTimeField() )
			{
				case ALERT_TIME:
					criteria.add( Restrictions.disjunction().add( Restrictions.between( "alertTime", Long.valueOf( startTime ), Long.valueOf( stopTime ) ) ).add( Restrictions.between( "lastInstanceTime", Long.valueOf( startTime ), Long.valueOf( stopTime ) ) ) );

					break;

				case ALERT_RESOLVED_TIME:
					criteria.add( Restrictions.ge( "alertResolvedTime", Long.valueOf( startTime ) ) );
					criteria.add( Restrictions.le( "alertResolvedTime", Long.valueOf( stopTime ) ) );
					break;

				case USER_CLOSED_AT:
					criteria.add( Restrictions.ge( "lastUserStateChangedTime", Long.valueOf( startTime ) ) );
					criteria.add( Restrictions.le( "lastUserStateChangedTime", Long.valueOf( stopTime ) ) );
			}
		}
	}
}
