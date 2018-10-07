package de.hallerweb.enterprise.prioritize.view;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.controller.usersetting.UserPreferenceController;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.usersetting.UserPreference;

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
	UserPreferenceController preferenceController;
	@Inject
	SessionController sessionController;

	private String username;
	private String password;
	private boolean loggedIn;

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	private static final String NAVIGATION_LOGIN = "login";

	public User getCurrentUser() {
		return sessionController.getUser();
	}

	int dashboardTabsActiveIndex = 0;

	public int getDashboardTabsActiveIndex() {
		return dashboardTabsActiveIndex;
	}

	public void setDashboardTabsActiveIndex(int dashboardTabsActiveIndex) {
		this.dashboardTabsActiveIndex = dashboardTabsActiveIndex;
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

	@Named
	public List<Resource> getWatchedResources() {
		UserPreference prefs = sessionController.getUser().getPreference();
		List<Resource> watchedResources = preferenceController.getWatchedResources(prefs);
		Set<Resource> depdupeResources = new LinkedHashSet<>(watchedResources);
		watchedResources.clear();
		watchedResources.addAll(depdupeResources);
		if (!watchedResources.isEmpty()) {
			// TODO: depdupeResources sublist is a hack here!!!
			return watchedResources;
		} else {
			return new ArrayList<>();
		}
	}

	public String login() {
		User user = userRoleController.findUserByUsername(username, AuthorizationController.getSystemUser());
		if (user == null) {
			loggedIn = false;
			return NAVIGATION_LOGIN;
		}

		if (!user.getPassword().equals(password)) {
			loggedIn = false;
			return NAVIGATION_LOGIN;
		}

		user.setLastLogin(new Date());
		sessionController.setUser(user);
		loggedIn = true;
		return "index";
	}

	/**
	 * Perform a login for clients only.
	 * 
	 * @return
	 */
	public String clientLogin() {
			return initializeBasicSession();
	}

	private String initializeBasicSession() {
		User user = userRoleController.findUserByUsername(username, AuthorizationController.getSystemUser());
		if (user == null) {
			loggedIn = false;
			return NAVIGATION_LOGIN;
		}

		user.setLastLogin(new Date());
		sessionController.setUser(user);
		loggedIn = true;
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		try {
			context.redirect(context.getApplicationContextPath() + "/client/dashboard/dashboard.xhtml");
		} catch (IOException e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
		}
		return "dashboard";
	}

	public void initializeNoParamKeycloakSession() {
		if (Boolean.parseBoolean(InitializationController.getConfig().get(InitializationController.USE_KEYCLOAK_AUTH))) {
			String userName = username;
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			if (context.getUserPrincipal() != null) {
				userName = context.getUserPrincipal().getName();
			}
			this.username = userName;
			User user = userRoleController.findUserByUsername(username, AuthorizationController.getSystemUser());
			if (user == null) {
				loggedIn = false;
			}
			user.setLastLogin(new Date());
			sessionController.setUser(user);
			loggedIn = true;
		}
	}

	public String logout() {
		sessionController.setUser(null);
		loggedIn = false;
		if (Boolean.parseBoolean(InitializationController.getConfig().get(InitializationController.USE_KEYCLOAK_AUTH))) {
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			try {
				context.redirect("https://steamrunner.info:8443/auth/realms/master/protocol/openid-connect/logout?"
						+ "redirect_uri=https://prioritize-iot.com/PrioritizeWeb/client/dashboard/dashboard.xhtml");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return InitializationController.getConfig().get(InitializationController.KEYCLOAK_LOGOUT_URL);
		} else {
			return NAVIGATION_LOGIN;
		}
	}

	@Named
	public String logoutClient() {
		sessionController.setUser(null);
		loggedIn = false;
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		try {
			context.redirect(context.getApplicationContextPath() + "/client/login.xhtml");
		} catch (IOException e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
		}
		return NAVIGATION_LOGIN;
	}

	@Named
	public boolean getLoggedIn() {
		return loggedIn;
	}

	public void onDashboadTabChange(int tab) {
		this.dashboardTabsActiveIndex = tab;
	}

}
