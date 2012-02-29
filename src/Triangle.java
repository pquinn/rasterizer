import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.StringTokenizer;


//FIX getColor()
public class Triangle {
	//Variables
	//Original X coordinates of the triangle
	double[] xs = new double[3];
	//Original Y coordinates of the triangle
	double[] ys = new double[3];
	//Original Z coordinates of the triangle
	double[] zs = new double[3];
	//color of v0
	double[] v0Color = new double[3];
	//color of v1
	double[] v1Color = new double[3];
	//color of v2
	double[] v2Color = new double[3];
	//Transformed X coordinates
	double[] xT = new double[3];
	//Transformed Y coordinates
	double[] yT = new double[3];
	//Transformed Z coordinates
	double[] zT = new double[3];
	//Shaded color of v0
	double[] v0ColorPrime = new double[3];
	//Shaded color of v1
	double[] v1ColorPrime = new double[3];
	//Shaded color of v2
	double[] v2ColorPrime = new double[3];
	//Z component of normal
	double d;
	
	//Constructor
	public Triangle(String line){
		//Loop for parsing the given line.
		StringTokenizer st = new StringTokenizer(line);
		int i = 0;
		int j = 0;
		while(st.hasMoreTokens()){
			if(i <= 8){
				if(i % 3 == 0){
					this.xs[j] = Double.parseDouble(st.nextToken());
				} else if (i % 3 == 1){
					this.ys[j] = Double.parseDouble(st.nextToken());
				} else if (i % 3 == 2){
					this.zs[j] = Double.parseDouble(st.nextToken());
					j++;
				}
			} else {
				if (i >= 9 && i <= 11){
					this.v0Color[i % 9] = Double.parseDouble(st.nextToken());
				} else if (i >= 12 && i <= 14){
					this.v1Color[i % 12] = Double.parseDouble(st.nextToken());
				} else if (i >= 15 && i <= 17){
					this.v2Color[i % 15] = Double.parseDouble(st.nextToken());
				}
			}
			i++;
		}
	}
	
	//Computes f01
	public double computef01(double x, double y){
		return ((yT[0] - yT[1]) * x) + ((xT[1] - xT[0]) * y) + xT[0]*yT[1] - xT[1]*yT[0];
	}
	//Computes f12
	public double computef12(double x, double y){
		return ((yT[1] - yT[2]) * x) + ((xT[2] - xT[1]) * y) + xT[1]*yT[2] - xT[2]*yT[1];
	}
	//Computes f20
	public double computef20(double x, double y){
		return ((yT[2] - yT[0]) * x) + ((xT[0] - xT[2]) * y) + xT[2]*yT[0] - xT[0]*yT[2];
	}
	
	//Applies the transform to the original points
	public void applyTransforms(double[] transform){
		
		xT[0] = transform[0] * xs[0] + transform[1] * ys[0] + transform[2] * zs[0] + transform[3];
		yT[0] = transform[4] * xs[0] + transform[5] * ys[0] + transform[6] * zs[0] + transform[7];
		zT[0] = transform[8] * xs[0] + transform[9] * ys[0] + transform[10] * zs[0] + transform[11];
		
		xT[1] = transform[0] * xs[1] + transform[1] * ys[1] + transform[2] * zs[1] + transform[3];
		yT[1] = transform[4] * xs[1] + transform[5] * ys[1] + transform[6] * zs[1] + transform[7];
		zT[1] = transform[8] * xs[1] + transform[9] * ys[1] + transform[10] * zs[1] + transform[11];
		
		xT[2] = transform[0] * xs[2] + transform[1] * ys[2] + transform[2] * zs[2] + transform[3];
		yT[2] = transform[4] * xs[2] + transform[5] * ys[2] + transform[6] * zs[2] + transform[7];
		zT[2] = transform[8] * xs[2] + transform[9] * ys[2] + transform[10] * zs[2] + transform[11];
		
		
	}
	
	//finds the highest X coordinate of the transformed X coordinates
	public double findMaxX(){
		double max = 0.0f;
		for(int i = 0; i < xT.length; i++){
			if(xT[i] > max)
				max = xT[i];
		}
		return max;
	}
	
	//finds the highest Y coordinate of the transformed Y coordinates
	public double findMaxY(){
		double max = 0.0f;
		for(int i = 0; i < yT.length; i++){
			if(yT[i] > max)
				max = yT[i];
		}
		return max;
	}
	
	//finds the lowest X coordinate of the transformed X coordinates
	public double findMinX(){
		double min = xT[0];
		for(int i = 0; i < xT.length; i++){
			if(xT[i] < min)
				min = xT[i];
		}
		return min;
	}
	
	//finds the highest Y coordinate of the transformed Y coordinates
	public double findMinY(){
		double min = yT[0];
		for(int i = 0; i < yT.length; i++){
			if(yT[i] < min)
				min = yT[i];
		}
		return min;
	}
	
