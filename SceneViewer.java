import java.awt.Color;
import java.awt.Dimension;
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
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

import smTrace.SmTrace;

class SceneViewer extends GLCanvas implements MouseListener, MouseMotionListener, GLEventListener, ActionListener {
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
	ExtendedModeler modeler;		// Main class - currently only for check button access
	EMBCommandManager commandManager;		// Command do,redo... control
	JFrame frame;					// Access to main frame
	ControlMap controlMap;			// Control Map window/frame
	SmTrace smTrace; 				// Trace support
	ControlsOfView controls;	// Control boxes
	GLAutoDrawable drawable; 		// Save base
	GLU glu; // Needed for scene2WorldCoord
	GLUT glut;
	// Updated each display() call
	int viewport[] = new int[4];
	double mvmatrix[] = new double[16];
	double projmatrix[] = new double[16];
	public Scene scene;
	///public int indexOfSelectedBlock = -1; // -1 for none
									/**
									 * Only used to go to previously
									 * selecte block(s) in case of delete
									 */
	private Stack<BlockSelect> selectStack = new Stack<BlockSelect>();			// Stack of recent selected
	
	private Point3D selectedPoint = new Point3D();
	private Vector3D normalAtSelectedPoint = new Vector3D();
	public int indexOfHilitedBox = -1; // -1 for none
	private Point3D hilitedPoint = new Point3D();
	private Vector3D normalAtHilitedPoint = new Vector3D();

	Camera3D camera = new Camera3D();
	ExtendedModeler.AutoAddType autoAdd = ExtendedModeler.AutoAddType.NONE; // Add
																			// item
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

	public boolean displayAddControl = true;
	public boolean displayColorControl = true;
	public boolean displayPlacementControl = true;
	public boolean displayTextControl = true;
	public boolean displayWorldAxes = false;
	public boolean displayCameraTarget = false;
	public boolean displayBoundingBox = false;
	public boolean enableCompositing = false;

	int mouse_x, mouse_y, old_mouse_x, old_mouse_y;
	
