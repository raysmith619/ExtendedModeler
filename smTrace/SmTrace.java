package smTrace;

import java.io.BufferedWriter;

/**
 * Debug Tracing with logging support
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
 * Facilitate execution tracing / logging of execution.
 * 
 * @author raysm
 *
 */
public class SmTrace {
	private static SmTrace traceObj;

	/**
	 * Logging Support
	 */
	private static String logName;		// Logfile name
	private static BufferedWriter logWriter;
	private static boolean logToScreen = true;		// true - log, additionally to STDOUT
	private static boolean stdOutHasTs = false;		// true - timestamp prefix on STDOUT
	
	/**
	 * Create private constructor
	 */
	private SmTrace() {
		this.traceFlags = new HashMap<String, Integer>();
		// flags added as observed
		// create and load default properties
		this.defaultProps = new Properties();		
	}
	
	
	/**
	 * Setup access via singleton
	 */
	private static void init() {
		if (traceObj == null) {
			traceObj = new SmTrace();
		}
	}
	/**
	 * Shorthands
	 * ALL - forces all trace flags to evaluate at given level.
	 */
	public static boolean tr(String flag, int... levels) {
		init();		// Insure connection
		return traceObj.trace(flag,  levels);
	}
	
	
	/**
	 * Setup trace flags from string
	 * of the form flag1[=value][,flagN=valueN]*
	 * Flags are case-insensitive and must not contain ",".
	 * Values are optional and default to 1 if not present.
	 */
	public static void setFlags(String settings) {
		init();			// Insure access
		Pattern pat_flag_val = Pattern.compile("(\\w+)(=(\\d+),*)?");
		Matcher matcher = pat_flag_val.matcher(settings);
		while (matcher.find()) {
			String flag = matcher.group(1);
			String value = matcher.group(3);
			int val = 1;			// Default value if no =...
			if (value != null)
				val = Integer.parseInt(value);
			traceObj.setLevel(flag, val);
			int get_val = traceObj.getLevel(flag);
			System.out.println(String.format("flag=%s value=%d", flag, get_val));
		}
	}

	
	/**
	 * Setup Loging file name
	 * Name without extension is appended with "_YYYYMMDD_HHMMSS.tlog"
	 */
	public static void setLogName(String logName) {
		SmTrace.logName = logName;
	}

	/**
	 * Set logging to stdout
	 */
	public static void setLogToStd(boolean on) {
		logToScreen = on;
	}

