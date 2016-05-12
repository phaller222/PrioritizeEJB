package de.hallerweb.enterprise.prioritize.view;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.tagcloud.DefaultTagCloudItem;
import org.primefaces.model.tagcloud.DefaultTagCloudModel;
import org.primefaces.model.tagcloud.TagCloudModel;

import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * LoginBean - handels logins.
 * 
 * <p>
 * Copyright: (c) 2015
 * </p>
 * <p>
 * Peter Haller
 * </p>
 * 
 * @author peter
 */
@Named
@SessionScoped
public class LoginBean implements Serializable {

	@EJB
	UserRoleController userRoleController;

	@Inject
	SessionController sessionController;

	String username;
	String password;
	boolean loggedIn;
	Date lastLogin;

	@PostConstruct
	public void init() {

	}

	@Named
	public String getUsername() {
		return username;
	}

	@Named
	public void setUsername(String username) {
		this.username = username;
	}

	@Named
	public String getPassword() {
		return password;
	}

	@Named
	public void setPassword(String password) {
		this.password = String.valueOf(password.hashCode());
	}

	@Named
	public Date getLastLogin() {
		return sessionController.getUser().getLastLogin();
	}

	public String login() {
		User user = userRoleController.findUserByUsername(username, AuthorizationController.getSystemUser());
		if (user == null) {
			return "login";
		}

		if (!user.getPassword().equals(password)) {
			return "login";
		}

		user.setLastLogin(new Date());
		sessionController.setUser(user);
		return "index";
	}

	public TagCloudModel getModel() {
		TagCloudModel model = new DefaultTagCloudModel();
		model.addTag(new DefaultTagCloudItem("Transformers", 1));
		model.addTag(new DefaultTagCloudItem("RIA", "#", 3));
		model.addTag(new DefaultTagCloudItem("AJAX", 2));
		model.addTag(new DefaultTagCloudItem("jQuery", "#", 5));
		model.addTag(new DefaultTagCloudItem("NextGen", 4));
		model.addTag(new DefaultTagCloudItem("JSF 2.0", "#", 2));
		model.addTag(new DefaultTagCloudItem("FCB", 5));
		model.addTag(new DefaultTagCloudItem("Mobile", 3));
		model.addTag(new DefaultTagCloudItem("Themes", "#", 4));
		model.addTag(new DefaultTagCloudItem("Rocks", "#", 1));
		model.addTag(new DefaultTagCloudItem("iOt", 8));
		model.addTag(new DefaultTagCloudItem("Android", "#",3));
		model.addTag(new DefaultTagCloudItem("DevOps", "#", 7));
		model.addTag(new DefaultTagCloudItem("Nerds", "#", 4));
		model.addTag(new DefaultTagCloudItem("iOS", "#",3));
		model.addTag(new DefaultTagCloudItem("NodeJS", "#", 7));
		model.addTag(new DefaultTagCloudItem("Erlang", "#", 4));
		return model;
	}

	/**
	 * Perform a login for clients only.
	 * 
	 * @return
	 */
	public String clientLogin() {
		User user = userRoleController.findUserByUsername(username, AuthorizationController.getSystemUser());
		if (user == null) {
			return "login";
		}

		if (!user.getPassword().equals(password)) {
			return "login";
		}
		user.setLastLogin(new Date());
		sessionController.setUser(user);
		return "dashboard/dashboard.xhtml";
	}

	public String logout() {
		System.out.println("LOG OUT: " + username);
		sessionController.setUser(null);
		return "login";
	}

	@Named
	public boolean getLoggedIn() {
		return sessionController.getUser() != null;
	}

}