	public SceneViewer(GLCapabilities caps,
		ExtendedModeler modeler,
		JFrame frame,
		SmTrace trace)
		throws EMBlockError {

		super(caps);
		scene = new Scene();
		this.caps = caps;
		this.modeler = modeler;
		this.frame = frame;
		this.smTrace = trace; // Passed in
		this.commandManager = new EMBCommandManager(this);
		addGLEventListener(this);

		addMouseListener(this);
		addMouseMotionListener(this);
		this.controls = new ControlsOfView(this, trace);

		radialMenu.setItemLabelAndID(RadialMenuWidget.CENTRAL_ITEM, "", COMMAND_DUPLICATE_BLOCK);
		radialMenu.setItemLabelAndID(1, "Duplicate Block", COMMAND_DUPLICATE_BLOCK);
		radialMenu.setItemLabelAndID(2, "Set Color to Red", COMMAND_COLOR_RED);
		radialMenu.setItemLabelAndID(3, "Set Color to Yellow", COMMAND_COLOR_YELLOW);
		radialMenu.setItemLabelAndID(4, "Set Color to Green", COMMAND_COLOR_GREEN);
		radialMenu.setItemLabelAndID(5, "Set Color to Blue", COMMAND_COLOR_BLUE);
		radialMenu.setItemLabelAndID(7, "Delete Box", COMMAND_DELETE);

		camera.setSceneRadius((float) Math.max(5 * EMBlockBase.DEFAULT_SIZE,
				scene.getBoundingBoxOfScene().getDiagonal().length() * 0.5f));
		camera.reset();

	}

	
	/**
	 * Get generated blocks' ids
	 */
	public int[] getDisplayedIds() {
		return scene.getDisplayedIds();
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
		///if (!new_select.isEmpty()) {
		///	selectStack.push(new_select);		// Remember if deleting
		///}
		display();
		repaint();
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
		if (selectStack.isEmpty())
			return false;			// Nothing is selected
		
		BlockSelect select = selectStack.peek();
		if (select.hasIndex(id))
			return true;

		return false;
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
		while (!selectStack.isEmpty())
			clearSelection(bcmd);
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
		if (selectStack.isEmpty())
			return;						// None selected
		
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

	public int addNewBlock(EMBCommand bcmd, String blocktype, AlignedBox3D box, Color color) {
		 EMBlock cb = newBlock(blocktype, box, color);
		bcmd.addBlock(cb.iD());
		return cb.iD();
	}

	private EMBlock newBlock(String blocktype, AlignedBox3D box, Color color) {
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

	public int createNewBlock(EMBCommand bcmd, String blockType) {
		Vector3D halfDiagonalOfNewBox = new Vector3D(EMBlockBase.DEFAULT_SIZE * 0.5f, EMBlockBase.DEFAULT_SIZE * 0.5f,
				EMBlockBase.DEFAULT_SIZE * 0.5f);
		EMBlock cb = getSelectedBlock();
		if (cb != null) {
			Point3D centerOfNewBox = Point3D.sum(
					Point3D.sum(cb.getCenter(),
							Vector3D.mult(normalAtSelectedPoint,
									0.5f * (float) Math.abs(Vector3D.dot(cb.getDiagonal(), normalAtSelectedPoint)))),
					Vector3D.mult(normalAtSelectedPoint, EMBlockBase.DEFAULT_SIZE * 0.5f));
			float alpha = cb.getAlpha();
			SmTrace.lg("alpha=" + alpha, "color");
			Color color = new Color(clamp(cb.getRed() + 0.5f * ((float) Math.random() - 0.5f), 0, 1),
					clamp(cb.getGreen() + 0.5f * ((float) Math.random() - 0.5f), 0, 1),
					clamp(cb.getBlue() + 0.5f * ((float) Math.random() - 0.5f), 0, 1), alpha);
			AlignedBox3D box = new AlignedBox3D(Point3D.diff(centerOfNewBox, halfDiagonalOfNewBox),
					Point3D.sum(centerOfNewBox, halfDiagonalOfNewBox));
			cb = EMBlock.newBlock(blockType, box, color);
		} else {
			Point3D centerOfNewBox = camera.target;
			Color color = new Color((float) Math.random(), (float) Math.random(), (float) Math.random(),
					EMBlockBase.DEFAULT_ALPHA);
			AlignedBox3D box = new AlignedBox3D(Point3D.diff(centerOfNewBox, halfDiagonalOfNewBox),
					Point3D.sum(centerOfNewBox, halfDiagonalOfNewBox));
			cb = EMBlock.newBlock(blockType, box, color);
			normalAtSelectedPoint = new Vector3D(1, 0, 0);
		}
		if (cb != null) {
			int id = bcmd.addBlock(cb);
			bcmd.selectBlock(id);		// Select new block
			return id;
		}
		return -1;			// No block
		
	}

	public void createNewBlock(EMBCommand bcmd, String blockType, Point3D at_point, Vector3D size) {
		Vector3D halfDiagonalOfNewBox = new Vector3D(EMBlockBase.DEFAULT_SIZE * 0.5f, EMBlockBase.DEFAULT_SIZE * 0.5f,
				EMBlockBase.DEFAULT_SIZE * 0.5f);
		EMBlock cb = null;
		if (anySelected()) {
			cb = getSelectedBlock();
			Point3D centerOfNewBox = Point3D.sum(
					Point3D.sum(cb.getCenter(),
							Vector3D.mult(normalAtSelectedPoint,
									0.5f * (float) Math.abs(Vector3D.dot(cb.getDiagonal(), normalAtSelectedPoint)))),
					Vector3D.mult(normalAtSelectedPoint, EMBlockBase.DEFAULT_SIZE * 0.5f));
			AlignedBox3D box = new AlignedBox3D(Point3D.diff(centerOfNewBox, halfDiagonalOfNewBox),
					Point3D.sum(centerOfNewBox, halfDiagonalOfNewBox));
			Color color = new Color(clamp(cb.getRed() + 0.5f * ((float) Math.random() - 0.5f), 0, 1),
					clamp(cb.getGreen() + 0.5f * ((float) Math.random() - 0.5f), 0, 1),
					clamp(cb.getBlue() + 0.5f * ((float) Math.random() - 0.5f), 0, 1), cb.getAlpha());

			cb = EMBlock.newBlock(blockType, box, color);
		} else {
			Point3D centerOfNewBox = camera.target;
			AlignedBox3D box = new AlignedBox3D(Point3D.diff(centerOfNewBox, halfDiagonalOfNewBox),
					Point3D.sum(centerOfNewBox, halfDiagonalOfNewBox));
			Color color = new Color((float) Math.random(), (float) Math.random(), (float) Math.random(),
					EMBlockBase.DEFAULT_ALPHA);
			cb = EMBlock.newBlock(blockType, box, color);
			normalAtSelectedPoint = new Vector3D(1, 0, 0);
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
	public EMBlock cb(int id) {
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
		String blockType = cb(id).blockType();
		int idnew = createNewBlock(bcmd, blockType);
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

	public void lookAtSelection() {
		int id = getSelectedBlockIndex();
		if (id >= 0) {
			Point3D p = scene.getBox(id).getCenter();
			camera.lookAt(p);
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

	public void resetCamera() {
		camera.setSceneRadius((float) Math.max(5 * EMBlockBase.DEFAULT_SIZE,
				scene.getBoundingBoxOfScene().getDiagonal().length() * 0.5f));
		camera.reset();
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
		this.drawable = drawable; // Save for later
		GL2 gl = (GL2) drawable.getGL();
		gl.glClearColor(0, 0, 0, 0);
		glu = new GLU();
		glut = new GLUT();
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL gl = drawable.getGL();
		camera.setViewportDimensions(width, height);

		// set viewport
		gl.glViewport(0, 0, width, height);
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
		this.drawable = drawable;
		GL2 gl = (GL2) drawable.getGL();
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		camera.transform(gl);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glFrontFace(GL.GL_CCW);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glShadeModel(GL2.GL_FLAT);

		scene.drawScene(drawable, indexOfHilitedBox, enableCompositing);
		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, mvmatrix, 0);
		gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projmatrix, 0);
		if (SmTrace.tr("projection")) {
			printMatrix("display(): mvmatrix", mvmatrix);
			printMatrix("display(): projmatrix", projmatrix);
		}

		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		SmTrace.lg(String.format("display():    viewport: %d %d %d %d", viewport[0], viewport[1],
				viewport[2], viewport[3]), "projection");
		
		setControl("placement", displayPlacementControl);
		setControl("color", displayColorControl);
		setControl("text", displayTextControl);
		setControl("component", displayAddControl);
		controls.display(drawable);
		if (displayWorldAxes) {
			gl.glBegin(GL.GL_LINES);
			gl.glColor3f(1, 0, 0);
			gl.glVertex3f(0, 0, 0);
			gl.glVertex3f(1, 0, 0);
			gl.glColor3f(0, 1, 0);
			gl.glVertex3f(0, 0, 0);
			gl.glVertex3f(0, 1, 0);
			gl.glColor3f(0, 0, 1);
			gl.glVertex3f(0, 0, 0);
			gl.glVertex3f(0, 0, 1);
			gl.glEnd();
		}
		if (displayCameraTarget) {
			gl.glBegin(GL.GL_LINES);
			gl.glColor3f(1, 1, 1);
			gl.glVertex3fv(Point3D.sum(camera.target, new Vector3D(-0.5f, 0, 0)).get(), 0);
			gl.glVertex3fv(Point3D.sum(camera.target, new Vector3D(0.5f, 0, 0)).get(), 0);
			gl.glVertex3fv(Point3D.sum(camera.target, new Vector3D(0, -0.5f, 0)).get(), 0);
			gl.glVertex3fv(Point3D.sum(camera.target, new Vector3D(0, 0.5f, 0)).get(), 0);
			gl.glVertex3fv(Point3D.sum(camera.target, new Vector3D(0, 0, -0.5f)).get(), 0);
			gl.glVertex3fv(Point3D.sum(camera.target, new Vector3D(0, 0, 0.5f)).get(), 0);
			gl.glEnd();
		}
		if (displayBoundingBox) {
			gl.glColor3f(0.5f, 0.5f, 0.5f);
			scene.drawBoundingBoxOfScene(drawable);
		}

		if (radialMenu.isVisible()) {
			radialMenu.draw(gl, glut, getWidth(), getHeight());
		}

		// gl.glFlush(); // I don't think this is necessary
	}

	private void updateHiliting() {
		Ray3D ray = camera.computeRay(mouse_x, mouse_y);
		Point3D newIntersectionPoint = new Point3D();
		Vector3D newNormalAtIntersection = new Vector3D();
		int newIndexOfHilitedBox = scene.getIndexOfIntersectedBox(ray, newIntersectionPoint, newNormalAtIntersection);
		hilitedPoint.copy(newIntersectionPoint);
		normalAtHilitedPoint.copy(newNormalAtIntersection);
		if (newIndexOfHilitedBox != indexOfHilitedBox) {
			indexOfHilitedBox = newIndexOfHilitedBox;
			repaint();
		}
	}

	public void mouseClicked(MouseEvent e) {
		SmTrace.lg("mouseClick", "mouse");
		if (e.isControlDown())
			SmTrace.lg("isControlDown");
		if (indexOfHilitedBox >= 0) {
			if (e.isControlDown()) {
				EMBCommand bcmd;
				String action = "mouseAddSelect";
				try {
					bcmd = new BlkCmdAdd(action);
				} catch (Exception e2) {
					e2.printStackTrace();
					return;
				}
				selectAdd(bcmd, indexOfHilitedBox, true);		// keep with selected
				bcmd.doCmd();
			} else {
				EMBCommand bcmd;
				String action = "mouseNewSelect";
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
		selectedPoint.copy(hilitedPoint);
		normalAtSelectedPoint.copy(normalAtHilitedPoint);
		repaint();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();
		SmTrace.lg(String.format("mousePressed(%d,%d)", mouse_x, mouse_y), "mouse");
		if (autoAdd != ExtendedModeler.AutoAddType.NONE) {
			// Placement point assuming z == 0
			double world_z = 0;
			double wcoord[] = new double[4];
			screen2WorldCoord(mouse_x, mouse_y, world_z, wcoord);
			Point3D p = new Point3D((float) wcoord[0], (float) wcoord[1], (float) world_z);
			if (anySelected()) {
				Point3D pnew = new Point3D((float) wcoord[0], (float) wcoord[1], (float) wcoord[2]);
				p = pnew;
			}
			camera.lookAt(p);
			EMBCommand bcmd = null;
			switch (autoAdd) {
			case DUPLICATE:
				try {
					bcmd = new BlkCmdAdd("duplicate");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
				try {
					duplicateBlock(bcmd);
				} catch (EMBlockError e2) {
					e2.printStackTrace();
					SmTrace.lg("duplicateBlock error");
					return;
				}
				break;

			case BOX:
				try {
					bcmd = new BlkCmdAdd("duplicate");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
				createNewBlock(bcmd, "box");
				break;

			case BALL:
				try {
					bcmd = new BlkCmdAdd("ball");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
				createNewBlock(bcmd, "ball");
				break;

			case CONE:
				try {
					bcmd = new BlkCmdAdd("cone");
				} catch (Exception e1) {
					e1.printStackTrace();
					return;
				}
				createNewBlock(bcmd, "cone");
				break;

			case CYLINDER:
				try {
					bcmd = new BlkCmdAdd("cylinder");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
				createNewBlock(bcmd, "cylinder");
				break;

			case TEXT:
				try {
					bcmd = new BlkCmdAdd("text");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
				createNewBlock(bcmd, "text");
				break;
				
			default:
				SmTrace.lg("Unrecognized AutoAdd value - ignored");
			}
			if (bcmd != null)
				bcmd.doCmd();
			return;
		}
		if (radialMenu.isVisible()
				|| (SwingUtilities.isRightMouseButton(e) && !e.isShiftDown() && !e.isControlDown())) {
			int returnValue = radialMenu.pressEvent(mouse_x, mouse_y);
			if (returnValue == CustomWidget.S_REDRAW)
				repaint();
			if (returnValue != CustomWidget.S_EVENT_NOT_CONSUMED)
				return;
		}

		updateHiliting();
	}

		
	public void mouseReleased(MouseEvent e) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();
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
					bcmd = new BlkCmdAdd("create_box");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
				createNewBlock(bcmd, "box");
				break;
			case COMMAND_CREATE_BALL:
				try {
					bcmd = new BlkCmdAdd("create_ball");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
				createNewBlock(bcmd, "ball");
				break;
			case COMMAND_CREATE_CONE:
				try {
					bcmd = new BlkCmdAdd("create_cone");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
				createNewBlock(bcmd, "cone");
				break;
			case COMMAND_CREATE_CYLINDAR:
				try {
					bcmd = new BlkCmdAdd("create_cylinder");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
				createNewBlock(bcmd, "cylinder");
				break;
			case COMMAND_COLOR_RED:
				try {
					bcmd = new BlkCmdAdd("color_red");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
				setColorOfSelection(bcmd, 1, 0, 0);
				break;
			case COMMAND_COLOR_YELLOW:
				try {
					bcmd = new BlkCmdAdd("color_yello");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
				setColorOfSelection(bcmd, 1, 1, 0);
				break;
			case COMMAND_COLOR_GREEN:
				try {
					bcmd = new BlkCmdAdd("color_green");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
				setColorOfSelection(bcmd, 0, 1, 0);
				break;
			case COMMAND_COLOR_BLUE:
				try {
					bcmd = new BlkCmdAdd("color_blue");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
				setColorOfSelection(bcmd, 0, 0, 1);
				break;
			case COMMAND_DELETE:
				try {
					bcmd = new BlkCmdAdd("delete");
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
	}

	public void mouseMoved(MouseEvent e) {

		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();

		if (radialMenu.isVisible()) {
			int returnValue = radialMenu.moveEvent(mouse_x, mouse_y);
			if (returnValue == CustomWidget.S_REDRAW)
				repaint();
			if (returnValue != CustomWidget.S_EVENT_NOT_CONSUMED)
				return;
		}

		updateHiliting();
	}

	public void mouseDragged(MouseEvent e) {
		old_mouse_x = mouse_x;
		old_mouse_y = mouse_y;
		mouse_x = e.getX();
		mouse_y = e.getY();
		int delta_x = mouse_x - old_mouse_x;
		int delta_y = old_mouse_y - mouse_y;

		if (radialMenu.isVisible()) {
			int returnValue = radialMenu.dragEvent(mouse_x, mouse_y);
			if (returnValue == CustomWidget.S_REDRAW)
				repaint();
			if (returnValue != CustomWidget.S_EVENT_NOT_CONSUMED)
				return;
		} else if (e.isControlDown()) {
			if (SwingUtilities.isLeftMouseButton(e) && SwingUtilities.isRightMouseButton(e)) {
				camera.dollyCameraForward((float) (3 * (delta_x + delta_y)), false);
			} else if (SwingUtilities.isLeftMouseButton(e)) {
				camera.orbit(old_mouse_x, old_mouse_y, mouse_x, mouse_y);
			} else {
				camera.translateSceneRightAndUp((float) (delta_x), (float) (delta_y));
			}
			repaint();
		} else if (SwingUtilities.isLeftMouseButton(e) && !e.isControlDown() && anySelected()) {
			if (!e.isShiftDown()) {
				EMBlock cb = getSelectedBlock();
				if (cb == null)
					return;
				// translate a box

				Ray3D ray1 = camera.computeRay(old_mouse_x, old_mouse_y);
				Ray3D ray2 = camera.computeRay(mouse_x, mouse_y);
				Point3D intersection1 = new Point3D();
				Point3D intersection2 = new Point3D();
				Plane plane = new Plane(normalAtSelectedPoint, selectedPoint);
				if (plane.intersects(ray1, intersection1, true) && plane.intersects(ray2, intersection2, true)) {
					EMBCommand bcmd;
					String action = "mouseDragBlock";
					try {
						bcmd = new BlkCmdAdd(action);
					} catch (Exception e2) {
						e2.printStackTrace();
						return;
					}
					bcmd.addPrevBlock(cb);
					Vector3D translation = Point3D.diff(intersection2, intersection1);
					scene.translateBlock(getSelectedBlockIndex(), translation);
					bcmd.addBlock(cb);
					bcmd.doCmd();
				}
			} else {
				// resize a box
				SmTrace.lg("resize");
				Ray3D ray1 = camera.computeRay(old_mouse_x, old_mouse_y);
				Ray3D ray2 = camera.computeRay(mouse_x, mouse_y);
				Point3D intersection1 = new Point3D();
				Point3D intersection2 = new Point3D();
				Vector3D v1 = Vector3D.cross(normalAtSelectedPoint, ray1.direction);
				Vector3D v2 = Vector3D.cross(normalAtSelectedPoint, v1);
				Plane plane = new Plane(v2, selectedPoint);
				if (plane.intersects(ray1, intersection1, true) && plane.intersects(ray2, intersection2, true)) {
					Vector3D translation = Point3D.diff(intersection2, intersection1);
					SmTrace.lg(String.format("resize translation: %s", translation));

					// project the translation onto the normal, so that it is
					// only along one axis
					translation = Vector3D.mult(normalAtSelectedPoint,
							Vector3D.dot(normalAtSelectedPoint, translation));
					scene.resizeBlock(getSelectedBlockIndex(), getSelectedBlock().getBox()
							.getIndexOfExtremeCorner(normalAtSelectedPoint), translation);
					repaint();
				}
			}
		}
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
			case "duplicateBlockButton":
				duplicateBlock(bcmd); 			// Duplicate selected block
				break;
				
			case "deleteBlockButton":
				deleteSelection(bcmd);
				break;
				
			case "deleteBlockAllButton":
				deleteAll(bcmd);
				break;
			
			case "addBoxButton":
				createNewBlock(bcmd, "box");
				break;
				
			case "addBallButton":
				createNewBlock(bcmd, "ball");
				break;
				
			case "addConeButton":
				createNewBlock(bcmd, "cone");
				break;
				
			case "addCylinderButton":
				createNewBlock(bcmd, "cylinder");
				break;
				
			case "addTextButton":
				createNewBlock(bcmd, "text");
				break;
				
			default:
				SmTrace.lg(String.format(
						"Unrecognized addBlockButton: %s - ignored", action));
				return;
		}
	}
	
	/**
	 * Add/Remove Control/Display
	 */
	public void setControl(String controlName, boolean on) {
		controls.setControl(controlName, on);
	}

	

	public void setCheckBox(String name, boolean checked) {
		if (modeler != null)
			modeler.setCheckBox(name, checked);
	}


	public EMDisplay getDisplay() {
		EMDisplay disp = new EMDisplay("SceneViewer");
		disp.putBlocks(getDisplayedBlocks());
		return disp;
	}


	private EMBlockGroup getDisplayedBlocks() {
		return scene.displayedBlocks;
	}

	
}
