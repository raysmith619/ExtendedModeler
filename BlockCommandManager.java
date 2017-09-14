import java.util.Iterator;
import java.util.Stack;

/**
 * Implement basic Command Interface/command to support Undo/Redo
 * @author raysm
 *
 */
public class BlockCommandManager {
	SceneViewer scene;
	BlockCommand currentCmd;				// Currently executing command
	Stack<BlockCommand> commandStack;		// Commands done
	Stack<BlockCommand> undoStack;			// Commands undone
	
	public BlockCommandManager(SceneViewer scene) {
		this.scene = scene;
		commandStack = new Stack<BlockCommand>();
		undoStack = new Stack<BlockCommand>();
		if (BlockCommand.commandManager == null)
			BlockCommand.setManager(this);	// Ensure a manager is in place
	}

	/**
	 * Check if command stack is empty
	 */
	public boolean isEmpty() {
		return commandStack.isEmpty();
	}

	/**
	 * Check if undo command stack is empty
	 */
	public boolean isUndoEmpty() {
		return undoStack.isEmpty();
	}
	
	/**
	 * Peek at last command
	 */
	public BlockCommand lastCommand() {
		return commandStack.peek();
	}
	
	/**
	 * Peek at undo command
	 */
	public BlockCommand lastUndoCommand() {
		return undoStack.peek();
	}
	
	/**
	 * Push most done command
	 */
	public void Command(BlockCommand cmd) {
		commandStack.push(cmd);
	}
	

	/**
	 * Check if can redo this command
	 */
	public boolean canRedo() {
		if (undoStack.isEmpty())
			return false;
		BlockCommand cmd = lastUndoCommand();
		return cmd.canRedo();
	}

	/**
	 * Check if can repeat this command
	 */
	public boolean canRepeat() {
		if (commandStack.isEmpty())
			return false;
		BlockCommand cmd = lastCommand();
		return cmd.canRepeat();
	}

	/**
	 * Check if can undo this command
	 */
	public boolean canUndo() {
		if (commandStack.isEmpty())
			return false;
		BlockCommand cmd = lastCommand();
		if (!cmd.canUndo())
			return false;		// Can't undo
		return true;
	}
	
	/**
	 * Get newest block index
	 */
	public int cbIndex() {
		return scene.cbIndex();
	}
	

	/**
	 * Get block, given index
	 */
	public OurBlock cb(int id) {
		return scene.cb(id);
	}

	
	/**
	 * Undo if possible
	 * command and select stack modifications are done through BlockCommand functions
	 */
	public boolean undo() {
		System.out.println(String.format("undo"));
		if (!canUndo()) {
			System.out.println(String.format("Can't undo"));
			return false;
		}
		BlockCommand cmd = commandStack.pop();
		return cmd.undo();
	}

	/**
	 * Re-execute the most recently undone command
	 */
	public boolean redo() {
		System.out.println(String.format("redo"));
		if (!canRedo()) {
			System.out.println(String.format("Can't redo"));
			return false;
		}
		BlockCommand cmd = undoStack.pop();
		return cmd.redo();
	}

	/**
	 * Re execute the most recently done command
	 */
	public boolean repeat() {		
		System.out.println(String.format("repeat"));
		if (!canRepeat()) {
			System.out.println(String.format("Can't repeat"));
			return false;
		}
		BlockCommand cmd = lastCommand();
		return cmd.doCmd();
	}

	/**
	 * Save command for undo/redo...
	 */
	public void saveCmd(BlockCommand bcmd) {
		commandStack.push(bcmd);
	}
	


	public boolean anySelected() {
		BlockSelect select = getSelected();
		return !select.isEmpty();
		
	}

	
	/**
	 * Access to selection
	 * We may want to reorganize this knowledge from the BlockCommand
	 */

	/**
	 * Get current command, if any
	 * @return current command, if one, else null
	 */
	public BlockCommand getCurrentCommand() {
		return currentCmd;
	}

	/**
	 * Get previous command, if any
	 * Return previous command, if one else
	 * return current command if one else
	 * return null
	 */
	public BlockCommand getPrevCommand() {
		if (commandStack.isEmpty())
			return null;
		if (commandStack.size() < 2)
			return commandStack.peek();
		return commandStack.elementAt(1);
	}

	/**
	 * Get previously executed command's selection
	 */
	public BlockSelect getPrevSelected() {
		BlockCommand prev_cmd = getPrevCommand();
		if (prev_cmd == null)
			return new BlockSelect();
		return prev_cmd.newSelect;
			
	}

	
	/**
	 * Get previously executed command's selected block
	 * Use current command if command stack only has one 
	 */
	public OurBlock getPrevSelectedBlock() {
		BlockSelect prev_select = getPrevSelected();
		if (prev_select.isEmpty())
			return null;
		return cb(prev_select.getIndex(0));
	}
	
	
	/**
	 * Get currently selected
	 */
	public BlockSelect getSelected() {
		BlockCommand cmd = getCurrentCommand();
		if (cmd == null)
			return new BlockSelect();		// Empty when none
		
		return cmd.newSelect;
	}
	/**
	 * Get currently selected block if any
	 */
	public OurBlock getSelectedBlock() {
		BlockSelect select = getSelected();
		if (select.isEmpty())
			return null;
		return cb(select.getList().get(0));
	}
	
	
	/**
	 * Check select stack
	 */
	public boolean selectIsEmpty() {
		return getSelected().isEmpty();
	}
	
	
	public BlockSelect selectPop() {
		System.out.print("No select stack");
		return getSelected();
	}
	

	/**
	 * Set selection
	 */
	public void setSelected(BlockSelect select) {
		BlockCommand cmd = getCurrentCommand();
		cmd.setSelect(select);
	}
	
	/**
	public OurBlock cb(int bindex) {
		return scene.cb(bindex);
	}

	

	/**
	 * Print displayed blocks
	 */
	public void displayPrint(String tag) {
		scene.displayPrint(tag);
	}

	
	/**
	 * Update selection display
	 * For now we just unselect all previous and select all new
	 */
	public boolean displayUpdate(BlockSelect new_select, BlockSelect prev_select) {
		return (scene.displayUpdate(new_select, prev_select));
	}
	

	/**
	 * Print command stack
	 */
	public void cmdStackPrint(String tag) {
		String str = "";
		if (commandStack.isEmpty() ) {
			System.out.println(String.format("%s commandStack: Empty", tag));
			return;
		}
		Iterator<BlockCommand> cmd_itr = commandStack.iterator();
		
		while (cmd_itr.hasNext()) {
			BlockCommand cmd = cmd_itr.next();
			if (!str.equals(""))
				str += "; ";
			str += cmd;
		}
		System.out.println(String.format("%s commandStack: %s", tag, str));
	}
	
	
	/**
	 * Print select stack state
	 */
	public void selectPrint(String tag) {
		scene.selectPrint(tag);
	}

}
