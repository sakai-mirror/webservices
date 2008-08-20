package org.sakaiproject.simpleLTI;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper class for the LTILaunch.java web service. Helps out with checking validity of date strings.
 * @author katieedwards
 *
 */
public class TimeCalculator {
	
	public static SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-ddHH:mm:ssZ");
	private static final int MS_PER_DAY = (24 * 60 * 60 *1000);

	/**
	 * 
	 * @param iso8601dateString Should look like 2008-06-0313:51:20Z
	 * @return 
	 */
	public static Date parseDate(String iso8601dateString) {
		int tee = iso8601dateString.indexOf('T');
		int zed = iso8601dateString.indexOf('Z');
		
		String pattern = iso8601dateString.substring(0, tee) + iso8601dateString.substring(tee+1, zed) + "GMT";
		
		Date date = null;
		try {
			date = iso8601Format.parse(pattern);
		}
		catch(ParseException e) {}
		return date;
		
	}
	
	/**
	 * 
	 * @param iso8601dateString Should look like 2008-06-03T13:51:20Z
	 * @return The difference (in milliseconds) between the time defined by 
	 * 			iso8601dateString and the current time. Positive if the current
	 * 			time comes after the time described by iso8601dateString
	 */
	public static long getTimeDifference(String iso8601dateString) {
		Date other = parseDate(iso8601dateString);

		return other.getTime() - System.currentTimeMillis();
	}
	
	public static boolean within2Days(String iso8601dateString) {
		return Math.abs(getTimeDifference(iso8601dateString)) < 2*MS_PER_DAY;
	}
	
	
	public static void main(String[] args) {
		
		
		long cur = (System.currentTimeMillis()/1000)*1000;	//adjust for lack of milliseconds
		Date d = new Date(cur);
		String teststring = iso8601Format.format(cur);
		teststring = teststring.substring(0, 10) + "T" + teststring.substring(10,18) + "Z";
		
		System.out.println(teststring);
		System.out.println(getTimeDifference(teststring)/60000 + "minutes from gmt");
		System.out.println(cur - System.currentTimeMillis());
		
		String far = "2008-06-03T13:51:20Z";
		String close = "2008-06-20T13:51:20Z";
		
		System.out.println("MS away from close date: " + getTimeDifference(close));
		System.out.println("Minutes: " + (getTimeDifference(close) / 60000));
		System.out.println("close date is close: " + within2Days(close));
		
		System.out.println("MS away from far date: " + getTimeDifference(far));
		System.out.println("Minutes: " + (getTimeDifference(far) / 60000));
		System.out.println("far date is close: " + within2Days(far));
	}

}
