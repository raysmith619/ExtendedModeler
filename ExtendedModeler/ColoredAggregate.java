package ExtendedModeler;
import java.awt.Color;
import java.util.Iterator;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLUquadric;

import smTrace.SmTrace;

import com.jogamp.opengl.glu.GLU;

public class ColoredAggregate extends EMBlockBase {
	private EMBlockGroup group;


	/**
	 * Base object
	 */
	public ColoredAggregate(EMBlockGroup group, Vector3D up) {
		super(null, null, up);
		this.group = new EMBlockGroup(group);
	}

	public ColoredAggregate() {
		this(new EMBlockGroup(), null);
	}

	/**
	 * Basic dimensions to encompassing radius
	 * @param height
	 * @param rBase
	 * @return
	 */
	public static float dim2radius(float height, float rBase) {
		float radius = (float) Math.sqrt(height*height/4 + rBase*rBase);
		return radius;
		
	}

	
	/**
	 * Create new object, from controls
	 * @throws EMBlockError 
	 **/
	public static ColoredAggregate newBlock(ControlsOfScene controlsOfScene) throws EMBlockError {
		ControlOfScene ctl = controlsOfScene.getControl("placement");
		ControlOfPlacement cop = (ControlOfPlacement)ctl;
		Point3D center = cop.getPosition();
		Vector3D up =  cop.getUp();
		return new ColoredAggregate();
	}

	public String blockType() {
		return "cylinder";
	}

	public boolean intersects(
		Ray3D ray, // input
		Point3D intersection, // output
		Vector3D normalAtIntersection // output
	) {
		return getBox().intersects(ray, intersection, normalAtIntersection);		
	}
	
	public ColoredAggregate(
		EMBox3D box,
		Color color
	) {
		super(box, color);
		isOk = true;
	}

	/**
	 * Same iD
	 * @param cb_base
	 */
	public ColoredAggregate(EMBlockBase cb_base) {
		super(cb_base);
	}
	


	public void draw(
		GLAutoDrawable drawable,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		Iterator<EMBlock> itr = group.getIterator();
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			cb.draw(drawable, expand, drawAsWireframe, cornersOnly);
		}
	}
	
	
	/**
	 * duplicate block
	 * @throws EMBlockError 
	 */
	@Override
	public EMBlockBase duplicate() throws EMBlockError {
		EMBlockBase cb = copy();
		return cb;
	}


	public Point3D getMin() {
		float xmin = getMinX();
		float ymin = getMinY();
		float zmin = getMinZ();
		return new Point3D(xmin, ymin, zmin);
	}

	
	public float getMinX() {
		Iterator<EMBlock> itr = group.getIterator();
		float limit = 0;
		boolean first = true;
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			float value = cb.getMinX();
			if (first) {
				limit = value;
				first = false;
			} else if (value < limit){
				limit = value; 
			}
		}
		return limit;		
	}

	
	public float getMinY() {
		Iterator<EMBlock> itr = group.getIterator();
		float limit = 0;
		boolean first = true;
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			float value = cb.getMinY();
			if (first) {
				limit = value;
				first = false;
			} else if (value < limit){
				limit = value; 
			}
		}
		return limit;		
	}

	
	public float getMinZ() {
		Iterator<EMBlock> itr = group.getIterator();
		float limit = 0;
		boolean first = true;
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			float value = cb.getMinZ();
			if (first) {
				limit = value;
				first = false;
			} else if (value < limit){
				limit = value; 
			}
		}
		return limit;		
	}
	
	
	public Point3D getMax() {
		float xmax = getMaxX();
		float ymax = getMaxY();
		float zmax = getMaxZ();
		return new Point3D(xmax, ymax, zmax);
	}

	
	public float getMaxX() {
		Iterator<EMBlock> itr = group.getIterator();
		float limit = 0;
		boolean first = true;
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			float value = cb.getMaxX();
			if (first) {
				limit = value;
				first = false;
			} else if (value < limit){
				limit = value; 
			}
		}
		return limit;		
	}

	
	public float getMaxY() {
		Iterator<EMBlock> itr = group.getIterator();
		float limit = 0;
		boolean first = true;
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			float value = cb.getMaxY();
			if (first) {
				limit = value;
				first = false;
			} else if (value < limit){
				limit = value; 
			}
		}
		return limit;		
	}

	
	public float getMaxZ() {
		Iterator<EMBlock> itr = group.getIterator();
		float limit = 0;
		boolean first = true;
		while (itr.hasNext()) {
			EMBlock cb = itr.next();		// NOTE - NO Checking for recursive groups
			float value = cb.getMaxZ();
			if (first) {
				limit = value;
				first = false;
			} else if (value < limit){
				limit = value; 
			}
		}
		return limit;		
	}



	/**
	 * 
	 * @param new_size - calculate from components
	 */
	public void resize(
		Vector3D size
	) {
		///TBD
	}
	
}
