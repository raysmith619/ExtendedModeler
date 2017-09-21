public class ExtendedModelerTest {
	ExtendedModeler modeler;
	SceneViewer sceneViewer;
	int nTest = 0;
	int nPass = 0;
	int nFail = 0;
	
	public ExtendedModelerTest() {
	}
	
	public boolean test(ExtendedModeler em, String test_tag) {
		this.modeler = em;
		this.sceneViewer = em.sceneViewer;
		
		boolean res = false;
		switch (test_tag) {
			case "ALL":
				res = test_adj_position(test_tag);
				if (!res)
					return res;
				break;
				
			case "simple":
				res = test_simple(test_tag);
				break;
			
			case "adj_position":
				res = test_adj_position(test_tag);
				break;
				
			default:
				System.out.println(String.format("Unrecognized test(%s, args)", test_tag));
				break;
		}
		
		return res;
		
	}
	
	
	/**
	 * Test adjust position
	 * 
	 */
	private boolean test_simple(String test_tag) {
		ControlOfPlacement cpla = new ControlOfPlacement(sceneViewer, "cpla");
		ControlOfComponent ccom = new ControlOfComponent(sceneViewer, "ccom");
		ControlOfColor ccol = new ControlOfColor(sceneViewer, "ccol");

		if (!ccom.ckDoAction("addBoxButton")) {
			System.out.println("addBoxButton failed");
			return false;
		}
		String action_pla = "adjposUpButton";
		if (!cpla.ckDoAction(action_pla)) {
			System.out.println(String.format("%s failed", action_pla));
			return false;
		}
		String action = "addBallButton";
		if (!ccom.ckDoAction(action)) {
			System.out.println(String.format("%s failed", action));
			return false;
		}
		sceneViewer.repaint();
		action_pla = "adjposUpButton";
		if (!cpla.ckDoAction(action_pla)) {
			System.out.println(String.format("%s failed", action_pla));
			return false;
		}
		sceneViewer.repaint();
		
		String action_col = "adjcolorUpButton";
		if (!ccol.ckDoAction(action_col)) {
			System.out.println(String.format("%s failed", action_col));
			return false;
		}
		action = "addConeButton";
		if (!ccom.ckDoAction(action)) {
			System.out.println(String.format("%s failed", action));
			return false;
		}
		
		if (!sceneViewer.cmdUndo()) {
			System.out.println("sceneViewer.cmdUndo() failed");
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Test adjust position
	 * 
	 */
	private boolean test_adj_positione(String test_tag) {
		ControlOfPlacement cpla = new ControlOfPlacement(sceneViewer, "cpla");
		ControlOfComponent ccom = new ControlOfComponent(sceneViewer, "ccom");
		ControlOfColor ccol = new ControlOfColor(sceneViewer, "ccol");

		if (!ccom.ckDoAction("addBoxButton")) {
			System.out.println("addBoxButton failed");
			return false;
		}
		String action_pla = "adjposUpButton";
		if (!cpla.ckDoAction(action_pla)) {
			System.out.println(String.format("%s failed", action_pla));
			return false;
		}
		String action = "addBallButton";
		if (!ccom.ckDoAction(action)) {
			System.out.println(String.format("%s failed", action));
			return false;
		}
		sceneViewer.repaint();
		action_pla = "adjposUpButton";
		if (!cpla.ckDoAction(action_pla)) {
			System.out.println(String.format("%s failed", action_pla));
			return false;
		}
		sceneViewer.repaint();
		
		String action_col = "adjcolorUpButton";
		if (!ccol.ckDoAction(action_col)) {
			System.out.println(String.format("%s failed", action_col));
			return false;
		}
		action = "addConeButton";
		if (!ccom.ckDoAction(action)) {
			System.out.println(String.format("%s failed", action));
			return false;
		}
		
		if (!sceneViewer.cmdUndo()) {
			System.out.println("sceneViewer.cmdUndo() failed");
			return false;
		}
		
		return true;
	}


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
