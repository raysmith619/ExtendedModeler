package ExtendedModeler;
import smTrace.SmTrace;

public class ExtendedModelerTest {
	int nRun = 1; // Number of test runs
	boolean doSetup = true; // Do general setup, false - test is responsible for setup
	boolean initEachTest = false; // true - initialize before each test
	boolean initEachRun = false; // true - initialize before each run
	int nTestRun = 1; // Number of times to repeat each test
	int runNo = 0; // Current run number
	int testNo; // Current test number within the current run
	float afterSetupDelay = 1.f; // After setup delay in sec
	float testDelay = .010f; // Delay before each test in sec
	float runDelay = .10f; // Delay before each run in sec
	ExtendedModeler modeler;
	SceneViewer sceneViewer;
	ExtendedModeler setupModeler;
	ControlOfPlacement cpla;
	ControlOfComponent ccom;
	ControlOfColor ccol;
	int tagno = 0; // Tag number for auto generated tags
	int nTest = 0;
	int nPass = 0;
	int nFail = 0;

	/**
	 * Debugging variables
	 */
	float xpos = -2;
	float ypos = -2;
	float zpos = -2;
	float inc = .1f;

	public ExtendedModelerTest() {
	}

	/**
	 * Set number of times to execute run
	 * 
	 * @param nRun
	 *            - number of times run test
	 */
	public void setNRun(int nRun) {
		this.nRun = nRun;
	}

	/**
	 * Get test stats
	 */
	public int getRunNo() {
		return runNo;
	}

	public int getNFail() {
		return nFail;
	}

	public int getNPass() {
		return nPass;
	}

	public int getNTest() {
		return nTest;
	}

	/**
	 * Set number of times to execute each test
	 * 
	 * @param nRun
	 *            - number of times to each test
	 */
	public void setNTestRun(int nTestRun) {
		this.nTestRun = nTestRun;
	}

	/**
	 * Set delay before each run
	 */
	public void setRunDelay(float runDelay) {
		this.runDelay = runDelay;
	}

	/**
	 * Set delay after setup
	 */
	public void setAfterSetupDelay(float delay) {
		this.afterSetupDelay = delay;
	}

	/**
	 * Set delay before each test
	 */
	public void setTestDelay(float testDelay) {
		this.testDelay = testDelay;
	}

	/**
	 * Set to initialize before each run
	 * 
	 * @param initEachRun
	 *            - initialize between runs
	 */
	public void setInitEachRun(boolean initEachRun) {
		this.initEachRun = initEachRun;
	}

	/**
	 * Set to initialize before each test
	 * 
	 * @param initEachTest
	 *            - initialize between tests
	 */
	public void setInitEachTest(boolean initEachTest) {
		this.initEachTest = initEachTest;
	}

	/**
	 * Setup testing
	 * 
	 * @param test_tag
	 * @return
	 * @throws EMBlockError
	 */
	public void setupTest(String test_tag) throws EMTFail {
		if (doSetup) {
			SmTrace.lg("Setup modeler");
			modeler = ExtendedModeler.setupModeler();
			delay(afterSetupDelay);
			sceneViewer = modeler.sceneViewer;
			String control_name = null;
			ControlOf ctl = null;
			control_name = "component";
			ctl = sceneViewer.controls.getControl(control_name);
			if (ctl == null) {
				SmTrace.lg(String.format("Control %s not setup", control_name));
				System.exit(1);
			}
			ccom = (ControlOfComponent) ctl;

			control_name = "placement";
			ctl = sceneViewer.controls.getControl(control_name);
			if (ctl == null) {
				SmTrace.lg(String.format("Control %s not setup", control_name));
				System.exit(1);
			}
			cpla = (ControlOfPlacement) ctl;

			control_name = "color";
			ctl = sceneViewer.controls.getControl(control_name);
			if (ctl == null) {
				throw new EMTFail(String.format("Control %s not setup", control_name));
			}
			ccol = (ControlOfColor) ctl;
		}
	}

	/**
	 * Snap shot current testing SceneViewer display
	 * 
	 * @param tag
	 * @return
	 */
	public EMDisplay ssD(String tag) {
		SmTrace.lg(String.format("Snapshot display: %s", tag));
		EMDisplay disp = new EMDisplay(tag, sceneViewer.getDisplay());
		return disp;
	}

