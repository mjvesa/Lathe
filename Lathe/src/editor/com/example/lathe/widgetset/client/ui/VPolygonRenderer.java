package com.example.lathe.widgetset.client.ui;


import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

/**
 * Client side widget which communicates with the server. Messages from the
 * server are shown as HTML and mouse clicks are sent to the server.
 */
public class VPolygonRenderer extends Composite implements Paintable,  MouseMoveHandler,MouseDownHandler,MouseUpHandler {

	/** Set the CSS class name to allow styling. */
	public static final String CLASSNAME = "v-polygonrenderer";

	/** The client side widget identifier */
	protected String paintableId;

	/** Reference to the server connection object. */
	protected ApplicationConnection client;
	
	private int counter = 0;
	
	private int pointBeingMoved = -1;
	
	private Integer points[] = new Integer[256*2];
	private Integer pointCount=0;
	
	private int light[];

	private int texture[];
	
	private Panel panel;
	
	private String renderMethod = "point";
	

	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */
	public VPolygonRenderer() {
		
		panel = new FlowPanel();
		panel.setStylePrimaryName(CLASSNAME);

		sinkEvents(Event.ONMOUSEMOVE);
		sinkEvents(Event.ONMOUSEDOWN);
		sinkEvents(Event.ONMOUSEUP);

		addDomHandler(this, MouseMoveEvent.getType());
		addDomHandler(this, MouseDownEvent.getType());
		addDomHandler(this, MouseUpEvent.getType());
		
		CanvasElement ce = new CanvasElement();
		panel.add(ce);
		
		calculateLight();
		calculateTexture();
		
		initWidget(panel);
	}
	
	
	private void calculateLight() {
		
		light = new int[256*256];
		
		for (int j=0;j<10;j++) {
			int k=5487*j+736;
			int i=k*k*k; //i is now random enough..		
			
			for (int y=-128;y<128;y++) {
				for (int x=-128;x<128;x++) {
					int color =10000 / (((x*x+y*y) / 6)+1);
					color+=light[i&65535];
					if (color>255) color=255;
					light[i&65535]= color;
					i++;
				}
			}
		}
	}

	private void calculateTexture() {

		texture = new int[256*256];
		
		Math.random();
		
		
		

		for (int j=0;j<100;j++) {
			int k=5483*j+736;
			int i=k*k*k; //i is now random enough..		
			for (int y=-128;y<128;y++) {
				for (int x=-128;x<128;x++) {
					texture[i&65535]=(texture[i&65535]+((x*x+y*y) / 128))&255;
					i++;
				}
			}
		}
		
		for (int i=0;i<65536;i++) {
			texture[i]=((texture[i]-128)*(texture[i]-128)) / 64;
		}
	}
	
	
	private void addPoint(int x,int y) {
	
		points[pointCount*2]=x;
		points[pointCount*2+1]=y;
		pointCount++;
		drawPoints();
		drawObject(0,0);
	}
	private void movePoint(int index,int x,int y) {
		
		points[index*2]=x;
		points[index*2+1]=y;
		drawPoints();
		drawObject(0,0);
	}

	
	private int getPointIfHit(int x,int y) {
		boolean pointFound=false;
		
		for (int i=0;i<pointCount;i++) {
			
			int px = points[i*2];
			int py = points[i*2+1];
			
			if (((px-x)*(px-x)+(py-y)*(py-y))<50) {
				return i;
			}
		}
		
		return -1;
		
	}
	
	private void deletePoint(int x,int y) {
		
		boolean pointFound=false;
		
		for (int i=0;i<pointCount;i++) {
			
			int px = points[i*2];
			int py = points[i*2+1];
			
			if (((px-x)*(px-x)+(py-y)*(py-y))<50) {
				for (int j=i;j<pointCount;j++) {
					points[j*2]=points[(j+1)*2];
					points[j*2+1]=points[(j+1)*2+1];
				}
				pointCount--;
				pointFound=true;
				break;
			}
		}
		
		if (pointFound) {
			drawPoints();
			drawObject(0,0);
		}
	}
	

	
	private native void drawPoints() /*-{
	
	  	var canvas = $doc.getElementById('editorcanvas');
	    var ctx = canvas.getContext('2d');
	    ctx.clearRect(0, 0, canvas.width, canvas.height);

	    
	    var points = this.@com.example.lathe.widgetset.client.ui.VPolygonRenderer::points;
	    var pointCount = this.@com.example.lathe.widgetset.client.ui.VPolygonRenderer::pointCount;
	    
	    ctx.beginPath();
	    ctx.moveTo(points[0],points[1]);
	    for (var i=0;i<pointCount;i++) {
	    
	    	var x = points[i*2];
	    	var y = points[i*2+1];
	    	
	    	ctx.lineTo(x,y);
	    	ctx.strokeRect(x-5,y-5,10,10);
	    }
	    ctx.stroke();
	}-*/;

	
	/*-
	    var canvasData = ctx.createImageData(canvas.width, canvas.height);
		ctx.putImageData(canvasData, 0, 0);
	 */

