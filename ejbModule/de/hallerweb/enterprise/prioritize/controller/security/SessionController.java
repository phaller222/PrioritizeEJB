package de.hallerweb.enterprise.prioritize.controller.security;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * Session Bean implementation class SessionController. Holds information of the current logged in user.
 */
@Stateful
@SessionScoped
@LocalBean
public class SessionController {

	private User user;

	@PersistenceContext
	EntityManager em;
	@EJB
	UserRoleController controller;

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

}
