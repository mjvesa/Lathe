package com.example.lathe;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext.TransactionListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;

public class LatheApplication extends Application implements  TransactionListener  {
	 
	
	private static ThreadLocal<LatheApplication> currentApplication = new ThreadLocal<LatheApplication>();
	
	private Window mainWindow;
	
	@Override
	public void init() {

		setCurrent(this); // So that we immediately have access to the current application
        // Register a transaction listener that updates our ThreadLocal with each request
        if (getContext() != null) {
            getContext().addTransactionListener(this);
        }	
		mainWindow = new Window("Lathe Application");

		setTheme("lathe");

		HorizontalLayout hl = new HorizontalLayout();


		Renderer r = new Renderer();
		hl.addComponent(r);

		mainWindow.addComponent(hl);

		setMainWindow(mainWindow);
	}

		
	  /**
     * @return the current application instance
     */
    public static LatheApplication getCurrent() {
        return currentApplication.get();
    }
    
    /**
     * Set the current application instance
     */
    public static void setCurrent(LatheApplication application) {
        if (getCurrent() == null) {
            currentApplication.set(application);
        }
    }

    /**
     * Remove the current application instance
     */
    public static void removeCurrent() {
        currentApplication.remove();
    }
    
    /**
     * TransactionListener
     */
    public void transactionStart(Application application, Object transactionData) {
        if (application == this) {
            LatheApplication.setCurrent(this);

            // Store current users locale
            //Lang.setLocale(getLocale());
        }
    }

    public void transactionEnd(Application application, Object transactionData) {
        if (application == this) {
            // Remove locale from the executing thread
            removeCurrent();
        }
    }
    
    
    public Window getMainWindow() {
    	return mainWindow; 
    }
    
	
	
}
