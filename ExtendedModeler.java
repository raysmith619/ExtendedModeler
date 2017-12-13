package ExtendedModeler;
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
import javax.swing.JTextField;
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
	JMenuItem resetMenuItem;		// Command reset
	JButton undoButton;				// Command undo
	JButton redoButton;				// Command redo
	JButton repeatButton;			// Command repeat
	JButton resetButton;			// Reset to initial settings
	
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
	JButton eyeAtSelectionButton;
	JButton lookAtSelectionButton;
	JButton traceAllButton;
	JButton traceNoneButton;
	JButton traceSpecifyButton;
	JTextField traceSpecifyField;
	JButton traceSelectionButton;
	JPanel traceSelectionCkBoxPanel;
	JButton resetCameraButton;
	JCheckBox displayAddControlCheckBox;
	JCheckBox displayEyeAtControlCheckBox;
	JCheckBox displayLookAtControlCheckBox;
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
		else if (source == resetButton || source == resetMenuItem) {
			int response = JOptionPane.showConfirmDialog(
				frame,
				"Really Reset to initial state",
				"Confirm Reset",
				JOptionPane.YES_NO_OPTION
			);

			if (response == JOptionPane.YES_OPTION) {
				sceneViewer.reset();
			}
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
		else if ( source == eyeAtSelectionButton ) {
			sceneViewer.eyeAtSelection();
			sceneViewer.repaint();
		}
		else if ( source == lookAtSelectionButton ) {
			sceneViewer.lookAtSelection();
			sceneViewer.repaint();
		}
		else if ( source == traceAllButton ) {
			sceneViewer.traceSet("ALL");
			sceneViewer.repaint();
		}
		else if ( source == traceNoneButton ) {
			sceneViewer.traceSet("");
			sceneViewer.repaint();
		}
		else if ( source == traceSpecifyButton ) {
			sceneViewer.traceSet(traceSpecifyField.getText());
			sceneViewer.repaint();
		}
		else if ( source == traceSelectionButton ) {
			sceneViewer.traceSelection();
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
		else if ( source == displayEyeAtControlCheckBox ) {
			sceneViewer.displayEyeAtControl = ! sceneViewer.displayEyeAtControl;
			sceneViewer.setControl("eyeAt", sceneViewer.displayEyeAtControl);
			sceneViewer.repaint();
		}
		else if ( source == displayLookAtControlCheckBox ) {
			sceneViewer.displayLookAtControl = ! sceneViewer.displayLookAtControl;
			sceneViewer.setControl("lookat", sceneViewer.displayLookAtControl);
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
		frame.setJMenuBar(menuBar);

		
		// Need to set visible first before starting the rendering thread due
		// to a bug in JOGL. See JOGL Issue #54 for more information on this
		// https://jogl.dev.java.net/issues/show_bug.cgi?id=54
		frame.setVisible(true);

		GLCapabilities caps = new GLCapabilities(null);
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);
		try {
			sceneViewer = new SceneViewer(caps, this, frame);
		} catch (EMBlockError e) {
			SmTrace.lg(String.format("SceneViewer error %s", e.getMessage()));
			e.printStackTrace();
			System.exit(1);
		}
		

		JMenu menu = new JMenu("File");
			deleteAllMenuItem = new JMenuItem("Delete All");
			deleteAllMenuItem.addActionListener(this);
			menu.add(deleteAllMenuItem);

			menu.addSeparator();

			quitMenuItem = new JMenuItem("Quit");
			quitMenuItem.addActionListener(this);
			menu.add(quitMenuItem);
		menuBar.add(menu);
		
		menu =  toolMenu();
		menuBar.add(menu);
		
		menu =  traceMenu();
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
		
		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		menuBar.add(resetButton);
		frame.setJMenuBar(menuBar);
					
		Container pane = frame.getContentPane();
		// We used to use a BoxLayout as the layout manager here,
		// but it caused problems with resizing behavior due to
		// a JOGL bug https://jogl.dev.java.net/issues/show_bug.cgi?id=135
		pane.setLayout( new BorderLayout() );
		pane.add( sceneViewer, BorderLayout.CENTER );
		frame.setSize(800, 600);
		///sceneViewer.setSize(300,400);
		///pane.setVisible(true);
		///sceneViewer.setVisible(true);
		sceneViewer.setControl("component", sceneViewer.displayAddControl);
		sceneViewer.setControl("placement", sceneViewer.displayPlacementControl);
		sceneViewer.setControl("color", sceneViewer.displayColorControl);
		sceneViewer.setControl("text", sceneViewer.displayTextControl);


	}

	/**
	 * Create display menu drop down
	 * @retun - display menu item
	 */
	private JMenu toolMenu() {
		JMenu menu = new JMenu("Tools");
		JFrame toolFrame = new JFrame();

		Container toolPane = toolFrame.getContentPane();
		JPanel toolPanel = new JPanel();
		toolPanel.setLayout( new BoxLayout( toolPanel, BoxLayout.Y_AXIS ) );
		toolPanel.setLayout( new BoxLayout( toolPanel, BoxLayout.Y_AXIS ) );
		///menu.add(pane);
		// We used to use a BoxLayout as the layout manager here,
		// but it caused problems with resizing behavior due to
		// a JOGL bug https://jogl.dev.java.net/issues/show_bug.cgi?id=135
		toolPane.setLayout( new BorderLayout() );
		toolPane.add( toolPanel, BorderLayout.LINE_START );
		toolPane.add( sceneViewer, BorderLayout.CENTER );
		///toolPane.add(toolPanel);
		menu.add(toolPane);
		
		eyeAtSelectionButton = new JButton("EyeAt Selection");
		eyeAtSelectionButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		eyeAtSelectionButton.addActionListener(this);
		toolPanel.add( eyeAtSelectionButton );
		
		lookAtSelectionButton = new JButton("Look At Selection");
		lookAtSelectionButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		lookAtSelectionButton.addActionListener(this);
		toolPanel.add( lookAtSelectionButton );

		resetCameraButton = new JButton("Reset Camera");
		resetCameraButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		resetCameraButton.addActionListener(this);
		toolPanel.add( resetCameraButton );

		displayAddControlCheckBox = new JCheckBox("Display Add Control");
		displayAddControlCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayAddControlCheckBox.addActionListener(this);
		toolPanel.add(displayAddControlCheckBox);

		displayEyeAtControlCheckBox = new JCheckBox("Display EyeAt Control");
		displayEyeAtControlCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayEyeAtControlCheckBox.addActionListener(this);
		toolPanel.add(displayEyeAtControlCheckBox);

		displayLookAtControlCheckBox = new JCheckBox("Display LookAt Control");
		displayLookAtControlCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayLookAtControlCheckBox.addActionListener(this);
		toolPanel.add(displayLookAtControlCheckBox);

		displayPlacementControlCheckBox = new JCheckBox("Display Placement Control");
		displayPlacementControlCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayPlacementControlCheckBox.addActionListener(this);
		toolPanel.add(displayPlacementControlCheckBox);

		displayColorControlCheckBox = new JCheckBox("Display Color Control");
		displayColorControlCheckBox.setAlignmentX( Component.LEFT_ALIGNMENT );
		displayColorControlCheckBox.addActionListener(this);
		toolPanel.add(displayColorControlCheckBox);

		displayTextControlCheckBox = new JCheckBox("Display Text Control");
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

		return menu;
	}

	/**
	 * Create trace menu drop down
	 * Adapted from toolMenu
	 * @return - display menu item
	 */
	private JMenu traceMenu() {
		JMenu menu = new JMenu("Trace");
		JFrame traceFrame = new JFrame();

		Container tracePane = traceFrame.getContentPane();
		JPanel tracePanel = new JPanel();
		///tracePanel.setLayout( new BoxLayout( tracePanel, BoxLayout.Y_AXIS ) );
		///tracePanel.setLayout( new BoxLayout( tracePanel, BoxLayout.Y_AXIS ) );
		///tracePane.setLayout( new BorderLayout() );
		tracePane.add( tracePanel, BorderLayout.LINE_START );
		tracePane.add( sceneViewer, BorderLayout.CENTER );
		///tracePane.add(tracePanel);
		menu.add(tracePane);
		
		traceAllButton = new JButton("ALL");
		traceAllButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		traceAllButton.addActionListener(this);
		tracePanel.add( traceAllButton );
		
		traceNoneButton = new JButton("None");
		traceNoneButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		traceNoneButton.addActionListener(this);
		tracePanel.add( traceNoneButton );
		
		traceSpecifyButton = new JButton("Specify:");
		traceSpecifyButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		traceSpecifyButton.addActionListener(this);
		tracePanel.add( traceSpecifyButton );
		traceSpecifyField = new JTextField(String.format("%15s", ""));
		tracePanel.add( traceSpecifyField );
		
		traceSelectionButton = new JButton("Select:");
		traceSelectionButton.setAlignmentX( Component.LEFT_ALIGNMENT );
		traceSelectionButton.addActionListener(this);
		tracePanel.add( traceSelectionButton );
		traceSelectionCkBoxPanel = traceCkBoxPanel();
		tracePanel.add(traceSelectionCkBoxPanel);
		frame.pack();
		frame.setVisible( true );

		return menu;
	}

	/**
	 * Create trace flag check box panel using SmTrace flags
	 */
	public JPanel traceCkBoxPanel() {
		return new JPanel();	/// TBD
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
				
			case "eyeat":
				displayEyeAtControlCheckBox.setSelected(checked);
				sceneViewer.displayEyeAtControl = checked;
				break;
				
			case "lookat":
				displayLookAtControlCheckBox.setSelected(checked);
				sceneViewer.displayLookAtControl = checked;
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
		SmTrace.setProps("ExtendedModeler");
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
		///maxtime = 1000000;				/// lengthen for debugging
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

