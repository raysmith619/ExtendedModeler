import java.util.ArrayList;
import java.util.Stack;

/**
 * Implement basic Command Interface/command to support Undo/Redo
 * @author raysm
 *
 */
public abstract class BlockCommand {
	static BlockCommandManager commandManager;
	
	public BlockCommand() throws Exception {
		if (commandManager == null) {
			throw new Exception("No BlockCommandManager");
			
		}
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
	 * Check if can undo this command
	 */
	public abstract boolean canUndo();

	/**
	 * Check if can redo this command
	 */
	public abstract boolean canRedo();

	/**
	 * Check if can repeat this command
	 */
	public abstract boolean canRepeat();

	
	
	/**
	 * Execute this command
	 * @return 
	 */
	public abstract boolean execute();

	/**
	 * Redo last undo - reverse the effects of the latest  undo
	 * @return
	 */
	public abstract boolean redo();
	
	/**
	 * Remove the effects of the most recently done command
	 *  1. remove command from commandStack
	 *  2. add command to undoStack
	 *  3. reverse changes caused by the command
	 *  4. return true iff could undo
	 */
	public abstract boolean undo();
	
	/**
	 * Re execute the most recently done command
	 */
	public abstract void repeat();

	/**
	 * Utility functions to add data to command
	 * @param bcmd 
	 */
	public abstract void addBlock(OurBlock cb);
	public abstract void addBlocks(ArrayList<OurBlock> cbs);
	public abstract void addPrevBlock(int bindex);
	public abstract void addPrevBlocks(ArrayList<Integer> binds);
	public abstract void saveSelection();
	public abstract void pushSelection(BlockSelect select);

	/**
	 * Save cmd,  using enclosed manager
	 */
	 public abstract void saveCmd();
	 	 
	 
	/**
	 * Do command for the first time
	 */
	public abstract boolean doCmd();
}
