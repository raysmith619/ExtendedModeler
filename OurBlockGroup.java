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
	 */
	public OurBlock getBlock(int id) {
		return blocks.get(id);
	}

	/**
	 * Get array of ids(indexes)
	 */
	public int[] getIds( ) {
		Object[] idso = blocks.keySet().toArray();
		int[] ids = new int[idso.length];
		for (int i = 0; i < idso.length; i++) {
			Object ido = idso[i];
			Integer iid = (Integer) ido;
			ids[i] = iid.intValue();
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
	 */
	public OurBlock putBlock(OurBlock cb) {
		blocks.put(cb.iD, cb);
		newestStack.push(cb);
		return cb;
	}

	
	/**
	 * Get latest stored - null if none
	 */
	public OurBlock getNewestBlock() {
		while (!newestStack.isEmpty()) {
			OurBlock cb = newestStack.peek();
			if (blocks.containsKey(cb.iD)) {
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
			return cb.iD;
		
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

}
