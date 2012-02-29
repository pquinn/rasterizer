import java.util.ArrayList;


public interface Part{
	//returns whether or not the triangles are shaded
	public boolean getShaded();
	//returns whether or not the triangles are rendered in wireframe
	public boolean getWireframe();
	//checks if depthsort is on
	public boolean getDepthSort();
	//returns the name of the current part
	public String getName();
	//returns the ArrayList of triangles
	public ArrayList<Triangle> getTriangles();
	//checks if Z Buffering is on
	public boolean getZBuffer();
}
