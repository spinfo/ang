package de.uni_koeln.spinfo.ang.util;

public class Patterns {
	
	public static final String TWITTER_MENTION 			= "(?<=^|(?<=[^a-zA-Z0-9-\\.]))@([A-Za-z0-9_]+)([^a-zA-Z0-9_]|$)";
	public static final String TWITTER_HASHTAG 			= "(?<=^|(?<=[^a-zA-Z0-9-_\\.]))#([A-Za-z]+[A-Za-z0-9_]+)";
	public static final String TWITTER_RETWEET 			= "RT\\s" + TWITTER_MENTION + "\\s";
	public static final String UNICODES					= "\\\\u(?=[a-fA-F0-9]{4})";
	public static final String HAS_LATIN_CHARS			= ".*\\p{L}+.*";
	public static final String URL						= "(http|https|ftp)\\:\\/\\/[^\\s$]+(?=(\\s|$))";
	public static final String JSON_OBJECT 				= "^\\{.*\\}$";
	public static final String JSON_OBJECT_LANG_DE 		= "^.*\\\"lang\\\"\\:\\\"de\\\".*$";
	public static final String WORD_BOUNDARY 			= "\\P{L}";
	public static final String INCOMPLETE_UNICODE 		= "\\\\u[0-9A-Fa-f]{0,3}[^0-9A-Fa-f]";
	public static final String CONTROL_CHARS	 		= "[\\x11-\\x14]";
	public static final String DATE_YEAR		 		= "(18|19|20)\\d\\d";
	public static final String DATE_MONTH_WORD_SHORT	= "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dez)";
	public static final String TWITTER_JSON_FILES		= ".+\\.(json|geo)";

}
