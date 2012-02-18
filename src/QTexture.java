public class QTexture {
	String name;
	int flags;
	int contents;
	
	public QTexture( String name, int flags, int contents ) {
		this.name = name;
		this.flags = flags;
		this.contents = contents;
	}
	
	public String toString() {
		return name + "\t" + flags + "\t" + contents;
	}
}
