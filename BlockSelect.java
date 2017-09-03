import java.awt.List;
import java.util.ArrayList;
import java.util.Stack;

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
	
	public Integer remove() {
		return selected.remove(0);
	}
	
	public int removeInt() {
		if (isEmpty())
			return -1;
		return remove().intValue();
	}
}
