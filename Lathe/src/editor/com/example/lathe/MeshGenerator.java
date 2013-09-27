package com.example.lathe;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.google.gwt.user.client.ui.CustomButton.Face;

/**
 * 
 * A wavefront .obj generator. Objects are generated from surfaces of revolution
 * data.
 * 
 * @author mjvesa
 */
public class MeshGenerator {

	public static String generate(Integer[] points,int pointCount) {

		StringBuffer sb = new StringBuffer();
		
		sb.append("#Object generated in "+ new Date().toString()+"\n");
		
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		ArrayList<Face> faces = new ArrayList<Face>();
		
		double x,y,z;

		sb.append("#Vertices\n");
		
		DecimalFormat df = new DecimalFormat("0.000",new DecimalFormatSymbols(Locale.US));
		

		for (int i = 0; i < pointCount; i++) {
			double r = points[i*2];
			for (int j = 0; j < 16; j++) {
			
				x = Math.cos(r * 3.14 / 8.0) * r;
				y = points[i*2+1];
				z = Math.sin(r * 3.14 / 8.0) * r;
				
				sb.append("v "+ df.format(x)+" " + df.format(y) + " " + df.format(z) + "\n");
			}
		}
		
		sb.append("#Faces\n");
		int a,b,c;

		for (int i = 0; i < pointCount-3; i++) {
			for (int j = 0; j < 16; j++) {

				a =1+ j + (i * 16);
				b =1+ j + ((i + 1) * 16);
				c =1+ ((j + 1) & 15) + ((i + 1) * 16);
				sb.append("f "+a+" " + b + " " + c + "\n");
				
				a =1+ j + (i * 16);
				b =1+ ((j + 1) & 15) + (i * 16);
				c =1+ ((j + 1) & 15) + ((i + 1) * 16);
				sb.append("f "+a+" " + b + " " + c + "\n");
			}
		}
		return sb.toString();

	}
}
