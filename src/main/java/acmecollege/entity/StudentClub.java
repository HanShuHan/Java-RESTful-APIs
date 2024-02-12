/***************************************************************************
 * File:  StudentClub.java Course materials (23S) CST 8277
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
import java.util.Objects;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * The persistent class for the student_club database table.
 */
// SC01 - Add the missing annotations.
@Entity
@Table(name = "student_club", catalog = "acmecollege")
// SC02 - StudentClub has subclasses AcademicStudentClub and NonAcademicStudentClub.  Look at week 9 slides for InheritanceType.
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(columnDefinition = "BIT(1)", name = "academic", discriminatorType = DiscriminatorType.INTEGER)
// SC03 - Do we need a mapped super class?  If so, which one?
@AttributeOverride(name = "id", column = @Column(name = "club_id"))
// SC06 - Add in JSON annotations to indicate different sub-classes of StudentClub
@JsonTypeInfo(use = Id.NAME, property = "student-club", include = As.PROPERTY)
@JsonSubTypes({ 
	@Type(value = AcademicStudentClub.class, name = "academic-student-club"),
	@Type(value = NonAcademicStudentClub.class, name = "non-academic-student-club") 
})
@NamedQuery(name = StudentClub.FIND_BY_ID, query = "SELECT sc FROM StudentClub sc LEFT JOIN FETCH sc.clubMemberships WHERE sc.id = :param1")
@NamedQuery(name = StudentClub.COUNT_BY_NAME, query = "SELECT COUNT(sc) FROM StudentClub sc WHERE sc.name = :param1")
public abstract class StudentClub extends PojoBase implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String FIND_BY_ID = "StudentClub.findById";
	public static final String COUNT_BY_NAME = "StudentClub.countByName";

	//  SC04 - Add the missing annotations.
	@Column(name = "name", length = 100, nullable = false, unique = true)
	private String name;

	//  SC05 - Add the 1:M annotation. This list should be effected by changes
	// to this object (cascade).
	@OneToMany(mappedBy = "club", cascade = CascadeType.ALL)
	private Set<ClubMembership> clubMemberships = new HashSet<>();

	@Transient
	private boolean isAcademic;

	public StudentClub() {
		super();
	}

	public StudentClub(boolean isAcademic) {
		this();
		this.isAcademic = isAcademic;
	}

	// Simplify Json body, skip ClubMemberships
	@JsonIgnore
//	@JsonManagedReference("studentClub-clubMembership")
	public Set<ClubMembership> getClubMemberships() {
		return clubMemberships;
	}

	public void setClubMembership(Set<ClubMembership> clubMemberships) {
		this.clubMemberships = clubMemberships;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	// Inherited hashCode/equals is NOT sufficient for this entity class

	/**
	 * Very important: Use getter's for member variables because JPA sometimes needs
	 * to intercept those calls<br/>
	 * and go to the database to retrieve the value
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		// Only include member variables that really contribute to an object's identity
		// i.e. if variables like version/updated/name/etc. change throughout an
		// object's lifecycle,
		// they shouldn't be part of the hashCode calculation

		// The database schema for the STUDENT_CLUB table has a UNIQUE constraint for
		// the NAME column,
		// so we should include that in the hash/equals calculations

		return prime * result + Objects.hash(getId(), getName());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}

		if (obj instanceof StudentClub otherStudentClub) {
			// See comment (above) in hashCode(): Compare using only member variables that
			// are
			// truly part of an object's identity
			return Objects.equals(this.getId(), otherStudentClub.getId())
					&& Objects.equals(this.getName(), otherStudentClub.getName());
		}
		return false;
	}
}
