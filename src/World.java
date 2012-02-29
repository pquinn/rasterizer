import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.*;

public class World extends JFrame implements KeyListener{
	//variables
	JFrame f = new JFrame();
	static ArrayList<Triangle> t = new ArrayList<Triangle>();
	boolean wireframe = false, shaded = false, depthSort = false, zbuffer = false;
	double[] transform = reflectionTransform();
	static Part part;
	//rotating factor
	double rot = Math.PI/8;
	//scaling factors
	double DOWNSCALE = .9;
	double UPSCALE = 1.1;
	float[][] pixels = new float[512][512];
	
	//rotation matrix that rotates cw about the x axis
	double[] rotUp = {1, 0, 0, 0,
					  0, Math.cos(-rot), -Math.sin(-rot), 0,
					  0, Math.sin(-rot), Math.cos(-rot), 0};
	
	//rotation matrix that rotates ccw about the x axis
	double[] rotDown = {1, 0, 0, 0,
			  			0, Math.cos(rot), -Math.sin(rot), 0,
			  			0, Math.sin(rot), Math.cos(rot), 0};
	
	//rotation matrix that rotates ccw about the y axis
	double[] rotRight = {Math.cos(rot), 0, Math.sin(rot), 0,
			  		     0, 1, 0, 0,
			             -Math.sin(rot), 0, Math.cos(rot), 0};
	
	//rotation matrix that rotates cw about the y axis
	double[] rotLeft = {Math.cos(-rot), 0, Math.sin(-rot), 0,
 		     			0, 1, 0, 0,
 		     			-Math.sin(-rot), 0, Math.cos(-rot), 0};
	
	//scaling matrix that scales the frame down by the scaling factor
	double[] scaleDown = {DOWNSCALE, 0, 0, 0,
						  0, DOWNSCALE, 0, 0,
						  0, 0, DOWNSCALE, 0};
	
	//scaling matrix that scales the frame up by the scaling factor
	double[] scaleUp = {UPSCALE, 0, 0, 0,
			              0, UPSCALE, 0, 0,
			              0, 0, UPSCALE, 0};
	
