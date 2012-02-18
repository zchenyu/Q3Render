import java.awt.image.BufferedImage;
import java.util.Arrays;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

public class QLightMap {
	short[][][] map;
	
	public QLightMap( short[][][] map ) {
		this.map = map;
	}
	
	public Texture genTexture() {
		BufferedImage img = new BufferedImage( 128, 128, BufferedImage.TYPE_INT_RGB );
		
		for( int i = 0; i < 128; i++ )
			for( int j = 0; j < 128; j++ )
				img.setRGB( j, i, (map[i][j][0] << 16) | (map[i][j][1] << 8) | map[i][j][2] );
		
		return TextureIO.newTexture( img, false );
	}
	
	public String toString() {
		return Arrays.deepToString( map );
	}
}
