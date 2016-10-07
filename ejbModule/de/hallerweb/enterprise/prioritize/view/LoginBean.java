package de.hallerweb.enterprise.prioritize.view;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.jgroups.protocols.AUTH;

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
	private Date lastLogin;
	private User currentUser;

	public User getCurrentUser() {
		this.currentUser = sessionController.getUser();
		return currentUser;
	}

	List<Resource> watchedResources;
	int dashboardTabsActiveIndex = 0;

	public int getDashboardTabsActiveIndex() {
		return dashboardTabsActiveIndex;
	}

	public void setDashboardTabsActiveIndex(int dashboardTabsActiveIndex) {
		this.dashboardTabsActiveIndex = dashboardTabsActiveIndex;
	}

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

	@Named
	public List<Resource> getWatchedResources() {
		UserPreference prefs = sessionController.getUser().getPreference();
		List<Resource> watchedResources = preferenceController.getWatchedResources(prefs);
		Set<Resource> depdupeResources = new LinkedHashSet<>(watchedResources);
		watchedResources.clear();
		watchedResources.addAll(depdupeResources);
		if (!watchedResources.isEmpty()) {
			// TODO: sublist is a hack!!!
			List<Resource> watched = watchedResources;
			return watched;
		} else
			return watchedResources;
	}

	public String login() {
		User user = userRoleController.findUserByUsername(username, AuthorizationController.getSystemUser());
		if (user == null) {
			loggedIn = false;
			return "login";
		}

		if (!user.getPassword().equals(password)) {
			loggedIn = false;
			return "login";
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

		boolean autoLogin = InitializationController.getAsBoolean(InitializationController.ADMIN_AUTO_LOGIN);
		if (autoLogin) {
			User user = userRoleController.findUserByUsername("admin", AuthorizationController.getSystemUser());
			user.setLastLogin(new Date());
			sessionController.setUser(user);
			loggedIn = true;
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			try {
				context.redirect(context.getApplicationContextPath() + "/client/dashboard/dashboard.xhtml");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "dashboard";
		}

		User user = userRoleController.findUserByUsername(username, AuthorizationController.getSystemUser());
		if (user == null) {
			loggedIn = false;
			return "login";
		}

		if (!user.getPassword().equals(password)) {
			loggedIn = false;
			return "login";
		}
		user.setLastLogin(new Date());
		sessionController.setUser(user);
		loggedIn = true;
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		try {
			context.redirect(context.getApplicationContextPath() + "/client/dashboard/dashboard.xhtml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "dashboard";
	}

	public String logout() {
		sessionController.setUser(null);
		loggedIn = false;
		return "login";
	}

	@Named
	public String logoutClient() {
		sessionController.setUser(null);
		loggedIn = false;
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		try {
			context.redirect(context.getApplicationContextPath() + "/client/login.xhtml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "login";
	}

	@Named
	public boolean getLoggedIn() {
		return loggedIn;
	}

	public void onDashboadTabChange(int tab) {
		this.dashboardTabsActiveIndex = tab;
	}

}
