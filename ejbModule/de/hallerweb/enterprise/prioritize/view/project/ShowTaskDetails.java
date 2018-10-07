package de.hallerweb.enterprise.prioritize.view.project;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

@Named
@SessionScoped
public class ShowTaskDetails implements Serializable {

	public void viewTask() {
        Map<String,Object> options = new HashMap<>();
        options.put("width", 640);
        options.put("height", 340);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        options.put("resizable", false);
        PrimeFaces.current().dialog().openDynamic("taskDetails", options, null);
    }
}