package de.uni_koeln.spinfo.ang.preprocess;

public class Patterns {
	
	public static final String TWITTER_MENTION 				= "(?<=^|(?<=[^a-zA-Z0-9-\\.]))@([A-Za-z0-9_]+)([^a-zA-Z0-9_]|$)";
	public static final String TWITTER_HASHTAG 				= "(?<=^|(?<=[^a-zA-Z0-9-_\\.]))#([A-Za-z]+[A-Za-z0-9_]+)";
	public static final String PATTERN_TWITTER_RETWEET 		= "RT\\s" + TWITTER_MENTION + "\\s";
	public static final String PATTERN_UNICODES				= "\\\\u(?=[a-fA-F0-9]{4})";
	public static final String PATTERN_HAS_LATIN_CHARS		= ".*\\p{L}+.*";
	public static final String PATTERN_URL					= "(http|https|ftp)\\:\\/\\/[^\\s$]+(?=(\\s|$))";
	public static final String PATTERN_JSON_OBJECT 			= "^\\{.*\\}$";
	public static final String PATTERN_JSON_OBJECT_LANG_DE 	= "^.*\\\"lang\\\"\\:\\\"de\\\".*$";
	public static final String PATTERN_WORD_BOUNDARY 		= "\\P{L}";
	public static final String PATTERN_INCOMPLETE_UNICODE 	= "\\\\u[0-9A-Fa-f]{0,3}[^0-9A-Fa-f]";
	public static final String PATTERN_CONTROL_CHARS	 	= "[\\x11-\\x14]";
	

}
