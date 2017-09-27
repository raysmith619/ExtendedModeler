import java.awt.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

/**
 * Collection of OurBlocks, facilitating group storage and access
 * @author raysm
 *
 */
public class OurBlockGroup {
	Stack<OurBlock> newestStack;			// Latest stored
	
	private Hashtable<Integer, OurBlock> blocks;

	public OurBlockGroup() {
		blocks = new Hashtable<Integer, OurBlock>();
		newestStack = new Stack<OurBlock>();
		
	}

	
	/**
	 * Get unique block
	 * null if none
	 */
	public OurBlock getBlock(int id) {
		if (!blocks.containsKey(id))
			return null;
		
		return blocks.get(id);
	}

	/**
	 * Get array of ids(indexes)
	 */
	public int[] getIds( ) {
///		Object[] idso = blocks.keySet().toArray();
		Enumeration ide = blocks.keys();
		ArrayList idl = Collections.list(ide);
		Collections.sort(idl);
		int[] ids = new int[idl.size()];
		for (int i = 0; i < ids.length; i++) {
			int  id = (int)idl.get(i);
			ids[i] = (int)idl.get(i);
		}
		return ids;
	}

	
	/**
	 * Test if group is empty
	 */
	public boolean isEmpty() {
		return blocks.isEmpty();
	}
	
	/**
	 * Insert unique block
	 * Copy, in case of subsequent modification
	 */
	public OurBlock putBlock(OurBlock cb) {
		OurBlock cb_copy = cb.copy();
		blocks.put(cb.iD(), cb_copy);
		newestStack.push(cb);
		return cb;
	}

	
	/**
	 * Get latest stored - null if none
	 */
	public OurBlock getNewestBlock() {
		while (!newestStack.isEmpty()) {
			OurBlock cb = newestStack.peek();
			if (blocks.containsKey(cb.iD())) {
				return cb;
			}
			newestStack.pop();    // Remove if not displayed
		}
		return null;				// None present
	}
	
	/**
	 * Get id of newest, if any else -1
	 */
	public int getNewestId() {
		OurBlock cb = getNewestBlock();
		if (cb != null)
			return cb.iD();
		
		return -1;
	}
	
	
	/**
	 * Remove block from group
	 */
	public OurBlock removeBlock(int id) {
		return blocks.remove(id);
	}
	
	
	/**
	 * Remove all blocks from group
	 * - Initializes as empty
	 */
	public void removeAllBlocks() {
		blocks = new Hashtable<Integer, OurBlock>();
		newestStack = new Stack<OurBlock>();
	}

	/**
	 * Descriptive string
	 */
	public String toString() {
		String str = "";
		for (int id : this.getIds()) {
			if (!str.equals(""))
				str += ", ";
			str += getBlock(id);
		}
		return str;
	}

}