	/**
	 * Generate tags
	 * 
	 * @return
	 * @throws EMBlockError
	 */
	public EMDisplay ssD() {
		tagno++;
		String tag = String.format("Snapshot_%02d", tagno);
		EMDisplay disp = new EMDisplay(tag, sceneViewer.getDisplay());
		return disp;
	}

	public boolean test(String test_tag) throws EMTFail, EMBlockError {
		SmTrace.lg(String.format("\nTesting %s", test_tag));
		boolean res = true;
		try {
			for (int i = 0; i < nRun; i++) {
				runNo++;
				testNo = 0;
				SmTrace.lg(String.format("Run: %d", i + 1));
				if (i == 0 || initEachRun) {
					setupTest(test_tag);
				}
				delay(runDelay);
				testOne(test_tag);
			}

		} catch (EMTFail e) {
			nFail++;
			String fmsg = String.format("Test Fail Run: %d Test: %d Fail: %d %s", runNo, testNo, nFail, e.getMessage());
			throw new EMTFail(fmsg);
		}
		return res;

	}

	/**
	 * Delay before continuing
	 * 
	 * @param dly
	 *            - delay in sec
	 */
	public void delay(float dly) {
		try {
			Thread.sleep((long) (1000 * dly));
		} catch (InterruptedException e) {
		}
	}

	/**
	 * delay allowing double value, esp constants
	 * 
	 * @param dly
	 */
	public void delay(double dly) {
		delay((float) dly);
	}

	/**
	 * Do one test, possibly repeated
	 * 
	 * @throws EMTFail
	 * @throws EMBlockError
	 */
	public void testOne(String test_tag) throws EMTFail, EMBlockError {
		for (int j = 0; j < nTestRun; j++) {
			testNo++;
			nTest++;
			SmTrace.lg(String.format("\nTesting %s  Run: %d Test: %d",
					test_tag, runNo, testNo, test_tag));
			if (j == 2 || initEachTest) { // First setup is done for run
				setupTest(test_tag);
			}
			delay(testDelay);
			switch (test_tag.toLowerCase()) {
			case "all": // Do all tests. Result is true iff ALL pass
				testOne("simple");
				testOne("adj_position");
				testOne("move");
				testOne("undo");
				break;

			case "simple":
				test_simple(test_tag);
				break;

			case "adj_position":
				test_adj_position(test_tag);
				break;

			case "move":
				test_move(test_tag);
				break;

			case "undo":
				test_undo(test_tag);
				break;

			case "ueb":
				test_unexpected_ball(test_tag);
				break;
				
			default:
				SmTrace.lg(String.format("Unrecognized test(%s)", test_tag));
				break;
			}
			nPass++; // Count as pass
			SmTrace.lg(String.format("Run %d Test %d END", runNo, testNo));
		}
	}

	/**
	 * Test adjust position
	 * 
	 * @throws EMBlockError
	 * 
	 */
	private void test_simple(String test_tag) throws EMTFail, EMBlockError {

		if (!ccom.ckDoAction("addBoxButton")) {
			throw new EMTFail("addBoxButton failed");
		}
		String action_pla = "adjposUpButton";
		if (!cpla.ckDoAction(action_pla)) {
			throw new EMTFail(String.format("%s failed", action_pla));
		}
		String action = "addBallButton";
		if (!ccom.ckDoAction(action)) {
			throw new EMTFail(String.format("%s failed", action));
		}
		action_pla = "adjposUpButton";
		if (!cpla.ckDoAction(action_pla)) {
			throw new EMTFail(String.format("%s failed", action_pla));
		}
		sceneViewer.repaint();

		String action_col = "adjcolorUpButton";
		if (!ccol.ckDoAction(action_col)) {
			throw new EMTFail(String.format("%s failed", action_col));
		}
		action = "addConeButton";
		if (!ccom.ckDoAction(action)) {
			throw new EMTFail(String.format("%s failed", action));
		}

		if (!sceneViewer.cmdUndo()) {
			throw new EMTFail("sceneViewer.cmdUndo() failed");
		}
	}

