import java.io.*;
import java.nio.*;

public final class QuakeIO {
	private static String mapdir;
	private static DataInputStream in;
	private static byte[] b;
	private static ByteBuffer buf;
	
	private static int[] lumpoff = new int[17];
	private static int[] lumplen = new int[17];
	
	private static QEntity[] entities;
	private static QTexture[] textures;
	private static QPlane[] planes;
	private static QNode[] nodes;
	private static QLeaf[] leafs;
	private static QLeafFace[] leaffaces;
	private static QLeafBrush[] leafbrushes;
	private static QModel[] models;
	private static QBrush[] brushes;
	private static QBrushSide[] brushsides;
	private static QVertex[] vertexes;
	private static QMeshVert[] meshverts;
	private static QEffect[] effects;
	private static QFace[] faces;
	private static QLightMap[] lightmaps;
	private static QLightVol[] lightvols;
	private static QVisData visdata;
	
	public static final void mapdir( String dir ) {
		mapdir = dir;
	}
	
	public static final void loadMap() throws IOException {
		String[] dir = new File( mapdir + "maps/" ).list();
		File file = null;
		
		for( String s : dir )
			if( s.endsWith( ".bsp" ) ) {
				file = new File( mapdir + "maps/" + s );
				break;
			}
		
		if( file == null )
			throw new IOException( "Map file not found" );
		
		in = new DataInputStream( new FileInputStream( file ) );
		b = new byte[(int)file.length()];
		in.read( b );
		buf = ByteBuffer.wrap( b ).order( ByteOrder.LITTLE_ENDIAN );
		
		getString( 4 );
		buf.getInt();
		
		for( int i = 0; i < 17; i++ ) {
			lumpoff[i] = buf.getInt();
			lumplen[i] = buf.getInt();
		}
		
		readEntities();
		readTextures();
		readPlanes();
		readNodes();
		readLeafs();
		readLeaffaces();
		readLeafbrushes();
		readModels();
		readBrushes();
		readBrushsides();
		readVertexes();
		readMeshverts();
		readEffects();
		readFaces();
		readLightmaps();
		readLightvols();
		readVisdata();
	}
	
	private static void readEntities() {
		buf.position( lumpoff[0] );
		//String s = getString( lumplen[0] );
		//System.out.println( getString( lumplen[0] ) );
	}
	
	private static void readTextures() {
		int n = lumplen[1] / 72;
		textures = new QTexture[n];
		buf.position( lumpoff[1] );
		
		for( int i = 0; i < n; i++ ) {
			String name = getString( 64 );
			int flags = buf.getInt();
			int contents = buf.getInt();
			textures[i] = new QTexture( name, flags, contents );
		}
	}
	
	private static void readPlanes() {
		int n = lumplen[2] / 16;
		planes = new QPlane[n];
		buf.position( lumpoff[2] );
		
		for( int i = 0; i < n; i++ ) {
			float[] normal = new float[]{ buf.getFloat(), buf.getFloat(), buf.getFloat() };
			float dist = buf.getFloat();
			planes[i] = new QPlane( normal, dist );
		}
	}
	
	private static void readNodes() {
		int n = lumplen[3] / 36;
		nodes = new QNode[n];
		buf.position( lumpoff[3] );
		
		for( int i = 0; i < n; i++ ) {
			int plane = buf.getInt();
			int[] children = new int[]{ buf.getInt(), buf.getInt() };
			int[] mins = new int[]{ buf.getInt(), buf.getInt(), buf.getInt() };
			int[] maxs = new int[]{ buf.getInt(), buf.getInt(), buf.getInt() };
			nodes[i] = new QNode( plane, children, mins, maxs );
		}
	}
	
	private static void readLeafs() {
		int n = lumplen[4] / 48;
		leafs = new QLeaf[n];
		buf.position( lumpoff[4] );
		
		for( int i = 0; i < n; i++ ) {
			int cluster = buf.getInt();
			int area = buf.getInt();
			int[] mins = new int[]{ buf.getInt(), buf.getInt(), buf.getInt() };
			int[] maxs = new int[]{ buf.getInt(), buf.getInt(), buf.getInt() };
			int leafface = buf.getInt();
			int n_leaffaces = buf.getInt();
			int leafbrush = buf.getInt();
			int n_leafbrushes = buf.getInt();
			leafs[i] = new QLeaf( cluster, area, mins, maxs, leafface, n_leaffaces, leafbrush, n_leafbrushes );
		}
	}
	
