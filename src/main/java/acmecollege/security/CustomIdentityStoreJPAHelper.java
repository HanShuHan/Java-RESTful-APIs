/**
 * File:  CustomIdentityStoreJPAHelper.java Course materials (23S) CST 8277
 * 
 * @author Teddy Yap
 * @author Mike Norman
 * 
 * Updated by:  Group 11
 *   041060762, Shu-Han Han
 *   041060761, Wan-Hsuan Lee
 *   041061567, Syedmoinuddi Hassan
 *   041066323, Gurarman Singh
 *
 */
package acmecollege.security;

import static acmecollege.utility.MyConstants.PU_NAME;
import static java.util.Collections.emptySet;

import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import acmecollege.entity.SecurityRole;
import acmecollege.entity.SecurityUser;

import static acmecollege.utility.MyConstants.PARAM1;

@SuppressWarnings("unused")

@Singleton
public class CustomIdentityStoreJPAHelper {

	private static final Logger LOG = LogManager.getLogger();

	@PersistenceContext(name = PU_NAME)
	protected EntityManager em;

	public SecurityUser findUserByName(String username) {
		LOG.debug("find a SecurityUser by name = {}", username);
		/*
		 * CISJPAH01 - Call the entity manager's createNamedQuery() method to call a
		 * named query on SecurityUser The named query should be labeled
		 * "SecurityUser.userByName" and accepts a parameter called "param1"
		 * 
		 * Call getSingleResult() inside a try-catch statement (NoResultException)
		 * 
		 * Note: Until this method is complete, the Basic Authentication for all HTTP
		 * requests will fail, none of the REST'ful endpoints will work.
		 * 
		 */
		SecurityUser user = null;

		try {
			user = em.createNamedQuery(SecurityUser.FIND_BY_USERNAME, SecurityUser.class)
					.setParameter(PARAM1, username)
					.getSingleResult();
		} catch (NoResultException e) {
//			LOG.error(e);
		}

        return user;
	}

	public Set<String> findRoleNamesForUser(String username) {
		LOG.debug("find Roles For Username={}", username);
		
		Set<String> roleNames = emptySet();
		SecurityUser securityUser = findUserByName(username);
		
		if (securityUser != null) {
			roleNames =  securityUser.getRoles().stream().map(role -> role.getRoleName()).collect(Collectors.toSet());
		}
		
		return roleNames;
	}
	
	public Set<String> findRoleNamesForUser(SecurityUser user) {
		LOG.debug("find Roles For SecurityUser={}", user);

		return  user.getRoles().stream().map(role -> role.getRoleName()).collect(Collectors.toSet());
	}

	@Transactional
	public void saveSecurityUser(SecurityUser user) {
		LOG.debug("adding new user={}", user);
		
		em.persist(user);
	}

	@Transactional
	public void saveSecurityRole(SecurityRole role) {
		LOG.debug("adding new role={}", role);
		
		em.persist(role);
	}
}