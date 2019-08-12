package org.transitime.core.dataCache.comparator;

import java.util.Comparator;

import org.transitime.db.structs.IArrivalDeparture;

public class ArrivalDepartureComparator implements Comparator<IArrivalDeparture> {
	

	@Override
	public int compare(IArrivalDeparture ad1, IArrivalDeparture ad2) {
		
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
