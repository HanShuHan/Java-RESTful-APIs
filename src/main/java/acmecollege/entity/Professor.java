/***************************************************************************
 * File:  Professor.java Course materials (23S) CST 8277
 * 
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @date August 28, 2022
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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@SuppressWarnings("unused")

/**
 * The persistent class for the professor database table.
 */
//Hint - @Entity marks this class as an entity which needs to be mapped by JPA.
//Hint - @Entity does not need a name if the name of the class is sufficient.
//Hint - @Entity name does not matter as long as it is consistent across the code.
@Entity
//Hint - @Table defines a specific table on DB which is mapped to this entity.
@Table(name = "professor")
//Hint - @NamedQuery attached to this class which uses JPQL/HQL.  SQL cannot be used with NamedQuery.
//Hint - @NamedQuery uses the name which is defined in @Entity for JPQL, if no name is defined use class name.
//Hint - @NamedNativeQuery can optionally be used if there is a need for SQL query.
@NamedQuery(name = Professor.IS_DUPLICATE_QUERY_NAME, query = "SELECT count(p) FROM Professor p where p.firstName = :param1 and p.lastName = :param2 and p.department = :param3")
@NamedQuery(name = Professor.QUERY_PROFESSOR_BY_NAME_DEPARTMENT, query = "SELECT p FROM Professor p where p.firstName = :param1 and p.lastName = :param2 and p.department = :param3")
@NamedQuery(name = Professor.COUNT_BY_ID, query = "SELECT COUNT(p) FROM Professor p where p.id = :param1")
@NamedQuery(name = Professor.FIND_BY_ID, query = "SELECT p FROM Professor p where p.id = :param1")
@NamedQuery(name = Professor.FIND_BY_ID_FETCH_ALL, query = "SELECT p FROM Professor p LEFT JOIN FETCH p.courseRegistrations where p.id = :param1")
@NamedQuery(name = Professor.FIND_ALL, query = "SELECT p FROM Professor p LEFT JOIN FETCH p.courseRegistrations")
//Hint - @AttributeOverride can override column details.  This entity uses professor_id as its primary key name, it needs to override the name in the mapped super class.
@AttributeOverride(name = "id", column = @Column(name = "professor_id"))
//Hint - PojoBase is inherited by any entity with integer as their primary key.
//Hint - PojoBaseCompositeKey is inherited by any entity with a composite key as their primary key.
public class Professor extends PojoBase implements Serializable {
	
	private static final long serialVersionUID = 1L;

    public static final String IS_DUPLICATE_QUERY_NAME = "Professor.isDuplicate";
    public static final String QUERY_PROFESSOR_BY_NAME_DEPARTMENT = "Professor.findByNameDepartment";
    public static final String COUNT_BY_ID = "Professor.countById";
    public static final String FIND_BY_ID = "Professor.findById";
    public static final String FIND_BY_ID_FETCH_ALL = "Professor.findByIdFetchAll";
    public static final String FIND_ALL = "Professor.findAll";

	// Hint - @Basic(optional = false) is used when the object cannot be null.
	// Hint - @Basic or none can be used if the object can be null.
	// Hint - @Basic is for checking the state of object at the scope of our code.

	// Hint - @Column is used to define the details of the column which this object will map to.
	// Hint - @Column is for mapping and creation (if needed) of an object to DB.
	// Hint - @Column can also be used to define a specific name for the column if it is different than our object name.
	@Column(name = "first_name", nullable = false, length = 50)
	private String firstName;

	@Column(name = "last_name", nullable = false, length = 50)
	private String lastName;

	@Column(name = "department", nullable = false, length = 50)
	private String department;

	// Hint - @Transient is used to annotate a property or field of an entity class, mapped superclass, or embeddable class which is not persistent.
	@Transient
	private String highestEducationalDegree; // Examples:  BS, MS, PhD, DPhil, MD, etc.
	
	@Transient
	private String specialization; // Examples:  Computer Science, Mathematics, Physics, etc.

	// Hint - @OneToMany is used to define 1:M relationship between this entity and another.
	// Hint - @OneToMany option cascade can be added to define if changes to this entity should cascade to objects.
	// Hint - @OneToMany option cascade will be ignored if not added, meaning no cascade effect.
	// Hint - @OneToMany option fetch should be lazy to prevent eagerly initializing all the data.
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "professor")
	// Hint - java.util.Set is used as a collection, however List could have been used as well.
	// Hint - java.util.Set will be unique and also possibly can provide better get performance with HashCode.
	private Set<CourseRegistration> courseRegistrations = new HashSet<>();

	public Professor() {
		super();
	}
	
	public Professor(String firstName, String lastName, String department) {
		this();
		this.firstName = firstName;
		this.lastName = lastName;
		this.department = department;
	}
	
	public Professor(String firstName, String lastName, String department, Set<CourseRegistration> courseRegistrations) {
		this();
		this.firstName = firstName;
		this.lastName = lastName;
		this.department = department;
		this.courseRegistrations = courseRegistrations;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getHighestEducationalDegree() {
		return highestEducationalDegree;
	}

	public void setHighestEducationalDegree(String highestEducationalDegree) {
		this.highestEducationalDegree = highestEducationalDegree;
	}

	public String getSpecialization() {
		return specialization;
	}

	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}
	
	public boolean isValid() {
		return ObjectUtils.allNotNull(getFirstName(), getLastName(), getDepartment());
	}

	@JsonManagedReference("prof-courseRegistration")
//	@JsonIgnore
	public Set<CourseRegistration> getCourseRegistrations() {
		return courseRegistrations;
	}

	public void setCourseRegistrations(Set<CourseRegistration> courseRegistrations) {
		this.courseRegistrations = courseRegistrations;
	}

	public void setProfessor(String firstName, String lastName, String department) {
		setFirstName(firstName);
		setLastName(lastName);
		setDepartment(department);
	}

	//Inherited hashCode/equals is sufficient for this entity class

}
