import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

public final class ShaderLoader {
	private static String mapdir;
	
	public static final void mapdir( String dir ) {
		mapdir = dir;
	}
	
	public static Texture[] loadSkyBox() throws IOException {
		String[] dir = new File( mapdir + "scripts/" ).list();
		File file = null;
		
		for( String s : dir )
			if( s.endsWith( ".shader" ) ) {
				file = new File( mapdir + "scripts/" + s );
				break;
			}
		
		if( file == null )
			throw new IOException( "Map file not found" );
		
		Scanner in = new Scanner( file );
		String s = null;
		
		while( in.hasNext() ) {
			if( in.next().equalsIgnoreCase( "skyparms" ) ) {
				s = in.next();
				break;
			}
		}
		
		if( s == null )
			throw new IOException();
		
		String[] suffixes = new String[]{ "_ft", "_lf", "_bk", "_rt", "_up", "_dn" };
		Texture[] sides = new Texture[6];
		
		for( int i = 0; i < 6; i++ ) {
			try {
				BufferedImage img = ImageIO.read( new File( mapdir + s + suffixes[i] + ".jpg" ) );
				sides[i] = TextureIO.newTexture( img, false );
			} catch( Exception e1 ) {
				e1.printStackTrace();
			}
		}
		
		return sides;
	}
}