	//instantiates the panel
	private JPanel panel = new JPanel(){
        public void paintComponent(Graphics g){
            super.paintComponent(g);
        
            Graphics2D g2d = (Graphics2D) g;
            //translates the frame
            g2d.translate(256, 256);
            
            RenderingHints rh = g2d.getRenderingHints ();
            rh.put (RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHints(rh);
            
            //sorts the triangles by their z components if depthSort is active
            if(depthSort == true){
            	Collections.sort(t, new Comparator<Triangle>(){
            		public int compare(Triangle t1, Triangle t2) {
            			return (int) (t1.findMinZ() - t2.findMinZ());
            		}
            	});
            }
            
            if(zbuffer = true){
            	for(int i = 0; i < 512; i++){
            		for(int j = 0; j < 512; j++){
            			pixels[j][i] = Float.NEGATIVE_INFINITY;
            		}
            	}
            }
            
            //goes through each triangle in t and renders it
            for(Triangle s : t){
            	//applies the transform to the triangle
        		s.applyTransforms(transform);
        		//find the loop variables
            	double xMin = Math.floor(s.findMinX());
        		double xMax = Math.ceil(s.findMaxX());
        		double yMin = Math.floor(s.findMinY());
        		double yMax = Math.ceil(s.findMaxY());
        		//calculates the lines
        		double falpha = s.computef12(s.xT[0], s.yT[0]);
        		double fbeta = s.computef20(s.xT[1], s.yT[1]);
        		double fgamma = s.computef01(s.xT[2], s.yT[2]);
        		//renders the triangles based on the variables and if they're not backfacing
        		s.computePrimeColors();
        		if(wireframe == false){
        			if(s.isBackfacing() == false){
        				for(double i = yMin; i <= yMax; i++){
        					for(double j = xMin; j <= xMax; j++){
        						//compute barycentric coordinates
        						double alpha = s.computef12(j, i) / falpha;
        						double beta = s.computef20(j, i) / fbeta;
        						double gamma = s.computef01(j, i) / fgamma;
        						double[] bary = new double[]{alpha, beta, gamma};
        						float interpZ = s.getInterpZ(bary);
        						//checks if the current pixel((j,i)) is inside the triangle
        						if(alpha >= 0 && beta >= 0 && gamma >=0){
        							if((alpha > 0 || falpha*s.computef12(-1, -1) > 0) &&
        								(beta > 0 || fbeta*s.computef20(-1, -1) > 0) &&
        								(gamma > 0 || fgamma*s.computef01(-1, -1) > 0)){
        								//sets the paint based on the barycentric coordinates
        								if(zbuffer == true){
        									if(interpZ > pixels[(int)j+256][(int)i+256]){
        										pixels[(int)j+256][(int)i+256] = interpZ;
        										if(shaded == false){
        											g2d.setPaint(s.getColor(bary));
        											g2d.draw(new Line2D.Double(j, i, j, i));
        										} else {
        											g2d.setPaint(s.getShadedColor(bary));
        											g2d.draw(new Line2D.Double(j, i, j, i));
        										}
        									}
        								} else {
        									if(shaded == false){
        										g2d.setPaint(s.getColor(bary));
        										g2d.draw(new Line2D.Double(j, i, j, i));
        									} else {
        										g2d.setPaint(s.getShadedColor(bary));
        										g2d.draw(new Line2D.Double(j, i, j, i));
        									}
        								}
        							}
        						}
        					}
        				}
        			}
        		} else {
        			//if the mode is wireframe, it just draws a line from the verts
        			if(s.isBackfacing() == false){
        				for(int i = 0; i < 3; i++){
        					g2d.draw(
        						new Line2D.Double(
        							s.xT[i], s.yT[i], s.xT[(i + 1) % 3], s.yT[(i + 1) % 3]));
        				}
        			}
        		}
            }
        }
    };
    
    
    
    //constructs a basic Y-reflect matrix
    public double[] reflectionTransform(){
    	double[] refl = new double[12];
    	refl[0] = 1;
    	refl[1] = 0;
    	refl[2] = 0;
    	refl[3] = 0;
    	refl[4] = 0;
    	refl[5] = -1;
    	refl[6] = 0;
    	refl[7] = 0;
    	refl[8] = 0;
    	refl[9] = 0;
    	refl[10] = 1;
    	refl[11] = 0;
    	return refl;
    }
    
    public void updateTransform(double[] input){
    	//multiplies the transform of the current instance of world
    	//by the input transform matrix
    	double[] result = new double[12];
    	result[0] = this.transform[0]*input[0] + this.transform[1]*input[4] 
    	              + this.transform[2]*input[8] + this.transform[3] * 0;
    	result[4] = this.transform[4]*input[0] + this.transform[5]*input[4]
    	              + this.transform[6]*input[8] + this.transform[7] * 0;
    	result[8] = this.transform[8]*input[0] + this.transform[9]*input[4]
    	              + this.transform[10]*input[8] + this.transform[11] * 0;
    	result[1] = this.transform[0]*input[1] + this.transform[1]*input[5] 
    	              + this.transform[2]*input[9] + this.transform[3] * 0;
    	result[5] = this.transform[4]*input[1] + this.transform[5]*input[5] 
    	              + this.transform[6]*input[9] + this.transform[7] * 0;
    	result[9] = this.transform[8]*input[1] + this.transform[9]*input[5]
    	              + this.transform[10]*input[9] + this.transform[11] * 0;
    	result[2] = this.transform[0]*input[2] + this.transform[1]*input[6] 
    	              + this.transform[2]*input[10] + this.transform[3] * 0;
    	result[6] = this.transform[4]*input[2] + this.transform[5]*input[6]
    	              + this.transform[6]*input[10] + this.transform[7] * 0;
    	result[10] = this.transform[8]*input[2] + this.transform[9]*input[6]
    	              + this.transform[10]*input[10] + this.transform[11] * 0;
    	result[3] = this.transform[0]*input[3] + this.transform[1]*input[7]
    	              + this.transform[2]*input[11] + this.transform[3] * 1;
    	result[7] = this.transform[4]*input[3] + this.transform[5]*input[7] 
    	              + this.transform[6]*input[11] + this.transform[7] * 1;
    	result[11] = this.transform[8]*input[3] + this.transform[9]*input[7] 
    	              + this.transform[10]*input[11] + this.transform[11] * 1;
    	
    	this.transform = result;
    }
	
	World(Part c){
		//sets up window
		super(c.getName());
		panel.setBackground(Color.WHITE);
		panel.setVisible(true);
		panel.setSize(512,512);
		this.add(panel);
		this.setBounds(30,30,512,512);
		this.setResizable(false);
		this.setContentPane(panel);
		this.addKeyListener(this);
		
		this.part = c;
		
		//adjusts flags
		this.shaded = c.getShaded();
		this.wireframe = c.getWireframe();
		this.depthSort = c.getDepthSort();
		this.zbuffer = c.getZBuffer();
		
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){System.exit(0);}
		});
		//panel.addKeyListener(this);
	}
	
	//creates and shows the frame
	public static void createAndShow(){
		t = part.getTriangles();
		World world = new World(part);
		world.setVisible(true);
	}
	
	public static void main(String[] args){
        javax.swing.SwingUtilities.invokeLater(new Runnable(){
        	public void run(){
        		createAndShow();
        	}
        });
    }
	
	//handles key presses
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_A){
			System.out.println("A");
		} else if(e.getKeyCode() == KeyEvent.VK_W){
			wireframe = !wireframe;
			repaint();
		} else if(e.getKeyCode() == KeyEvent.VK_S){
			shaded = !shaded;
			repaint(); 
		} else if(e.getKeyCode() == KeyEvent.VK_LEFT){
			updateTransform(rotLeft);
			repaint();
		} else if(e.getKeyCode() == KeyEvent.VK_RIGHT){
			updateTransform(rotRight);
			repaint();
		} else if(e.getKeyCode() == KeyEvent.VK_UP){
			updateTransform(rotUp);
			repaint();
		} else if(e.getKeyCode() == KeyEvent.VK_DOWN){
			updateTransform(rotDown);
			repaint();
		} else if(e.getKeyCode() == KeyEvent.VK_MINUS){
			updateTransform(scaleDown);
			repaint();
		} else if(e.getKeyCode() == KeyEvent.VK_EQUALS){
			updateTransform(scaleUp);
			repaint();
		} else if(e.getKeyCode() == KeyEvent.VK_Z){
			zbuffer = !zbuffer;
			repaint();
		} else if(e.getKeyCode() == KeyEvent.VK_D){
			depthSort = !depthSort;
			repaint();
		}
	}
	public void keyReleased(KeyEvent e){}
	
	public void keyTyped(KeyEvent e) {}
}
