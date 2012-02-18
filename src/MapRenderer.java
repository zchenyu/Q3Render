import static java.lang.Math.*;

import java.io.*;
import java.nio.*;

import java.awt.*;
import java.awt.event.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.swing.JFileChooser;

import com.sun.opengl.util.*;
import com.sun.opengl.util.texture.*;

public class MapRenderer extends Frame implements KeyListener, MouseMotionListener, Runnable {
	private static final long serialVersionUID = 1L;
	
	static final int WIDTH = 800;
	static final int HEIGHT = 600;
	static final double SPEED = 0.4;
	static final double SPEED_W = 0.2;
	static final double SPEED_R = 0.6;
	static final float A_AMT = 0.001f;
	static final int COL_MUL = 20;
	static final long SLEEP = 1l;
	
	static String mapdir;
	static File file;
	static DataInputStream in;
	static byte[] b;
	static ByteBuffer buf;
	
	QEntity[] entities;
	QTexture[] textures;
	QPlane[] planes;
	QNode[] nodes;
	QLeaf[] leafs;
	QLeafFace[] leaffaces;
	QLeafBrush[] leafbrushes;
	QModel[] models;
	QBrush[] brushes;
	QBrushSide[] brushsides;
	QVertex[] vertexes;
	QMeshVert[] meshverts;
	QEffect[] effects;
	QFace[] faces;
	QLightMap[] lightmaps;
	QLightVol[] lightvols;
	QVisData visdata;
	
	Texture[] lm;
	Texture[] tex;
	Texture[] skybox;
	FloatBuffer vbuf;
	FloatBuffer tbuf;
	FloatBuffer lbuf;
	IntBuffer mbuf;
	
	GLCanvas canvas;
	Robot robot;
	int frame = 0;
	
	double cx, cy, cz, nx, ny, nz;
	double ctheta, cphi;
	double speed;
	double fov, nfov;
	double[] zoom = new double[]{ 60.0, 30.0, 10.0 };
	int scope;
	
	int numFaces, numTris;
	long t = System.nanoTime();
	double fps;
	
