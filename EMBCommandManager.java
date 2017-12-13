package ExtendedModeler;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Stack;

import smTrace.SmTrace;

/**
 * Implement basic Command Interface/command to support Undo/Redo
 * @author raysm
 *
 */
public class EMBCommandManager {
	SceneViewer scene;
	EMBCommand currentCmd;				// Currently executing command
	Stack<EMBCommand> commandStack;		// Commands done
	Stack<EMBCommand> undoStack;			// Commands undone
	
	public EMBCommandManager(SceneViewer scene) {
		this.scene = scene;
		commandStack = new Stack<EMBCommand>();
		undoStack = new Stack<EMBCommand>();
		EMBCommand.setManager(this);	// Ensure a manager is in place
		///if (EMBCommand.commandManager == null)
		///	EMBCommand.setManager(this);	// Ensure a manager is in place
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
	public EMBCommand lastCommand() {
		return commandStack.peek();
	}
	
	/**
	 * Peek at undo command
	 */
	public EMBCommand lastUndoCommand() {
		return undoStack.peek();
	}
	

	/**
	 * Check if can redo this command
	 */
	public boolean canRedo() {
		if (undoStack.isEmpty())
			return false;
		EMBCommand cmd = lastUndoCommand();
		return cmd.canRedo();
	}

	/**
	 * Check if can repeat this command
	 */
	public boolean canRepeat() {
		if (commandStack.isEmpty())
			return false;
		EMBCommand cmd = lastCommand();
		return cmd.canRepeat();
	}

	/**
	 * Check if can undo this command
	 */
	public boolean canUndo() {
		if (commandStack.isEmpty())
			return false;
		EMBCommand cmd = lastCommand();
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
	public EMBlock cb(int id) {
		return scene.getCb(id);
	}

	
	/**
	 * Check point command state
	 * by pushing command, which upon undo, will create current state
	 */
	public void checkPoint() {
		SmTrace.lg(String.format("checkPoint"), "execute");
		EMBCommand cmd;
		try {
			cmd = EMBCommand.checkPointCmd();
			undoStack.push(cmd);
		} catch (EMBlockError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Undo if possible
	 * command and select stack modifications are done through EMBCommand functions
	 */
	public boolean undo() {
		SmTrace.lg(String.format("undo"), "execute");
		if (!canUndo()) {
			SmTrace.lg(String.format("Can't undo"));
			return false;
		}
		EMBCommand cmd = commandStack.pop();
		return cmd.undo();
	}

	/**
	 * Re-execute the most recently undone command
	 */
	public boolean redo() {
		SmTrace.lg(String.format("redo"), "execute");
		if (!canRedo()) {
			SmTrace.lg(String.format("Can't redo"));
			return false;
		}
		EMBCommand cmd = undoStack.pop();
		return cmd.redo();
	}

	/**
	 * Re execute the most recently done command
	 */
	public boolean repeat() {		
		SmTrace.lg(String.format("repeat"));
		if (!canRepeat()) {
			SmTrace.lg(String.format("Can't repeat"));
			return false;
		}
		EMBCommand cmd = lastCommand();
		return cmd.doCmd();
	}

	/**
	 * Save command for undo/redo...
	 */
	public void saveCmd(EMBCommand bcmd) {
		commandStack.push(bcmd);
	}
	


	public boolean anySelected() {
		BlockSelect select = getSelected();
		return !select.isEmpty();
		
	}

	
	/**
	 * Access to selection
	 * We may want to reorganize this knowledge from the EMBCommand
	 */

	/**
	 * Get current command, if any
	 * @return current command, if one, else null
	 */
	public EMBCommand getCurrentCommand() {
		return currentCmd;
	}

	/**
	 * Get previous command, if any
	 * Return previous command, if one else
	 * return current command if one else
	 * return null
	 */
	public EMBCommand getPrevCommand() {
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
		EMBCommand prev_cmd = getPrevCommand();
		if (prev_cmd == null)
			return new BlockSelect();
		return prev_cmd.newSelect;
			
	}

	
	/**
	 * Get previously executed command's selected block
	 * Use current command if command stack only has one 
	 */
	public EMBlock getPrevSelectedBlock() {
		BlockSelect prev_select = getPrevSelected();
		if (prev_select.isEmpty())
			return null;
		return cb(prev_select.getIndex(0));
	}

	
	/**
	 * Get currently selected blocks
	 */
	public EMBlock[] getSelectedBlocks() {
		BlockSelect select = getSelected();
		int ids[] = select.getIds();
		EMBlock[] cbs = new EMBlock[ids.length];
		for (int i = 0; i < cbs.length; i++) {
			cbs[i] = cb(ids[i]);
		}
		return cbs;
	}
	
	/**
	 * Get currently selected
	 */
	public BlockSelect getSelected() {
		EMBCommand cmd = getCurrentCommand();
		if (cmd == null)
			return new BlockSelect();		// Empty when none
		
		return cmd.newSelect;
	}
	/**
	 * Get currently selected block if any
	 */
	public EMBlock getSelectedBlock() {
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
		SmTrace.lg("No select stack");
		return getSelected();
	}
	

	/**
	 * Set selection
	 */
	public void setSelected(BlockSelect select) {
		EMBCommand cmd = getCurrentCommand();
		cmd.setSelect(select);
	}
	
	/**
	public EMBlock cb(int bindex) {
		return scene.cb(bindex);
	}

	

	/**
	 * Print displayed blocks
	 */
	public void displayPrint(String tag, String trace) {
		scene.displayPrint(tag, trace);
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
	public void cmdStackPrint(String tag, String trace) {
		final int maxPrint = 5;
		String str = "";
		if (commandStack.isEmpty() ) {
			SmTrace.lg(String.format("%s commandStack: Empty", tag), trace);
			return;
		}
		ListIterator<EMBCommand> cmd_itr = commandStack.listIterator();
		int nprint = maxPrint;
		if (SmTrace.trace("verbose"))
			nprint = 9999;
		while (cmd_itr.hasNext())
			cmd_itr.next();
		for (int i = 0; i < nprint && cmd_itr.hasPrevious(); i++) {
			EMBCommand cmd = cmd_itr.previous();
			if (!str.equals(""))
				str += "; ";
			str += cmd;
		}
		SmTrace.lg(String.format("%s commandStack: (n:%d) %s",
				tag, commandStack.size(), str), trace);
	}
	
	
	/**
	 * Print select stack state
	 */
	public void selectPrint(String tag, String trace) {
		scene.selectPrint(tag, trace);
	}

}