	//finds the lowest Z coordinate of the transform Z coordinates
	public double findMinZ(){
		double min = zT[0];
		for(int i = 0; i < zT.length; i++){
			if(zT[i] < min)
				min = zT[i];
		}
		return min;
	}

	//checks whether or not the triangle is backfacing(verts are CW)
	public boolean isBackfacing(){
		boolean isBackfacing;
		double[] p1 = new double[3];
		double[] p2 = new double[3];
		p1[0] = xT[1] - xT[0];
		p1[1] = yT[1] - yT[0];
		p1[2] = zT[1] - zT[0];
		p2[0] = xT[2] - xT[0];
		p2[1] = yT[2] - yT[0];
		p2[2] = zT[2] - zT[0];
		double[] cross = cross(p1, p2);
		double magnitude = magnitude(cross);
		double[] nhat = new double[3];
		nhat[0] = cross[0] / magnitude;
		nhat[1] = cross[1] / magnitude;
		nhat[2] = -cross[2] / magnitude;
		if(nhat[2] <= 0){
			this.d = nhat[2];
			//System.out.println(nhat[2]);
			isBackfacing = true;
		} else {
			this.d = nhat[2];
			//System.out.println(nhat[2]);
			isBackfacing = false;
		}
		return isBackfacing;
	}
	
	//Computes the cross product of the two arrays
	public double[] cross(double[] p1, double[] p2){
		double[] result = new double[3];
		result[0] = p1[1] * p2[2] - p1[2] * p2[1];
		result[1] = p1[2] * p2[0] - p1[0] * p2[2];
		result[2] = p1[0] * p2[1] - p1[1] * p2[0];
		return result;
	}
	
	//Takes the dot product of two arrays
	public double dot(double[] p1, double[] p2){
		return p1[0] * p2[0] + p1[1] * p2[1] + p1[2] * p2[2];
	}
	
	//Takes the magnitude of an array
	public double magnitude(double[] p1){
		return Math.sqrt((p1[0] * p1[0]) + (p1[1] * p1[1]) + (p1[2] * p1[2])); 
	}
	
	//calculates the color of the point based on the three vert colors
	public Color getColor(double[] bary){
		return new Color((float)(this.v0Color[0]*bary[0] + this.v1Color[0]*bary[1] + this.v2Color[0]*bary[2]), 
						 (float)(this.v0Color[1]*bary[0] + this.v1Color[1]*bary[1] + this.v2Color[1]*bary[2]),
						 (float)(this.v0Color[2]*bary[0] + this.v1Color[2]*bary[1] + this.v2Color[2]*bary[2]));
						 
	}
	
	//multiplies the vert colors by the z component of the normal to shade them
	public void computePrimeColors(){
		double newV0CP0 = this.d * this.v0Color[0];
		double newV0CP1 = this.d * this.v0Color[1];
		double newV0CP2 = this.d * this.v0Color[2];
		
		double newV1CP0 = this.d * this.v1Color[0];
		double newV1CP1 = this.d * this.v1Color[1];
		double newV1CP2 = this.d * this.v1Color[2];
		
		double newV2CP0 = this.d * this.v2Color[0];
		double newV2CP1 = this.d * this.v2Color[1];
		double newV2CP2 = this.d * this.v2Color[2];
		
		v0ColorPrime[0] = newV0CP0;
		v0ColorPrime[1] = newV0CP1;
		v0ColorPrime[2] = newV0CP2;
		
		v1ColorPrime[0] = newV1CP0;
		v1ColorPrime[1] = newV1CP1;
		v1ColorPrime[2] = newV1CP2;
		
		v2ColorPrime[0] = newV2CP0;
		v2ColorPrime[1] = newV2CP1;
		v2ColorPrime[2] = newV2CP2;
	}
	
	//calculates the color of the point based on the shaded vert colors
	public Color getShadedColor(double[] bary){
		float r = (float) (this.v0ColorPrime[0]*bary[0] + this.v1ColorPrime[0]*bary[1] + this.v2ColorPrime[0]*bary[2]);
		float g = (float) (this.v0ColorPrime[1]*bary[0] + this.v1ColorPrime[1]*bary[1] + this.v2ColorPrime[1]*bary[2]);
		float b = (float) (this.v0ColorPrime[2]*bary[0] + this.v1ColorPrime[2]*bary[1] + this.v2ColorPrime[2]*bary[2]);
		if(r < 0)
			r = 0;
		if(r > 1)
			r = 1;
		if(g < 0)
			g = 0;
		if(g > 1)
			g = 1;
		if(b < 0)
			b = 0;
		if(b > 1)
			b = 1;
		return new Color(r, g, b);
	}
	
	public float getInterpZ (double[] bary){
		return (float) (this.zT[0]*bary[0] + this.zT[1]*bary[1] + this.zT[2]*bary[2]);
						 
	}
	
}
