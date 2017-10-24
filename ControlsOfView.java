import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JFrame;

import com.jogamp.newt.Window;
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
		public ControlOf control;		// Control object, if ever active
	
		ControlEntry(String name, ControlOf control) {
			this.name = name;
			this.control = control;
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
		SmTrace.lg(String.format("setControl(%s,%b)", controlName, on));
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
		control.setControl(on);
		
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
			ControlOf control = control_entry.control;
			if (!control.isActive())
				return false;	// Not active
			
			return control.ckDoAction(action);
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
				control.setFull();		// Set full display 
				break;
			
			case "placement":
				control = new ControlOfPlacement(scene, "placement");
				break;
				
			case "color":
				control = new ControlOfColor(scene, "color");
				break;
				
			case "text":
				control = new ControlOfText(scene, "text");
				control.setFull();		// Set full display 
				break;
				
			default:
				SmTrace.lg(
						String.format("Unrecognized control(%s) - ignored", controlName));
				return;
		}
		controlh.put(controlName, new ControlEntry(controlName, control));
		control.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		control.addWindowListener(control);
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
			ControlOf control = centry.control;
			if (control.isActive())
				control.adjustControls();			
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
			ControlOf control = ctl_entry.control;
			if (!control.isActive())
				continue;							// Ignore if not active

			control.display(drawable);
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
		ControlOf control = ctl_entry.control;
		if (!control.isActive())
			return null;
		
		return ctl_entry.control;
	}

	
	/**
	 * Place control component by name
	 * Default starting under main viewing area
	 * place controls as created
	 */
	public void placeControl(String controlName) {
		SmTrace.lg(String.format("placeControl(%s)", controlName));
		int xmargin = 30;
		int ymargin = 30;
		int yloc = scene.frame.getHeight() + 2;
		int xloc = xmargin;
		int ndisplayed = 0;		// Number displayed
		ControlOf prev_control = null;
		for (String name : controls) {
			ControlEntry ctl_entry = controlh.get(name);
			ControlOf control = ctl_entry.control;
			if (control.isInPos()) {
				xloc = control.getX();
				yloc = control.getY();
				if (prev_control != null && prev_control.isFull()) {
					yloc += prev_control.getHeight();
				}
			} else {
				if (ndisplayed > 0) {
					xloc += xmargin;		// Adjust if not first
					yloc += ymargin;
				}
			}
			ndisplayed++;
			if (name.equals(controlName)) {
				if (control.isInPos()) {
					xloc = control.getX();
					yloc = control.getY();
				}
				SmTrace.lg(String.format("setLocation(%d, %d)", xloc, yloc));
				control.setLocation(xloc, yloc);
				break;
			}
			prev_control = control;
		}
	}
	
	
}
