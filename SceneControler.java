package ExtendedModeler;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

import smTrace.SmTrace;

class SceneControler extends GLCanvas implements MouseListener, MouseMotionListener, GLEventListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;



	/**
	 * Placement action types
	 *
	 */
	public enum PlacementActionType { // Placement action
		NONE, // none added
		PLACEMENT_MOVE, PLACEMENT_OK, PLACEMENT_CANCEL,
	}
	GLCapabilities caps;			// Main set of capabilities
	public SceneViewer localViewer = null;
	public SceneViewer externalViewer = null;
	ArrayList<SceneViewer> sceneViewers;			// independent viewers
	ExtendedModeler modeler;		// Main class - currently only for check button access
	EMBCommandManager commandManager;		// Command do,redo... control
	JFrame frame;					// Access to main frame
	ControlMap controlMap;			// Control Map window/frame
	SmTrace smTrace; 				// Trace support
	ControlsOfScene controls;		// Control boxes
	Scene scene;					// Modeled scene
	GLU glu; // Needed for scene2WorldCoord
	GLUT glut;
	SceneViewer displayedViewer = null;	// Most recent(currently) displayed viewer
	
	// Updated each display() call
	int viewport[] = new int[4];
	double mvmatrix[] = new double[16];
	double projmatrix[] = new double[16];
	///public int indexOfSelectedBlock = -1; // -1 for none
									/**
									 * Only used to go to previously
									 * select block(s) in case of delete
									 */
	private Stack<BlockSelect> selectStack = new Stack<BlockSelect>();			// Stack of recent selected
	
	public int indexOfHilitedBox = -1; // -1 for none
	private Point3D hilitedPoint = new Point3D();
	private Vector3D normalAtHilitedPoint = new Vector3D();

	ExtendedModeler.AutoAddType autoAdd = ExtendedModeler.AutoAddType.NONE; // Add
	boolean mousePressed;		// true iff mouse is selected																		// item
	EMBlock prevEye;			// Most recent eye object
																		// source
																			// menu

	RadialMenuWidget radialMenu = new RadialMenuWidget();
	private static final int COMMAND_CREATE_BOX = 10;
	private static final int COMMAND_CREATE_BALL = 11;
	private static final int COMMAND_CREATE_CYLINDAR = 12;
	private static final int COMMAND_CREATE_CONE = 13;
	private static final int COMMAND_DUPLICATE_BLOCK = 14;
	private static final int COMMAND_COLOR_RED = 1;
	private static final int COMMAND_COLOR_YELLOW = 2;
	private static final int COMMAND_COLOR_GREEN = 3;
	private static final int COMMAND_COLOR_BLUE = 4;
	private static final int COMMAND_DELETE = 5;

	public boolean displayExternalControl = true;
	public boolean displayAddControl = true;
	public boolean displayColorControl = true;
	public boolean displayEyeAtControl = true;
	public boolean displayLookAtControl = true;
	public boolean displayPlacementControl = true;
	public boolean displayTextControl = true;
	public boolean displayWorldAxes = false;
	public boolean displayCameraTarget = false;
	public boolean displayLocalView = true;
	public boolean displayBoundingBox = false;
	public boolean enableCompositing = false;
	
	
	int mouse_x, mouse_y, old_mouse_x, old_mouse_y;
	
	public SceneControler(ExtendedModeler modeler)
		throws EMBlockError {
		this.sceneViewers = new ArrayList<SceneViewer>();
		if (modeler == null) {
			SmTrace.lg("SceneControler: null - modeler - just a stub");
			return;
		}
		this.modeler = modeler;
		this.scene = modeler.getScene();
		this.commandManager = new EMBCommandManager(this);
		this.controls = new ControlsOfScene(this);
		EMBlockBase.setSceneControler(this);		// TFD for tracing
		radialMenu.setItemLabelAndID(RadialMenuWidget.CENTRAL_ITEM, "", COMMAND_DUPLICATE_BLOCK);
		radialMenu.setItemLabelAndID(1, "Duplicate Block", COMMAND_DUPLICATE_BLOCK);
		radialMenu.setItemLabelAndID(2, "Set Color to Red", COMMAND_COLOR_RED);
		radialMenu.setItemLabelAndID(3, "Set Color to Yellow", COMMAND_COLOR_YELLOW);
		radialMenu.setItemLabelAndID(4, "Set Color to Green", COMMAND_COLOR_GREEN);
		radialMenu.setItemLabelAndID(5, "Set Color to Blue", COMMAND_COLOR_BLUE);
		radialMenu.setItemLabelAndID(7, "Delete Box", COMMAND_DELETE);
		reset();
	}

	
	/**
	 * Add viewer to control
	 */
	public void addViewer(SceneViewer viewer) {
		sceneViewers.add(viewer);
	}

	
	
	/**
	 * Remove external viewer from control
	 */
	public void removeExternalViewer() {
		if (externalViewer != null) {
			externalViewer.dropViewer();
			externalViewer = null;
		}
		externalViewer = null;
		if (localViewer != null) {
			localViewer.externalViewer = null;
		}
	}
	
	
	public EMBox3D getSphere( int index ) {
		return scene.getBox(index);
	}

	
	/**
	 * Get viewer's up
	 */
	public Vector3D getUp() {
		SceneViewer viewer = currentViewer();
		if (viewer == null)
			return EMBox3D.UP;
		
		return viewer.getUp();
	}
	
	
	public ColoredBox getBoundingBoxOfScene() {
		return scene.getBoundingBoxOfScene();
	}
	
	
	/**
	 * Get currently viewer's displayed block ids
	 */
	public int[] getDisplayedIds() {
		SceneViewer viewer = getSceneViewer();		
		return viewer.getDisplayedIds();
	}
	

	public int getIndexOfIntersectedBox(
		Ray3D ray, // input
		Point3D intersectionPoint, // output
		Vector3D normalAtIntersection // output
	) {
		return scene.getIndexOfIntersectedBox(ray, intersectionPoint, normalAtIntersection);
	}

	
	public Dimension getPreferredSize() {
		return new Dimension(512, 512);
	}

	public EMBlock getPrevSelectedBlock() {
		return commandManager.getPrevSelectedBlock();
		
	}

	/**
	 * Get currently selected
	 */
	public BlockSelect getSelected() {
		return commandManager.getSelected();
	}
	

	/**
	 * Get currently selected block
	 */
	public EMBlock getSelectedBlock() {
		return commandManager.getSelectedBlock();
	}

	/**
	 * Get currently selected
	 */
	public EMBlock[] getSelectedBlocks() {
		EMBlock[] cbs = commandManager.getSelectedBlocks();
		return cbs;
	}
	

	/**
	 * Get currently selected block
	 */
	public int getSelectedBlockIndex() {
		BlockSelect select = getSelected();
		return select.getIndex();
	}
	
	/**
	 * enable / disable mouse add block
	 * 
	 * @param source
	 *            - source of add if set
	 */
	public void setAutoAdd(ExtendedModeler.AutoAddType type) {
		autoAdd = type;
	}

	/**
	 * Set/Restore selection
	 */
	public void pushSelected(BlockSelect select) {
		SmTrace.lg(String.format("pushSelected"));
		setSelected(select);
	}

	/**
	 * Set selection
	 */
	public void setSelected(BlockSelect select) {
		SmTrace.lg(String.format("setSelected"));
		commandManager.setSelected(select);
	}

	
	/**
	 * Set debug/diagnostic trace settings
	 * Sets absolutely, clearing all flags first
	 */
	public void traceSet(String trace_string) {
		SmTrace.clearFlags();
		SmTrace.setFlags(trace_string);
	}

	
	/**
	 * Set debug/diagnostic trace settings
	 * Sets from traceSelection group
	 */
	public void traceSelection() {
		SmTrace.clearFlags();
		String trace_string = "test1, test2";
		SmTrace.setFlags(trace_string);
	}
	
	/**
	 * Save selected in order
	 * Only push changed index
	 */
	public void pushSelected(int id) {
		BlockSelect select = new BlockSelect(id);
		setSelected(select);
	}

	/**
	 * Get previously selected
	 *  empty select if empty
	 */
	public BlockSelect popSelected() {
		SmTrace.lg(String.format("popSelected"));
		return getSelected();
	}
	
	/**
	 * Get int 
	 */
	public int popSelectedIndex() {
		BlockSelect top = popSelected();
		
		int id = top.getIndex();
		SmTrace.lg(String.format("popSelected = %d", id));
		return id;
	}
	
	/**
	 * Select given block
	 * @return index of selected block
	 * @param id - index of block to select, -1 - most recent block(s)
	 * @param keep - selection is added to currently selected, else currently selected group is pushed 
	 */
	public int select(int id, boolean keep) {
		if (id < 0)
			id = getSelectedBlockIndex();
		if (id < 0)
			id = scene.displayedBlocks.getNewestId();
		if (isSelected(id))
			return id;							// Already selected
		
		selectUnmark();								// Unmark previously selected  TBD - see if more efficient way
		if (!keep) {
			selectStack.push(new BlockSelect(id));		// New entry with newly selected
		} else {
			BlockSelect select;
			if (selectStack.isEmpty())
				select = new BlockSelect(id);
			else
				select = selectStack.peek();
			select.addIndex(id);
		}
		selectMark();
		return id;
	}

	/**
	 * Check if select stack empty
	 */
	public boolean selectIsEmpty() {
		return selectStack.isEmpty();
	}

	
	public BlockSelect selectPop() {
		return selectStack.pop();
	}
	
	
	public void selectPush(BlockSelect select) {
		selectStack.push(select);
	}
	
	
	/**
	 * command to replace selection
	 * Add another block to selection
	 * @param id - id to add
	 * @param keep - true keep any already selected, false - drop them
	 */
	public int selectAdd(EMBCommand bcmd, int id, boolean keep) {
		if (!keep) {
			bcmd.newSelect = new BlockSelect();
		}
		bcmd.newSelect.addIndex(id);
		return id;
	}
	
	
	/**
	 * Mark current selection(s)
	 */
	public BlockSelect selectMark() {
		BlockSelect select = getSelected();
		if (select.isEmpty())
			return select;				// None selected
		
		for (Integer bin : select.getList()) {
			int id = bin.intValue();
			scene.setSelectionStateOfBox(id, true);
			indexOfHilitedBox = getSelectedBlockIndex();	/// TBD generalize hilighted
		}
		controls.adjustControls();

		return select;
	}

	
	/**
	 * Mark current selection(s)
	 */
	public BlockSelect selectUnmark() {
		BlockSelect select = getSelected();
		if (select.isEmpty())
			return select;				// None selected
		
		for (Integer bin : select.getList()) {
			int id = bin.intValue();
			scene.setSelectionStateOfBox(id, false);
		}

		return select;
	}

	/**
	 * Print current selected blocks
	 */
	public void selectPrint(String tag, String trace) {
		if (tag == null) {
			tag = "select";
		}
		EMBCommand currentCmd = commandManager.currentCmd;
		if (currentCmd == null ) {
			SmTrace.lg(String.format("%s select: NONE", tag), trace);
			return;
		}
		String str =  currentCmd.newSelect.toString(scene.genBlocks);
		SmTrace.lg(String.format("%s select:%s", tag, str), trace);
		
			/**
			 * Select stack
			 */
		String str2 = "";
		if (selectStack.isEmpty() ) {
			SmTrace.lg(String.format("%s selectStack: Empty", tag), trace);
			return;
		}
		Iterator<BlockSelect> sel_itr = selectStack.iterator();
		
		while (sel_itr.hasNext()) {
			BlockSelect sel = sel_itr.next();
			if (!str2.equals(""))
				str2 += "; ";
			str2 += sel.toString(scene.genBlocks);
		}
		SmTrace.lg(String.format("%s selectStack: %s", tag, str2), trace);
	
	}

	
	/**
	 * Text representation
	 */
	public String selectToString(BlockSelect select) {
		String str = "";
		for (int id : select.getIds()) {
			if (str != "")
				str += ", ";
			str += scene.genBlocks.getBlock(id);
		}
		return str;
	}
	
	/**
	 * Update selection display
	 * For now we just unselect all previous and select all new
	 * Add to selectStack if new_select is populated
	 */
	public boolean displayUpdate(BlockSelect new_select, BlockSelect prev_select) {
		for (int id : prev_select.getIds()) {
			scene.setSelectionStateOfBox(id, false);
		}
		for (int id : new_select.getIds()) {
			scene.setSelectionStateOfBox(id, true);
		}
		/**
		 * Update control display
		 */
		controls.displayUpdate(new_select, prev_select);
		/*** again Reversed order TFD ***/
		for (int i = 0; i < sceneViewers.size(); i++) {
			SceneViewer viewer = sceneViewers.get(i);
			SmTrace.lg(String.format("displayUpdate: viewer[%d]=%s", i, viewer.name), "displayviewer");
			if (SmTrace.trace("skipimage")) {
				if (viewer.getSceneName().startsWith("Ext")) {
					SmTrace.lg(String.format("viewer %s displayUpdate.display skipping in External Viewer", viewer.getSceneName()));
					continue;
				}
			}
			
			viewer.display();
			viewer.repaint();
		}
		
		/*** Reverse order TFD
		for (SceneViewer viewer: sceneViewers) {
			viewer.display();
			viewer.repaint();
		}
		***/
		return true;
	}
	
	
	/**
	 * Check if any selected
	 */
	public boolean anySelected() {
		return commandManager.anySelected();
	}
	
	/**
	 * Check if block at this index is already selected
	 */
	public boolean isSelected(int id) {
		EMBlock cb = getCb(id);
		return cb.isSelected();
	}

	/**
	 * Select new block, unselecting current
	 * @param id
	 * @return selected block's index
	 */
	public int select(int id) {
		return select(id, false);
	}

	/**
	 * select latest block
	 */
	public int select() {
		return select(scene.displayedBlocks.getNewestId());
	}

	/**
	 * Clear out selections, remembering them for undo
	 * @param bcmd
	 */
	public void clearSelections(EMBCommand bcmd) {
		BlockSelect selected = getSelected();
		int[] selids = selected.getIds();
		unSelect(selids);
	}

