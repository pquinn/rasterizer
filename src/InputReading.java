import java.awt.Component;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;


public class InputReading extends Component {
	JFileChooser fc = new JFileChooser();
	File inputFile = null;
	ArrayList<Triangle> t = new ArrayList<Triangle>();
	
	//Reads the file and sends each line to triangle constructor to be parsed
	InputReading(){
		//Adds the filter to the file chooser
		fc.addChoosableFileFilter(new MyFilter());
		//Opens the file chooser
		int returnVal = fc.showOpenDialog(InputReading.this);
		//If the approve option was selected, it gets the file and sets
		//it to the global variable
		if(returnVal == JFileChooser.APPROVE_OPTION){
			inputFile = fc.getSelectedFile();
		}
		try{
			//Parses the file line by line while it still has lines
		    FileInputStream fstream = new FileInputStream(inputFile);
		    DataInputStream in = new DataInputStream(fstream);
		    BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    StringTokenizer st;
		    String strLine;
		    while ((strLine = br.readLine()) != null){
		    	String strLine2 = strLine;
		    	st = new StringTokenizer(strLine2);
		    	if(!(st.nextToken().charAt(0) == '#'))
		    		t.add(new Triangle(strLine));
		    }
		    in.close();
		    }catch (Exception e){
		      System.err.println("Error: " + e.toString() + " LOOK HERE");
		      e.printStackTrace();
		    }
	}
	
	//Returns the list of triangles that is gathered in the input
	public ArrayList<Triangle> getList(){
		return t;
	}
}

//Filter class that lets you choose what kind of files you should accept
class MyFilter extends javax.swing.filechooser.FileFilter {
	  public boolean accept(File file) {
	    String filename = file.getName();
	    return filename.endsWith(".tri");
	  }

	  public String getDescription() {
	    return "*.tri";
	  }
	}
