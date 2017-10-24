/**
 * ExtendedModeler - Adapted from SimpleModeller-3D_JavaApplication-JOGL
   Still trying to work with GitHub.
 */

import java.awt.Container;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.BoxLayout;

import com.jogamp.opengl.GLCapabilities;

import smTrace.SmTrace;		// Execution Trace support








public class ExtendedModeler implements ActionListener {

	static final String applicationName = "Extended Modeler";
	public static SmTrace smTrace;

	JFrame frame;
	Container toolPanel;
	SceneViewer sceneViewer;
	private static ExtendedModeler em_base = null;		// Used internally		
	private static ExtendedModelerTest emt = null;		// Set if testing setup
	private static int mainArgIndex;					// current Arg index
	private static String[] mainArgs;						// Args array
	private static int mainArgsLength;					// Number of args
	private static String logName ="emt_";				// Default log prefix

	public enum AutoAddType {	// Auto add block type
		NONE,					// none added
		PLACEMENT_COMPUTE,		// Block placement
		PLACEMENT_DRAG,
		PLACEMENT_PUSH,
		PLACEMENT_ROTATE,
		DUPLICATE,
		BOX,
		BALL,
		CONE,
		CYLINDER,
		TEXT
	}
	
	JMenuItem deleteAllMenuItem, quitMenuItem, aboutMenuItem;
	
	JMenuItem undoMenuItem;			// Command undo
	JMenuItem redoMenuItem;			// Command redo
	JMenuItem repeatMenuItem;		// Command repeat
	JButton undoButton;				// Command undo
	JButton redoButton;				// Command redo
	JButton repeatButton;			// Command repeat
	
	JMenuItem placementMenuItem;
	JMenuItem placementComputeMenuItem;
	JMenuItem placementDragMenuItem;
	JMenuItem placementPushMenuItem;
	JMenuItem placementRotateMenuItem;

	JMenuItem colorMenuItem;
	JMenuItem colorComputeMenuItem;

