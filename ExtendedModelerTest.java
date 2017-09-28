import smTrace.SmTrace;

public class ExtendedModelerTest {
	int nRun = 1;					// Number of test runs
	boolean doSetup = true;			// Do general setup, false - test is responsible for setup
	boolean initEachTest = false;	// true - initialize before each test
	boolean initEachRun = false;	// true - initialize before each run
	int nTestRun = 1;				// Number of times to repeat each test
	float afterSetupDelay = 1.f;	// After setup delay in sec
	float testDelay = .010f;		// Delay before each test in sec
	float runDelay = .10f;			// Delay before each run in sec
	ExtendedModeler modeler;
	SceneViewer sceneViewer;
	ExtendedModeler setupModeler;
	ControlOfPlacement cpla;
	ControlOfComponent ccom;
	ControlOfColor ccol;
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
	 * @param nRun - number of times run test
	 */
	public void setNRun(int nRun) {
		this.nRun = nRun;
	}

	
	/**
	 * Set number of times to execute each test
	 * @param nRun - number of times to each test
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
	 * @param initEachRun - initialize between runs
	 */
	public void setInitEachRun(boolean initEachRun) {
		this.initEachRun = initEachRun;
	}

	
	/**
	 * Set to initialize before each test
	 * @param initEachTest - initialize between tests
	 */
	public void setInitEachTest(boolean initEachTest) {
		this.initEachTest = initEachTest;
	}

	
	/**
	 * Setup testing
	 * @param test_tag
	 * @return
	 * @throws OurBlockError 
	 */
	public void setupTest(String test_tag) throws OurBlockError {
		if (doSetup) {
			SmTrace.lg("Setup modeler");
			modeler = ExtendedModeler.setupModeler();
			testDelay(afterSetupDelay);
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
				throw new OurBlockError(String.format("Control %s not setup", control_name));
			}
			ccol = (ControlOfColor) ctl;
		}
	}
	
	
	public boolean test(String test_tag) throws OurBlockError {
		boolean res = true;
		try {
			for (int i = 0; i < nRun; i++) {
				SmTrace.lg(String.format("Run: %d", i+1));
				if (i == 0 || initEachRun) {
					setupTest(test_tag);
				}
				testDelay(runDelay);
				testOne(test_tag);
			}
				
			} catch (EMTFail e) {
		}
		
		return res;
		
	}

	
	/**
	 * Delay before continuing
	 * @param dly - delay in sec
	 */
	public void testDelay(float dly) {
		try {
			Thread.sleep((long) (1000 * dly));
		} catch (InterruptedException e) {
		}
	}

	
	/**
	 * Do one test, possibly repeated
	 * @throws OurBlockError 
	 */
	public void testOne(String test_tag) throws EMTFail, OurBlockError {
		for (int j = 0; j < nTestRun; j++) {
			SmTrace.lg(String.format("Test: %d", j+1));
			if (j == 2 || initEachTest) {	// First setup is done for run
				setupTest(test_tag);
			}
			testDelay(testDelay);
			switch (test_tag.toLowerCase()) {
				case "all":							// Do all tests. Result is true iff ALL pass
					test_simple(test_tag);
					testDelay(testDelay);
					test_adj_position(test_tag);
					testDelay(testDelay);
					test_move(test_tag);
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
					
				default:
					SmTrace.lg(String.format("Unrecognized test(%s)", test_tag));
					break;
			}
			SmTrace.lg(String.format("test %d END", j+1));
		}
	}
	
	/**
	 * Test adjust position
	 * @throws OurBlockError 
	 * 
	 */
	private void test_simple(String test_tag) throws EMTFail, OurBlockError {

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
	 * @throws OurBlockError 
	 * 
	 */
	private boolean test_adj_position(String test_tag) throws OurBlockError {

		if (!ccom.ckDoAction("addBoxButton")) {
			SmTrace.lg("addBoxButton failed");
			return false;
		}
		String action = "addBallButton";
		if (!ccom.ckDoAction(action)) {
			SmTrace.lg(String.format("%s failed", action));
			return false;
		}
		sceneViewer.repaint();
		String action_pla = "adjposUpButton";
		if (!cpla.ckDoAction(action_pla)) {
			SmTrace.lg(String.format("%s failed", action_pla));
			return false;
		}
		
		return true;
	}

	
	/**
	 * Test short cut move
	 * @return
	 * @throws OurBlockError 
	 */
	private boolean test_move(String test_tag)  throws EMTFail, OurBlockError {
		if (!ccom.ckDoAction("addBoxButton")) {
			SmTrace.lg("addBoxButton failed");
			return false;
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cpla = (ControlOfPlacement) sceneViewer.controls.getControl("placement");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!cpla.MoveTo(xpos, ypos, zpos)) {
			SmTrace.lg(String.format("MoveTo(%g, %g, %g) failed", xpos, ypos, zpos));
			return false;
		}
		xpos += inc;
		ypos += inc;
		zpos += inc;
		return true;
	}
	
	
	@SuppressWarnings("unused")
	private BlockCommand newCmd() {
		return newCmd(null);
	}
	
	
	private BlockCommand newCmd(String name) {
		if (name == null) {
			name = "test_command";
		}
		BlockCommand bcmd = null;
		try {
			bcmd = new BlkCmdAdd(name);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bcmd;
	}
}