	private native void drawObject(int x,int y) /*-{

		var zBuffer= [];
//		
//		for (var i =0;i<400*400;i++) {
//			zBuffer[i] = 100000;
//		}

	    var renderMethod = this.@com.example.lathe.widgetset.client.ui.VPolygonRenderer::renderMethod;
	    var light = this.@com.example.lathe.widgetset.client.ui.VPolygonRenderer::light;
	    var texture = this.@com.example.lathe.widgetset.client.ui.VPolygonRenderer::texture;
	    var lathePoints = this.@com.example.lathe.widgetset.client.ui.VPolygonRenderer::points;
	    var pointCount = this.@com.example.lathe.widgetset.client.ui.VPolygonRenderer::pointCount;

		interp = function(a,b,c) {
		          return ((b-a)*c+a*10)/10;
		}

		drawPoly = function(a,b,c,color,canvasData) {
			var tmp;
			var index = 0;
			var x,y;
			  
			for (var i=0;i<10;i++) {
			    for (var j=0;j<10;j++) {
			         x=interp(interp(a.x,c.x,i),interp(a.x,b.x,i),j);
			         y=interp(interp(a.y,c.y,i),interp(a.y,b.y,i),j);
			         
			         index = (Math.floor(x)+Math.floor(y)*400)*4;
			         canvasData.data[index]=color;
			         canvasData.data[index+1]=color;
			         canvasData.data[index+2]=color;
			         canvasData.data[index+3]=255;
			 	}
			}
		}
		
		//Filled polygon////////////////////////////////////////////////////////
		
		filledHLine = function(x1,x2,z,y,color,canvasData) {
		
			if (x2<x1) {
				var temp = x1;
				x1=x2;
				x2=temp;
			}
			
			var i	= Math.floor(x1)+Math.floor(y)*400;
			var ptr = i*4;
			for (var x = x1;x<x2; x++) {
			
				if (zBuffer[i]>z) {
					zBuffer[i]=z;
		            canvasData.data[ptr]=color;
		            canvasData.data[ptr+1]=color;
		            canvasData.data[ptr+2]=color;
		            canvasData.data[ptr+3]=255;
	            }
	            ptr=ptr+4;
	            i=i+1;
	       	}
		}
	      	
		drawFilledPoly = function(a,b,c,color,canvasData) {
		
			var temp;
			
			if (a.y>b.y) {
				temp=a;
				a=b;
				b=temp;
			}
			
			if (b.y>c.y) {
				temp=b;
				b=c;
				c=temp;
			}

			if (a.y>b.y) {
				temp=a;
				a=b;
				b=temp;
			}
			
			var leftXDelta = (b.x-a.x) / (b.y-a.y+1);
			var rightXDelta = (c.x-a.x) / (c.y-a.y+1);
			
			leftX=a.x;
			rightX=a.x;

			var z = (a.z+b.z+c.z) / 3;
		
			for (var sy=Math.floor(a.y);sy<Math.floor(b.y);sy++) {
				filledHLine(Math.floor(leftX),Math.floor(rightX),z,sy,color,canvasData);
				leftX=leftX+leftXDelta;
				rightX=rightX+rightXDelta;
			}
			
			leftXDelta = (c.x-b.x) / (c.y-b.y+1);
			leftX=b.x;

			for (var sy=Math.floor(b.y);sy<Math.floor(c.y);sy++) {
				filledHLine(Math.floor(leftX),Math.floor(rightX),z,sy,color,canvasData);
				leftX=leftX+leftXDelta;
				rightX=rightX+rightXDelta;
			}
		}
		
		//Textured polygon//////////////////////////////////////////////////////

		
   		texturedHLine = function(left,right,z,y,canvasData) {
		
			if (right.x<left.x) {
				var temp = right;
				right=left;
				left=temp;
			}

			var leftX = Math.floor(left.x);
			var rightX = Math.floor(right.x);

			var tx=left.tx;
			var ty=left.ty;

			var txDelta = (right.tx-left.tx) / (rightX-leftX+1);
			var tyDelta = (right.ty-left.ty) / (rightX-leftX+1);
			
			var i	= Math.floor(leftX)+Math.floor(y)*400;
			var ptr = i*4;
			for (var x = leftX;x<rightX; x++) {
			
				if (zBuffer[i]>z) {
					zBuffer[i]=z;
					color = texture[(Math.floor(tx)&255)+(Math.floor(ty)&255)*256];
		            canvasData.data[ptr]=color;
		            canvasData.data[ptr+1]=color;
		            canvasData.data[ptr+2]=color;
		            canvasData.data[ptr+3]=255;
	            }
				tx = tx + txDelta;
				ty = ty + tyDelta;
	            ptr=ptr+4;
	            i=i+1;
	       	}
		}

		drawTexturedPoly = function(a,b,c,canvasData) {

			var temp;
			
			if (a.y>b.y) {
				temp=a;
				a=b;
				b=temp;
			}
			
			if (b.y>c.y) {
				temp=b;
				b=c;
				c=temp;
			}

			if (a.y>b.y) {
				temp=a;
				a=b;
				b=temp;
			}

			var left = new Object();
			var right = new Object();
			
			left.xDelta = (b.x-a.x) / (b.y-a.y+1);
			right.xDelta = (c.x-a.x) / (c.y-a.y+1);
			left.txDelta = (b.tx-a.tx) / (b.y-a.y+1);
			right.txDelta = (c.tx-a.tx) / (c.y-a.y+1);
			left.tyDelta = (b.ty-a.ty) / (b.y-a.y+1);
			right.tyDelta = (c.ty-a.ty) / (c.y-a.y+1);
			
			left.x=a.x;
			right.x=a.x;
			left.tx=a.tx;
			right.tx=a.tx;
			left.ty=a.ty;		
			right.ty=a.ty;

			var z = (a.z+b.z+c.z) / 3;
		
			for (var sy=Math.floor(a.y);sy<Math.floor(b.y);sy++) {
				texturedHLine(left,right,z,sy,canvasData);
				
				left.x=left.x+left.xDelta;
				right.x=right.x+right.xDelta;
				left.tx=left.tx+left.txDelta;
				right.tx=right.tx+right.txDelta;
				left.ty=left.ty+left.tyDelta;
				right.ty=right.ty+right.tyDelta;
			}
			
			left.xDelta = (c.x-b.x) / (c.y-b.y+1);
			left.txDelta = (c.tx-b.tx) / (c.y-b.y+1);
			left.tyDelta = (c.ty-b.ty) / (c.y-b.y+1);
			left.x=b.x;
			left.tx=b.tx;
			left.ty=b.ty;

			for (var sy=Math.floor(b.y);sy<Math.floor(c.y);sy++) {
				texturedHLine(left,right,z,sy,canvasData);
				
				left.x=left.x+left.xDelta;
				right.x=right.x+right.xDelta;
				left.tx=left.tx+left.txDelta;
				right.tx=right.tx+right.txDelta;
				left.ty=left.ty+left.tyDelta;
				right.ty=right.ty+right.tyDelta;
			}
		}

		//Envmapped and textured polygon///////////////////////////////////////
		
   		shadedTexturedHLine = function(left,right,z,y,canvasData) {
		
			if (right.x<left.x) {
				var temp = right;
				right=left;
				left=temp;
			}

			var leftX = Math.floor(left.x);
			var rightX = Math.floor(right.x);

			var tx=left.tx;
			var ty=left.ty;
			var nx=left.nx;
			var ny=left.ny;

			var txDelta = (right.tx-left.tx) / (rightX-leftX+1);
			var tyDelta = (right.ty-left.ty) / (rightX-leftX+1);
			var nxDelta = (right.nx-left.nx) / (rightX-leftX+1);
			var nyDelta = (right.ny-left.ny) / (rightX-leftX+1);
			
			var i	= Math.floor(leftX)+Math.floor(y)*400;
			var ptr = i*4;
			for (var x = leftX;x<rightX; x++) {
			
				if (zBuffer[i]>z) {
					zBuffer[i]=z;
					color = texture[(Math.floor(tx)&255)+(Math.floor(ty)&255)*256] / 2;
					shade = light[(Math.floor(nx)&255)+(Math.floor(ny)&255)*256];
		            canvasData.data[ptr]=color+shade;
		            canvasData.data[ptr+1]=shade;
		            canvasData.data[ptr+2]=color+shade;
		            canvasData.data[ptr+3]=255;
	            }
				tx = tx + txDelta;
				ty = ty + tyDelta;
				nx = nx + nxDelta;
				ny = ny + nyDelta;
	            ptr=ptr+4;
	            i=i+1;
	       	}
		}

		drawShadedTexturedPoly = function(a,b,c,canvasData) {

			var temp;
			
			if (a.y>b.y) {
				temp=a;
				a=b;
				b=temp;
			}
			
			if (b.y>c.y) {
				temp=b;
				b=c;
				c=temp;
			}

			if (a.y>b.y) {
				temp=a;
				a=b;
				b=temp;
			}

			var left = new Object();
			var right = new Object();
			
			left.xDelta = (b.x-a.x) / (b.y-a.y+1);
			right.xDelta = (c.x-a.x) / (c.y-a.y+1);
			left.txDelta = (b.tx-a.tx) / (b.y-a.y+1);
			right.txDelta = (c.tx-a.tx) / (c.y-a.y+1);
			left.tyDelta = (b.ty-a.ty) / (b.y-a.y+1);
			right.tyDelta = (c.ty-a.ty) / (c.y-a.y+1);
			left.nxDelta = (b.nx-a.nx) / (b.y-a.y+1);
			right.nxDelta = (c.nx-a.nx) / (c.y-a.y+1);
			left.nyDelta = (b.ny-a.ny) / (b.y-a.y+1);
			right.nyDelta = (c.ny-a.ny) / (c.y-a.y+1);
			
			left.x=a.x;
			right.x=a.x;
			left.tx=a.tx;
			right.tx=a.tx;
			left.ty=a.ty;		
			right.ty=a.ty;
			left.nx=a.nx;
			right.nx=a.nx;
			left.ny=a.ny;		
			right.ny=a.ny;

			var z = (a.z+b.z+c.z) / 3;
		
			for (var sy=Math.floor(a.y);sy<Math.floor(b.y);sy++) {
				shadedTexturedHLine(left,right,z,sy,canvasData);
				
				left.x=left.x+left.xDelta;
				right.x=right.x+right.xDelta;
				left.tx=left.tx+left.txDelta;
				right.tx=right.tx+right.txDelta;
				left.ty=left.ty+left.tyDelta;
				right.ty=right.ty+right.tyDelta;
				left.nx=left.nx+left.nxDelta;
				right.nx=right.nx+right.nxDelta;
				left.ny=left.ny+left.nyDelta;
				right.ny=right.ny+right.nyDelta;
			}
			
			left.xDelta = (c.x-b.x) / (c.y-b.y+1);
			left.txDelta = (c.tx-b.tx) / (c.y-b.y+1);
			left.tyDelta = (c.ty-b.ty) / (c.y-b.y+1);
			left.nxDelta = (c.nx-b.nx) / (c.y-b.y+1);
			left.nyDelta = (c.ny-b.ny) / (c.y-b.y+1);
			left.x=b.x;
			left.tx=b.tx;
			left.ty=b.ty;
			left.nx=b.nx;
			left.ny=b.ny;

			for (var sy=Math.floor(b.y);sy<Math.floor(c.y);sy++) {
				shadedTexturedHLine(left,right,z,sy,canvasData);
				
				left.x=left.x+left.xDelta;
				right.x=right.x+right.xDelta;
				left.tx=left.tx+left.txDelta;
				right.tx=right.tx+right.txDelta;
				left.ty=left.ty+left.tyDelta;
				right.ty=right.ty+right.tyDelta;
				left.nx=left.nx+left.nxDelta;
				right.nx=right.nx+right.nxDelta;
				left.ny=left.ny+left.nyDelta;
				right.ny=right.ny+right.nyDelta;
			}
		}
		
		//Rotation and rendering////////////////////////////////////////////////
	  	
	  	var canvas = $doc.getElementById('rendercanvas');
	    var ctx = canvas.getContext('2d');
	    var canvasData = ctx.createImageData(canvas.width, canvas.height);
	    
	    var lineNorms = [];

	    for (var i=0;i<pointCount-1; i++) {
	    
	    	normal = new Object();
			
			var nx = -(lathePoints[(i+1)*2+1]-lathePoints[i*2+1]);
			var ny = lathePoints[(i+1)*2]-lathePoints[i*2];
			var	length = Math.sqrt(nx*nx+ny*ny);
			
	    	normal.x=nx / length;
	    	normal.y=ny / length;

			lineNorms[i]=normal;
	    }
	    
	    var pointNorms = [];
		
	    for (var i=0;i<pointCount; i++) {

			var normal = new Object();

			if (i===0) {
				normal.x=lineNorms[0].x;
				normal.y=lineNorms[0].y;
			} else if (i===pointCount-1) {
				normal.x=lineNorms[pointCount-2].x;
				normal.y=lineNorms[pointCount-2].y;
			} else {
				normal.x=(lineNorms[i-1].x+lineNorms[i].x)/2;
				normal.y=(lineNorms[i-1].y+lineNorms[i].y)/2;
			}

			pointNorms[i]=normal;
		}
	    
	    var points = [];
	    var count = 0;
    	var	pointIndex = 0;

		for (var i=0;i<pointCount;i++) {
			for (var j=0;j<10;j++) {
			
				var pi = 3.14159265358979323846264338327950288419716939937510;

				//var distort =20; //+ ((Math.sin(j*pi*2/2.5)+Math.sin(i*pi*2/5))*5);
				

				var r = lathePoints[pointIndex];
				var y1 = lathePoints[pointIndex+1]-200;
				var x1 = r*Math.cos(j*2*pi/9);
				var z1 = r*Math.sin(j*2*pi/9);

				var ny1 = pointNorms[i].y;
				var nx1 = pointNorms[i].x*Math.cos(j*2*pi/9);
				var nz1 = pointNorms[i].x*Math.sin(j*2*pi/9);

				var cx = Math.cos(x*pi/200);
				var sx = Math.sin(x*pi/200);
	
				var cy = Math.cos(y*pi/200);
				var sy = Math.sin(y*pi/200);
				
				var y2 = cx * y1+ sx * z1;
				var z2 = sx * y1- cx * z1;  

				var x2 = cy * x1+ sy * z2;
				var z3 = sy * x1- cy * z2;

				var ny2 = cx * ny1+ sx * nz1;
				var nz2 = sx * ny1- cx * nz1;  

				var nx2 = cy * nx1+ sy * nz2;
				var nz3 = sy * nx1- cy * nz2;
				
				points[count]= new Object();
				
				points[count].x=200+(x2*256 / (z3+500));
				points[count].y=200+(y2*256 / (z3+500));
				points[count].z=128+z3;

				points[count].tx=j*28.45;
				points[count].ty=i*28.45;

				points[count].nx=nx2*128+128;
				points[count].ny=ny2*128+128;
				points[count].nz=nz3*128+128;
				count=count+1;
			}
				pointIndex+=2;
		}

		count=0;
		
		for (var i=0;i<pointCount-1;i++) {
			for (var j=0;j<9;j++) {
			
				var a = j+count;
				var b = (j+1)+count;
				var c = j+count+10;
				var d = (j+1)+count+10;

				if (renderMethod === "point") {
					drawPoly(points[a],points[b],points[c],points[a].nz,canvasData);
					drawPoly(points[d],points[b],points[c],points[a].nz,canvasData);
				} else 	if (renderMethod === "flat") {
					drawFilledPoly(points[a],points[b],points[c],points[a].nz,canvasData);
					drawFilledPoly(points[d],points[b],points[c],points[a].nz,canvasData);
				} else 	if (renderMethod === "texture") {
					drawTexturedPoly(points[a],points[b],points[c],canvasData);
					drawTexturedPoly(points[d],points[b],points[c],canvasData);
				} else 	if (renderMethod === "shaded_texture") {
					drawShadedTexturedPoly(points[a],points[b],points[c],canvasData);
					drawShadedTexturedPoly(points[d],points[b],points[c],canvasData);
				} else 	if (renderMethod === "envmap") {

					points[a].tx=points[a].nx;
					points[a].ty=points[a].ny;
					points[b].tx=points[b].nx;
					points[b].ty=points[b].ny;
					points[c].tx=points[c].nx;
					points[c].ty=points[c].ny;
					points[d].tx=points[d].nx;
					points[d].ty=points[d].ny;

					texture=light;

					drawTexturedPoly(points[a],points[b],points[c],canvasData);
					drawTexturedPoly(points[d],points[b],points[c],canvasData);
				}
			}
			count=count+10;
		}

		ctx.putImageData(canvasData, 0, 0);}-*/;
	
	
    /**
     * Called whenever an update is received from the server 
     */
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		// This call should be made first. 
		// It handles sizes, captions, tooltips, etc. automatically.
		if (client.updateComponent(this, uidl, true)) {
		    // If client.updateComponent returns true there has been no changes and we
		    // do not need to update anything.
			return;
		}

