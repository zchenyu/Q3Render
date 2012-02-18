import java.util.Arrays;

public class QFace {
	int texture;
	int effect;
	int type;
	int vertex;
	int n_vertexes;
	int meshvert;
	int n_meshverts;
	int lm_index;
	int[] lm_start;
	int[] lm_size;
	float[] lm_origin;
	float[][] lm_vecs;
	float[] normal;
	int[] size;
	
	public QFace( int texture, int effect, int type, int vertex, int n_vertexes, int meshvert, int n_meshverts, int lm_index, int[] lm_start, int[] lm_size, float[] lm_origin, float[][] lm_vecs, float[] normal, int[] size ) {
		this.texture = texture;
		this.effect = effect;
		this.type = type;
		this.vertex = vertex;
		this.n_vertexes = n_vertexes;
		this.meshvert = meshvert;
		this.n_meshverts = n_meshverts;
		this.lm_index = lm_index;
		this.lm_start = lm_start;
		this.lm_size = lm_size;
		this.lm_origin = lm_origin;
		this.lm_vecs = lm_vecs;
		this.normal = normal;
		this.size = size;
	}
	
	public String toString() {
		return texture + "\t" + effect + "\t" + type + "\t" + vertex + "\t" + n_vertexes + "\t" + meshvert + "\t" + n_meshverts + "\t" + lm_index + "\t" + Arrays.toString( lm_start ) + "\t" + Arrays.toString( lm_size ) + "\t" + Arrays.toString( lm_origin ) + "\t" + Arrays.deepToString( lm_vecs ) + "\t" + Arrays.toString( normal ) + "\t" + Arrays.toString( size );
	}
}