	/**
	 * Set stdout log to have timestamp prefix
	 */
	public static void setLogStdTs(boolean on) {
		stdOutHasTs = on;
	}
	
	
	/**
	 * Setup writing to log file ( via lg(string))
	 * @throws IOException 
	 */
	public static BufferedWriter setupLogging() throws IOException {
		if (logWriter != null)
			return logWriter;
		
		if (logName == null) {
			logName = "smt_";		// Default prefix
		}
		if (!new File(logName).isAbsolute()) {
			logName = "log" + File.separator + logName;
		}
		File base_file = new File(logName);
		File directory = base_file.getParentFile();
		if (!directory.exists()) {
		    try{
		        directory.mkdir();
		    } 
		    catch(SecurityException se){
		    	System.out.println(String.format(
		    			"Can't create logging directory %s",
		    			logName));
		    	System.exit(1);
		    }
		    
		    System.out.println(String.format("Logging Directory %s  created\n",
		        		directory.getAbsolutePath()));  
		}

		if (!logName.contains(".")) {
			String ts = getTs();
			logName = logName + ts;
			logName += ".smlog";		// Default extension
		}
		BufferedWriter bw = null;
		FileWriter fw = null;
		fw = new FileWriter(logName);
		logWriter = new BufferedWriter(fw);
		lg(String.format("Log File Name: %s", logName));
		return bw;
	}
	
	
	/**
	 * Log string to file, and optionally to console
	 * @throws IOException 
	 */
	public static void lg(String msg) {
		lg(msg, logToScreen);
	}
	
	
	/**
	 * Log string to file, and optionally to console
	 * @param msg - message to output
	 * @param trace_flag - when to trace
	 * @param levels - level to trace - default  1
	 * @throws IOException 
	 */
	public static void lg(String msg, String trace_flag, int...levels) {
		lg(msg, SmTrace.tr(trace_flag, levels));
	}
	
	
	/**
	 * Log string to file (based on second flag),
	 * and optionally to console
	 * @param msg - message to output
	 * @param trace_flag - when to trace
	 * @param levels - level to trace - default  1
	 * @param 
	 * @throws IOException 
	 */
	public static void lg(String msg, String trace_flag, String trace_flag_2, int...levels) {
		if (SmTrace.tr(trace_flag_2)) {
			lg(msg, SmTrace.tr(trace_flag, levels));
		}
	}
	
	
	/**
	 * Log string to file, and optionally to console
	 * STDOUT display is based on trace info
	 * @throws IOException 
	 */
	public static void lg(String msg, boolean to_stdout) {
		try {
			setupLogging();
		} catch (IOException e) {
			System.out.println("IOException in lg setupLogging");
			return;
		}
		String ts = getTs();
		if (to_stdout) {
			String prefix = "";
			if (stdOutHasTs) {
				prefix += " " + ts;
			}
			System.out.println(prefix + " " + msg);
		}
		if (logWriter == null) {
			System.out.println("Can't write to log file");
			return;
		}
		try {
			logWriter.write(" " + ts + " " + msg  +"\n");
			logWriter.flush();			// Force output
		} catch (IOException e) {
			System.out.println("IOException in lg write");
		}
	}

	
	/**
	 * Get / generate time stamp
	 * Format: YYYYMMDD_HHMMSS
	 */
	public static String getTs() {
	    final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
	    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	    String ts = sdf.format(timestamp);
	    return ts;
	}
	
	/**
	 * Set up based on properties file
	 * 
	 * @throws IOException
	 */
	public static void setProps(String propFile) {
		init();		// Setup singleton
		// Setup trace flags
		FileInputStream in;
		try {
			in = new FileInputStream(propFile);
		} catch (FileNotFoundException e) {
			File inf = new File(propFile);
			String abs_path;
			try {
				abs_path = inf.getCanonicalPath();
			} catch (IOException e1) {
				abs_path = "NO PATH for '" + propFile + "'";
				e1.printStackTrace();
			}

			System.err.println("Properties file " + abs_path + " not found");
			// e.printStackTrace();
			return;
		}
		try {
			traceObj.defaultProps.load(in);
		} catch (IOException e) {
			System.err.println("Can't load Properties file " + propFile);
			e.printStackTrace();
			return;
		}
		try {
			in.close();
		} catch (IOException e) {
			System.err.println("Can't load Properties file " + propFile);
			e.printStackTrace();
		}

	}

	/**
	 * Get trace level for flag
	 * If "traceAll" level -- usually traces all flags unless trace is higher level
	 * Else if flag is not set return 0
	 * This facilitates easier tracing of all trace(flag) calls by setLevel("ALL")
	 */

	public int getLevel(String trace_name) {
		if (traceAll > 0)
			return traceAll;		// ALL specified - all get this level
		trace_name = trace_name.toLowerCase();		
		Integer v = this.traceFlags.get(trace_name);
		if (v == null)
			return 0;		// Not there == 0
		
		return (int)v;
	}
	
	public void setLevel(String trace_name) {
		setLevel(trace_name, 1);
	}

	public void setLevel(String trace_name, int level) {
		trace_name = trace_name.toLowerCase();
		if (trace_name.equals("all"))
			this.traceAll = level;
		else
			this.traceFlags.put(trace_name, level);
	}

	public boolean traceVerbose(int... levels) {
		return trace("verbose", levels);
	}

	/**
	 * @param debug
	 *            to set
	 */
	public void setDebug(int level) {
		setLevel("debug", level);
	}

	/**
	 * @param level
	 */
	public void setVerbose(int level) {
		setLevel("verbose", level);
	}

