import java.util.ArrayList;

/**
 * Implement basic Command Interface/command to support Undo/Redo
 * @author raysm
 *
 */
public abstract class BlockCommand {
	static BlockCommandManager commandManager;
	String action;				// Unique action name
	BlockSelect prevSelect;		// Previously selected
	BlockSelect newSelect;		// selected after execution
	OurBlockGroup prevBlocks;	// Blocks which may change or deleted
	OurBlockGroup newBlocks;	// New blocks - may be redundant
	
	public BlockCommand(String action) throws Exception {
		this.action = action;
		if (commandManager == null) {
			throw new Exception("No BlockCommandManager");
			
		}
		prevSelect = new BlockSelect(commandManager.scene.getSelected());
		newSelect = new BlockSelect(prevSelect);	// Default  - no change
		prevBlocks = new OurBlockGroup();
		newBlocks = new OurBlockGroup();
	}

	public BlockCommand(BlockCommand cmd) throws Exception  {
		this(cmd.action);
		prevSelect = cmd.prevSelect;
		newSelect = cmd.newSelect;
		prevBlocks = cmd.prevBlocks;
		newBlocks = cmd.newBlocks;
		setManager(commandManager);
	}
	
	
	/**
	 * Setup command manager
	 * @param mgr
	 */
	public static void setManager(SceneViewer scene) {
		setManager(new BlockCommandManager(scene));
	}

	/**
	 * Setup a custom manager
	 * @param mgr
	 */
	public static void setManager(BlockCommandManager mgr) {
		BlockCommand.commandManager = mgr;
	}

	
	
	/**
	 * Execute constructed command
	 * without modifying commandStack.
	 * All commands capable of undo/redo call this
	 * without storing it for redo
	 */
	public boolean execute() {
		commandManager.currentCmd = this;
		int[] prev_ids = prevBlocks.getIds();
		commandManager.scene.removeBlocks(prev_ids);
		if (newBlocks == null)
			System.out.println("execute - null newBlocks");
		int[] new_ids = newBlocks.getIds();
		commandManager.scene.insertBlocks(newBlocks);
		commandManager.displayUpdate(newSelect, prevSelect);
		commandManager.displayPrint(String.format("execute(%s) AFTER", this.action));
		commandManager.selectPrint(String.format("execute(%s) AFTER", this.action));
		commandManager.cmdStackPrint(String.format("execute(%s) AFTER", this.action));
		return true;
	}

	
	
	/**
	 * Remove the effects of the most recently done command
	 *  1. remove command from commandStack
	 *  2. add command to undoStack
	 *  3. reverse changes caused by the command
	 *  4. return true iff could undo
	 * Non destructive execution of command
	 */
	boolean undo() {
		BlockCommand cmd;
		try {
			cmd = new BlkCmdAdd(this);	// Create "disposable" copy of command
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		OurBlockGroup temp = cmd.newBlocks;
		cmd.newBlocks = cmd.prevBlocks;
		cmd.prevBlocks = temp;
		
		BlockSelect temp_sel = cmd.newSelect;
		cmd.newSelect = cmd.prevSelect;
		cmd.prevSelect = temp_sel;
		
		boolean res = cmd.execute();
		if (res)
			commandManager.undoStack.push(this);
		return res;
	}
	/**
	 * Redo last undo - reverse the effects of the latest  undo
	 * The commandManager has already popped command from undo stack
	 * @return
	 */
	public boolean redo() {
		if (canRedo()) {
			return doCmd();
		}
		return false;
	}
	
	/**
	 * Add block to display
	 * @param id
	 * @return
	 */
	public int addBlock(int id) {
		newBlocks.putBlock(cb(id));
		return id;
	}
	
	/**
	 * Add block, possibly new/changed to display
	 * @param id
	 * @return
	 */
	public int addBlock(OurBlock cb) {
		OurBlock cb_put = newBlocks.putBlock(cb.copy());
		return cb_put.iD;
	}

							/**
							 * Selection Management
							 */
	public void addSelect(int id) {
		newSelect.addIndex(id);
	}
	

	/**
	 * Get block, given index
	 */
	private OurBlock cb(int id) {
		return commandManager.cb(id);
	}
	
	public BlockSelect setSelect(BlockSelect select) {
		newSelect = new BlockSelect(select);
		return newSelect;
	}
	
	public boolean removeSelect(int id) {
		if (!newSelect.hasIndex(id)) {
			return false;		// Not present
		}
		newSelect.removeIndex(id);
		return true;
	}
	
	/**
	 * Select block
	 * @param id - id of block to select
	 * @param keep - Keep previously selected blocks iff true
	 */
	public void selectBlock(int id, boolean keep) {
		if (!keep) {
			newSelect = new BlockSelect();
		}
		newSelect.addIndex(id);
	}

	/**
	 * Select, unselecting others
	 * @param id
	 */
	public void selectBlock(int id) {
		selectBlock(id, false);		// Default unselect all others
	}
	
	
	public void toggleSelection(int id) {
		if (newSelect.hasIndex(id)) {
			newSelect.removeIndex(id);
		} else {
			newSelect.addIndex(id);
		}
	}
	
	
	/**
	 * Display updated selection
	 */
	public boolean displaySelection(BlockSelect new_select, BlockSelect prev_select) {
		return commandManager.displayUpdate(new_select, prev_select);
	}
	 	 
	 
	/**
	 * Do command, storing, if command can be undone or repeated, for redo,repeat
	 */
	public boolean doCmd() {
		commandManager.selectPrint(String.format("doCmd(%s)", this.action));
		commandManager.displayPrint(String.format("doCmd(%s)", this.action));
		commandManager.cmdStackPrint(String.format("doCmd(%s)", this.action));
		boolean res = execute();
		if (res) {
			if (canUndo() || canRepeat()) {
				System.out.println("add to commandStack");
				commandManager.commandStack.add(this);
			} else {
				System.out.println(String.format("doCmd(%s) can't undo/repeat", this.action));
			}
		}
		commandManager.cmdStackPrint(String.format("doCmd(%s) AFTER", this.action));
		return res;
	}
	
	
	

	/**
	 * Utility functions to add data to command
	 */
	public void addPrevBlocks(ArrayList<Integer> list) {
		for (Integer bin : list) {
			addPrevBlock(bin.intValue());
		}
	}
	
	public void addPrevBlock(int bindex) {
		OurBlock cb = cb(bindex);
		if (cb != null)
			prevBlocks.putBlock(cb);
	}

	/**
	 * Get currently selected
	 */
	public BlockSelect getSelected() {
		return commandManager.getSelected();
	}
	
	
	/**
	 * Save selected items for possible restoration
	 * Include section criterion to support restoration of "selected" state
	 */
	public void saveSelection() {
		
	}

	/**
	 * Save command for undo/redo...
	 */
	public void saveCmd() {
		commandManager.saveCmd(this);
	}


	public void addBlocks(int[] ids) {
		for (int id : ids) {
			addBlock(id);
		}
	}


	public boolean anySelected() {
		return commandManager.anySelected();
	}

	public boolean canRedo() {
		return canUndo();				// Default - can redo if we can undo
	}


	public boolean canUndo() {
		return true;
	}


	public boolean canRepeat() {
		return canUndo();
	}

	/**
	 * Command Description String
	 */
	public String toString() {
		String str = action;
		if (!newBlocks.isEmpty())
		{
			str += " " + newBlocks;
		}
		return str;
	}

	public void addPrevBlock(OurBlock cb) {
		prevBlocks.putBlock(cb.copy());
	}
		


}