	private static void readLeaffaces() {
		int n = lumplen[5] / 4;
		leaffaces = new QLeafFace[n];
		buf.position( lumpoff[5] );
		
		for( int i = 0; i < n; i++ )
			leaffaces[i] = new QLeafFace( buf.getInt() );
	}
	
	private static void readLeafbrushes() {
		int n = lumplen[6] / 4;
		leafbrushes = new QLeafBrush[n];
		buf.position( lumpoff[6] );
		
		for( int i = 0; i < n; i++ )
			leafbrushes[i] = new QLeafBrush( buf.getInt() );
	}
	
	private static void readModels() {
		int n = lumplen[7] / 40;
		models = new QModel[n];
		buf.position( lumpoff[7] );
		
		for( int i = 0; i < n; i++ ) {
			float[] mins = new float[]{ buf.getFloat(), buf.getFloat(), buf.getFloat() };
			float[] maxs = new float[]{ buf.getFloat(), buf.getFloat(), buf.getFloat() };
			int face = buf.getInt();
			int n_faces = buf.getInt();
			int brush = buf.getInt();
			int n_brushes = buf.getInt();
			models[i] = new QModel( mins, maxs, face, n_faces, brush, n_brushes );
		}
	}
	
	private static void readBrushes() {
		int n = lumplen[8] / 12;
		brushes = new QBrush[n];
		buf.position( lumpoff[8] );
		
		for( int i = 0; i < n; i++ ) {
			int brushside = buf.getInt();
			int n_brushsides = buf.getInt();
			int texture = buf.getInt();
			brushes[i] = new QBrush( brushside, n_brushsides, texture );
		}
	}
	
	private static void readBrushsides() {
		int n = lumplen[9] / 8;
		brushsides = new QBrushSide[n];
		buf.position( lumpoff[9] );
		
		for( int i = 0; i < n; i++ ) {
			int plane = buf.getInt();
			int texture = buf.getInt();
			brushsides[i] = new QBrushSide( plane, texture );
		}
	}
	
	private static void readVertexes() {
		int n = lumplen[10] / 44;
		vertexes = new QVertex[n];
		buf.position( lumpoff[10] );
		
		for( int i = 0; i < n; i++ ) {
			float[] position = new float[]{ buf.getFloat(), buf.getFloat(), buf.getFloat() };
			float[][] texcoord = new float[][]{ { buf.getFloat(), buf.getFloat() }, { buf.getFloat(), buf.getFloat() } };
			float[] normal = new float[]{ buf.getFloat(), buf.getFloat(), buf.getFloat() };
			short[] color = getUbyte( 4 );
			vertexes[i] = new QVertex( position, texcoord, normal, color );
		}
	}
	
	private static void readMeshverts() {
		int n = lumplen[11] / 4;
		meshverts = new QMeshVert[n];
		buf.position( lumpoff[11] );
		
		for( int i = 0; i < n; i++ )
			meshverts[i] = new QMeshVert( buf.getInt() );
	}
	
	private static void readEffects() {
		int n = lumplen[12] / 72;
		effects = new QEffect[n];
		buf.position( lumpoff[12] );
		
		for( int i = 0; i < n; i++ ) {
			String name = getString( 64 );
			int brush = buf.getInt();
			int unknown = buf.getInt();
			effects[i] = new QEffect( name, brush, unknown );
		}
	}
	
