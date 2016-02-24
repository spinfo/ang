package de.uni_koeln.spinfo.ang.util;

import de.uni_koeln.spinfo.ang.preprocess.Patterns;

public class FormatConvert {
	
	public static int monthWordShortToNumber(String month){
		month = month.toUpperCase();
		if		(month.equals("JAN")) return 1;
		else if (month.equals("FEB")) return 2;
		else if (month.equals("MAR")) return 3;
		else if (month.equals("APR")) return 4;
		else if (month.equals("MAY")) return 5;
		else if (month.equals("JUN")) return 6;
		else if (month.equals("JUL")) return 7;
		else if (month.equals("AUG")) return 8;
		else if (month.equals("SEP")) return 9;
		else if (month.equals("OKT")) return 10;
		else if (month.equals("NOV")) return 11;
		else if (month.equals("DEZ")) return 12;
		else return -1;
	}
	
	public static int yearFromTwitterDateString(String date){
		return Integer.parseInt(
				date.replaceAll(".+(?=" + Patterns.DATE_YEAR + ")", "")
					.replaceAll("(?<=" + Patterns.DATE_YEAR + ").+", ""));
	}
	
	public static int monthFromTwitterDateString(String date){
		return monthWordShortToNumber(
				date.replaceAll(".+(?=" + Patterns.DATE_MONTH_WORD_SHORT + ")", "")
					.replaceAll("(?<=" + Patterns.DATE_MONTH_WORD_SHORT + ").+", ""));
	}

}
