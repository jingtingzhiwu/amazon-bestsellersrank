package com.saina.amazon.config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AmazonConfig {

	public static enum AmazonServiceUrl {
		CA("https://www.amazon.ca", "Canada/Central"),
		MX("https://www.amazon.com.mx", "Mexico/General"),
		US("https://www.amazon.com", "EST"),
		BR("https://www.amazon.com.br", "Brazil/East"), 
		DE("https://www.amazon.de", "Europe/Berlin"), 
		ES("https://www.amazon.es", "Europe/Madrid"), 
		FR("https://www.amazon.fr", "Europe/Paris"), 
		IT("https://www.amazon.it", "CET"),
		UK("https://www.amazon.co.uk", "GMT"), 
		IN("https://www.amazon.in", "IST"),
		CN("https://www.amazon.cn", "Asia/Shanghai"),
		AU("https://www.amazon.com.au", "Australia/Sydney"), 
		JP("https://www.amazon.co.jp", "JST"),;
		protected String value;
		protected String timezone;

		private AmazonServiceUrl(String value, String timezone) {
			this.value = value;
			this.timezone = timezone;
		}

		public String getTimezone() {
			return timezone;
		}

		public void setTimezone(String timezone) {
			this.timezone = timezone;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}
	
	/**
	 * 本地时区-目标时区 = 时区偏移量
	 * @param site
	 * @param days	，天数
	 * @param 60 * 60 * 24 * 1000 millis偏移量
	 * @return
	 */
	public static Date toLocaleDiffDays(AmazonServiceUrl site, int days) {
		int diff = TimeZone.getDefault().getRawOffset() - TimeZone.getTimeZone(site.getTimezone()).getRawOffset();
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(c.getTimeInMillis() - diff - (days * 60 * 60 * 24 * 1000));
		return c.getTime();
	}
	
	public static Date toLocaleDate(AmazonServiceUrl site, String date) {
		SimpleDateFormat sdf = null;
		date = date.replaceAll("[\\,\\.]", "");

		switch (site) {
		case CA:
			date = date.substring(3);
			sdf = new SimpleDateFormat("MMMM d yyyy", Locale.CANADA);
			break;
		case MX:
			date = date.substring(3);
			date = date.replaceAll("\\bde \\b", "");
			sdf = new SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("ES"));
			break;
		case US:
			date = date.substring(3);
			sdf = new SimpleDateFormat("MMMM d yyyy", Locale.US);
			break;
		case BR:
			date = date.substring(3);
			date = date.replaceAll("\\bde \\b", "");
			sdf = new SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("pt-BR"));
			break;
		case DE:
			date = date.substring(3);
			sdf = new SimpleDateFormat("a d MMMM yyyy", Locale.GERMANY);
			break;
		case ES:
			date = date.substring(3);
			date = date.replaceAll("\\bde \\b", "");
			sdf = new SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("ES"));
			break;
		case FR:
			date = date.substring(3);
			sdf = new SimpleDateFormat("d MMMM yyyy", Locale.FRENCH);
			break;
		case IT:
			date = date.substring(3);
			sdf = new SimpleDateFormat("d MMMM yyyy", Locale.ITALY);
			break;
		case UK:
			date = date.substring(3);
			sdf = new SimpleDateFormat("d MMMM yyyy", Locale.US);
			break;
		case IN:
			date = date.substring(3);
			sdf = new SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("IN"));
			break;
		case CN:
			sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
			break;
		case AU:
			date = date.substring(3);
			sdf = new SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("AU"));
			break;
		case JP:
			sdf = new SimpleDateFormat("yyyy年M月d日", Locale.JAPAN);
			break;

		default:
			sdf = new SimpleDateFormat("d MMMM yyyy", Locale.US);
			break;
		}
		try {
			return sdf.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return new Date();
		}
	}

	/*	public static void main(String[] args) throws ParseException {
	String[] date = new String[] { 
			"2017年3月8日", // 日本、中国
			"am 7. Januar 2018", // de
			"il 6 dicembre 2017", // it
			"on 10 November 2017", // in、au
			"on November 10 2017", // uk、us
			"on April 13, 2017", // ca
			"el 10 de diciembre de 2017", // mx
			"em 6 de janeiro de 2018", // br
			"el 7 de marzo de 2017", // es
			"le 11 août 2017",// fr
	};
//	SimpleDateFormat sdf = new SimpleDateFormat("a d. MMMM yyyy", Locale.GERMANY);
//	SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.JAPAN);
//	SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
//	SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.ITALY);
//	SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.US);
//	SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.CANADA);
//	SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.FRENCH);
//	SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("ES"));
//	SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("pt-BR"));
	
	String datestr = "10 November 2017";
	System.err.println(datestr);
	SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("AU"));
	System.err.println(sdf.parse(datestr));
	System.err.println(sdf.format(new Date()));
	}*/
}
