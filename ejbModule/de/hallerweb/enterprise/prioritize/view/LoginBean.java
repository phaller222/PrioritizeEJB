/*
 * Copyright 2015-2020 Peter Michael Haller and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.hallerweb.enterprise.prioritize.view;

import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.SessionController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.controller.usersetting.UserPreferenceController;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.User;
import de.hallerweb.enterprise.prioritize.model.usersetting.UserPreference;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    transient UserRoleController userRoleController;
    transient @EJB AuthorizationController authController;
    @Inject
    UserPreferenceController preferenceController;
    @Inject
    SessionController sessionController;
    transient @EJB InitializationController initController;

    private String username;
    private String password;
    private boolean loggedIn;

    private static final String NAVIGATION_LOGIN = "login";

    int dashboardTabsActiveIndex = 0;

    public int getDashboardTabsActiveIndex() {
        return dashboardTabsActiveIndex;
    }

    public void setDashboardTabsActiveIndex(int dashboardTabsActiveIndex) {
        this.dashboardTabsActiveIndex = dashboardTabsActiveIndex;
    }

    public User getCurrentUser() {
        return sessionController.getUser();
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
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
        if (watchedResources.isEmpty()) {
            return new ArrayList<>();
        } else {
            // TODO: depdupeResources sublist is a hack here!!!
            return watchedResources;
        }
    }

    public String login() {
        User user = userRoleController.findUserByUsername(username, authController.getSystemUser());
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
        User user = userRoleController.findUserByUsername(username, authController.getSystemUser());
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
        if (Boolean.parseBoolean(InitializationController.config.get(InitializationController.USE_KEYCLOAK_AUTH))) {
            String userName = username;
            ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
            if (context.getUserPrincipal() != null) {
                userName = context.getUserPrincipal().getName();
            }
            this.username = userName;
            User user = userRoleController.findUserByUsername(username, authController.getSystemUser());
            if (user == null) {
                loggedIn = false;
            } else {
                user.setLastLogin(new Date());
                sessionController.setUser(user);
                loggedIn = true;
            }
        }
    }

    public String logout() {
        sessionController.setUser(null);
        loggedIn = false;
        if (Boolean.parseBoolean(InitializationController.config.get(InitializationController.USE_KEYCLOAK_AUTH))) {
            ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
            try {
                context.redirect("https://localhost:8443/auth/realms/master/protocol/openid-connect/logout?"
                        + "redirect_uri=https://localhost/PrioritizeWeb/client/dashboard/dashboard.xhtml");
            } catch (IOException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
            }
            return InitializationController.config.get(InitializationController.KEYCLOAK_LOGOUT_URL);
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
