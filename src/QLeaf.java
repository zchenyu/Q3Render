import java.util.Arrays;

public class QLeaf {
	int cluster;
	int area;
	int[] mins;
	int[] maxs;
	int leafface;
	int n_leaffaces;
	int leafbrush;
	int n_leafbrushes;
	
	public QLeaf( int cluster, int area, int[] mins, int[] maxs, int leafface, int n_leaffaces, int leafbrush, int n_leafbrushes ) {
		this.cluster = cluster;
		this.area = area;
		this.mins = mins;
		this.maxs = maxs;
		this.leafface = leafface;
		this.n_leaffaces = n_leaffaces;
		this.leafbrush = leafbrush;
		this.n_leafbrushes = n_leafbrushes;
	}
	
	public String toString() {
		return cluster + "\t" + area + "\t" + Arrays.toString( mins ) + "\t" + Arrays.toString( maxs ) + "\t" + leafface + "\t" + n_leaffaces + "\t" + leafbrush + "\t" + n_leafbrushes;
	}
}