	private static void readFaces() {
		int n = lumplen[13] / 104;
		faces = new QFace[n];
		buf.position( lumpoff[13] );
		
		for( int i = 0; i < n; i++ ) {
			int texture = buf.getInt();
			int effect = buf.getInt();
			int type = buf.getInt();
			int vertex = buf.getInt();
			int n_vertexes = buf.getInt();
			int meshvert = buf.getInt();
			int n_meshverts = buf.getInt();
			int lm_index = buf.getInt();
			int[] lm_start = new int[]{ buf.getInt(), buf.getInt() };
			int[] lm_size = new int[]{ buf.getInt(), buf.getInt() };
			float[] lm_origin = new float[]{ buf.getFloat(), buf.getFloat(), buf.getFloat() };
			float[][] lm_vecs = new float[][]{ { buf.getFloat(), buf.getFloat(), buf.getFloat() }, { buf.getFloat(), buf.getFloat(), buf.getFloat() } };
			float[] normal = new float[]{ buf.getFloat(), buf.getFloat(), buf.getFloat() };
			int[] size = new int[]{ buf.getInt(), buf.getInt() };
			faces[i] = new QFace( texture, effect, type, vertex, n_vertexes, meshvert, n_meshverts, lm_index, lm_start, lm_size, lm_origin, lm_vecs, normal, size );
		}
	}
	
	private static void readLightmaps() {
		int n = lumplen[14] / 128 / 128 / 3;
		lightmaps = new QLightMap[n];
		buf.position( lumpoff[14] );
		
		for( int i = 0; i < n; i++ ) {
			short[][][] map = new short[128][128][];
			
			for( int j = 0; j < 128; j++ )
				for( int k = 0; k < 128; k++ )
					map[j][k] = getUbyte( 3 );
			
			lightmaps[i] = new QLightMap( map );
		}
	}
	
	private static void readLightvols() {
		int n = lumplen[15] / 8;
		lightvols = new QLightVol[n];
		buf.position( lumpoff[15] );
		
		for( int i = 0; i < n; i++ ) {
			short[] ambient = getUbyte( 3 );
			short[] directional = getUbyte( 3 );
			short[] dir = getUbyte( 2 );
			lightvols[i] = new QLightVol( ambient, directional, dir );
		}
	}
	
	private static void readVisdata() {
		buf.position( lumpoff[16] );
		
		int n_vecs = buf.getInt();
		int sz_vecs = buf.getInt();
		short[] vecs = getUbyte( n_vecs * sz_vecs );
		visdata = new QVisData( n_vecs, sz_vecs, vecs );
	}
	
	public static String getString( int length ) {
		StringBuilder sb = new StringBuilder( length );
		char c;
		
		for( int i = 0; i < length; i++ )
			if( (c = (char)buf.get()) >= 32 )
				sb.append( c );
		
		return new String( sb );
	}
	
	private static short[] getUbyte( int length ) {
		short[] b = new short[length];
		
		for( int i = 0; i < length; i++ )
			b[i] = convertUbyte( 0xFF & buf.get() );
		
		return b;
	}
	
	private static short convertUbyte( int b ) {
		return (short)(0xFF & b);
	}
	
	public static QEntity[] getEntities() {
		return entities;
	}
	
	public static QTexture[] getTextures() {
		return textures;
	}
	
	public static QPlane[] getPlanes() {
		return planes;
	}
	
	public static QNode[] getNodes() {
		return nodes;
	}
	
	public static QLeaf[] getLeafs() {
		return leafs;
	}
	
	public static QLeafFace[] getLeaffaces() {
		return leaffaces;
	}
	
	public static QLeafBrush[] getLeafbrushes() {
		return leafbrushes;
	}
	
	public static QModel[] getModels() {
		return models;
	}
	
	public static QBrush[] getBrushes() {
		return brushes;
	}
	
	public static QBrushSide[] getBrushsides() {
		return brushsides;
	}
	
	public static QVertex[] getVertexes() {
		return vertexes;
	}
	
	public static QMeshVert[] getMeshverts() {
		return meshverts;
	}
	
	public static QEffect[] getEffects() {
		return effects;
	}
	
	public static QFace[] getFaces() {
		return faces;
	}
	
	public static QLightMap[] getLightmaps() {
		return lightmaps;
	}
	
	public static QLightVol[] getLightvols() {
		return lightvols;
	}
	
	public static QVisData getVisdata() {
		return visdata;
	}
}
