import java.util.Stack;

/**
 * Implement basic Command Interface/command to support Undo/Redo
 * @author raysm
 *
 */
public class BlockCommandManager {
	SceneViewer scene;
	Stack<BlockCommand> commandStack;	// Commands done
	Stack<BlockCommand> undoStack;				// Commands undone
	
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
	 * Undo if possible
	 * Return true after undoing
	 * Remove the effects of the most recently done command
	 *  1. remove command from commandStack
	 *  2. add command to undoStack
	 *  3. reverse changes caused by the command
	 *  4. return true iff could undo
	 */
	public boolean undo() {
		System.out.println(String.format("undo"));
		if (!canUndo()) {
			System.out.println(String.format("Can't undo"));
			return false;
		}
		BlockCommand cmd = lastCommand();
		if (cmd.undo()) {
			commandStack.pop();  	// Remove command of command stack
			undoStack.push(cmd);	// Save iff successful
			return true;
		} else {
			return false;
		}
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
		BlockCommand cmd = lastUndoCommand();
		cmd = undoStack.pop();
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
		if (cmd.redo()) {
			commandStack.push(cmd);	// push iff successful
			return true;
		}
		return false;		// Indicate unsuccessful
	}

	/**
	 * Save command for undo/redo...
	 */
	public void saveCmd(BlockCommand bcmd) {
		commandStack.push(bcmd);
	}
	


	public boolean anySelected() {
		return scene.anySelected();
	}

	/**
	 * Get currently selected
	 */
	public BlockSelect getSelected() {
		return scene.getSelected();
	}

	public OurBlock cb(int bindex) {
		return scene.cb(bindex);
	}
	
}
