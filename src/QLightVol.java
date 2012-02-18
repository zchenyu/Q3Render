import java.util.Arrays;

public class QLightVol {
	short[] ambient;
	short[] directional;
	short[] dir;
	
	public QLightVol( short[] ambient, short[] directional, short[] dir ) {
		this.ambient = ambient;
		this.directional = directional;
		this.dir = dir;
	}
	
	public String toString() {
		return Arrays.toString( ambient ) + "\t" + Arrays.toString( directional ) + "\t" + Arrays.toString( dir );
	}
}
