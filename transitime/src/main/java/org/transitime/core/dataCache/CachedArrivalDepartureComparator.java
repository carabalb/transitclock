package org.transitime.core.dataCache;

import java.util.Comparator;

public class CachedArrivalDepartureComparator implements Comparator<ITripHistoryArrivalDeparture> {
	

	@Override
	public int compare(ITripHistoryArrivalDeparture ad1, ITripHistoryArrivalDeparture ad2) {
		
		if(ad1.getDate().getTime()<ad2.getDate().getTime())
		{
			 return 1;
		}else if(ad1.getDate().getTime()> ad2.getDate().getTime())
		{
			 return -1;
		}
		return 0;
	}
}
