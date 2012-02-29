import java.awt.Component;
import java.util.ArrayList;

import javax.swing.SwingUtilities;


public class HW5 extends Component implements Part{
	boolean depthSort;
	boolean shaded;
	boolean wireframe;
	boolean zbuffer;
	ArrayList<Triangle> t = new ArrayList<Triangle>();
	
	public HW5(){
		super();
		InputReading in = new InputReading();
		this.t = in.getList();
		this.wireframe = false;
		this.shaded = true;
		this.depthSort = false;
		this.zbuffer = true;
	}
	
	public static void main(String[] args){
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	World w = new World(new HW5());
            	w.createAndShow();
            }
        });
	}
	
	public String getName(){return "HW5";}

	public boolean getDepthSort() {
		return this.depthSort;
	}

	public boolean getShaded() {
		return this.shaded;
	}

	public ArrayList<Triangle> getTriangles() {
		return this.t;
	}

	public boolean getWireframe() {
		return this.wireframe;
	}
	
	public boolean getZBuffer(){
		return this.zbuffer;
	}
	

}
