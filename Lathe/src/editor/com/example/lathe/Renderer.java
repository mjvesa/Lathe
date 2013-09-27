package com.example.lathe;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Select;
import com.vaadin.ui.VerticalLayout;

public class Renderer extends Panel {

	private PolygonRenderer polygonRenderer;
	
	
	public Renderer() {
		super();
		
		
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);
		hl.setMargin(true);
		setContent(hl);
		
		polygonRenderer = new PolygonRenderer();
		this.addComponent(polygonRenderer);
		
		VerticalLayout vl = new VerticalLayout();
		
		Select s = new Select("Choose rendering method");
		s.setImmediate(true);
		s.addItem(PolygonRenderer.RENDER_POINTS);
		s.addItem(PolygonRenderer.RENDER_FLAT);
		s.addItem(PolygonRenderer.RENDER_TEXTURE);
		s.addItem(PolygonRenderer.RENDER_ENVMAP);
		s.addItem(PolygonRenderer.RENDER_SHADED_TEXTURE);
		
		s.addListener(new ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
			
				polygonRenderer.setRenderingMethod((String)event.getProperty().getValue());
			}
		});
		
		vl.addComponent(s);
		
		Button b = new Button("generate mesh");
		b.addListener(new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {

				polygonRenderer.exportMesh();				
			}
		});
		
		vl.addComponent(b);
		this.addComponent(vl);
	}
}