	JMenuItem duplicateMenuItem;
	JMenuItem boxMenuItem;
	JMenuItem ballMenuItem;
	JMenuItem coneMenuItem;
	JMenuItem cylinderMenuItem;
	JMenuItem cancelAddMenuItem;
	JButton createBoxButton;
	JButton createBallButton;
	JButton createConeButton;
	JButton createCylinderButton;
	JButton deleteSelectionButton;
	JButton lookAtSelectionButton;
	JButton resetCameraButton;
	JCheckBox displayAddControlCheckBox;
	JCheckBox displayPlacementControlCheckBox;
	JCheckBox displayColorControlCheckBox;
	JCheckBox displayTextControlCheckBox;
	JCheckBox displayWorldAxesCheckBox;
	JCheckBox displayCameraTargetCheckBox;
	JCheckBox displayBoundingBoxCheckBox;
	JCheckBox enableCompositingCheckBox;

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == undoButton || source == undoMenuItem) {
			sceneViewer.cmdUndo();
			return;
		}
		else if (source == redoButton || source == redoMenuItem) {
			sceneViewer.cmdRedo();
			return;
		}
		else if (source == repeatButton || source == repeatMenuItem) {
			sceneViewer.cmdRepeat();
			return;
		}
		if ( source == deleteAllMenuItem ) {
			int response = JOptionPane.showConfirmDialog(
				frame,
				"Really delete all?",
				"Confirm Delete All",
				JOptionPane.YES_NO_OPTION
			);

			if (response == JOptionPane.YES_OPTION) {
				EMBCommand bcmd;
				try {
					bcmd = new BlkCmdAdd("deleteAllMenuItem");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
				sceneViewer.deleteAll(bcmd);
				bcmd.doCmd();
			}
		}
		else if ( source == quitMenuItem ) {
			int response = JOptionPane.showConfirmDialog(
				frame,
				"Really quit?",
				"Confirm Quit",
				JOptionPane.YES_NO_OPTION
			);

			if (response == JOptionPane.YES_OPTION) {
				System.exit(0);
			}
		}
		else if ( source == aboutMenuItem ) {
			JOptionPane.showMessageDialog(
				frame,
				"'" + applicationName + "' Sample Program\n"
					+ "Original version written April-May 2008",
				"About",
				JOptionPane.INFORMATION_MESSAGE
			);
		}
		else if ( source == deleteSelectionButton ) {
			EMBCommand bcmd;
			try {
				bcmd = new BlkCmdAdd("deleteSelectionButton");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return;
			}
			sceneViewer.deleteSelection(bcmd);
			bcmd.doCmd();
		}
		else if ( source == lookAtSelectionButton ) {
			sceneViewer.lookAtSelection();
			sceneViewer.repaint();
		}
		else if ( source == resetCameraButton ) {
			sceneViewer.resetCamera();
			sceneViewer.repaint();
		}
		else if ( source == displayAddControlCheckBox ) {
			sceneViewer.displayAddControl = ! sceneViewer.displayAddControl;
			sceneViewer.setControl("component", sceneViewer.displayAddControl);
			sceneViewer.repaint();
		}
		else if ( source == displayPlacementControlCheckBox ) {
			sceneViewer.displayPlacementControl = ! sceneViewer.displayPlacementControl;
			sceneViewer.setControl("placement", sceneViewer.displayPlacementControl);
			sceneViewer.repaint();
		}
		else if ( source == displayColorControlCheckBox ) {
			sceneViewer.displayColorControl = ! sceneViewer.displayColorControl;
			sceneViewer.setControl("color", sceneViewer.displayColorControl);
			sceneViewer.repaint();
		}
		else if ( source == displayTextControlCheckBox ) {
			sceneViewer.displayTextControl = ! sceneViewer.displayTextControl;
			sceneViewer.setControl("text", sceneViewer.displayTextControl);
			sceneViewer.repaint();
		}
		else if ( source == displayWorldAxesCheckBox ) {
			sceneViewer.displayWorldAxes = ! sceneViewer.displayWorldAxes;
			sceneViewer.repaint();
		}
		else if ( source == displayCameraTargetCheckBox ) {
			sceneViewer.displayCameraTarget = ! sceneViewer.displayCameraTarget;
			sceneViewer.repaint();
		}
		else if ( source == displayBoundingBoxCheckBox ) {
			sceneViewer.displayBoundingBox = ! sceneViewer.displayBoundingBox;
			sceneViewer.repaint();
		}
		else if ( source == enableCompositingCheckBox ) {
			sceneViewer.enableCompositing = ! sceneViewer.enableCompositing;
			sceneViewer.repaint();
		}
	}


	// For thread safety, this should be invoked
	// from the event-dispatching thread.
	//
	private void createUI() {
		if ( ! SwingUtilities.isEventDispatchThread() ) {
			SmTrace.lg(
				"Warning: UI is not being created in the Event Dispatch Thread!");
			assert false;
		}
		frame = new JFrame( applicationName );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		JMenuBar menuBar = new JMenuBar();
			JMenu menu = new JMenu("File");
				deleteAllMenuItem = new JMenuItem("Delete All");
				deleteAllMenuItem.addActionListener(this);
				menu.add(deleteAllMenuItem);

				menu.addSeparator();

				quitMenuItem = new JMenuItem("Quit");
				quitMenuItem.addActionListener(this);
				menu.add(quitMenuItem);
			menuBar.add(menu);
		
			menu = new JMenu("CMD");
				undoMenuItem = new JMenuItem("Undo");
				undoMenuItem.addActionListener(this);
				menu.add(undoMenuItem);
				redoMenuItem = new JMenuItem("Redo");
				redoMenuItem.addActionListener(this);
				menu.add(redoMenuItem);
				repeatMenuItem = new JMenuItem("Repeat");
				repeatMenuItem.addActionListener(this);
				menu.add(repeatMenuItem);
			menuBar.add(menu);
	
			menu = new JMenu("Help");
				aboutMenuItem = new JMenuItem("About");
				aboutMenuItem.addActionListener(this);
				menu.add(aboutMenuItem);
			menuBar.add(menu);
			
			menuBar.add(new JSeparator());
			undoButton = new JButton("Undo");
			undoButton.addActionListener(this);
			menuBar.add(undoButton);
			
			redoButton = new JButton("Redo");
			redoButton.addActionListener(this);
			menuBar.add(redoButton);
			
			repeatButton = new JButton("Repeat");
			repeatButton.addActionListener(this);
			menuBar.add(repeatButton);
					
					
		frame.setJMenuBar(menuBar);

		toolPanel = new JPanel();
		toolPanel.setLayout( new BoxLayout( toolPanel, BoxLayout.Y_AXIS ) );

		// Need to set visible first before starting the rendering thread due
		// to a bug in JOGL. See JOGL Issue #54 for more information on this
		// https://jogl.dev.java.net/issues/show_bug.cgi?id=54
		frame.setVisible(true);

		GLCapabilities caps = new GLCapabilities(null);
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);
		try {
			sceneViewer = new SceneViewer(caps, this, frame, smTrace);
		} catch (EMBlockError e) {
			SmTrace.lg(String.format("SceneViewer error %s", e.getMessage()));
			e.printStackTrace();
			return;
		}

		Container pane = frame.getContentPane();
		// We used to use a BoxLayout as the layout manager here,
		// but it caused problems with resizing behavior due to
		// a JOGL bug https://jogl.dev.java.net/issues/show_bug.cgi?id=135
		pane.setLayout( new BorderLayout() );
		pane.add( toolPanel, BorderLayout.LINE_START );
		pane.add( sceneViewer, BorderLayout.CENTER );


		lookAtSelectionButton = new JButton("Look At Selection");
		lookAtSelectionButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		lookAtSelectionButton.addActionListener(this);
		toolPanel.add( lookAtSelectionButton );

		resetCameraButton = new JButton("Reset Camera");
		resetCameraButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		resetCameraButton.addActionListener(this);
		toolPanel.add( resetCameraButton );

		displayAddControlCheckBox = new JCheckBox("Display Add Control", sceneViewer.displayAddControl);
		displayAddControlCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayAddControlCheckBox.addActionListener(this);
		toolPanel.add(displayAddControlCheckBox);

		displayPlacementControlCheckBox = new JCheckBox("Display Placement Control", sceneViewer.displayPlacementControl);
		displayPlacementControlCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayPlacementControlCheckBox.addActionListener(this);
		toolPanel.add(displayPlacementControlCheckBox);

		displayColorControlCheckBox = new JCheckBox("Display Color Control", sceneViewer.displayColorControl);
		displayColorControlCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayColorControlCheckBox.addActionListener(this);
		toolPanel.add(displayColorControlCheckBox);

		displayTextControlCheckBox = new JCheckBox("Display Text Control", sceneViewer.displayTextControl);
		displayTextControlCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayTextControlCheckBox.addActionListener(this);
		toolPanel.add(displayTextControlCheckBox);

		displayWorldAxesCheckBox = new JCheckBox("Display World Axes", sceneViewer.displayWorldAxes );
		displayWorldAxesCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayWorldAxesCheckBox.addActionListener(this);
		toolPanel.add( displayWorldAxesCheckBox );

		displayCameraTargetCheckBox = new JCheckBox("Display Camera Target", sceneViewer.displayCameraTarget );
		displayCameraTargetCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayCameraTargetCheckBox.addActionListener(this);
		toolPanel.add( displayCameraTargetCheckBox );

		displayBoundingBoxCheckBox = new JCheckBox("Display Bounding Box", sceneViewer.displayBoundingBox );
		displayBoundingBoxCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayBoundingBoxCheckBox.addActionListener(this);
		toolPanel.add( displayBoundingBoxCheckBox );

		enableCompositingCheckBox = new JCheckBox("Enable Compositing", sceneViewer.enableCompositing );
		enableCompositingCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		enableCompositingCheckBox.addActionListener(this);
		toolPanel.add( enableCompositingCheckBox );

		frame.pack();
		frame.setVisible( true );
	}

	/**
	 * Set/Clear check box
	 * and associated indicator variable
	 * 
	 */
	public void setCheckBox(String name, boolean checked) {
		if (SmTrace.tr("checkbox")) {
			SmTrace.lg(String.format("setCheckBox(%s, %b)", name, checked), "checkbox");
			sceneViewer.selectPrint(String.format("modeler.setCheckBox(%s): selected;", name), "select");
		}
		switch (name) {
			case "component":
				displayAddControlCheckBox.setSelected(checked);
				sceneViewer.displayAddControl = checked;
				break;
				
			case "placement":
				displayPlacementControlCheckBox.setSelected(checked);
				sceneViewer.displayPlacementControl = checked;
				break;
				
			case "color":
				displayColorControlCheckBox.setSelected(checked);
				sceneViewer.displayColorControl = checked;
				break;
				
			case "text":
				displayTextControlCheckBox.setSelected(checked);
				sceneViewer.displayTextControl = checked;
				break;
			
			default:
				SmTrace.lg(String.format("Unrecognized button name: %s - ignored", name));
		}
	}
	
	
	public static void main( String[] args ) throws EMBlockError, EMTFail {
		int n_test = 0;				// Number of testit calls
		boolean endAfterTest = false;	// true - end if testing
		
		String arg_str = "";
		for (String arg : args) {
			if (!arg_str.equals(""))
				arg_str += " ";
			arg_str += arg;
		}
		setupTest();
		String helpStr = "Extended Modeler - a simple graphics tool\n"
				+ "In general command line options are in the form:\n"
				+ "    --option-name [option-value]\n"
				+ "where:\n"
				+ "    option-name is case insensitive and usually has a optional shortened form\n"
				+ "    option-value is optional and depends on the option-name\n"
				+ "If the option-value is absent - at end of line, or another --option-name follows,\n"
				+ "then a default value is used.\n"
				+ "Options may appear in any order\n"
				+ "If an option appears multiple times, the most recent option\n"
				+ "takes effect untill the next occurance of that option.\n"
				+ "\n"
				+ "Options:\n"
				+ "    help(he)  - print this message and quit\n"
				+ "    trace [str(value)] - values e.g. \"select\" ]\n"
				+ "    test [str(test-tag)] - test to perform\n"
				+ "    testafterSetup(tas) [float(delay)] - delay(seconds) after setup\n"
				+ "    testrun(tr) [int(nrun)] - number of times to run test\n"
				+ "    testrundelay(trd) [float(delay)] - delay before each run\n"
				+ "    testtest(tt) [int(ntest)] - number of times torun each sub-test\n"
				+ "    testtestdelay(ttd) [float(delay)] - delay(seconds) before each test\n"
				+ "    testend(te)  - end run afer testing\n"
				+ "\n";
		/**
		 * Run 3 passes to facilitate picking up all options including logging options
		 * pass 1 - logging options
		 * pass 2 - gather possibly late options
		 * pass 3 - run tests
		 */
		mainArgs = args;
		mainArgsLength = mainArgs.length;
		/**
		 * 3 pas
		 */
		SmTrace.setLogName(logName); 	// Setup default log name
		for (int npass = 1; npass <= 3; npass++) {
			if (npass == 2) {
				SmTrace.lg("setupTest()");
				SmTrace.lg(String.format("Command Args: %s", arg_str));		// Logging options are set				
			}
			for (mainArgIndex = 0; mainArgIndex < mainArgsLength; ) {
				String arg = args[mainArgIndex++];
				if (arg.startsWith("--")) {
					String str_val = "";
					boolean boolean_val = false;
					String opt = arg.substring(2).toLowerCase();
					switch (opt) {
						case "log":
						case "lg":
							str_val = strArg(logName);
							if (npass > 1)
								continue;
							
							SmTrace.setLogName(str_val);
							break;
							
						case "logStdOut":
						case "lgso":
							boolean_val = booleanArg(true);
							SmTrace.setLogToStd(boolean_val);
							break;
							
						case "logTsScreen":
						case "lgts":
							boolean_val = booleanArg(true);
							SmTrace.setLogStdTs(boolean_val);
							break;
					
					
						case "help":
						case "h":
							if (npass < 2)		// Let logging options be processed
								continue;
							
							System.out.println(helpStr);
							System.exit(0);
							break;
							
						case "trace":
							str_val = strArg("ALL");
							SmTrace.setFlags(str_val);
							break;
							
						case "test":
							boolean res = false;		// Set true iff PASS
							String test_name = strArg("ALL");
							if (npass < 3)
								continue;		// Skip till pass 3
							
							n_test++;
							res = testit(test_name);
							if (!res) {
								SmTrace.lg(String.format("Test %s FAILED", test_name));
							}
							break;
						
						case "testafterSetup":
						case "tas":
							float afterSetupDelay = floatArg(1.f);
							emt.setAfterSetupDelay(afterSetupDelay);
							break;
							
						case "testrun":
						case "tr":
							int nRun = intArg(1);
							emt.setNRun(nRun);
							break;
							
						case "testrundelay":
						case "trd":
							float runDelay = floatArg(1.f);
							emt.setRunDelay(runDelay);
							break;
							
						case "testtest":
						case "tt":
							int nTestRun = intArg(1);
							emt.setNTestRun(nTestRun);
							break;
							
						case "testtestdelay":
						case "ttd":
							float testDelay = floatArg(1f);
							emt.setTestDelay(testDelay);
							break;
						
						case "testend":
						case "te":
							endAfterTest = booleanArg(true);		// Quit program after testing completed, else continues
							break;
							
						default:
							SmTrace.lg(String.format("Unrecognized flag(%s)", opt));
							break;
					}
				} else {
					SmTrace.lg(String.format("Unrecognized arg(%s) - quitting", arg));
					System.exit(1);
				}
						
			}
		}
		if (n_test > 0) {
			SmTrace.lg(String.format("End of %d runs  %d Tests  %d PASSES  %d FAILS",
						emt.getRunNo(), emt.getNTest(), emt.getNPass(), emt.getNFail()));
			if (endAfterTest)
				System.exit(0);
		} else {
			setupModeler();
		}
	}

	/**
	 * main argument value parsing
	 * Checking for optionally missing values
	 * Providing default values
	 */

	
	/**
	 * Get next arg if exists, or default
	 * @param def - default value
	 */
	private static boolean booleanArg(boolean def) {
		if (mainArgIndex >= mainArgsLength)
			return def;		// No more args
		if (mainArgs[mainArgIndex].startsWith("--"))
			return def;		// Next is an option name
		String val_str = mainArgs[mainArgIndex++];
		boolean val = false;
		if (val_str.equalsIgnoreCase("true")
				|| val_str.equalsIgnoreCase("on")
				|| val_str.equals("1"))
			val = true;
		return val;
	}

	
	/**
	 * Get next arg if exists, or default
	 * @param def - default value
	 */
	private static float floatArg(float def) {
		if (mainArgIndex >= mainArgsLength)
			return def;		// No more args
		if (mainArgs[mainArgIndex].startsWith("--"))
			return def;		// Next is an option name
		float val = Float.parseFloat(mainArgs[mainArgIndex++]);
		return val;
	}

	
	/**
	 * Get next arg if exists, or default
	 * @param def_str - default value
	 */
	private static int intArg(int def_int) {
		if (mainArgIndex >= mainArgsLength)
			return def_int;		// No more args
		if (mainArgs[mainArgIndex].startsWith("--"))
			return def_int;		// Next is an option name
		int val_int = Integer.parseInt(mainArgs[mainArgIndex++]);
		return val_int;
	}

	
	/**
	 * Get next arg if exists, or default
	 * @param def_str - default value
	 */
	private static String strArg(String def_str) {
		if (mainArgIndex >= mainArgsLength)
			return def_str;		// No more args
		if (mainArgs[mainArgIndex].startsWith("--"))
			return def_str;		// Next is an option name
		String val_str = mainArgs[mainArgIndex++];
		return val_str;
	}

	
	/**
	 * Used to allow testing
	 */
	public static boolean isSetup() {
		return em_base != null;
	}
	
	/**
	 * Setup modeler for execution
	 * Waits for setup to complete so we can use it for testing
	 */
	public static ExtendedModeler setupModeler() {
		// Schedule the creation of the UI for the event-dispatching thread.
		javax.swing.SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					ExtendedModeler em = new ExtendedModeler();
					em.createUI();
					em_base = em;		// 
				}
			}
		);
		int inctime = 1000;				// milliseconds
		int maxtime = 10000;			// Max wait in milliseconds
		int dur = 0;					// Current duration
		while (em_base == null) {
			try {
				Thread.sleep(inctime);
			} catch (InterruptedException e) {
				SmTrace.lg("setupModeler interupt exception");
				e.printStackTrace();
			}
			dur += inctime;
			if (dur > maxtime) {
				SmTrace.lg(String.format("Wait time(%d) exceeded, time=%d", maxtime, dur));
				break;
			}
		}
		return em_base;
	}
			


	
	/**
	 * Setup Testing
	 * Only, if not already setup
	 */
	public static void setupTest() {
		if (emt != null)
			return;				// Already setup
		
		
		emt = new ExtendedModelerTest();
	}
	
	/**
	 * Testing
	 * @return true iff PASS
	 * @throws EMBlockError 
	 * @throws EMTFail 
	 */
	public static boolean testit(String test_tag) throws EMBlockError, EMTFail {
		SmTrace.lg(String.format("testit(%s)", test_tag));
		if (emt == null)
			setupTest();
		try {
			return emt.test(test_tag);
		} catch (EMBlockError e) {
			SmTrace.lg(String.format("Testing error in %s: %s",
					test_tag, e.getMessage()));
			
			e.printStackTrace();
			return false;
		}
	}
}

