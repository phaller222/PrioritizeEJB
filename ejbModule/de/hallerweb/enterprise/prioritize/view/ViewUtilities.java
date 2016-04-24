package de.hallerweb.enterprise.prioritize.view;

import javax.ejb.Stateless;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class ViewUtilities {

	public static void addErrorMessage(String component, String message) {
		if (component == null) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", message));
		} else {
			FacesContext.getCurrentInstance().addMessage(component, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", message));
		}
	}

}