/**
 * Command undo, redo, ... control
 * @return
 */
	public boolean cmdUndo() {
		return commandManager.undo();
	}
	public boolean cmdRedo() {
		return commandManager.redo();
	}
	public boolean cmdRepeat() {
		return commandManager.repeat();
	}

	/**
	 * Clear out current selection, remembering it for undo
	 * @param bcmd
	 */
	public void clearSelection(EMBCommand bcmd) {
		if (!selectStack.isEmpty()) {
			BlockSelect select = selectStack.pop();
			unSelect(select.getList());
			bcmd.setSelect(select);
		}
	}
	
	/**
	 * Unselect selection
	 * Unselect is restricted to those in the currently selected entry
	 * If the result is an empty selected set it is popped from the stack
	 * Any re-selection is done elsewhere.
	 * @param id - index to unselect, -1 => all
	 */
	public void unSelect(int id) {
		if (scene.displayedBlocks.isEmpty()) {
			return;
		}
		
		BlockSelect select = getSelected();
		ArrayList<Integer> bilist = new ArrayList<Integer>();
		for (Integer bin : select.getList())
			bilist.add(new Integer(bin));		// Make copy
		for (Integer bin : bilist) {
			int bi = bin.intValue();
			if (id < 0 || bi == id) {
				scene.setSelectionStateOfBox(bi, false);
				select.removeIndex(bi);
				indexOfHilitedBox = bi;		// TBD - highlight needs to track selected in form
			}
		}
		if (select.isEmpty()) {				// Pop selected entry if empty
			if (!selectStack.isEmpty())
				selectStack.pop();
		}
	}

	
	/**
	 * Unselect those specified
	 */
	public void unSelect(ArrayList<Integer> bins) {
		Iterator<Integer> bit = bins.iterator();
		for (Integer bii = bit.next(); bit.hasNext(); ) {
			unSelect(bii.intValue());
		}
	}

	
	/**
	 * Unselect those specified
	 */
	public void unSelect(int[] ids) {
		for (int id : ids) {
			unSelect(id);
		}
	}
	
	float clamp(float x, float min, float max) {
		if (x < min)
			return min;
		if (x > max)
			return max;
		return x;
	}

	/**
	 * Add another block to scene
	 */
	public void addBlock(EMBCommand bcmd, EMBlock cb) {
		int icb = bcmd.addBlock(cb.iD());
		bcmd.addSelect(icb);
	}

	/**
	 * Add another block to scene
	 */
	public void addBlock(EMBCommand bcmd, int id) {
		scene.addBlock(bcmd, id);
	}

	public int addNewBlock(EMBCommand bcmd, String blocktype, EMBox3D box, Color color) {
		 EMBlock cb = newBlock(blocktype, box, color);
		bcmd.addBlock(cb.iD());
		return cb.iD();
	}

	private EMBlock newBlock(String blocktype, EMBox3D box, Color color) {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * Add blocks to scene
	 */
	public void addBlocks(EMBCommand bcmd, int[] ids) {
		for (int id : ids) {
			addBlock(bcmd, id);
		}
	}

	/**
	 * Add a duplicate block at the given point
	 * @throws EMBlockError 
	 */
	public void addDuplicateBlock(EMBCommand bcmd, EMBlock cb, Point3D atpoint) throws EMBlockError {
		EMBlock cbnew = cb.duplicate();
		addBlock(bcmd, cbnew);
	}
	public int createNewBlock(EMBCommand bcmd, String blockType) throws EMBlockError  {
		return createNewBlock(bcmd, blockType, null);
	}

	
	public int createNewBlock(EMBCommand bcmd, String blockType, String name) throws EMBlockError  {
		if (isAddAtMouse()) {
		} else {
			EMBlock cb_sel = getSelectedBlock();
			if (cb_sel != null) {
				cb_sel.setControls(controls);
				ControlOfPlacement cop = (ControlOfPlacement) controls.getControl("placement");
				cop.adjPos();
			}
		}
		EMBlock cb = null;
		try {
			cb = EMBlock.newBlock(blockType, controls, name);
		} catch (EMBlockError e) {
			SmTrace.lg(String.format("createNewBlock %s error: %s",
					blockType, e.getMessage()));
			return -1;
		}
		
		///cb.adjustFromControls(controls, bcmd);
		int id = bcmd.addBlock(cb);
		selectAdd(bcmd, id, false);
		///bcmd.selectBlock(id);		// Select new block
		return id;
		
	}

	public void createNewBlock(EMBCommand bcmd, String blockType, Point3D at_point, Vector3D size) throws EMBlockError {
		EMBlock cb = null;
		if (anySelected()) {
			cb = getSelectedBlock();
			Point3D centerOfNewBox = Point3D.sum(
					Point3D.sum(cb.getCenter(),
							Vector3D.mult(normalAtSelectedPoint(),
									0.5f * (float) Math.abs(Vector3D.dot(cb.getDiagonal(), normalAtSelectedPoint())))),
					Vector3D.mult(normalAtSelectedPoint(), EMBlockBase.DEFAULT_SIZE * 0.5f));
			EMBox3D box = new EMBox3D(centerOfNewBox, cb.getSize().length()/2);
			Color color = new Color(clamp(cb.getRed() + 0.5f * ((float) Math.random() - 0.5f), 0, 1),
					clamp(cb.getGreen() + 0.5f * ((float) Math.random() - 0.5f), 0, 1),
					clamp(cb.getBlue() + 0.5f * ((float) Math.random() - 0.5f), 0, 1), cb.getAlpha());
			if (blockType.equals("image")) {
				cb = new EMBlock(ColoredImage.newBlock(controls));
			} else {
				cb = EMBlock.newBlock(blockType, box, color);
			}
		} else {
			Point3D centerOfNewBox = currentViewerCamera().target;
			EMBox3D box = new EMBox3D(centerOfNewBox, EMBox3D.defaultRadius());
			Color color = new Color((float) Math.random(), (float) Math.random(), (float) Math.random(),
					EMBlockBase.DEFAULT_ALPHA);
			if (blockType.equals("image")) {
				cb = new EMBlock(ColoredImage.newBlock(controls));
			} else {
				cb = EMBlock.newBlock(blockType, box, color);
			}
			normalAtSelectedPoint(new Vector3D(1, 0, 0));
		}
		if (cb != null) {
			bcmd.addBlock(cb.iD());
		}
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
	public EMBlock getCb(int id) {
		return scene.cb(id);
	}

	
	/**
	 * Get generated block, given index
	 * null, if none
	 */
	public EMBlock cbGen(int id) {
		return scene.genBlocks.getBlock(id);
	}

	
	/**
	 * Duplicate selected block - offset a bit
	 * @throws EMBlockError 
	 */
	public int duplicateBlock(EMBCommand bcmd) throws EMBlockError {
		EMBlock cb = getSelectedBlock();
		if (cb != null) {
			cb = cb.duplicate();
		}
		if (cb != null) {
			bcmd.addBlock(cb.iD());
			return cb.iD();
		}
		return -1;		// None created
	}

	/**
	 * Duplicate block
	 * 
	 * @param id - original block
	 * @return - id of new block
	 * @throws EMBlockError 
	 */
	public int duplicateBlock(EMBCommand bcmd, int id) throws EMBlockError {
		String blockType = getCb(id).blockType();
		String name = getCb(id).getName();
		int idnew = createNewBlock(bcmd, blockType, name);
		return idnew;
	}

	public void setColorOfSelection(EMBCommand bcmd, float r, float g, float b) {
		if (anySelected()) {
///			bcmd.saveSelection();
			scene.setColorOfBlock(getSelectedBlockIndex(), r, g, b);
		}
	}

///	public void deleteBlock(int id) {
///		scene.deleteBlock(id);
///	}

	/**
	 * Delete blocks
	 */
	public void deleteBlocks(EMBCommand bcmd, int[] ids) {
		for (int id : ids) {
			deleteBlock(bcmd, id);
		}
		
	}

	/**
	 * Select blocks
	 * Add blocks to selected list
	 */
	public void selectBlocks(EMBCommand bcmd, int[] ids) {
		for (int id : ids) {
			selectBlock(bcmd, id);
		}
		
	}
	
	
	/**
	 * Delete block, as part of a command
	 * Selection process is handled by others
	 * @param bcmd
	 * @param id
	 */
	public void deleteBlock(EMBCommand bcmd, int id) {
		SmTrace.lg(String.format("deleteBlock(%d)", id));
		bcmd.addPrevBlock(id);
	}
	
	public void deleteSelection(EMBCommand bcmd) {
		SmTrace.lg("deleteSelection");
		selectPrint(String.format("deleteSelection"), "select");
		BlockSelect select = getSelected();
		if (!select.isEmpty()) {
			deleteBlocks(bcmd, select.getIds());
			if (!selectStack.isEmpty()) {
				selectStack.pop();
				if (!selectStack.isEmpty()) {
					BlockSelect select2 = selectStack.pop();
					bcmd.setSelect(select2);
				}
			}
		}
	}

	public void deleteAll(EMBCommand bcmd) {
		int[] allids = getDisplayedIds();
		deleteBlocks(bcmd, allids);
		indexOfHilitedBox = -1;
	}

	public void selectAll(EMBCommand bcmd) {
		int[] allids = getDisplayedIds();
		selectBlocks(bcmd, allids);
		indexOfHilitedBox = -1;
	}

	public void selectNone(EMBCommand bcmd) {
		clearSelections(bcmd);
		indexOfHilitedBox = -1;
	}

	/**
	 * select next available (from first)
	 * @param bcmd
	 */
	public void selectNext(EMBCommand bcmd) {
		BlockSelect selected = getSelected();
		int[] allids = getDisplayedIds();
		int[] selids = selected.getIds();

		if (allids.length == selids.length)
			return;			// None to select
		for (int i = 0; i < allids.length; i++) {
			int id = allids[i];
			if (!isSelected(id)) {
				selectBlock(bcmd, id);
				return;
			}
		}
	}
	
	/**
	 * Select block, as part of a command
	 * @param bcmd
	 * @param id
	 */
	public void selectBlock(EMBCommand bcmd, int id) {
		SmTrace.lg(String.format("selectBlock(%s)", getCb(id).toString()));
		bcmd.selectBlock(id, true);
	}

	public void eyeAtSelection() {
		int id = getSelectedBlockIndex();
		if (id >= 0) {
			Point3D p = scene.getBox(id).getCenter();
			currentViewerCamera().eyeAt(p);
		}
	}

	
	/**
	 * Sets all viewers' camera(target) at a point
	 * Updates visible controls
	 * @param pt
	 */
	public void eyeAt(Point3D pt) {
		for (SceneViewer viewer : sceneViewers)
			viewer.eyeAt(pt);
	}

	
	/**
	 * Set camera to look at a point
	 * Updates visible controls
	 * @param pt
	 */
	public void lookAt(Point3D pt) {
		for (SceneViewer viewer : sceneViewers)
			viewer.lookAt(pt);
	}
	
	
	public void lookAtSelection() {
		int id = getSelectedBlockIndex();
		if (id >= 0) {
			Point3D p = scene.getBox(id).getCenter();
			lookAt(p);
		}
	}
	
	
	/**
	 * Remove block from display
	 * No other processing is done.
	 * Physical display update is done elsewhere.
	 * @param id
	 */
	public void removeBlock(int id) {
		SmTrace.lg(String.format("removeBlock(%d)", id), "blocks");
		scene.displayedBlocks.removeBlock(id);
	}
	
	/**
	 * Remove blocks from display
	 * No additional processing is done
	 */
	public void removeBlocks(int[] ids) {
		for (int id : ids) {
			removeBlock(id);
		}
		
	}

	/**
	 * Reset graphics
	 */
	public void reset() {
		SmTrace.lg("Reset");
		controls.reset();
		commandManager = new EMBCommandManager(this);
		mousePressed = false;
		prevEye = null;
		repaint();
	}

	public boolean isAddAtMouse() {
		ControlOfComponent coco = (ControlOfComponent)controls.getControl("component");
		if (coco == null) {
			return false;
		}
		return coco.isAddAtMouse();
	}

	/**
	 * Check if we are in a mouse pressed state
	 */
	public boolean isMousePressed() {
		return mousePressed;
	}
	
	/**
	 * Set our mouse pressed state
	 */
	public void setMousePressed(boolean setting) {
		mousePressed = setting;
	}

	/**
	 * Get current viewer
	 */
	public SceneViewer currentViewer() {
		return sceneViewers.get(0);		/// TBD - for now, just first one
	}
	
	/**
	 * Get camera of current viewer
	 */
	public Camera3D currentViewerCamera() {
		return currentViewer().camera;
	}
	
	public void resetCameras() {
		for (SceneViewer viewer : sceneViewers) {
			viewer.resetCamera();
		}
		ColoredText.reset();
	}

	
	/**
	 * Insert new block into scene display
	 * 
	 */
	public void insertBlock(EMBlock cb) {
		scene.insertBlock(cb);
	}

	/**
	 * Insert block to scene display
	 * No other processing is done here
	 * Display update is done elsewhere
	 */
	public void insertBlock(int id) {
		scene.insertBlock(id);
	}
	
	/**
	 * Insert blocks to display
	 * No additional processing is done here.
	 * Display update is done elsewhere.
	 */
	public void insertBlocks(int[] ids) {
		for (int id : ids) {
			insertBlock(id);
		}
	}
	
	/**
	 * Insert blocks to display
	 * No additional processing is done here.
	 * Display update is done elsewhere.
	 */
	public void insertBlocks(EMBlockGroup blocks) {
		scene.insertBlocks(blocks);
	}

	public void init(GLAutoDrawable drawable) {
	}

	/**
	 * Screen location to world coordinates
	 * 
	 * @param args
	 */
	void screen2WorldCoord(int x, // Screen (mouse) x
			int y, // Screen (mouse) y
			double zval, // desired z value
			double[] wcoord // World coordinates returned x,y,z,
	) {
		int realy = viewport[3] - (int) y - 1;
		SmTrace.lg(String.format("screen2WorldCoord(x=%d,y=%d (realy=%d), z=%.2f", x, y, realy, zval), "projection");
		glu.gluUnProject((double) x, (double) realy, zval, //
				mvmatrix, 0, projmatrix, 0, viewport, 0, wcoord, 0);
		SmTrace.lg(String.format("   wx=%.2f, wy=%.2f, wz=%.2f", wcoord[0], wcoord[1], wcoord[2]), "projection");
	}

	/**
	 * Display 4x4 matrix (from double[16]
	 * 
	 * @param heading
	 *            - heading
	 * @param vals
	 *            double[16]
	 */
	public void printMatrix(String heading, double vals[]) {
		SmTrace.lg("\n" + heading);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				int idx = i + 4 * j;
				System.out.print(String.format("%7.2g", vals[idx]));
			}
			System.out.print("\n");
		}
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
		// leave this empty
		SmTrace.lg("displayChanged");
	}

	public void display(GLAutoDrawable drawable) {
		/*** Reversed order TFD ***/
		for (int i = sceneViewers.size()-1; i >= 0; i--) {
			SceneViewer viewer = sceneViewers.get(i);
			if (SmTrace.trace("skipimage")) {
				if (viewer.getSceneName().startsWith("Ext")) {
					SmTrace.lg(String.format("viewer %s display() skipping in External Viewer", viewer.getSceneName()));
					continue;
				}
			}
			viewer.display(drawable);
		}
		
		/*** Reverse order TFD
		for (SceneViewer viewer: sceneViewers) {
			viewer.display(drawable);
		}
		***/
	}

	
	/**
	 * Set the currently(most recently) displayed viewer
	 * @param viewer
	 */
	public void setDisplayedViewer(SceneViewer viewer) {
		displayedViewer = viewer;
	}
	

	
	/**
	 * Get the currently(most recently) displayed viewer
	 * @param viewer
	 */
	public SceneViewer getDisplayedViewer() {
		return displayedViewer;
	}


	/**
	 * setup external, local viewer pair
	 **/
	public void setExternalViewer(SceneViewer localViewer, SceneViewer externalViewer) {
		 this.localViewer = localViewer;
		 this.externalViewer = externalViewer;

	}

	
	
	public void mouseClicked(MouseEvent e) {
		SmTrace.lg("mouseClick", "mouse");
		if (e.isControlDown())
			SmTrace.lg("isControlDown", "mouse");
		if (indexOfHilitedBox >= 0) {
			if (e.isControlDown()) {
				EMBCommand bcmd;
				String action = "emc_mouseAddSelect";
				try {
					bcmd = new BlkCmdAdd(action);
				} catch (Exception e2) {
					e2.printStackTrace();
					return;
				}
				if (isSelected(indexOfHilitedBox)) {
					bcmd.removeSelect(indexOfHilitedBox);
				} else {
					selectAdd(bcmd, indexOfHilitedBox, true);		// keep with selected
				}
				bcmd.doCmd();
			} else {
				EMBCommand bcmd;
				String action = "emc_mouseNewSelect";
				try {
					bcmd = new BlkCmdAdd(action);
				} catch (Exception e2) {
					e2.printStackTrace();
					return;
				}
				selectAdd(bcmd, indexOfHilitedBox, false);
				bcmd.doCmd();
			}
		}
		selectedPoint(hilitedPoint());
		normalAtSelectedPoint(normalAtHilitedPoint());
		repaint();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		SmTrace.lg(String.format("SceneControler mousePressed(%d,%d)", mouse_x, mouse_y), "mouse");
	}

		
	public void mouseReleased(MouseEvent e) {
		setMousePressed(false);
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();
		try {
			SmTrace.lg(String.format("mouseReleased(%d,%d)", mouse_x, mouse_y), "mouse");
	
			if (radialMenu.isVisible()) {
				int returnValue = radialMenu.releaseEvent(mouse_x, mouse_y);
	
				int itemID = radialMenu.getItemID(radialMenu.getSelection());
				SmTrace.lg(String.format("itemID=%d returnValue=%d", itemID, returnValue), "select");
				EMBCommand bcmd = null;
				switch (itemID) {
				case COMMAND_DUPLICATE_BLOCK:
					try {
						bcmd = new BlkCmdAdd("duplicate_block");
					} catch (Exception e1) {
						e1.printStackTrace();
						return;
					}
					try {
						duplicateBlock(bcmd);
					} catch (EMBlockError e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
						SmTrace.lg("duplicateBlock error");
						return;
					} // Duplicate selected block
					break;
				case COMMAND_CREATE_BOX:
					try {
						bcmd = new BlkCmdAdd("emc_create_box");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					createNewBlock(bcmd, "box");
					break;
				case COMMAND_CREATE_BALL:
					try {
						bcmd = new BlkCmdAdd("emc_create_ball");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					createNewBlock(bcmd, "ball");
					break;
				case COMMAND_CREATE_CONE:
					try {
						bcmd = new BlkCmdAdd("emc_create_cone");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					createNewBlock(bcmd, "cone");
					break;
				case COMMAND_CREATE_CYLINDAR:
					try {
						bcmd = new BlkCmdAdd("emc_create_cylinder");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					createNewBlock(bcmd, "cylinder");
					break;
				case COMMAND_COLOR_RED:
					try {
						bcmd = new BlkCmdAdd("emc_color_red");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					setColorOfSelection(bcmd, 1, 0, 0);
					break;
				case COMMAND_COLOR_YELLOW:
					try {
						bcmd = new BlkCmdAdd("emc_color_yello");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					setColorOfSelection(bcmd, 1, 1, 0);
					break;
				case COMMAND_COLOR_GREEN:
					try {
						bcmd = new BlkCmdAdd("emc_color_green");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					setColorOfSelection(bcmd, 0, 1, 0);
					break;
				case COMMAND_COLOR_BLUE:
					try {
						bcmd = new BlkCmdAdd("emc_color_blue");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					setColorOfSelection(bcmd, 0, 0, 1);
					break;
				case COMMAND_DELETE:
					try {
						bcmd = new BlkCmdAdd("emc_delete");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					deleteSelection(bcmd);
					break;
				}
	
				repaint();
	
				if (returnValue != CustomWidget.S_EVENT_NOT_CONSUMED)
					return;
				if (bcmd != null) {
					bcmd.saveCmd();
				}
			}
		} catch (EMBlockError eb) {
			SmTrace.lg(String.format("mouseReleased error %s", eb.getMessage()));
			return;
		}
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
	}

	/**
	 * OpenGl to screen coordinate translation
	 * 
	 * The first thing you should remember once and for all regarding screen
	 * coordinates - the upper left corner of the screen is (0,0) and lower
	 * right is (width, height) - no arguing! Now, lets say we got a 3D point in
	 * world coordinate space at (x, y, z) - this is not relative to the camera,
	 * but absolute (so the camera can have coordinates (cx, cy, cz)). The
	 * camera defines the viewMatrix, and I suppose you also have defined a
	 * projectionMatrix (you better have!). The last thing you need is the width
	 * and height of the are you are rendering on (not the whole screen!). If
	 * you have all these things, then it's pretty easy:
	 * 
	 * 
	 * function point2D get2dPoint(Point3D point3D, Matrix viewMatrix, Matrix
	 * projectionMatrix, int width, int height) {
	 * 
	 * Matrix4 viewProjectionMatrix = projectionMatrix * viewMatrix; //transform
	 * world to clipping coordinates point3D =
	 * viewProjectionMatrix.multiply(point3D); int winX = (int) Math.round(((
	 * point3D.getX() + 1 ) / 2.0) * width ); //we calculate -point3D.getY()
	 * because the screen Y axis is //oriented top->down int winY = (int)
	 * Math.round((( 1 - point3D.getY() ) / 2.0) * height ); return new
	 * Point2D(winX, winY); }
	 * 
	 **/

	/**
	 * Convert screen coordinates to OpenGL coordinates
	 * 
	 * @param point2D
	 * @param width
	 * @param height
	 * @param viewMatrix
	 * @param projectionMatrix
	 * @return
	 */
	/**
	 * TBD public Point3D get3dPoint(Point2D point2D, int width, int height,
	 * Matrix viewMatrix, Matrix projectionMatrix) {
	 * 
	 * double x = 2.0 * winX / clientWidth - 1; double y = - 2.0 * winY /
	 * clientHeight + 1; Matrix4 viewProjectionInverse =
	 * inverse(projectionMatrix * viewMatrix);
	 * 
	 * Point3D point3D = new Point3D(x, y, 0F); return
	 * viewProjectionInverse.multiply(point3D); }
	 */

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub

	}



	/**
	 * Print displayed blocks
	 */
	public void displayPrint(String tag, String trace) {
		if (tag == null) {
			tag = "displayList";
		}
		int[] block_ids = scene.getDisplayedIds();
		if (block_ids.length == 0) {
			SmTrace.lg(String.format("%s displayed: None", tag), trace);
			return;
		}

		String str = "";
		
		for (int id : block_ids) {
			if (str != "") {
				str += ", ";
			}
			str += scene.displayedBlocks.getBlock(id);
		}
		SmTrace.lg(String.format("%s displayed:%s", tag, str), trace);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		String action = ae.getActionCommand();
		SmTrace.lg(String.format("action: %s", action));
		try {
			if (controls.ckDoAction(action))
				return;
		} catch (EMBlockError e) {
			SmTrace.lg(String.format(
					"controls.ckDoaction(%s): %s",
					action, e.getMessage()));
			e.printStackTrace();
			return;
		}
	}

	
	/**
	 * Add component control
	 * @throws EMBlockError 
	 */
	public void addBlockButton(EMBCommand bcmd, String action) throws EMBlockError {
		selectPrint(String.format("addBlockButton(%s) select", action), "action");
		try {
			if (bcmd == null) {
				SmTrace.lg(String.format(
						"addBlockButton(%s) with no cmd - ignored",
						action));
				return;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		switch(action) {
			case "emc_duplicateBlockButton":
				duplicateBlock(bcmd); 			// Duplicate selected block
				break;
				
			case "emc_selectAllButton":
				selectAll(bcmd);
				break;
			
			case "emc_selectNoneButton":
				selectNone(bcmd);
				break;
				
			case "emc_selectNextButton":
				selectNext(bcmd);
				break;
			
			case "emc_deleteBlockButton":
				deleteSelection(bcmd);
				break;
				
			case "emc_deleteBlockAllButton":
				deleteAll(bcmd);
				break;
			
			case "emc_addBoxButton":
				createNewBlock(bcmd, "box");
				break;
				
			case "emc_addBallButton":
				createNewBlock(bcmd, "ball");
				break;
				
			case "emc_addConeButton":
				createNewBlock(bcmd, "cone");
				break;
				
			case "emc_addCylinderButton":
				createNewBlock(bcmd, "cylinder");
				break;
								
			case "emc_addImageButton":
				createNewBlock(bcmd, "image");
				break;
				
			case "emc_addPointerButton":
				createNewBlock(bcmd, "pointer");
				break;
				
			case "emc_addTextButton":
				createNewBlock(bcmd, "text");
				break;

			default:
				SmTrace.lg(String.format(
						"Unrecognized addBlockButton: %s - ignored", action));
				return;
		}
	}

	
	/**
	 * Add new text
	 * @throws EMBlockError 
	 */
	public void addTextButton(EMBCommand bcmd, String action) throws EMBlockError {
		selectPrint(String.format("addTextButton(%s) select", action), "action");
		if (bcmd == null) {
			SmTrace.lg(String.format(
					"addTextButton(%s) with no cmd - ignored",
					action));
			return;
		}
		
		EMBlockBase cb_base = ColoredText.newBlock(controls);
		if (cb_base != null) {
			EMBlock cb = new EMBlock(cb_base);
			int id = bcmd.addBlock(cb);
			bcmd.selectBlock(id);		// Select new block
		}
	}

	
	/**
	 * Change selected text, based on control settings
	 * @throws EMBlockError 
	 */
	public void changeTextButton(EMBCommand bcmd, String action) throws EMBlockError {
		selectPrint(String.format("changeTextButton(%s) select", action), "action");
		BlockSelect bg = getSelected();
		int[] ids = bg.getIds();
		for (int i = 0; i < ids.length; i++) {
			int id = ids[i];
			EMBlock cb = getCb(id);
			bcmd.addPrevBlock(cb);
			cb.adjustFromControl((ControlOfText)controls.getControl("text"), bcmd);
			bcmd.addBlock(cb);
		}
	}

	
	/**
	 * Add new text, duplicate of selected
	 * @throws EMBlockError 
	 */
	public void dupTextButton(EMBCommand bcmd, String action) throws EMBlockError {
		selectPrint(String.format("dupTextButton(%s) select", action), "action");
		selectTextButton(bcmd, action);
		addTextButton(bcmd, action);
	}

	
	/**
	 * Populate ControlOfText with currently selected block
	 * @throws EMBlockError 
	 */
	public void selectTextButton(EMBCommand bcmd, String action) throws EMBlockError {
		selectPrint(String.format("selectTextButton(%s) select", action), "action");
	}

	
	/**
	 * Add new pointer
	 * Experiment in pointer(orientation) drawing / debugging
	 * @throws EMBlockError 
	 */
	public void addPointerButton(EMBCommand bcmd, String action) throws EMBlockError {
		selectPrint(String.format("addPointerButton(%s) select", action), "action");
		if (bcmd == null) {
			SmTrace.lg(String.format(
					"addPointerButton(%s) with no cmd - ignored",
					action));
			return;
		}
	}
	
	
	/**
	 * Add new eye
	 * Experiment in eye drawing / debugging
	 * @throws EMBlockError 
	 */
	public void addEyeButton(EMBCommand bcmd, String action) throws EMBlockError {
		selectPrint(String.format("addEyeButton(%s) select", action), "action");
		if (bcmd == null) {
			SmTrace.lg(String.format(
					"addEyeButton(%s) with no cmd - ignored",
					action));
			return;
		}

		EMBlock cb = null;
		Point3D target = new Point3D(0, 0, 0);
		Vector3D up = new Vector3D(0, 1, 0);
		
		if (anySelected() && getSelectedBlock() != null) {
		///if (anySelected()) {
			cb = getSelectedBlock();
			Point3D center = Point3D.sum(
					Point3D.sum(cb.getCenter(),
							Vector3D.mult(normalAtSelectedPoint(),
									0.5f * (float) Math.abs(Vector3D.dot(cb.getDiagonal(), normalAtSelectedPoint())))),
					Vector3D.mult(normalAtSelectedPoint(), EMBlockBase.DEFAULT_SIZE * 0.5f));
			cb = EMBlock.newBlock("eye", center, target, up);
		} else if (prevEye != null) {
			Point3D center = Point3D.sum(
					Point3D.sum(prevEye.getPosition(),
							Vector3D.mult(normalAtSelectedPoint(),
									0.5f * (float) Math.abs(Vector3D.dot(prevEye.getDiagonal(), normalAtSelectedPoint())))),
					Vector3D.mult(normalAtSelectedPoint(), EMBlockBase.DEFAULT_SIZE * 0.5f));
			cb = EMBlock.newBlock("eye", center, target, up);
			
		} else {
			Point3D center = currentViewerCamera().target;
			cb = EMBlock.newBlock("eye", center, target, up);
			normalAtSelectedPoint(new Vector3D(1, 0, 0));
		}
/** No need to adjust		
		try {
			cb.setFromControls(controls);
		} catch (EMBlockError e) {
			SmTrace.lg(String.format("setFromControls %s error: %s",
					cb.blockType(), e.getMessage()));
			return;
		}
		cb.adjustFromControls(controls, bcmd);
	**/
		prevEye = cb;
		int id = bcmd.addBlock(cb);
		bcmd.selectBlock(id);		// Select new block
	}

	
	/**
	 * Add/Remove Control/Display
	 */
	public void setControl(String controlName, boolean on) {
		controls.setControl(controlName, on);
	}

	
	public void setSelectionStateOfBox( int id, boolean state ) {
		scene.setSelectionStateOfBox(id, state);
	}
	

	public void setCheckBox(String name, boolean checked) {
		if (modeler != null)
			modeler.setCheckBox(name, checked);
	}

	
	public ControlsOfScene getControls() {
		return controls;
	}

	
	public EMDisplay getDisplay() {
		EMDisplay disp = new EMDisplay("SceneViewer");
		disp.putBlocks(getDisplayedBlocks());
		return disp;
	}

	/**
	 * Get currently viewed
	 */
	public SceneViewer getSceneViewer() {
		EMBCommand cmd = commandManager.getCurrentCommand();
		if (cmd == null)
			return null;
		
		SceneViewer viewer = cmd.newViewer;
		return viewer;
	}

	/**
	 * Control / Access to highlighted
	 */
	public Point3D hilitedPoint(Point3D point) {
		SceneViewer viewer = getSceneViewer();
		if (viewer == null)
			return new Point3D(0,0,0);
		
		return viewer.hilitedPoint(point);
	}
	
	public Point3D hilitedPoint() {
		return hilitedPoint(null);
	}

	
	public Vector3D normalAtHilitedPoint(Vector3D normal) {
		SceneViewer viewer = getSceneViewer();
		if (viewer == null)
			return EMBox3D.UP;
		
		return viewer.normalAtHilitedPoint(normal);
	}
	
	
	public Vector3D normalAtHilitedPoint() {
		return normalAtHilitedPoint(null);
	}

	/**
	 * Control / Access to selected
	 */
	public Point3D selectedPoint(Point3D point) {
		SceneViewer viewer = getSceneViewer();
		if (viewer == null)
			return new Point3D(0,0,0);
		
		return viewer.selectedPoint(point);
	}
	
	public Point3D selectedPoint() {
		return selectedPoint(null);
	}

	
	public Vector3D normalAtSelectedPoint(Vector3D normal) {
		SceneViewer viewer = getSceneViewer();
		if (viewer == null)
			return EMBox3D.UP;
		
		return viewer.normalAtSelectedPoint(normal);
	}
	
	
	public Vector3D normalAtSelectedPoint() {
		return normalAtSelectedPoint(null);
	}
	
	/**
	 * Repaint each viewer
	 */
	public void repaint() {
		for (SceneViewer viewer: sceneViewers) {
			viewer.repaint();
		}
	}

	public EMBlockGroup getDisplayedBlocks() {
		return scene.displayedBlocks;
	}


	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	
}
