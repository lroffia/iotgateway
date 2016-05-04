package arces.unibo.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logging {
	private static boolean consoleLog = true;
	private static boolean fileLog = false;
	private static VERBOSITY verbosity = VERBOSITY.INFO;
	
	public static enum VERBOSITY { 
		DEBUG, INFO, WARNING, ERROR, FATAL;
		
		@Override
		public String toString() {
			switch(this){
				case DEBUG: return "DEBUG";
				case INFO: return "INFO";
				case WARNING: return "WARNING";
				case ERROR: return "ERROR";
				case FATAL: return "FATAL";
				default: return "";
			}
		}
	};
	
	public static void enableConsoleLog(){
		consoleLog = true;
	}
	
	public static void disableConsoleLog(){
		consoleLog = false;
	}
	
	public static void enableFileLog(){
		fileLog = true;
	}
	
	public static void disableFileLog(){
		fileLog = false;
	}
	
	public static void setVerbosityLevel(VERBOSITY level) {
		verbosity = level;
	}
	
	public static void log(VERBOSITY level, String tag,String message) {
		int nTab = 20 - tag.length();
		if (!consoleLog && !fileLog) return;
		if (level.compareTo(verbosity) < 0) return; 	
		
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		String timestamp = sdf.format(date);
		
		for (int i=0; i < nTab; i++) tag += " ";
		
		if (consoleLog) System.out.println(timestamp+"\t\t"+level.toString()+"\t\t"+tag+"\t"+message);
	}
}