	/**
	 * Test adjust position
	 * 
	 * @throws EMTFail
	 * @throws EMBlockError
	 * 
	 */
	private boolean test_adj_position(String test_tag) throws EMTFail, EMBlockError {
		EMDisplay d1 = ssD("add block");
		EMDisplay d2;
		if (!ccom.ckDoAction("addBoxButton")) {
			throw new EMTFail("addBoxButton failed");
		}
		d2 = ssD("after add block");
		ckDispDiff(d1, d2, 0, 1, "box");
		String action = "addBallButton";
		if (!ccom.ckDoAction(action)) {
			throw new EMTFail(String.format("%s failed", action));
		}
		EMDisplay d3 = ssD("after add ball");
		ckDispDiff(d2, d3, 0, 1, "ball");
		boolean debugging = true;
		if (debugging)
			return true;
		
		String action_pla = "adjposUpButton";
		if (!cpla.ckDoAction(action_pla)) {
			throw new EMTFail(String.format("%s failed", action_pla));
		}
		EMDisplay d4 = ssD("after adjposUpButton");
		ckDispDiff(d3, d4, 1, 1, "ball");

		return true;
	}

	/**
	 * Test short cut move
	 * 
	 * @return
	 * @throws EMBlockError
	 */
	private boolean test_move(String test_tag) throws EMTFail, EMBlockError {
		delay(.1);
		EMDisplay d0 = ssD("Before box");
		if (!ccom.ckDoAction("addBoxButton")) {
			throw new EMTFail("addBoxButton failed");
		}
		EMDisplay d1 = ssD("before move");
		ckDispDiff(d0, d1, 0, 1, "box");
		cpla = (ControlOfPlacement) sceneViewer.controls.getControl("placement");
		delay(.1);

		//EMBlock cb1 = d1.getNewestBlock();
		//Point3D p1 = cb1.getMin();
		Point3D pdest = new Point3D(xpos, ypos, zpos);
		if (!cpla.MoveTo(xpos, ypos, zpos)) {
			SmTrace.lg(String.format("MoveTo(%g, %g, %g) failed", xpos, ypos, zpos));
			return false;
		}
		EMDisplay d2 = ssD("After move");
		ckDispDiff(d1, d2, 1, 1, "box");
		EMBlock cb2 = d2.getNewestBlock();
		Point3D p2 = cb2.getMin();

		Vector3D diff2dest = pdest.diff(pdest, p2);
		float maxdiff = 1.E-6f;
		float difflen = diff2dest.length();
		if (difflen > maxdiff) {
			throw new EMTFail(
					String.format("%s: Block location(%s) distance(%g) > %g" + " away from expected location(%s)",
							d2.getTag(), p2.toString(), difflen, maxdiff, pdest.toString()));
		}
		xpos += inc;
		ypos += inc;
		zpos += inc;
		return true;
	}

	/**
	 * Simple test of undo/redo
	 * @throws EMTFail 
	 * @throws EMBlockError 
	 *
	 * @returns true iff pass and complete
	 */
	public boolean test_undo(String test_tag) throws EMBlockError, EMTFail {
		EMDisplay d_blank_display = ssD("blank_display");
		if (!ccom.ckDoAction("addBoxButton")) {
			throw new EMTFail("addBoxButton failed");
		}
		EMDisplay dfirst = ssD("before undo/redo");
		add_blocks(5, "addBallButton");
		EMDisplay dafter_adds = ssD("after adds");
		undos(5);
		EMDisplay dafter_undos = ssD("after undos");
		if (!dafter_undos.equals(dfirst)) {
			SmTrace.lg(String.format("\nBeginning Display(%s):\n%s\n",
					dfirst.getTag(), dfirst.toString()));
			throw new EMTFail(String.format("%s - we're not back to first"
					+ "\nDisplay:\n%s\n\n",
					dafter_undos.getTag(), dafter_undos.toString()));
		}
		redos(5);
		EMDisplay dafter_redos = ssD("after redos");
		if (!dafter_redos.equals(dafter_adds)) {
			SmTrace.lg(String.format("\nBeginning Display(%s):\n%s\n",
					dafter_adds.getTag(), dafter_adds.toString()));
			String msg = String.format("%s - we're not back to before undos"
					+ "\nDisplay:\n%s\n\n",
					dafter_redos.getTag(), dafter_redos.toString());
			throw new EMTFail(msg);
		}
		undos(5);
		EMDisplay dafter_undos2 = ssD("after undos2");
		if (!dafter_undos.equals(dfirst)) {
			SmTrace.lg(String.format("\nBeginning Display(%s):\n%s\n",
					dfirst.getTag(), dfirst.toString()));
			throw new EMTFail(String.format("%s - we're not back to first"
					+ "\nDisplay:\n%s\n\n",
					dafter_undos.getTag(), dafter_undos.toString()));
		}
		undos(1);
		EMDisplay dafter_undos_final = ssD("after undos_final");
		if (!dafter_undos_final.equals(d_blank_display)) {
			throw new EMTFail(String.format("%s - we're not back to first",
					dafter_undos_final.getTag(), dafter_undos_final.toString()));
		}
		return true;
		
		
	}

