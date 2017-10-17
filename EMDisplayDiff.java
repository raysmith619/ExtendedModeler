

/**
 * Display Difference
 * Initially we will go the Unix "diff" command rouote
 * and generate the EMBCommand that will convert the first
 * display to the second
 * @author raysm
 *
 */
public class EMDisplayDiff {
	private EMBCommand diffCmd;

	/**
	 * Create simplest command to change display d1 into d2
	 * 1. Compare blocks with the same id, adding modify operations
	 * 2. Add new blocks for those in d2 but not in d1
	 * 3. Add prev blocks for those in d1 but not in d2
	 * @param d1
	 * @param d2
	 * @throws Exception
	 */
	public EMDisplayDiff(EMDisplay d1, EMDisplay d2) throws EMBlockError {
		diffCmd = new BlkCmdAdd("displaydiff");
		int[] d1_ids = d1.getIds();
		int[] d2_ids = d2.getIds();
							// Do 2 passes - some duplication
							// but simplest
		for (int d1_id : d1_ids) {
			EMBlock d1_cb = d1.getBlock(d1_id);
			EMBlock d2_cb = d2.getBlock(d1_id);
			if (d2_cb != null) {
				if (d1_cb.cmp(d2_cb) == 0)
					continue;			// No change
				diffCmd.addPrevBlock(d1_cb);	// delete old
				diffCmd.addBlock(d2_cb);		// add new
			} else {
				diffCmd.addPrevBlock(d1_cb);	// Block gone
			}
		}
		for (int d2_id : d2_ids) {
			EMBlock d2_cb = d2.getBlock(d2_id);
			EMBlock d1_cb = d1.getBlock(d2_id);
			if (d1_cb != null) {
				// In both lists - handled by first pass
			} else {
				diffCmd.addBlock(d2_cb);	// New block
			}
		}
	}

	
	/**
	 * Test if displays are identical
	 */
	public boolean isSame() {
		if (diffCmd.newBlocks.isEmpty() && diffCmd.prevBlocks.isEmpty()) {
			return true;
		}
		return false;
	}

	
	/**
	 * get new blocks, i.e. those that are new to difference
	 */
	public int[] getNewIds() {
		return diffCmd.newBlocks.getIds();
	}

	
	/**
	 * get previous blocks, i.e. those that are old to difference
	 */
	public int[] getPrevIds() {
		return diffCmd.prevBlocks.getIds();
	}
	
	
	/**
	 * number of new blocks
	 */
	public int nNew() {
		return diffCmd.newBlocks.getIds().length;
	}

	/**
	 * number of new blocks
	 */
	public int nPrev() {
		return diffCmd.prevBlocks.getIds().length;
	}
	
	
	/**
	 * Descriptive string
	 */
	public String toString() {
		String chgcmdstr = diffCmd.toString();
		return "diffCmd:" + chgcmdstr;
	}

}
