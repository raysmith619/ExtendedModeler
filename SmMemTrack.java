package smTrace;
/**
 * Instance to track
 * @author raysm
 *
 */
import java.lang.Math.*;
	
public class SmMemTrack {
	String name;
	SmMem.Type type;
	Runtime rT;
	int nCall = 0;			// Number of times called
	long maxMem = 0;
	long minMem = 0;
	long curMem = 0;
	long maxMemFree = 0;
	long minMemFree = 0;
	long curMemFree = 0;
	
	public SmMemTrack(String name, SmMem.Type type) {
		this.name = name;
		this.type = type;
		this.rT = Runtime.getRuntime();
	}

	
	/**
	 * Simple copy
	 */
	public SmMemTrack copy() {
		SmMemTrack copy = new SmMemTrack(this.name, this.type);
		copy.rT = this.rT;
		copy.nCall = this.nCall;			// Number of times called
		copy.maxMem = this.maxMem;
		copy.minMem = this.minMem;
		copy.curMem = this.curMem;
		copy.maxMemFree = this.maxMemFree;
		copy.minMemFree = this.minMemFree;
		copy.curMemFree = this.curMemFree;
		return copy;
	}

	/**
	 * Simple uniform memory format
	 * @return
	 */
	public String mFmt(long membytes) {
		String suffix = "KB";
		long factor = 1000;
		if (java.lang.Math.abs(membytes) >= 1.e6) {
			suffix = "MB";
			factor = (long) 1.e6;
		} else if (java.lang.Math.abs(membytes) >= 1.e9) {
			suffix = "GB";
			factor = (long) 1.e9;
		} else if (java.lang.Math.abs(membytes) >= 1.e12) {
			suffix = "TB";
			factor = (long) 1.e12;
		}
		String str = String.format("%.1f%s", (float)membytes/factor, suffix);
		return str;
	}
	
	
	public void ck() {
		long mem = rT.totalMemory();
		long memFree = rT.freeMemory();
		long memChg = mem - curMem;
		long memFreeChg = memFree - curMemFree;
		if (SmTrace.trace("memTrace")) {
			SmTrace.lg(String.format("memTrace: %s %d chg:%s cur:%s prev max:%s free:%s",
					name, nCall+1, mFmt(memChg), mFmt(mem), mFmt(maxMem), mFmt(memFree)));
		}
		nCall++;
		if (mem > maxMem) {
			if (SmTrace.trace("memTrace") || SmTrace.trace("memTraceLimits")) {
				SmTrace.lg(String.format("memTrace: %s %d New Max chg:%s cur:%s max:%s free:%s",
						name, nCall+1, mFmt(memChg), mFmt(mem), mFmt(maxMem), mFmt(memFree)));
			}
			maxMem = mem;
		}
		curMem = mem;
		if (mem < minMem)
			minMem = mem;

		if (memFree < minMemFree) {
			if (SmTrace.trace("memTrace") || SmTrace.trace("memTraceLimits")) {
				SmTrace.lg(String.format("memTrace: %s %d New Min Free chg:%.3g cur:%.1g max:%.1 free:%.2g",
						name, nCall+1, memChg, mem, maxMem, memFree));
			}
			minMemFree = memFree;
		}
		curMemFree = memFree;
	}
	
	/**
	 * Previous values
	 */
	public SmMemTrack prevTrack() {
		SmMemTrack prev = copy();
		prev.nCall = nCall;
		prev.maxMem = maxMem;
		prev.minMem = minMem;
		prev.curMem = curMem;
		prev.maxMemFree = curMemFree;
		prev.minMemFree = minMemFree;
		prev.curMemFree = curMemFree;
		return prev;
	}
}
