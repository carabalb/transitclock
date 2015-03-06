/* 
 * This file is part of Transitime.org
 * 
 * Transitime.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Transitime.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transitime.org .  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transitime.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.applications.Core;
import org.transitime.db.structs.AvlReport;
import org.transitime.db.structs.Block;
import org.transitime.db.structs.Trip;
import org.transitime.gtfs.DbConfig;

/**
 * Singleton class that handles block assignments from AVL feed. 
 * 
 * @author SkiBu Smith
 * 
 */
public class TripAssigner {

	// Singleton class
	private static TripAssigner singleton = new TripAssigner();

	private static final Logger logger =
			LoggerFactory.getLogger(TripAssigner.class);

	/********************** Member Functions **************************/

	/**
	 * Constructor private since singleton class
	 */
	private TripAssigner() {}
	
	/**
	 * Returns the BlockAssigner singleton
	 * 
	 * @return
	 */
	public static TripAssigner getInstance() {
		return singleton;
	}
	
	/**
	 * Gets the appropriate block associated with the AvlReport by getting the
	 * proper serviceId using the AVL timestamp and then determining the
	 * appropriate block using the serviceId and the blockId from the AVL
	 * report. If the blockId not specified in AVL data or the block could not
	 * be found for the serviceId then null will be returned
	 * 
	 * @param avlReport
	 * @return Block corresponding to the time and blockId from AVL report.
	 */
	public Trip getTripAssignment(AvlReport avlReport) {
		// If vehicle has assignment...
		if (avlReport != null && avlReport.getAssignmentId() != null) {
			DbConfig config = Core.getInstance().getDbConfig();

			if (avlReport.isTripIdAssignmentType()) {
				// Using trip ID
				Trip trip = config.getTrip(avlReport.getAssignmentId());
				if (trip != null) {
					Block block = trip.getBlock();
					logger.info("For vehicleId={} the trip assignment from "
							+ "the AVL feed is tripId={} which corresponds to "
							+ "blockId={}", 
							avlReport.getVehicleId(), 
							avlReport.getAssignmentId(), block.getId());
					return trip;
				} else {
					logger.error("For vehicleId={} AVL report specifies " + 
							"assignment tripId={} but that trip is not valid.",
							avlReport.getVehicleId(), 
							avlReport.getAssignmentId());
				}
			} else if (avlReport.isTripShortNameAssignmentType()) {
				// Using trip short name
				String tripShortName = avlReport.getAssignmentId();
				Trip trip = config.getTripUsingTripShortName(tripShortName);
				if (trip != null) {
					Block block = trip.getBlock();
					logger.info("For vehicleId={} the trip assignment from "
							+ "the AVL feed is tripShortName={} which "
							+ "corresponds to blockId={}", 
							avlReport.getVehicleId(), tripShortName, 
							block.getId());
					return trip;
				} else {
					logger.error("For vehicleId={} AVL report specifies "
							+ "assignment tripShortName={} but that trip is not "
							+ "valid.",
							avlReport.getVehicleId(), tripShortName);
				}
			}
		}

		return null;
	}
}