	/**
	 * Test unexpected ball - not part of all
	 * 
	 * @throws EMTFail
	 * @throws EMBlockError
	 * 
	 */
	private boolean test_unexpected_ball(String test_tag) throws EMTFail, EMBlockError {
		EMDisplay d1 = ssD("add block");
		boolean debugging = false;
		debugging =  true;
		EMDisplay d2;
		if (!debugging) {
				if (!ccom.ckDoAction("addBoxButton")) {
					throw new EMTFail("addBoxButton failed");
				}
				d2 = ssD("after add block");
				ckDispDiff(d1, d2, 0, 1, "box");
									// Move box off center
				xpos = ypos = zpos = 1;
				if (!cpla.MoveTo(xpos, ypos, zpos)) {
					SmTrace.lg(String.format("MoveTo(%g, %g, %g) failed", xpos, ypos, zpos));
					return false;
				}
		} else {
			d2 = ssD("debugging - no box");			
		}
				
		String action = "addBallButton";
		action = "addConeButton";
		action = "addCylinderButton";
		action = "addBoxButton";
		action = "addBallButton";

		if (!ccom.ckDoAction(action)) {
			throw new EMTFail(String.format("%s failed", action));
		}
		EMDisplay d3 = ssD("after add ball");
		///ckDispDiff(d2, d3, 0, 1, "ball");
		if (debugging) {
			if (!cpla.MoveTo(xpos, ypos, zpos)) {
				SmTrace.lg(String.format("MoveTo(%g, %g, %g) failed", xpos, ypos, zpos));
				return false;
			}
			String action1 = "deleteBlockButton";
			if (!ccom.ckDoAction(action1)) {
				throw new EMTFail(String.format("%s failed", action1));
			}
			
		}
		return true;
	}


	/**
	 * add generic blocks for testing
	 * @param ntoadd
	 * @param blockType
	 * @throws EMBlockError
	 * @throws EMTFail
	 */
	public void add_blocks(int ntoadd, String blockType) throws EMBlockError, EMTFail {
		for (int i = 0; i < ntoadd; i++) {
			if (!ccom.ckDoAction(blockType)) {
				throw new EMTFail(String.format(
						"addBoxButton %s failed",
						blockType));
			}
			
		}
	}

	/**
	 * Do repeated undos
	 */
	public void undos(int ntime) {
		for (int i = 0; i < ntime; i++) {
			sceneViewer.cmdUndo();
		}
	}

	/**
	 * Do repeated redos
	 */
	public void redos(int ntime) {
		for (int i = 0; i < ntime; i++) {
			sceneViewer.cmdRedo();
		}
	}
	
	@SuppressWarnings("unused")
	private EMBCommand newCmd() {
		return newCmd(null);
	}

	private EMBCommand newCmd(String name) {
		if (name == null) {
			name = "test_command";
		}
		EMBCommand bcmd = null;
		try {
			bcmd = new BlkCmdAdd(name);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bcmd;
	}

	/**
	 * Check for expected display changes
	 * 
	 * @throws EMBlockError
	 */
	public void ckDispDiff(EMDisplay d1, EMDisplay d2, int nprev, int nnew, String blockType)
			throws EMTFail, EMBlockError {
		EMDisplayDiff ed = new EMDisplayDiff(d1, d2);
		int nprev_found = ed.nPrev();
		int nnew_found = ed.nNew();
		if (nprev_found != nprev) {
			throw new EMTFail(String.format("Number previous(%d) != expected(%d)", nprev_found, nprev));
		}
		if (nnew_found != nnew) {
			throw new EMTFail(String.format("Number new(%d) != expected(%d)", nnew_found, nnew));
		}
		// Check type if any new
		if (nnew > 0) {
			int[] ids = ed.getNewIds();
			for (int i = 0; i < nnew; i++) {
				EMBlock cb = d2.getBlock(ids[i]);
				if (!cb.blockType().equals(blockType)) {
					throw new EMTFail(String.format("%s: block(id=%d) type=%s not the expected type(%s)", d2.getTag(),
							ids[i], cb.blockType(), blockType));
				}
			}
		}
	}
}