		// Save reference to server connection object to be able to send
		// user interaction later
		this.client = client;

		// Save the client side identifier (paintable id) for the widget
		paintableId = uidl.getId();
		
		if (uidl.hasAttribute("renderMethod")) {
			renderMethod = uidl.getStringAttribute("renderMethod");
		}
		
		if (uidl.hasAttribute("exportPoints")) {
				client.updateVariable(paintableId, "points", points,false);
				client.updateVariable(paintableId, "point_count", pointCount,true);
		}
		

	}
	

	@Override
	public void onMouseMove(MouseMoveEvent event) {

		Element el = Document.get().getElementById("rendercanvas");
		int y = event.getRelativeY(el);
		int x = event.getRelativeX(el);
		if ((y>0) && (y<400) && (x>0) && (x<400)) {
			drawObject(y,x);
		}
		
		el = Document.get().getElementById("editorcanvas");
		y = event.getRelativeY(el);
		x = event.getRelativeX(el);
		if ((y>0) && (y<400) && (x>0) && (x<400) && (pointBeingMoved>-1)) {
			movePoint(pointBeingMoved,x,y);
		}

	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
		Element el = Document.get().getElementById("editorcanvas");
		int y = event.getRelativeY(el);
		int x = event.getRelativeX(el);
		if ((y>0) && (y<400) && (x>0) && (x<400)) {
			
			if (pointBeingMoved>-1) {
				movePoint(pointBeingMoved,x,y);
			} else if (event.getNativeButton()==NativeEvent.BUTTON_LEFT) {
				
					int index = getPointIfHit(x, y);
					
					if (index==-1) {
						addPoint(x,y);
					} else {
						pointBeingMoved = index;
					}
			} else if (event.getNativeButton() == NativeEvent.BUTTON_MIDDLE) {
					deletePoint(x,y);
			}
		}
	}


	@Override
	public void onMouseUp(MouseUpEvent event) {
		
		pointBeingMoved = -1;
		
	}

	/**
	 * 
	 * This contains our render and editor canvases
	 * 
	 * @author mjvesa
	 *
	 */
	private class CanvasElement extends Widget {
		
		public CanvasElement() {
				Element root = Document.get().createDivElement();
				setElement(root);
				
				//setStyleName(CLASSNAME);
				
				getElement().setInnerHTML(	"<div class=\"v-rendercanvas\"><canvas id=\"rendercanvas\" width=\"400\" height=\"400\"></canvas></div>"+
				"<div class=\"v-editorcanvas\"><canvas id=\"editorcanvas\" width=\"400\" height=\"400\"></canvas></div>");

				/*-
				
				Element render = Document.get().createDivElement();
				render.setClassName("v-rendercanvas");
				root.appendChild(render);
				Element renderCanvas = Document.get().createElement("canvas");
				renderCanvas.setAttribute("width", "400");
				renderCanvas.setAttribute("height", "400");
				render.appendChild(renderCanvas);
				
				Element editor = Document.get().createDivElement();
				editor.setClassName("v-editorcanvas");
				root.appendChild(editor);
				Element editorCanvas = Document.get().createElement("canvas");
				editorCanvas.setAttribute("width", "400");
				editorCanvas.setAttribute("height", "400");
				editor.appendChild(editorCanvas);

				*/

											
		}
	}

}
