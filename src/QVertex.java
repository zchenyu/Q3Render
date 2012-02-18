import java.util.Arrays;

public class QVertex {
	float[] position;
	float[][] texcoord;
	float[] normal;
	short[] color;
	
	public QVertex( float[] position, float[][] texcoord, float[] normal, short[] color ) {
		this.position = position;
		this.texcoord = texcoord;
		this.normal = normal;
		this.color = color;
	}
	
	public String toString() {
		return Arrays.toString( position ) + "\t" + Arrays.deepToString( texcoord ) + "\t" + Arrays.toString( normal ) + "\t" + Arrays.toString( color );
	}
}
