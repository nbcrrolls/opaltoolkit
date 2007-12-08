package edu.sdsc.nbcr.opal.dashboard.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DateHelper {

    static final int MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
    protected static Log log = LogFactory.getLog(DateHelper.class.getName());
    
    /**
     * this function returns (date - 1day)
     */
    public static Date subtractDay(Date date){
        return addDays(date, -1);
    }
    
    /**
     * this function returns (date - numDays)
     */
    public static Date addDays(Date date, int numDays){
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        //log.debug("The date before subctracting one day is: " + date);
        cal1.add(Calendar.DAY_OF_MONTH, numDays);
        //log.debug("The date after subtracting one day is: " + cal1.getTime());
        return cal1.getTime();
    }
    
    /**
     * this functions subctract a month form the date
     * @param date
     * @return
     */
    public static Date subtractMonth(Date date){
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        //log.debug("The date before subctracting one day is: " + date);
        cal1.add(Calendar.MONTH, -1);
        return cal1.getTime();
    }
    
    /**
     * it tries to parse the string date following the format MM/dd/yy
     * it return null if it can not parse the date
     * @param date
     * @return
     */
    public static Date parseDate(String date){
        if ( date == null) return null;
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy");
        Date returnDate = null;
        try {
            returnDate = formatter.parse(date);
        }
        catch (Exception e){
            log.error("Impossible to parse date: " + date, e );
        }
        return returnDate;
    }
    

    public static String formatDate(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy");
        return formatter.format(date);
    }
    
    /**
     * return true if d1 and d1 are on the same day
     * 
     * @param d1
     * @param d2
     * @return
     */
    public static boolean  compareDates(Date d1, Date d2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d2);
        
        if ( (cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)) && 
                ( cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) && 
                (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)) ){
            log.debug("Date comparison with " + d1 + " and " + d2 + " retunring true");
            return true;
        }
        log.debug("Date comparison with " + d1 + " and " + d2 + " retunring false");
        return false;
    }
    
    
    /**
     * this function return the number of days between date1 and date2
     * @param date1
     * @param date2
     * @return
     */
    public static int getOffsetDays(Date date1, Date date2){
        long diff = date1.getTime() - date2.getTime();
        long numberOfDays =  diff / MILLISECONDS_PER_DAY;
        //log.info("the differnce between: " + date1 + " and " + date2 + " is: " + numberOfDays);
        return (int)numberOfDays;
    }

    /**
     * return true if (d1 + 1day) == d2
     * @param d1
     * @param d2
     * @return
     */
    public static boolean isOneDayOffset(Date d1, Date d2){
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);
        cal1.add(Calendar.DAY_OF_MONTH, 1);
        return compareDates(cal1.getTime(), d2);
    }//isOneDayOffset
    
    /**
     * return the default start date that is (today - 31)
     * @return
     */
    public static Date getStartDate(){
        Date toDay = new Date();
        toDay = DateHelper.subtractDay(toDay);
        return DateHelper.subtractMonth(toDay);
    }
    
    /**
     * return the default end date that is yesterday
     * @return
     */
    public static Date getEndDate(){
        return DateHelper.subtractDay(new Date());
    }
    
    /**
     * this should not be here... but I just didn't know where to put it
     * return true if the value is contained in str
     */
    public static boolean containsString(String [] str, String value){
        for ( int i = 0; i < str.length; i++ ) {
            if ( str[i].equals(value) )
                return true;
        }
        return false;
    }
    
}
