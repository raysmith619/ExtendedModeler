import java.util.ArrayList;

import smTrace.SmTrace;

/**
 * Implement basic Command Interface/command to support Undo/Redo
 * @author raysm
 *
 */
public abstract class EMBCommand {
	static EMBCommandManager commandManager;
	String action;				// Unique action name
	BlockSelect prevSelect;		// Previously selected
	BlockSelect newSelect;		// selected after execution
	EMBlockGroup prevBlocks;	// Blocks which may change or deleted
	EMBlockGroup newBlocks;	// New blocks - may be redundant
	
	public EMBCommand(String action) throws EMBlockError {
		this.action = action;
		if (commandManager == null) {
			throw new EMBlockError("No EMBCommandManager");
			
		}
		prevSelect = new BlockSelect(commandManager.scene.getSelected());
		newSelect = new BlockSelect(prevSelect);	// Default  - no change
		prevBlocks = new EMBlockGroup();
		newBlocks = new EMBlockGroup();
	}

	public EMBCommand(EMBCommand cmd) throws Exception  {
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
		setManager(new EMBCommandManager(scene));
	}

	/**
	 * Setup a custom manager
	 * @param mgr
	 */
	public static void setManager(EMBCommandManager mgr) {
		EMBCommand.commandManager = mgr;
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
			SmTrace.lg("execute - null newBlocks");
		commandManager.scene.insertBlocks(newBlocks);
		commandManager.displayUpdate(newSelect, prevSelect);
		commandManager.displayPrint(String.format("execute(%s) AFTER", this.action), "execute");
		commandManager.selectPrint(String.format("execute(%s) AFTER", this.action), "execute");
		commandManager.cmdStackPrint(String.format("execute(%s) AFTER", this.action), "execute");
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
		EMBCommand cmd;
		try {
			cmd = new BlkCmdAdd(this);	// Create "disposable" copy of command
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		EMBlockGroup temp = cmd.newBlocks;
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
		return addBlock(cb(id));
	}
	
	/**
	 * Add block, possibly new/changed to display
	 * @param id
	 * @return
	 */
	public int addBlock(EMBlock cb) {
		EMBlock cb_put = newBlocks.putBlock(cb.copy());
		return cb_put.iD();
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
	private EMBlock cb(int id) {
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
		commandManager.selectPrint(String.format("doCmd(%s)", this.action), "execute");
		commandManager.displayPrint(String.format("doCmd(%s)", this.action), "execute");
		commandManager.cmdStackPrint(String.format("doCmd(%s)", this.action), "execute");
		boolean res = execute();
		if (res) {
			if (canUndo() || canRepeat()) {
				SmTrace.lg("add to commandStack", "execute");
				commandManager.commandStack.add(this);
			} else {
				SmTrace.lg(String.format("doCmd(%s) can't undo/repeat", this.action), "execute");
			}
		}
		commandManager.cmdStackPrint(String.format("doCmd(%s) AFTER", this.action), "execute");
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
		EMBlock cb = cb(bindex);
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

	public void addPrevBlock(EMBlock cb) {
		prevBlocks.putBlock(cb.copy());
	}
		


}
