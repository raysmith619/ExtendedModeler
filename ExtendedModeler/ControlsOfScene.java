package ExtendedModeler;
import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import com.jogamp.newt.Window;
import com.jogamp.opengl.GLAutoDrawable;

import smTrace.SmTrace;

/**
 * Supports operations providing a list of scene object manipulation controls
 * @author raysmith
 *
 */
public class ControlsOfScene {
		
	class ControlEntry {
		public String name;				// Unique control name
		public ControlOfScene control;		// Control object, if ever active
	
		ControlEntry(String name, ControlOfScene control) {
			this.name = name;
			this.control = control;
		}
	}

	// Controls dictionary
	SceneControler sceneControler;
	Map<String, ControlEntry> controlh;
	LinkedList<String> controlNames = new LinkedList<String>();			// Ordered set of control names
	/**
	 * Setup controls access
	 */
	ControlsOfScene(SceneControler sceneControler) {
		this.sceneControler = sceneControler;
		this.controlh = new HashMap<String, ControlEntry>();
	}

	
	/**
	 * 
	 */
	public void reset() {
		for (Map.Entry<String,ControlEntry> entry : controlh.entrySet()) {
			String controlName = entry.getKey();
			ControlOfScene control = getControl(controlName);
		}
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
			controlNames.add(controlName);			// Add to ordered list of control names
		}
		ControlEntry ctl_ent = controlh.get(controlName);
		ControlOfScene control = ctl_ent.control;
		
		if (on == control.isOn()) {
			return;						// No need to change
		}
		
		SmTrace.lg(String.format("setControl(%s,%b)", controlName, on));
		control.setControl(on);
		
		sceneControler.setCheckBox(controlName, on);		// Have check box reflect setting
		if (on) {
			int bindex = sceneControler.getSelectedBlockIndex();
			SmTrace.lg(String.format("ControlsOf.setControl(%s): before - selected(%d)",controlName, bindex), "controls", "controls");
			placeControl(controlName);
			int bindex2 = sceneControler.getSelectedBlockIndex();
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
			ControlOfScene control = control_entry.control;
			if (!control.isActive())
				continue;			// Skip if not active
			
			if (control.ckDoAction(action))
				return true;				// Return if action performed
		}
		return false;			// No action done
	}
	
	/**
	 * Add instance of unique control
	 */
	public void addControl(String controlName) {
		ControlOfScene control = null;
		switch (controlName) {
			case "component":
				control = new ControlOfComponent(sceneControler, "component");
				control.setFull();		// Set full display 
				break;
			
			case "placement":
				control = new ControlOfPlacement(sceneControler, "placement");
				break;
				
			case "color":
				control = new ControlOfColor(sceneControler, "color");
				break;
				
			case "text":
				control = new ControlOfText(sceneControler, "text");
				control.setFull();		// Set full display 
				break;
				
			default:
				SmTrace.lg(
						String.format("ControlsOfScene: Unrecognized control(%s) - ignored", controlName));
				return;
		}
		controlh.put(controlName, new ControlEntry(controlName, control));
		control.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		control.addWindowListener(control);
	}


	/**
	 * Adjust controls based on current selection
	 * @param bindex - index of currently selected block
	 */
	public void adjustControls() {
		if (controlh == null)
			return;
		
		for (Map.Entry<String,ControlEntry> entry : controlh.entrySet()) {
			ControlEntry centry = entry.getValue();
			ControlOfScene control = centry.control;
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
		for (String name : controlNames) {
			ControlEntry ctl_entry = controlh.get(name);
			ControlOfScene control = ctl_entry.control;
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
	public ControlOfScene getControl(String controlName) {
		if (controlh == null)
			return null;
		if (controlName == null)
			return null;
		if (!controlh.containsKey(controlName)) {
			return null;
		}
		ControlEntry ctl_entry = controlh.get(controlName);
		ControlOfScene control = ctl_entry.control;
		if (!control.isActive())
			return null;
		
		return ctl_entry.control;
	}

	/**
	 * Get array of control names
	 */
	public String[] getControlNames() {
		Set<String> nameset = controlh.keySet();
		String[] names = nameset.toArray(new String[nameset.size()]);
		return names;
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
		int yloc = sceneControler.getHeight() + 2;
		int xloc = xmargin;
		int ndisplayed = 0;		// Number displayed
		ControlOfScene prev_control = null;
		ControlEntry ctl_entry = controlh.get(controlName);
		if (ctl_entry == null)
			return;
		ControlOfScene control = ctl_entry.control;
		if(control.isInPos()) {
			xloc = control.getX();
			yloc = control.getY();
		} else if (control.getXFromProp() >= 0) {
			xloc = control.getXFromProp();
			yloc = control.getYFromProp();
		} else {
			for (String name : controlNames) {
				if (name.equals(controlName))
					continue;
				ControlEntry ctl_ent = controlh.get(name);
				ControlOfScene ctl = ctl_entry.control;
				if (ctl.isInPos()) {
					xloc += xmargin;
					yloc += ymargin;
				}
			}
		}
		control.setLocation(xloc, yloc);	
	}

	/**
	 * Adjust object based on controls and new object
	 * @throws EMBlockError 
	 */
	public void adjustByControl(EMBlock cb, EMBCommand bcmd) throws EMBlockError {
		ControlOfPlacement cop = (ControlOfPlacement) getControl("placement");
		Vector3D adj = cop.getAdj();
		cb.translate(adj);
	}
	
	/**
	 * Update controls based on display update
	 * @param new_select
	 * @param prev_select
	 */
	public void displayUpdate(BlockSelect new_select, BlockSelect prev_select) {
		int[] cbis = new_select.getIds();
		if (cbis.length != 1)
			return;			// Ignore if not 1
		
		EMBlock cb = sceneControler.getCb(cbis[0]);
		if (cb == null)
			return;					// Not displayed
		
		cb.setControls(this);
	}
	
}
