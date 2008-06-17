package org.sakaiproject.axis.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeCalculator {
	
	static SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-ddHH:mm:ssZ");

	/**
	 * 
	 * @param iso8601dateString Should look like 2008-06-0313:51:20-04:00
	 * @return 
	 */
	public static Date parseDate(String iso8601dateString) {
		int tee = iso8601dateString.indexOf('T');
		int colon = iso8601dateString.lastIndexOf(':');
		
		String pattern = iso8601dateString.substring(0, tee) + iso8601dateString.substring(tee+1, colon) + iso8601dateString.substring(colon+1);
		
		Date date = null;
		try {
			date = iso8601Format.parse(pattern);
		}
		catch(ParseException e) {}
		return date;
		
	}
	
	/**
	 * 
	 * @param iso8601dateString Should look like 2008-06-03T13:51:20-04:00
	 * @return The difference (in milliseconds) between the time defined by 
	 * 			iso8601dateString and the current time. Positive if the current
	 * 			time comes after the time described by iso8601dateString
	 */
	public static long getTimeDifference(String iso8601dateString) {
		Date other = parseDate(iso8601dateString);
		return other.getTime() - System.currentTimeMillis();
	}
	
	
	public static void main(String[] args) {
		
		
		long cur = (System.currentTimeMillis()/1000)*1000;	//adjust for lack of milliseconds
		Date d = new Date(cur);
		String teststring = iso8601Format.format(cur);
		teststring = teststring.substring(0, 10) + "T" + teststring.substring(10,21) + ":" + teststring.substring(21);
		
		System.out.println(teststring);
		System.out.println(getTimeDifference(teststring));
		System.out.println(cur - System.currentTimeMillis());
		
	}

}
