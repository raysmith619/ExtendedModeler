package ExtendedModeler;
import java.awt.Color;
import java.util.ArrayList;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import smTrace.SmTrace;

class Scene {
	EMBlockGroup genBlocks;						// Generated
	EMBlockGroup displayedBlocks;					// Displayed
	AlignedBox3D boundingBoxOfScene = new AlignedBox3D();
	boolean isBoundingBoxOfSceneDirty = false;




	public Scene() throws EMBlockError {
		reset();
	}

	/**
	 * Reset to initial (repeatable) state
	 * @throws EMBlockError 
	 */
	public  void reset() {
		genBlocks = new EMBlockGroup();			// Generated
		EMBlock.setGenerated(genBlocks);
		try {
			EMBlock.setDefaults("box",
					new AlignedBox3D(new Point3D(0,0,0), new Point3D(1,1,1)),
					new Color(255, 0, 255));
		} catch (EMBlockError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		displayedBlocks = new EMBlockGroup();		// Displayed
		
	}
	
	
	public AlignedBox3D getBoundingBoxOfScene() {
		if ( isBoundingBoxOfSceneDirty ) {
			boundingBoxOfScene.clear();
			for ( int id : displayedBlocks.getIds()) {
				EMBlock block = displayedBlocks.getBlock(id);
				boundingBoxOfScene.bound(block.boundingBox());
			}
			isBoundingBoxOfSceneDirty = false;
		}
		return boundingBoxOfScene;
	}


	/**
	 * Add block from generated to displayed scene
	 */
	public void insertBlock(int id) {
		EMBlock cb = genBlocks.getBlock(id);
		if (cb != null) {
			displayedBlocks.putBlock(cb);
			isBoundingBoxOfSceneDirty = true;
		}
	}


	/**
	 * Add block to displayed scene
	 * May be modified versions of blocks in genBlocks
	 * May replace element of the same id 
	 */
	public void insertBlock(EMBlock cb) {
		if (cb != null) {
			if (genBlocks.getBlock(cb.iD()) == null) {
				// Shouldn't really happen
				SmTrace.lg(String.format("Adding new block %s id=%d", cb, cb.iD()));
				genBlocks.putBlock(cb);
			}
			displayedBlocks.putBlock(cb);
			isBoundingBoxOfSceneDirty = true;
		}
	}


	/**
	 * Add blocks to displayed list
	 * May be modified versions of blocks in genBlocks
	 * May replace element of the same id 
	 */
	public void insertBlocks(EMBlockGroup blocks) {
		for (int  id : blocks.getIds()) {
			insertBlock(blocks.getBlock(id));
		}
	}

	/**
	 * Add already created block to scene
	 * @return index of new element
	 */
	/***
	public int  addBlock(
		EMBCommand bcmd,
		EMBlock cb) {
		addBlock(cb.iD);
		bcmd.addBlock(cb);
		return cb.iD;
	}
***/	
	
	public int addBlock(
		EMBCommand bcmd,		// Current command
		String blockType, 		// Block type
		AlignedBox3D box,		// Bounding box
		Color color
	) {
		EMBlock cb = EMBlock.newBlock(blockType, box, color);		
		if (cb == null || !cb.isOk()) {
			SmTrace.lg(String.format("Couldn't create block type '%s'",
					blockType));
			return 0;
		}
		genBlocks.putBlock(cb);		// Add to generated blocks
		addBlock(bcmd, cb.iD());			// Add to displayed blocks
		return cb.iD();
	}

	public int addColoredBox(
		EMBCommand bcmd,
		AlignedBox3D box,
		Color color
	) {
		EMBlock cb = EMBlock.newBlock("box", box, color);
		return addBlock(bcmd, cb.iD());
	}

	public int getIndexOfIntersectedBox(
		Ray3D ray, // input
		Point3D intersectionPoint, // output
		Vector3D normalAtIntersection // output
	) {
		boolean intersectionDetected = false;
		int indexOfIntersectedBox = -1;
		float distanceToIntersection = 0;

		// candidate intersection
		Point3D candidatePoint = new Point3D();
		Vector3D candidateNormal = new Vector3D();
		float candidateDistance;

		for (int id : displayedBlocks.getIds())  {
			EMBlock block = displayedBlocks.getBlock(id);
			if (block == null) {
				SmTrace.lg(String.format("getIndexOfIntersectedBox: No display block id:%d", id));
				continue;
			}

			if (block.intersects(ray,candidatePoint,candidateNormal)) {
				candidateDistance = Point3D.diff(
					ray.origin, candidatePoint
				).length();
				if (
					! intersectionDetected
					|| candidateDistance < distanceToIntersection
				) {
					// We've found a new, best candidate
					intersectionDetected = true;
					indexOfIntersectedBox = id;
					distanceToIntersection = candidateDistance;
					intersectionPoint.copy( candidatePoint );
					normalAtIntersection.copy( candidateNormal );
				}
			}
		}
		return indexOfIntersectedBox;
	}


	public EMBlock getBlock( int id ) {
		return genBlocks.getBlock(id);
	}

	public AlignedBox3D getBox( int index ) {
		EMBlock cb = getBlock(index);
		if (cb == null)
			return null;
		return cb.getBox();
	}

	
	/**
	 * Get generated blocks' ids
	 */
	public int[] getDisplayedIds() {
		return displayedBlocks.getIds();
	}

	public boolean getSelectionStateOfBox( int id ) {
		EMBlock cb = displayedBlocks.getBlock(id);
		if (cb == null)
			return false;
		
		return cb.isSelected();
	}
	
	public void setSelectionStateOfBox( int id, boolean state ) {
		EMBlock cb = displayedBlocks.getBlock(id);
		if (cb == null)
			return;
		
		cb.setSelected(state);
	}
	public void toggleSelectionStateOfBox( int id ) {
		EMBlock cb = displayedBlocks.getBlock(id);
		if (cb == null)
			return;
		
		cb.toggleSelected();
	}

	public void setColorOfBlock( int id, float r, float g, float b ) {
		EMBlock cb = displayedBlocks.getBlock(id);
		if (cb != null)
			cb.setColor(r,g,b);
	}

	public void translateBlock( int id, Vector3D translation ) {
		EMBlock cb = displayedBlocks.getBlock(id);
		if (cb != null) {
			cb.translate(translation);
			isBoundingBoxOfSceneDirty = true;
		}
	}

	public void resizeBlock(
		int id, int indexOfCornerToResize, Vector3D translation
	) {
		EMBlock cb = displayedBlocks.getBlock(id);
		if (cb != null) {
			cb.resize(indexOfCornerToResize, translation);
			isBoundingBoxOfSceneDirty = true;
		}
	}

	public void deleteBlock( int id ) {
		EMBlock cb = displayedBlocks.getBlock(id);
		if (cb != null) {
			displayedBlocks.removeBlock(id);
			isBoundingBoxOfSceneDirty = true;
		}
	}

	public void deleteBlock(BlockSelect selected) {
		ArrayList<Integer> index_list = selected.getList();
		for (Integer si : index_list) {
			deleteBlock(si.intValue());
		}
	}
	
	/**
	 * Get newest block index
	 * -1 if none
	 */
	public int cbIndex() {
		return displayedBlocks.getNewestId();
	}

	
	/**
	 * Get block, given index
	 */
	public EMBlock cb(int id) {
		return genBlocks.getBlock(id);
	}


	public void deleteAllBlocks() {
		displayedBlocks.removeAllBlocks();
		isBoundingBoxOfSceneDirty = true;
	}


	static public void drawBlock(
		GLAutoDrawable drawable,
		EMBlock block,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		block.draw(drawable, expand, drawAsWireframe, cornersOnly);
	}


	public void drawScene(
		GLAutoDrawable drawable,
		int indexOfHilitedBox, // -1 for none
		boolean useAlphaBlending
	) {
		GL2 gl = (GL2) drawable.getGL();
		if ( useAlphaBlending ) {
			gl.glDisable(GL.GL_DEPTH_TEST);
			gl.glDepthMask(false);
			gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE );
			gl.glEnable( GL.GL_BLEND );
		}
		for (int id : displayedBlocks.getIds()) {
			EMBlock cb = displayedBlocks.getBlock(id);
			if (cb == null) {
				SmTrace.lg(String.format("drawScene: No display block for id:%d",id));
				continue;
			}
			if ( useAlphaBlending )
				gl.glColor4f(cb.getRed(), cb.getGreen(), cb.getBlue(), cb.getAlpha());
			else
				gl.glColor3f(cb.getRed(), cb.getGreen(), cb.getBlue());
				///gl.glColor3f(1, 1, 1);
			drawBlock( drawable, cb, false, false, false );
		}
		if ( useAlphaBlending ) {
			gl.glDisable( GL.GL_BLEND );
			gl.glDepthMask(true);
			gl.glEnable(GL.GL_DEPTH_TEST);
		}
		for ( int id : displayedBlocks.getIds()) {
			EMBlock cb = displayedBlocks.getBlock(id);
			if (cb == null) {
				SmTrace.lg(String.format("drawScene: No display block for id:%d", id));
				continue;
			}
			if (true)
				if (cb.blockType().equals("ball")) {
					SmTrace.lg(String.format("ball at[%d]", id), "tracing ball");
					if (cb.isSelected()) {
						SmTrace.lg("tracing ball selected", "ball");
					} else {
						SmTrace.lg("tracing ball not selected", "ball");
					}
				}
			if ( cb.isSelected() && indexOfHilitedBox == id )
				gl.glColor3f( 1, 1, 0 );
			else if ( cb.isSelected() )
				gl.glColor3f( 1, 0, 0 );
			else if ( indexOfHilitedBox == id )
				gl.glColor3f( 0, 1, 0 );
			else continue;
			drawBlock(drawable, cb, true, true, true );
		}
	}

	public void drawBoundingBoxOfScene(GLAutoDrawable drawable) {
		AlignedBox3D box = getBoundingBoxOfScene();
		if ( ! box.isEmpty() )
			ColoredBox.drawBox(drawable, box, false, true, false );
	}

	public int addBlock(EMBCommand bcmd, int id) {
		return bcmd.addBlock(id);
		
	}
}
