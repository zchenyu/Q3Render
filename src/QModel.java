import java.util.Arrays;

public class QModel {
	float[] mins;
	float[] maxs;
	int face;
	int n_faces;
	int brush;
	int n_brushes;
	
	public QModel( float[] mins, float[] maxs, int face, int n_faces, int brush, int n_brushes ) {
		this.mins = mins;
		this.maxs = maxs;
		this.face = face;
		this.n_faces = n_faces;
		this.brush = brush;
		this.n_brushes = n_brushes;
	}
	
	public String toString() {
		return Arrays.toString( mins ) + "\t" + Arrays.toString( maxs ) + "\t" + face + "\t" + n_faces + "\t" + brush + "\t" + n_brushes;
	}
}
