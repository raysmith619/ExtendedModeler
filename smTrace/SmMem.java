package smTrace;

import java.util.HashMap;

/**
 * Facilitate Memory Usage Tracing / logging of execution.
 * Uses SmTrace for reporting / logging
 * Assumes SmTrace is handled separately
 * 
 * @author Ray Smith
 *
 */

public class SmMem {
	static Runtime rT = Runtime.getRuntime();
	static SmMemTrack init = new SmMemTrack("INITIAL", Type.Simple);
	static HashMap<String, SmMemTrack>tracks = new HashMap<String, SmMemTrack>();

	public enum Type {
		Simple, Begin, End, Continue
	}

	/**
	 * Check memory
	 * If new add, else modify
	 * @param name
	 * @param type
	 */
	public static void ck(String name, Type type) {
		SmMemTrack track = tracks.get(name);
		if (track == null) {
			track = new SmMemTrack(name, type);
			tracks.put(name, track);
		}
		track.ck();
	}

	public static void ck(String name) {
		ck(name, Type.Simple);
	}





}
