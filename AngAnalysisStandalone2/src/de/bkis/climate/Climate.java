package de.bkis.climate;
import java.util.HashMap;
import java.util.Map;

public class Climate {

	private Map<String, Argument> args;
	private String prefix;

	public Climate() {
		this("-");
	}
	
	public Climate(String argumentPrefix){
		args = new HashMap<String, Argument>();
		prefix = argumentPrefix;
	}

	public void addArgument(String argument, String value, String description) {
		args.put(argument, new Argument(value, description));
	}
	
	public void addArgument(String argument, String value) {
		args.put(argument, new Argument(value, ""));
	}

	public void addArgument(String argument) {
		addArgument(argument, "", "");
	}
	
	public boolean hasArgument(String argument){
		return args.containsKey(argument);
	}
	
	public String getValue(String argument){
		return args.get(argument).getValue();
	}
	
	public void printHelp(){
		for (String a : args.keySet()){
			System.out.println(prefix + a + "\t" + args.get(a).getDesc());
		}
	}
	
	private class Argument {
		private String value;
		private String desc;
		
		public Argument(String value, String desc) {
			super();
			this.value = value;
			this.desc = desc;
		}

		public String getValue() {
			return value;
		}

		public String getDesc() {
			return desc;
		}
	}

}
