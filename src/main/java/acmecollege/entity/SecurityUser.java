/**
 * File:  SecurityUser.java Course materials (23S) CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman
 * 
 * Updated by:  Group 11
 *   041060762, Shu-Han Han
 *   041060761, Wan-Hsuan Lee
 *   041061567, Syedmoinuddi Hassan
 *   041066323, Gurarman Singh
 *
 */
package acmecollege.entity;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import acmecollege.rest.serializer.SecurityRoleSerializer;

@SuppressWarnings("unused")

/**
 * User class used for (JSR-375) Java EE Security authorization/authentication
 */

// - Make this into JPA entity and add all the necessary annotations
@Entity
@Table(name = "security_user", catalog = "acmecollege")
@NamedQuery(name = SecurityUser.COUNT_BY_USERNAME, query = "SELECT COUNT(su) FROM SecurityUser su WHERE su.username = :param1")
@NamedQuery(name = SecurityUser.FIND_BY_USERNAME, query = "SELECT su FROM SecurityUser su WHERE su.username = :param1")
@NamedQuery(name = SecurityUser.FIND_BY_STUDENT_ID_FETCH_ROLES, query = "SELECT su FROM SecurityUser su LEFT JOIN FETCH su.roles WHERE su.student.id = :param1")
public class SecurityUser implements Serializable, Principal {

	/** Explicit set serialVersionUID */
	private static final long serialVersionUID = 1L;

	public static final String COUNT_BY_USERNAME = "SecurityUser.countByUsername";
	public static final String FIND_BY_USERNAME = "SecurityUser.findByUsername";
	public static final String FIND_BY_STUDENT_ID_FETCH_ROLES = "SecurityUser.findByStudentId";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	protected int id;

	@Column(name = "username", length = 100, nullable = false, unique = true)
	protected String username;

	@Column(name = "password_hash", length = 256, nullable = false)
	protected String pwHash;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id", referencedColumnName = "id")
	protected Student student;

	@ManyToMany
	@JoinTable(name = "user_has_role", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "role_id"))
	protected Set<SecurityRole> roles = new HashSet<SecurityRole>();

	public SecurityUser() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPwHash() {
		return pwHash;
	}

	public void setPwHash(String pwHash) {
		this.pwHash = pwHash;
	}

	// SU01 - Setup custom JSON serializer
	@JsonSerialize(using = SecurityRoleSerializer.class)
	public Set<SecurityRole> getRoles() {
		return roles;
	}

	public void setRoles(Set<SecurityRole> roles) {
		this.roles = roles;
//
//		if (roles != null && !roles.isEmpty() && !roles.contains(null)) {
//			roles.stream().forEach(role -> {
//					role.addUserToRole(this);
//			});
//		}
	}

	public void addRoles(Set<SecurityRole> roles) {
		if (roles != null && !roles.isEmpty() && !roles.contains(null)) {
			this.roles.addAll(roles);
//			roles.stream().forEach(role -> {
//				role.addUser(this);
//			});
		}
	}

	public void addRole(SecurityRole role) {
		if (role != null) {
			getRoles().add(role);
//			role.addUser(this);
		}
	}
	
//	@JsonIgnore
	@JsonBackReference("student-security-user")
	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;

//		if (student != null) {
//			student.getSecurityUsers().add(this);
//		}
	}

	// Principal
	@Override
	public String getName() {
		return getUsername();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		// Only include member variables that really contribute to an object's identity
		// i.e. if variables like version/updated/name/etc. change throughout an
		// object's lifecycle,
		// they shouldn't be part of the hashCode calculation
		return prime * result + Objects.hash(getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj instanceof SecurityUser otherSecurityUser) {
			// See comment (above) in hashCode(): Compare using only member variables that
			// are
			// truly part of an object's identity
			return Objects.equals(this.getId(), otherSecurityUser.getId());
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SecurityUser [id = ").append(id).append(", username = ").append(username).append("]");
		return builder.toString();
	}

}
