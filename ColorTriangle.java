import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;

public class ColorTriangle {
	GLCanvas canvas;
	
	ColorTriangle(GLCanvas canvas, int  width, int height) {
		setup(canvas, width, height);
	}
	
    public void setup( GLCanvas canvas, int width, int height ) {
    	
    	GL2 gl2 = (GL2) canvas.getGL();
    	if (gl2 == null)
    		return;
    	
    	this.canvas = canvas;
    	
        gl2.glMatrixMode( GL2.GL_PROJECTION );
        gl2.glLoadIdentity();

        // coordinate system origin at lower left with width and height same as the window
        GLU glu = new GLU();
        int x0 = 0;
        int y0 = 0;
        glu.gluOrtho2D( (float)x0, width, (float)y0, height );

        gl2.glMatrixMode( GL2.GL_MODELVIEW );
        gl2.glLoadIdentity();

        gl2.glViewport( 0, 0, width, height );
    }

    public void render(int x0, int y0, int width, int height ) {
    	if (canvas == null)
    		return;
    	
    	GL2 gl2 = (GL2) canvas.getGL();
    	if (gl2 == null)
    		return;
    	
        gl2.glClear( GL.GL_COLOR_BUFFER_BIT );

        // draw a triangle filling the window
        gl2.glLoadIdentity();
        gl2.glBegin( GL.GL_TRIANGLES );
        gl2.glColor3f( 1, 0, 0 );
        gl2.glVertex2f( 0, 0 );
        gl2.glColor3f( 0, 1, 0 );
        gl2.glVertex2f( width, 0 );
        gl2.glColor3f( 0, 0, 1 );
        gl2.glVertex2f( width / 2, height );
        gl2.glEnd();
    }
}