	public MapRenderer( final String dir ) throws Throwable {
		super( "Quake 3 Map Renderer - Chenyu Zhao" );
		mapdir = dir.charAt( dir.length() - 1 ) == '/' ? dir : dir + '/';
		QuakeIO.mapdir( mapdir );
		QuakeIO.loadMap();
		ShaderLoader.mapdir( mapdir );
		
		entities = QuakeIO.getEntities();
		textures = QuakeIO.getTextures();
		planes = QuakeIO.getPlanes();
		nodes = QuakeIO.getNodes();
		leafs = QuakeIO.getLeafs();
		leaffaces = QuakeIO.getLeaffaces();
		leafbrushes = QuakeIO.getLeafbrushes();
		models = QuakeIO.getModels();
		brushes = QuakeIO.getBrushes();
		brushsides = QuakeIO.getBrushsides();
		vertexes = QuakeIO.getVertexes();
		meshverts = QuakeIO.getMeshverts();
		effects = QuakeIO.getEffects();
		faces = QuakeIO.getFaces();
		lightmaps = QuakeIO.getLightmaps();
		lightvols = QuakeIO.getLightvols();
		visdata = QuakeIO.getVisdata();
		
		vbuf = BufferUtil.newFloatBuffer( 3 * vertexes.length );
		tbuf = BufferUtil.newFloatBuffer( 2 * vertexes.length );
		lbuf = BufferUtil.newFloatBuffer( 2 * vertexes.length );
		mbuf = BufferUtil.newIntBuffer( meshverts.length );
		
		for( QVertex vertex : vertexes ) {
			vbuf.put( vertex.position[0] );
			vbuf.put( vertex.position[1] );
			vbuf.put( vertex.position[2] );
			tbuf.put( vertex.texcoord[0][0] );
			tbuf.put( vertex.texcoord[0][1] );
			lbuf.put( vertex.texcoord[1][0] );
			lbuf.put( vertex.texcoord[1][1] );
		}
		
		for( QMeshVert meshvert : meshverts )
			mbuf.put( meshvert.offset );
		
		nx = cx = 720;
		ny = cy = -384;
		nz = cz = 296;
		ctheta = 3.14;
		/*
		 * nx = cx = 1090;
		 * ny = cy = 5004;
		 * nz = cz = -756;
		 */
		cphi = 0;
		nfov = fov = zoom[scope = 0];
		speed = SPEED;
		
		add( canvas = new GLCanvas() );
		canvas.addGLEventListener( new GLEventListener() {
			public void displayChanged( GLAutoDrawable drawable, boolean arg1, boolean arg2 ) {
			}
			
			public void reshape( GLAutoDrawable drawable, int arg1, int arg2, int arg3, int arg4 ) {
			}
			
			public void display( GLAutoDrawable drawable ) {
				cx += (nx - cx) / 6.0;
				cy += (ny - cy) / 6.0;
				cz += (nz - cz) / 6.0;
				fov += (nfov - fov) / 2.2;
				
				GL gl = drawable.getGL();
				GLU glu = new GLU();
				GLUT glut = new GLUT();
				gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
				gl.glColor3f( 1.0f, 1.0f, 1.0f );
				
				gl.glPushAttrib( GL.GL_ENABLE_BIT );
				gl.glActiveTexture( GL.GL_TEXTURE0 );
				gl.glClientActiveTexture( GL.GL_TEXTURE0 );
				
				gl.glEnable( GL.GL_TEXTURE_2D );
				gl.glDisable( GL.GL_DEPTH_TEST );
				gl.glDisable( GL.GL_LIGHTING );
				gl.glDisable( GL.GL_BLEND );
				
				gl.glMatrixMode( GL.GL_PROJECTION );
				gl.glLoadIdentity();
				glu.gluPerspective( fov, (double)WIDTH / HEIGHT, 0.1, 5.0 );
				gl.glMatrixMode( GL.GL_MODELVIEW );
				gl.glLoadIdentity();
				glu.gluLookAt( 0, 0, 0, cos( ctheta ), sin( ctheta ), tan( cphi ), 0, 0, 1 );
				
				skybox[4].bind();
				gl.glBegin( GL.GL_QUADS );
				gl.glTexCoord2f( 0, 0 );
				gl.glVertex3f( -1.0f, 1.0f, 1.0f );
				gl.glTexCoord2f( 1, 0 );
				gl.glVertex3f( -1.0f, -1.0f, 1.0f );
				gl.glTexCoord2f( 1, 1 );
				gl.glVertex3f( 1.0f, -1.0f, 1.0f );
				gl.glTexCoord2f( 0, 1 );
				gl.glVertex3f( 1.0f, 1.0f, 1.0f );
				gl.glEnd();
				
				skybox[5].bind();
				gl.glBegin( GL.GL_QUADS );
				gl.glTexCoord2f( 0, 0 );
				gl.glVertex3f( 1.0f, 1.0f, -1.0f );
				gl.glTexCoord2f( 1, 0 );
				gl.glVertex3f( 1.0f, -1.0f, -1.0f );
				gl.glTexCoord2f( 1, 1 );
				gl.glVertex3f( -1.0f, -1.0f, -1.0f );
				gl.glTexCoord2f( 0, 1 );
				gl.glVertex3f( -1.0f, 1.0f, -1.0f );
				gl.glEnd();
				
				skybox[1].bind();
				gl.glBegin( GL.GL_QUADS );
				gl.glTexCoord2f( 0, 0 );
				gl.glVertex3f( -1.0f, -1.0f, 1.0f );
				gl.glTexCoord2f( 1, 0 );
				gl.glVertex3f( -1.0f, 1.0f, 1.0f );
				gl.glTexCoord2f( 1, 1 );
				gl.glVertex3f( -1.0f, 1.0f, -1.0f );
				gl.glTexCoord2f( 0, 1 );
				gl.glVertex3f( -1.0f, -1.0f, -1.0f );
				gl.glEnd();
				
				skybox[2].bind();
				gl.glBegin( GL.GL_QUADS );
				gl.glTexCoord2f( 0, 0 );
				gl.glVertex3f( -1.0f, 1.0f, 1.0f );
				gl.glTexCoord2f( 1, 0 );
				gl.glVertex3f( 1.0f, 1.0f, 1.0f );
				gl.glTexCoord2f( 1, 1 );
				gl.glVertex3f( 1.0f, 1.0f, -1.0f );
				gl.glTexCoord2f( 0, 1 );
				gl.glVertex3f( -1.0f, 1.0f, -1.0f );
				gl.glEnd();
				
				skybox[3].bind();
				gl.glBegin( GL.GL_QUADS );
				gl.glTexCoord2f( 0, 0 );
				gl.glVertex3f( 1.0f, 1.0f, 1.0f );
				gl.glTexCoord2f( 1, 0 );
				gl.glVertex3f( 1.0f, -1.0f, 1.0f );
				gl.glTexCoord2f( 1, 1 );
				gl.glVertex3f( 1.0f, -1.0f, -1.0f );
				gl.glTexCoord2f( 0, 1 );
				gl.glVertex3f( 1.0f, 1.0f, -1.0f );
				gl.glEnd();
				
				skybox[0].bind();
				gl.glBegin( GL.GL_QUADS );
				gl.glTexCoord2f( 0, 0 );
				gl.glVertex3f( 1.0f, -1.0f, 1.0f );
				gl.glTexCoord2f( 1, 0 );
				gl.glVertex3f( -1.0f, -1.0f, 1.0f );
				gl.glTexCoord2f( 1, 1 );
				gl.glVertex3f( -1.0f, -1.0f, -1.0f );
				gl.glTexCoord2f( 0, 1 );
				gl.glVertex3f( 1.0f, -1.0f, -1.0f );
				gl.glEnd();
				
				gl.glPopAttrib();
				
				gl.glMatrixMode( GL.GL_PROJECTION );
				gl.glLoadIdentity();
				glu.gluPerspective( fov, (double)WIDTH / HEIGHT, 1.0, 10000.0 );
				gl.glMatrixMode( GL.GL_MODELVIEW );
				gl.glLoadIdentity();
				glu.gluLookAt( cx, cy, cz, cx + cos( ctheta ), cy + sin( ctheta ), cz + tan( cphi ), 0, 0, 1 );
				
				numFaces = 0;
				numTris = 0;
				fps = 1e9 / ((double)System.nanoTime() - t);
				t = System.nanoTime();
				
				int cameraLeaf = getCameraLeaf( cx, cy, cz );
				int cameraCluster = leafs[cameraLeaf].cluster;
				
				for( QLeaf leaf : leafs ) {
					if( !isClusterVisible( cameraCluster, leaf.cluster ) )
						continue;
					
					for( int i = leaf.leafface; i < leaf.leafface + leaf.n_leaffaces; i++ ) {
						QFace face = faces[leaffaces[i].face];
						
						if( face.effect == frame || tex[face.texture] == null )
							continue;
						
						vbuf.position( 3 * face.vertex );
						tbuf.position( 2 * face.vertex );
						lbuf.position( 2 * face.vertex );
						mbuf.position( face.meshvert );
						
						gl.glVertexPointer( 3, GL.GL_FLOAT, 0, vbuf );
						
						gl.glActiveTexture( GL.GL_TEXTURE0 );
						gl.glClientActiveTexture( GL.GL_TEXTURE0 );
						gl.glEnable( GL.GL_TEXTURE_2D );
						tex[face.texture].bind();
						gl.glTexCoordPointer( 2, GL.GL_FLOAT, 0, tbuf );
						
						if( face.lm_index >= 0 ) {
							gl.glActiveTexture( GL.GL_TEXTURE1 );
							gl.glClientActiveTexture( GL.GL_TEXTURE1 );
							gl.glEnable( GL.GL_TEXTURE_2D );
							lm[face.lm_index].bind();
							gl.glTexCoordPointer( 2, GL.GL_FLOAT, 0, lbuf );
						}
						
						gl.glDrawElements( GL.GL_TRIANGLES, face.n_meshverts, GL.GL_UNSIGNED_INT, mbuf );
						gl.glDisable( GL.GL_TEXTURE_2D );
						
						face.effect = frame;
						
						numFaces++;
						numTris += face.n_meshverts;
					}
				}
				
				gl.glPushAttrib( GL.GL_ENABLE_BIT );
				
				gl.glColor4f( 0f, 0f, 0f, 0.7f );
				gl.glEnable( GL.GL_BLEND );
				gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
				gl.glActiveTexture( GL.GL_TEXTURE0 );
				gl.glDisable( GL.GL_TEXTURE_2D );
				gl.glActiveTexture( GL.GL_TEXTURE1 );
				gl.glDisable( GL.GL_TEXTURE_2D );
				
				gl.glMatrixMode( GL.GL_PROJECTION );
				gl.glLoadIdentity();
				glu.gluOrtho2D( 0.0, WIDTH, 0.0, HEIGHT );
				
				gl.glMatrixMode( GL.GL_MODELVIEW );
				gl.glLoadIdentity();
				gl.glScalef( 1f, -1f, 1f );
				gl.glTranslatef( 0f, -HEIGHT, 0f );
				
				gl.glBegin( GL.GL_QUADS );
				gl.glVertex2f( 0f, 0f );
				gl.glVertex2f( WIDTH, 0f );
				gl.glVertex2f( WIDTH, 120f );
				gl.glVertex2f( 0f, 120f );
				gl.glEnd();
				
				gl.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
				gl.glRasterPos2f( 10f, 30f );
				glut.glutBitmapString( GLUT.BITMAP_HELVETICA_18, "Quake 3 Map Viewer   |   Current Map: Advanced Tactical Combat Simulator (ATCS)" );
				gl.glRasterPos2f( 10f, 55f );
				glut.glutBitmapString( GLUT.BITMAP_HELVETICA_18, "WASD to move / Mouse to turn / Z to zoom / Shift to walk / X to sprint" );
				gl.glRasterPos2f( 10f, 80f );
				glut.glutBitmapString( GLUT.BITMAP_HELVETICA_18, "Faces: " + numFaces + "     Triangles: " + numTris + "     Frames per second: " + (long)fps );
				gl.glRasterPos2f( 10f, 105f );
				glut.glutBitmapString( GLUT.BITMAP_HELVETICA_18, "X: " + (long)cx + "     Y: " + (long)cy + "     Z: " + (long)cz + "     Theta: " + (long)(100 * ctheta) / 100.0 + "     Phi: " + (long)(100 * cphi) / 100.0 );
				
				gl.glPopAttrib();
				
				frame++;
			}
			
			public void init( GLAutoDrawable drawable ) {
				GL gl = drawable.getGL();
				gl.glViewport( 0, 0, WIDTH, HEIGHT );
				
				gl.glEnableClientState( GL.GL_VERTEX_ARRAY );
				
				gl.glActiveTexture( GL.GL_TEXTURE1 );
				gl.glClientActiveTexture( GL.GL_TEXTURE1 );
				gl.glEnable( GL.GL_TEXTURE_2D );
				gl.glEnableClientState( GL.GL_TEXTURE_COORD_ARRAY );
				gl.glActiveTexture( GL.GL_TEXTURE0 );
				gl.glClientActiveTexture( GL.GL_TEXTURE0 );
				gl.glEnable( GL.GL_TEXTURE_2D );
				gl.glEnableClientState( GL.GL_TEXTURE_COORD_ARRAY );
				
				gl.glShadeModel( GL.GL_SMOOTH );
				
				gl.glDisable( GL.GL_BLEND );
				
				gl.glDisable( GL.GL_ALPHA_TEST );
				gl.glAlphaFunc( GL.GL_ALWAYS, 1.0f );
				
				gl.glEnable( GL.GL_DEPTH_TEST );
				gl.glDepthFunc( GL.GL_LEQUAL );
				gl.glDepthMask( true );
				gl.glHint( GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST );
				
				lm = new Texture[lightmaps.length];
				
				for( int i = 0; i < lightmaps.length; i++ )
					lm[i] = lightmaps[i].genTexture();
				
				tex = new Texture[textures.length];
				
				for( int i = 0; i < textures.length; i++ ) {
					try {
						File texfile = new File( mapdir + textures[i].name + ".jpg" );
						
						if( texfile.length() == 0 )
							texfile = new File( mapdir + textures[i].name + ".tga" );
						
						if( texfile.length() == 0 ) {
							if( !textures[i].name.contains( "skybox" ) ) {
								tex[i] = TextureIO.newTexture( new File( "greentexture.png" ), true );
								System.out.println( "Texture not found: " + mapdir + textures[i].name + ".jpg" );
							}
						} else
							tex[i] = TextureIO.newTexture( texfile, true );
						
						gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR );
						gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT );
						gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT );
					} catch( Throwable e1 ) {
						e1.printStackTrace();
					}
				}
				
				try {
					skybox = ShaderLoader.loadSkyBox();
				} catch( IOException e1 ) {
					e1.printStackTrace();
				}
			}
		} );
		canvas.addKeyListener( this );
		canvas.addMouseMotionListener( this );
		new Thread( this ).start();
		robot = new Robot();
		new Animator( canvas ).start();
		
		canvas.setSize( WIDTH, HEIGHT );
		robot.mouseMove( WIDTH / 2, HEIGHT / 2 );
		
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		setCursor( toolkit.createCustomCursor( toolkit.getImage( "transparent.png" ), new Point( 0, 0 ), "clear" ) );
		
		pack();
		setVisible( true );
	}
	
	public boolean[] keys = new boolean[256];
	
	public void keyTyped( KeyEvent e1 ) {
	}
	
	public void keyPressed( KeyEvent e1 ) {
		if( !keys[KeyEvent.VK_Z] && e1.getKeyCode() == KeyEvent.VK_Z )
			nfov = zoom[scope = (scope + 1) % zoom.length];
		
		if( e1.getKeyCode() == KeyEvent.VK_X )
			speed = SPEED_R;
		
		if( e1.getKeyCode() == KeyEvent.VK_SHIFT )
			speed = SPEED_W;
		
		keys[e1.getKeyCode()] = true;
	}
	
	public void keyReleased( KeyEvent e1 ) {
		if( e1.getKeyCode() == KeyEvent.VK_SHIFT )
			speed = SPEED;
		
		keys[e1.getKeyCode()] = false;
	}
	
	public void mouseDragged( MouseEvent e1 ) {
		mouseMoved( e1 );
	}
	
	public void mouseMoved( MouseEvent e1 ) {
		if( e1.getX() == WIDTH / 2 && e1.getY() == HEIGHT / 2 )
			return;
		
		int dx = e1.getX() - WIDTH / 2;
		int dy = e1.getY() - HEIGHT / 2;
		
		ctheta -= dx * A_AMT;
		cphi -= dy * A_AMT;
		
		if( cphi > PI / 2 )
			cphi = (float)PI / 2 - 0.000001f;
		if( cphi < -PI / 2 )
			cphi = (float)-PI / 2 + 0.000001f;
		
		robot.mouseMove( WIDTH / 2 + getX() + canvas.getX(), HEIGHT / 2 + getY() + canvas.getY() );
	}
	
	public double dotv( double[] a, float[] b ) {
		double sum = 0;
		for( int i = 0; i < 3; i++ )
			sum += a[i] * b[i];
		return sum;
	}
	
	public boolean coltest( int node, double x, double y, double z ) {
		if( node < 0 ) {
			for( int i = 0; i < leafs[-node - 1].n_leafbrushes; i++ ) {
				int brushn = leafbrushes[leafs[-node - 1].leafbrush + i].brush;
				if( brushes[brushn].n_brushsides > 0 && ((textures[brushes[brushn].texture].contents & 1) != 0) ) {
					boolean wside = false;
					boolean collide = true;
					for( int j = 0; j < brushes[brushn].n_brushsides; j++ ) {
						int plane_n = brushsides[brushes[brushn].brushside + j].plane;
						if( j == 0 )
							wside = dotv( new double[]{ x, y, z }, planes[plane_n].normal ) - planes[plane_n].dist > 0;
						else if( (dotv( new double[]{ x, y, z }, planes[plane_n].normal ) - planes[plane_n].dist > 0) != wside ) {
							collide = false;
							break;
						}
					}
					if( collide )
						return true;
				}
			}
			return false;
		}
		double dist = dotv( new double[]{ x, y, z }, planes[nodes[node].plane].normal ) - planes[nodes[node].plane].dist;
		if( dist >= 0 )
			return coltest( nodes[node].children[0], x, y, z );
		else
			return coltest( nodes[node].children[1], x, y, z );
	}
	
	public void run() {
		while( true ) {
			if( keys[KeyEvent.VK_LEFT] )
				ctheta += A_AMT;
			if( keys[KeyEvent.VK_RIGHT] )
				ctheta -= A_AMT;
			if( keys[KeyEvent.VK_UP] && cphi < PI / 2 )
				cphi += A_AMT;
			if( keys[KeyEvent.VK_DOWN] && cphi > -PI / 2 )
				cphi -= A_AMT;
			
			if( keys[KeyEvent.VK_W] && !coltest( 0, nx + COL_MUL * speed * cos( ctheta ), ny + COL_MUL * speed * sin( ctheta ), nz + COL_MUL * speed * sin( cphi ) ) ) {
				nz += speed * sin( cphi );
				nx += speed * cos( ctheta );
				ny += speed * sin( ctheta );
			}
			if( keys[KeyEvent.VK_S] && !coltest( 0, nx - COL_MUL * speed * cos( ctheta ), ny - COL_MUL * speed * sin( ctheta ), nz - COL_MUL * speed * sin( cphi ) ) ) {
				nz -= speed * sin( cphi );
				nx -= speed * cos( ctheta );
				ny -= speed * sin( ctheta );
			}
			if( keys[KeyEvent.VK_A] && !coltest( 0, nx - COL_MUL * speed * sin( ctheta ) / 2.0, ny + COL_MUL * speed * cos( ctheta ) / 2.0, nz ) ) {
				nx -= speed * sin( ctheta ) / 2.0;
				ny += speed * cos( ctheta ) / 2.0;
			}
			if( keys[KeyEvent.VK_D] && !coltest( 0, nx + COL_MUL * speed * sin( ctheta ) / 2.0, ny - COL_MUL * speed * cos( ctheta ) / 2.0, nz ) ) {
				nx += speed * sin( ctheta ) / 2.0;
				ny -= speed * cos( ctheta ) / 2.0;
			}
			if( keys[KeyEvent.VK_C] && !coltest( 0, nx, ny, nz - speed / 1.5 ) )
				nz -= speed / 1.5;
			if( keys[KeyEvent.VK_SPACE] && !coltest( 0, nx, ny, nz + speed / 1.5 ) )
				nz += speed / 1.5;
			
			try {
				Thread.sleep( SLEEP );
			} catch( InterruptedException e1 ) {
			}
		}
	}
	
	public boolean isClusterVisible( int cam, int test ) {
		return cam >= 0 && (visdata.vecs[cam * visdata.sz_vecs + (test / 8)] & (1 << test % 8)) != 0;
	}
	
	public int getCameraLeaf( double camx, double camy, double camz ) {
		int index = 0;
		
		while( index >= 0 ) {
			QNode node = nodes[index];
			QPlane plane = planes[node.plane];
			double dist = camx * plane.normal[0] + camy * plane.normal[1] + camz * plane.normal[2];
			
			if( dist > plane.dist )
				index = node.children[0];
			else
				index = node.children[1];
		}
		
		return -index - 1;
	}
	
	public static void main( String[] args ) throws Throwable {
		JFileChooser fc = new JFileChooser( "C:/Users/chenyu/workspace/Q3Render" );
		fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
		
		if( fc.showOpenDialog( null ) == JFileChooser.APPROVE_OPTION )
			new MapRenderer( fc.getSelectedFile().getAbsolutePath() );
	}
}