	/**
	 * @return the verbose
	 */
	public int getVerbose() {
		return getLevel("verbose");
	}

	/**
	 * Trace if at or above this level
	 */
	public boolean trace(String flag, int... levels) {
		int level = 1;
		if (levels.length > 0)
			level = levels[0];
		if (level < 1)
			return false;		// Don't even look

		return traceLevel(flag) >= level;
	}

	/**
	 * Return trace level
	 */
	public int traceLevel(String flag) {
		int traceLevel = getLevel(flag);
		return traceLevel;
	}
	
	
	/**
	 * 
	 * @param key
	 *            - property key
	 * @return property value, "" if none
	 */
	public String getProperty(String key) {
		return this.defaultProps.getProperty(key, "");
	}

	public void setProperty(String key, String value) {
		this.defaultProps.setProperty(key, value);
	}

	/**
	 * Get source absolute path Get absolute if fileName is not absolute TBD
	 * handle chain of paths like C include paths
	 * 
	 * @param fileName
	 * @return absolute file path
	 */
	public String getSourcePath(String fileName) {
		Path p = Paths.get(fileName);
		if (!p.isAbsolute()) {
			String[] dirs = getAsStringArray("source_files");
			ArrayList<String> searched = new ArrayList<String>();
			for (String dir : dirs) {
				File inf = new File(dir, fileName);
				if (inf.exists() && !inf.isDirectory()) {
					try {
						return inf.getCanonicalPath();
					} catch (IOException e) {
						System.err.printf("Problem with path %s,%s", dir, fileName);
					}
				}
				try {
					searched.add(inf.getCanonicalPath());
				} catch (IOException e) {
					// Ignore
				}
			}
			System.err.printf("%s was not found\n", fileName);
			if (dirs.length > 0) {
				System.err.printf("Searched in:\n");
				for (String dir : dirs) {
					File dirf = new File(dir);
					try {
						String dirpath = dirf.getCanonicalPath();
						System.err.printf("\t%s\n", dirpath);
					} catch (IOException e) {
						System.err.printf("\tpath error for %s\n", dir);
					}
				}
			}
			return fileName; // Return unchanged
		}
		return fileName; // Already absolute path
	}

	/**
	 * Get include absolute path Get absolute if fileName is not absolute TBD
	 * handle chain of paths like C include paths
	 * 
	 * @param fileName
	 * @return absolute file path, "" if not found
	 */
	public String getIncludePath(String fileName) {
		Path p = Paths.get(fileName);
		if (!p.isAbsolute()) {
			String[] dirs = getAsStringArray("include_files");
			ArrayList<String> searched = new ArrayList<String>();
			for (String dir : dirs) {
				File inf = new File(dir, fileName);
				if (inf.exists() && !inf.isDirectory()) {
					try {
						return inf.getCanonicalPath();
					} catch (IOException e) {
						System.err.printf("Problem with path %s,%s", dir, fileName);
					}
				}
				try {
					searched.add(inf.getCanonicalPath());
				} catch (IOException e) {
					// Ignore
				}
			}
			System.err.printf("%s was not found\n", fileName);
			if (dirs.length > 0) {
				System.err.printf("Searched in:\n");
				for (String dir : dirs) {
					File dirf = new File(dir);
					try {
						String dirpath = dirf.getCanonicalPath();
						System.err.printf("\t%s\n", dirpath);
					} catch (IOException e) {
						System.err.printf("\tpath error for %s\n", dir);
					}
				}
			}
			return ""; // Indicate as not found
		}
		return fileName; // Already absolute path
	}

	/**
	 * Get default properties key with value stored as comma-separated values as
	 * an array of those values If propKey not found, return an empty array
	 * 
	 * @param propKey
	 * @return array of string values
	 */
	public String[] getAsStringArray(String propKey) {
		String[] vals = getProperty(propKey).split(",");
		return vals;
	}

	private Properties defaultProps; 	// program properties
	private int traceAll;				// For "all" trace
	private HashMap<String, Integer> traceFlags; // tracing flag/levels
}
