import java.util.Arrays;

public class QPlane {
	float[] normal;
	float dist;
	
	public QPlane( float[] normal, float dist ) {
		this.normal = normal;
		this.dist = dist;
	}
	
	public String toString() {
		return Arrays.toString( normal ) + "\t" + dist;
	}
}
