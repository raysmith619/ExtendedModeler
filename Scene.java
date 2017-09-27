import java.awt.Color;
import java.util.ArrayList;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import smTrace.SmTrace;

class Scene {
	OurBlockGroup genBlocks;						// Generated
	OurBlockGroup displayedBlocks;					// Displayed
	AlignedBox3D boundingBoxOfScene = new AlignedBox3D();
	boolean isBoundingBoxOfSceneDirty = false;




	public Scene() throws OurBlockError {
		genBlocks = new OurBlockGroup();			// Generated
		OurBlock.setGenerated(genBlocks);
		OurBlock.setDefaults("box",
				new AlignedBox3D(new Point3D(0,0,0), new Point3D(1,1,1)),
				new Color(255, 0, 255));
		displayedBlocks = new OurBlockGroup();		// Displayed
	}

	public AlignedBox3D getBoundingBoxOfScene() {
		if ( isBoundingBoxOfSceneDirty ) {
			boundingBoxOfScene.clear();
			for ( int id : displayedBlocks.getIds()) {
				OurBlock block = displayedBlocks.getBlock(id);
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
		OurBlock cb = genBlocks.getBlock(id);
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
	public void insertBlock(OurBlock cb) {
		if (cb != null) {
			displayedBlocks.putBlock(cb);
			isBoundingBoxOfSceneDirty = true;
		}
	}


	/**
	 * Add blocks to displayed list
	 * May be modified versions of blocks in genBlocks
	 * May replace element of the same id 
	 */
	public void insertBlocks(OurBlockGroup blocks) {
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
		BlockCommand bcmd,
		OurBlock cb) {
		addBlock(cb.iD);
		bcmd.addBlock(cb);
		return cb.iD;
	}
***/	
	
	public int addBlock(
		BlockCommand bcmd,		// Current command
		String blockType, 		// Block type
		AlignedBox3D box,		// Bounding box
		Color color
	) {
		OurBlock cb = OurBlock.newBlock(blockType, box, color);		
		if (cb == null || !cb.isOk()) {
			System.out.println(String.format("Couldn't create block type '%s'",
					blockType));
			return 0;
		}
		genBlocks.putBlock(cb);		// Add to generated blocks
		addBlock(bcmd, cb.iD());			// Add to displayed blocks
		return cb.iD();
	}

	public int addColoredBox(
		BlockCommand bcmd,
		AlignedBox3D box,
		Color color
	) {
		OurBlock cb = OurBlock.newBlock("box", box, color);
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
			OurBlock block = displayedBlocks.getBlock(id);
			if (block == null) {
				System.out.println(String.format("getIndexOfIntersectedBox: No display block id:%d", id));
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


	public OurBlock getBlock( int id ) {
		return genBlocks.getBlock(id);
	}

	public AlignedBox3D getBox( int index ) {
		OurBlock cb = getBlock(index);
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
		OurBlock cb = displayedBlocks.getBlock(id);
		if (cb == null)
			return false;
		
		return cb.isSelected();
	}
	
	public void setSelectionStateOfBox( int id, boolean state ) {
		OurBlock cb = displayedBlocks.getBlock(id);
		if (cb == null)
			return;
		
		cb.setSelected(state);
	}
	public void toggleSelectionStateOfBox( int id ) {
		OurBlock cb = displayedBlocks.getBlock(id);
		if (cb == null)
			return;
		
		cb.toggleSelected();
	}

	public void setColorOfBlock( int id, float r, float g, float b ) {
		OurBlock cb = displayedBlocks.getBlock(id);
		if (cb != null)
			cb.setColor(r,g,b);
	}

	public void translateBlock( int id, Vector3D translation ) {
		OurBlock cb = displayedBlocks.getBlock(id);
		if (cb != null) {
			cb.translate(translation);
			isBoundingBoxOfSceneDirty = true;
		}
	}

	public void resizeBlock(
		int id, int indexOfCornerToResize, Vector3D translation
	) {
		OurBlock cb = displayedBlocks.getBlock(id);
		if (cb != null) {
			cb.resize(indexOfCornerToResize, translation);
			isBoundingBoxOfSceneDirty = true;
		}
	}

	public void deleteBlock( int id ) {
		OurBlock cb = displayedBlocks.getBlock(id);
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
	public OurBlock cb(int id) {
		return genBlocks.getBlock(id);
	}


	public void deleteAllBlocks() {
		displayedBlocks.removeAllBlocks();
		isBoundingBoxOfSceneDirty = true;
	}


	static public void drawBlock(
		GL2 gl,
		OurBlock block,
		boolean expand,
		boolean drawAsWireframe,
		boolean cornersOnly
	) {
		block.draw(gl, expand, drawAsWireframe, cornersOnly);
	}


	public void drawScene(
		GL2 gl,
		int indexOfHilitedBox, // -1 for none
		boolean useAlphaBlending
	) {
		if ( useAlphaBlending ) {
			gl.glDisable(GL.GL_DEPTH_TEST);
			gl.glDepthMask(false);
			gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE );
			gl.glEnable( GL.GL_BLEND );
		}
		for (int id : displayedBlocks.getIds()) {
			OurBlock cb = displayedBlocks.getBlock(id);
			if (cb == null) {
				System.out.println(String.format("drawScene: No display block for id:%d",id));
				continue;
			}
			if ( useAlphaBlending )
				gl.glColor4f(cb.getRed(), cb.getGreen(), cb.getBlue(), cb.getAlpha());
			else
				gl.glColor3f(cb.getRed(), cb.getGreen(), cb.getBlue());
				///gl.glColor3f(1, 1, 1);
			drawBlock( gl, cb, false, false, false );
		}
		if ( useAlphaBlending ) {
			gl.glDisable( GL.GL_BLEND );
			gl.glDepthMask(true);
			gl.glEnable(GL.GL_DEPTH_TEST);
		}
		for ( int id : displayedBlocks.getIds()) {
			OurBlock cb = displayedBlocks.getBlock(id);
			if (cb == null) {
				System.out.println(String.format("drawScene: No display block for id:%d", id));
				continue;
			}
			if (SmTrace.tr("ball"))
				if (cb.blockType().equals("ball")) {
					System.out.println(String.format("ball at[%d]", id));
					if (cb.isSelected()) {
						System.out.println("ball selected");
					} else {
						System.out.println("ball not selected");
					}
				}
			if ( cb.isSelected() && indexOfHilitedBox == id )
				gl.glColor3f( 1, 1, 0 );
			else if ( cb.isSelected() )
				gl.glColor3f( 1, 0, 0 );
			else if ( indexOfHilitedBox == id )
				gl.glColor3f( 0, 1, 0 );
			else continue;
			drawBlock( gl, cb, true, true, true );
		}
	}

	public void drawBoundingBoxOfScene( GL2 gl ) {
		AlignedBox3D box = getBoundingBoxOfScene();
		if ( ! box.isEmpty() )
			ColoredBox.drawBox( gl, box, false, true, false );
	}

	public int addBlock(BlockCommand bcmd, int id) {
		return bcmd.addBlock(id);
		
	}
}
