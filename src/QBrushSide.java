public class QBrushSide {
	int plane;
	int texture;
	
	public QBrushSide( int plane, int texture ) {
		this.plane = plane;
		this.texture = texture;
	}
	
	public String toString() {
		return plane + "\t" + texture;
	}
}
