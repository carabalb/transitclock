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

package org.transitime.configData;

import org.transitime.config.BooleanConfigValue;
import org.transitime.config.IntegerConfigValue;
import org.transitime.config.StringConfigValue;

/**
 * Config params for database
 *
 * @author SkiBu Smith
 *
 */
public class MongoDbSetupConfig {
	public static String getMongoHost(){ return mongoHost.getValue(); }
	private static StringConfigValue mongoHost = new StringConfigValue(
			"transitime.cache.tripDataHistory.mongo.host", "localhost",
			"Host name for mongo database connection");

	public static Integer getMongoPort(){ return mongoPort.getValue(); }
	private static IntegerConfigValue mongoPort = new IntegerConfigValue(
			"transitime.cache.tripDataHistory.mongo.port", new Integer(27017),
			"Host name for mongo database connection");


	public static String getMongoUsername(){ return mongoUsername.getValue(); }
	private static StringConfigValue mongoUsername = new StringConfigValue(
			"transitime.cache.tripDataHistory.mongo.username", null,
			"Mongo DB connection username");

	public static String getMongoPassword(){ return mongoPassword.getValue(); }
	private static StringConfigValue mongoPassword = new StringConfigValue(
			"transitime.cache.tripDataHistory.mongo.password", null,
			"Mongo DB connection password", false);

	public static String getMongoDbName(){ return mongoDatabaseName.getValue(); }
	private static StringConfigValue mongoDatabaseName = new StringConfigValue(
			"transitime.cache.tripDataHistory.mongo.databaseName", "transitclock",
			"Database to connect to in mongo db");

	public static boolean getMongoSslEnabled() { return mongoSslEnabled.getValue(); }
	private static BooleanConfigValue mongoSslEnabled = new BooleanConfigValue(
			"transitime.cache.tripDataHistory.mongo.sslEnabled", false,
			"Use SSL to connect to mongo db");

	public static boolean getInvalidHostnameAllowed() { return mongoSslInvalidHostnameAllowed.getValue(); }
	private static BooleanConfigValue mongoSslInvalidHostnameAllowed = new BooleanConfigValue(
			"transitime.cache.tripDataHistory.mongo.sslInvalidHostnameAllowed", true,
			"Allow SSL connection to mongo db with invalid hostname");
}
