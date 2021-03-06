package ExtendedModeler;
import java.awt.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;

/**
 * Collection of OurBlocks, facilitating group storage and access
 * @author raysm
 *
 */
public class EMBlockGroup {
	Stack<EMBlock> newestStack;			// Latest stored
	
	private Hashtable<Integer, EMBlock> blocks;

	public EMBlockGroup() {
		blocks = new Hashtable<Integer, EMBlock>();
		newestStack = new Stack<EMBlock>();
		
	}

	
	public EMBlockGroup(EMBlockGroup group) {
		this();
		this.blocks.putAll(group.blocks);
	}


	/**
	 * Get unique block
	 * null if none
	 */
	public EMBlock getBlock(int id) {
		if (!blocks.containsKey(id))
			return null;
		
		return blocks.get(id);
	}

	/**
	 * Iterator support
	 */
	
	/**
	 * get iterator over group
	 * sets local copy to facilitate single iteration
	 * @return - copy of iterator
	 */
	public Iterator<EMBlock> getIterator() {
		Collection<EMBlock> bc = blocks.values();
		Iterator<EMBlock> itr;
		itr = bc.iterator();
		return itr;
	}
	
	public boolean hasNext(Iterator<EMBlock> itr) {
		return itr.hasNext();
	}
	
	public EMBlock next(Iterator<EMBlock> itr) {
		return itr.next();
	}
	
	/**
	 * Get array of ids(indexes)
	 * TBD Use Iterator
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
	public EMBlock putBlock(EMBlock cb) {
		EMBlock cb_copy = cb.copy();
		blocks.put(cb.iD(), cb_copy);
		newestStack.push(cb);
		return cb;
	}

	
	/**
	 * Get latest stored - null if none
	 */
	public EMBlock getNewestBlock() {
		while (!newestStack.isEmpty()) {
			EMBlock cb = newestStack.peek();
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
		EMBlock cb = getNewestBlock();
		if (cb != null)
			return cb.iD();
		
		return -1;
	}
	
	
	/**
	 * Remove block from group
	 */
	public EMBlock removeBlock(int id) {
		return blocks.remove(id);
	}
	
	
	/**
	 * Remove all blocks from group
	 * - Initializes as empty
	 */
	public void removeAllBlocks() {
		blocks = new Hashtable<Integer, EMBlock>();
		newestStack = new Stack<EMBlock>();
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
