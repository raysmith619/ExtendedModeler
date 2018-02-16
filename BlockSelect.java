package ExtendedModeler;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Selected entities
 * May be groups or singletons
 * @author raysm
 *
 */
public class BlockSelect {
	ArrayList<Integer> selected;
	
	public BlockSelect() {
		selected = new ArrayList<Integer>();
	}

	/**
	 * New copy of ids
	 * @param select
	 */
	public BlockSelect(BlockSelect select) {
		selected = new ArrayList<Integer>(select.getList());
	}

	/**
	 * Create select list from array of ints
	 */
	public BlockSelect(int [] sels) {
		selected = new ArrayList<Integer>();
		for (int i : sels)
			selected.add(i);
	}
	public boolean isEmpty() {
		return selected.isEmpty();
	}
	
	
	public BlockSelect(int val) {
		this();
		selected.add(val);
	}

	
	public boolean hasIndex(int index) {
		for (Integer bin : getList()) {
			int bi = bin.intValue();
			if (bi == index)
				return true;
		}
		return false;
	}

	
	public int addIndex(int index) {
		selected.add(new Integer(index));
		return index;
	}


	/**
	 * Remove first item with the given index(content value)
	 * @param index
	 */
	public boolean removeIndex(int index) {
		return selected.remove(new Integer(index));
	}
	/**
	 * Get index in selected
	 * @param offset - negative offset. 0 - last, -1 - one before last
	 * @return stored index, if one, else -1
	 */
	public int getIndex(int offset) {
		if (selected.size() < 1-offset)
			return -1;			// Not present
		int index = selected.get(-offset).intValue();
		return index;
	}
	
	public int getIndex() {
		return getIndex(0);
	}

	/**
	 * get list of selected
	 * @return
	 */
	public  ArrayList<Integer> getList() {
		return selected;
	}

	/**
	 * get array if ids of currently selected selected
	 * @return
	 */
	public  int[] getIds() {
		int ret[] = new int[selected.size()];
		Iterator<Integer> it = selected.iterator();
		for (int i = 0; i < ret.length; i++)
			ret[i] = it.next().intValue();
		return ret;
	}
	
	public Integer remove() {
		return selected.remove(0);
	}
	
	public int removeInt() {
		if (isEmpty())
			return -1;
		return remove().intValue();
	}


	
	/**
	 * Select description
	 */
	public String toString() {
		String str = "";
		for (int id : selected) {
			if (str != "")
				str += ", ";
			str += String.valueOf(id);
		}
		return str;
	}


	
	/**
	 * Select description
	 */
	public String toString(EMBlockGroup group) {
		String str = "";
		for (int id : selected) {
			if (str != "")
				str += ", ";
			str += group.getBlock(id);
		}
		return str;
	}
	
}
