import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import com.jogamp.opengl.GLAutoDrawable;

import smTrace.SmTrace;

/**
 * Supports operations providing a list of scene viewing controls
 * @author raysmith
 *
 */
public class ControlsOfView {
		
	class ControlEntry {
		public String name;				// Unique control name
		public boolean active;			// presently active
		public ControlOf control;			// Control object, if active
	
		ControlEntry(String name, ControlOf control) {
			this.name = name;
			this.control = control;
			active = false;				// Not yet activated
		}
	}

	// Controls dictionary
	SmTrace trace;
	SceneViewer scene;
	Map<String, ControlEntry> controlh;
	LinkedList<String> controls = new LinkedList<String>();			// Ordered set of control names
	/**
	 * Setup controls access
	 */
	ControlsOfView(SceneViewer scene, SmTrace trace) {
		this.scene = scene;
		this.trace = trace;
		this.controlh = new HashMap<String, ControlEntry>();
	}
	
	/**
	 * Add/Remove Control/Display
	 * @throws EMBlockError 
	 */
	public void setControl(String controlName, boolean on) {
		if (controlh == null)
			return;
		if (controlName == null)
			return;
		if (!controlh.containsKey(controlName)) {
			addControl(controlName);
			controls.add(controlName);			// Add to ordered list of control names
		}
		ControlEntry ctl_ent = controlh.get(controlName);
		ControlOf control = ctl_ent.control;
		//ControlEntry control_entry = new ControlEntry(ctl_ent.name, control);
		control.setControl(on);
		//control_entry.active = on;
		ctl_ent.active = on;
		controlh.put(controlName, ctl_ent);
		
		scene.setCheckBox(controlName, on);		// Have check box reflect setting
		if (on) {
			int bindex = scene.getSelectedBlockIndex();
			SmTrace.lg(String.format("ControlsOf.setControl(%s): before - selected(%d)",controlName, bindex), "controls", "controls");
			placeControl(controlName);
			int bindex2 = scene.getSelectedBlockIndex();
			SmTrace.lg(String.format("ControlsOf.setControl(%s): after - selected(%d)",controlName, bindex2), "controls", "controls");
		}
	}
	
	
	
	/**
	 * Check and do action defined by controls
	 * Assumes actions are unique
	 * Iterates through controls, checking active
	 * @return - true iff action performed
	 * @param action - action performed
	 * @throws EMBlockError 
	 */
	public boolean ckDoAction(String action) throws EMBlockError {
		for (Map.Entry<String,ControlEntry> entry : controlh.entrySet()) {
			ControlEntry control_entry = entry.getValue();
			if (control_entry.active)
				if (control_entry.control.ckDoAction(action)) {
					return true;
				}
		}
		return false;			// No action done
	}
	
	/**
	 * Add instance of unique control
	 */
	public void addControl(String controlName) {
		ControlOf control = null;
		switch (controlName) {
			case "component":
				control = new ControlOfComponent(scene, "component");
				break;
			
			case "placement":
				control = new ControlOfPlacement(scene, "placement");
				break;
				
			case "color":
				control = new ControlOfColor(scene, "color");
				break;
				
			default:
				SmTrace.lg(
						String.format("Unrecognized control(%s) - ignored", controlName));
				return;
		}
		controlh.put(controlName, new ControlEntry(controlName, control));
		scene.controls.placeControl(controlName);
	}


	/**
	 * Adjust controls based on current selection
	 * @param bindex - indes of currently selected block
	 */
	public void adjustControls() {
		if (controlh == null)
			return;
		
		for (Map.Entry<String,ControlEntry> entry : controlh.entrySet()) {
			ControlEntry centry = entry.getValue();
			if (centry.active)
				centry.control.adjustControls();			
		}
		
	}
	
	
	/**
	 * Clear controls entry's active - Currently does not dispose
	 * @param controlName
	 * @throws EMBlockError 
	 */
	void clearControl(String controlName) throws EMBlockError {
		if (controlh.containsKey(controlName)) {
			setControl(controlName, false);
		}
	}
	
	
	/**
	 * Clear controls entry's active - Currently does not dispose
	 * @param controlName
	 * @throws EMBlockError 
	 */
	void clearControls() throws EMBlockError {
		for (Map.Entry<String,ControlEntry> entry : controlh.entrySet()) {
			String controlName = entry.getKey();
			clearControl(controlName);
		}
	}

	/**
	 * Do control displays when approriate
	 * @param drawable
	 */
	public void display(GLAutoDrawable drawable) {
		for (String name : controls) {
			ControlEntry ctl_entry = controlh.get(name);
			if (!ctl_entry.active)
				continue;							// Ignore if not active
			ControlOf control = ctl_entry.control;
			if (control != null) {
				control.display(drawable);
			}
		}
	}

	
	/**
	 * Get instance of control
	 * @param controlName - unique control panel name
	 * @return control handle, null if not available(defined, set, active)
	 */
	public ControlOf getControl(String controlName) {
		if (controlh == null)
			return null;
		if (controlName == null)
			return null;
		if (!controlh.containsKey(controlName)) {
			return null;
		}
		ControlEntry ctl_entry = controlh.get(controlName);
		if (!ctl_entry.active)
			return null;
		return ctl_entry.control;
	}

	
	/**
	 * Place control component by name
	 * Default starting under main viewing area
	 * place controls as created
	 */
	public void placeControl(String controlName) {
		int yloc = scene.getHeight();
		for (String name : controls) {
			ControlEntry ctl_entry = controlh.get(name);
			if (!ctl_entry.active)
				continue;							// Ignore if not active
			ControlOf control = ctl_entry.control;
			if (control != null) {
				control.setLocation(new Point(0,yloc));
				control.setVisible(true);
				yloc += ctl_entry.control.getHeight();
			}
		}
	}
	
	
}
