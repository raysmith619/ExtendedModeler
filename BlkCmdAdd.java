import java.util.ArrayList;
import java.util.Stack;

public class BlkCmdAdd extends BlockCommand {
	String action;				// Unique action name
	BlockSelect prevSelect;		// Previously selected
	BlockSelect newSelect;		// Newly selected
	OurBlockGroup prevBlocks;	// Blocks which may change or deleted
	OurBlockGroup newBlocks;	// New blocks - may be redundant
	
	public BlkCmdAdd(String action) throws Exception {
		super();
		this.action = action;
		prevSelect = commandManager.scene.getSelected();		
		prevBlocks = new OurBlockGroup();
		newBlocks = new OurBlockGroup();
	}

	/**
	 * Execute constructed command
	 * Often for redo
	 */
	@Override
	public boolean execute() {
		for (int id : prevBlocks.getIds()) {
			commandManager.scene.deleteBlock(id);
		}
		for (int id : newBlocks.getIds()) {
			commandManager.scene.addBlock(id);
		}
		prevSelect = commandManager.scene.getSelected();
		commandManager.scene.pushSelected(newSelect);
		commandManager.commandStack.push(this);
		commandManager.scene.repaint();
		return true;
	}

	@Override
	public boolean canUndo() {
		return true;
	}
	
	@Override
	public boolean undo() {
		for (int id : newBlocks.getIds()) {
			commandManager.scene.deleteBlock(id);
		}
		for (int id : prevBlocks.getIds()) {
			commandManager.scene.addBlock(id);
		}
		
		commandManager.scene.pushSelected(prevSelect);
		commandManager.scene.repaint();
		return true;
	}

	@Override
	public void repeat() {
	}

	@Override
	public boolean doCmd() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean redo() {
		return execute();
	}

	/**
	 * Utility functions to add data to command
	 */
	public void addBlock(OurBlock cb) {
		newBlocks.putBlock(cb);
///		newIndexes.push(cbIndex());
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

	/**
	 * Get newest block index
	 */
	private int cbIndex() {
		return commandManager.cbIndex();
	}
	

	/**
	 * Get block, given index
	 */
	private OurBlock cb(int index) {
		return commandManager.cb(index);
	}


	public void addBlocks(ArrayList<OurBlock> cbs) {
		for (OurBlock cb : cbs) {
			addBlock(cb);
		}
	}


	public boolean anySelected() {
		return commandManager.anySelected();
	}
	
	@Override
	public void pushSelection(BlockSelect select) {
		
	}

	@Override
	public boolean canRedo() {
		return canUndo();				// Default - can redo if we can undo
	}

	@Override
	public boolean canRepeat() {
		return canUndo();
	}
}
