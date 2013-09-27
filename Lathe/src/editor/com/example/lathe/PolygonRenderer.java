package com.example.lathe;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import com.example.lathe.widgetset.client.ui.VPolygonRenderer;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.StreamResource;
import com.vaadin.terminal.StreamResource.StreamSource;
import com.vaadin.ui.AbstractComponent;

/**
 * Server side component for the VPolygonRenderer widget.
 */
@com.vaadin.ui.ClientWidget(VPolygonRenderer.class)
public class PolygonRenderer extends AbstractComponent {

	private int pointCount = 0;
	private Integer[] points;
	private String renderMethod;
	private boolean exportMesh = false;
	
	public static final String RENDER_POINTS 			= "point";
	public static final String RENDER_FLAT 				= "flat";
	public static final String RENDER_TEXTURE 			= "texture";
	public static final String RENDER_ENVMAP 			= "envmap";
	public static final String RENDER_SHADED_TEXTURE 	= "shaded_texture";

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
		
		if (renderMethod!=null) {
			target.addAttribute("renderMethod", renderMethod);
		}
		
		if (exportMesh) {
			exportMesh=false;
			target.addAttribute("exportPoints",true);
		}

	}

	/**
	 * Receive and handle events and other variable changes from the client.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);

		if (variables.containsKey("point_count")) { 
			pointCount =(Integer) variables.get("point_count");
		}

		if (variables.containsKey("points")) {
			
			Object[] obs = (Object[])variables.get("points");
			points = new Integer[pointCount*2];
			
			for (int i=0;i<pointCount*2;i++) {
				points[i] = (Integer)obs[i];
			}

			final String mesh = MeshGenerator.generate(points, pointCount);
			System.out.println(mesh);
			
			StreamSource ss = new StreamResource.StreamSource() {
				
				public InputStream getStream() {
					byte [] data = mesh.getBytes();
					return new ByteArrayInputStream(data);
				}
			};
			
			StreamResource sr = new StreamResource(ss, "mesh.obj", LatheApplication.getCurrent());
			sr.setMIMEType("text/plain");
			LatheApplication.getCurrent().getMainWindow().open(sr,"_blank");
		}
	}
	
	public void setRenderingMethod(String methodName) {
		
		renderMethod = methodName;
		requestRepaint();
	}
	
	public void exportMesh() {
		this.exportMesh=true;
		requestRepaint();
	}
}
