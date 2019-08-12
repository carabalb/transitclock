package org.transitime.core.dataCache.model;

import org.transitime.core.Indices;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Sean Og Crudden
 * 
 */
public class BlockCacheKey implements java.io.Serializable {

	/**
	 *
	 */
	private String serviceId;
	private String blockId;
	private Date date;


	public BlockCacheKey(String serviceId, String blockId)
	{
		super();
		this.serviceId = serviceId;
		this.blockId = blockId;
		this.date = Calendar.getInstance().getTime();
	}

	public BlockCacheKey(String serviceId, String blockId, Date date)
	{
		super();
		this.serviceId = serviceId;
		this.blockId = blockId;
		this.date = date;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getBlockId() {
		return blockId;
	}

	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "BlockCacheKey{" +
				"serviceId='" + serviceId + '\'' +
				", blockId='" + blockId + '\'' +
				'}';
	}
}




