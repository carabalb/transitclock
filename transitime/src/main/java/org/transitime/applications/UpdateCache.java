package org.transitime.applications;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.transitime.config.ConfigFileReader;
import org.transitime.configData.CoreConfig;
import org.transitime.core.dataCache.factory.StopArrivalDepartureCacheFactory;
import org.transitime.core.dataCache.factory.TripDataHistoryCacheFactory;
import org.transitime.db.hibernate.HibernateUtils;

import java.util.Calendar;
import java.util.Date;

public class UpdateCache {

    // Read in configuration files. This should be done statically before
    // the logback LoggerFactory.getLogger() is called so that logback can
    // also be configured using a transitime config file. The files are
    // specified using the java system property -Dtransitime.configFiles .
    static {
        ConfigFileReader.processConfig();
    }

    private static final Logger logger =
            LoggerFactory.getLogger(UpdateCache.class);

    private static void fillHistoricalCaches() {
        Session session = HibernateUtils.getSession();

        Date endDate= Calendar.getInstance().getTime();
        Date initialEndDate = new Date(endDate.getTime());

        /* populate one day at a time to avoid memory issue */
        for(int i=0;i<CoreConfig.getDaysPopulateHistoricalCache();i++)
        {
            Date startDate= DateUtils.addDays(endDate, -1);
            TripDataHistoryCacheFactory.getInstance().populateCacheFromDb(session, startDate, endDate);
            endDate=startDate;
        }
        TripDataHistoryCacheFactory.getInstance().saveCacheHistoryRecord(endDate, initialEndDate);

        endDate=Calendar.getInstance().getTime();
        initialEndDate = new Date(endDate.getTime());

        /* populate one day at a time to avoid memory issue */
        for(int i=0;i<CoreConfig.getDaysPopulateHistoricalCache();i++)
        {
            Date startDate=DateUtils.addDays(endDate, -1);
            StopArrivalDepartureCacheFactory.getInstance().populateCacheFromDb(session, startDate, endDate);
            endDate=startDate;
        }
        StopArrivalDepartureCacheFactory.getInstance().saveCacheHistoryRecord(endDate, initialEndDate);
    }


    /**
     * The main program that runs the entire Transitime application.!
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            fillHistoricalCaches();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

}


