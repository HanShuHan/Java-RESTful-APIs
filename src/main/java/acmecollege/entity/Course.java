/***************************************************************************
 * File:  Course.java Course materials (23S) CST 8277
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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@SuppressWarnings("unused")

/**
 * The persistent class for the course database table.
 */
// CO01 - Add the missing annotations.
@Entity
// CO02 - Do we need a mapped super class?  If so, which one?
@Table(name = "course", catalog = "acmecollege")
@AttributeOverride(name = "id", column = @Column(name = "course_id"))
@NamedQuery(name = Course.FIND_ALL, query = "SELECT c FROM Course c LEFT JOIN FETCH c.courseRegistrations")
@NamedQuery(name = Course.FIND_BY_ID, query = "SELECT c FROM Course c WHERE c.id = :param1")
@NamedQuery(name = Course.FIND_BY_ID_FETCH_ALL, query = "SELECT c FROM Course c LEFT JOIN FETCH c.courseRegistrations WHERE c.id = :param1")
public class Course extends PojoBase implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String FIND_ALL = "Course.findAll";
	public static final String FIND_BY_ID = "Course.findById";
	public static final String FIND_BY_ID_FETCH_ALL = "Course.findByIdFetchAll";

	//  CO03 - Add missing annotations.
	@Column(name = "course_code", nullable = false, length = 7, unique = true)
	private String courseCode;

	//  CO04 - Add missing annotations.
	@Column(name = "course_title", nullable = false, length = 100)
	private String courseTitle;

	//  CO05 - Add missing annotations.
	@Column(name = "year", nullable = false)
	private int year;

	//  CO06 - Add missing annotations.
	@Column(name = "semester", nullable = false, length = 6)
	private String semester;

	//  CO07 - Add missing annotations.
	@Column(name = "credit_units", nullable = false)
	private int creditUnits;

	//  CO08 - Add missing annotations.
	@Column(name = "online", nullable = false, columnDefinition = "BIT(1)")
	private byte online;

	//  CO09 - Add annotations for 1:M relation.  Changes to this class should not cascade.
	@OneToMany(mappedBy = "course")
	private Set<CourseRegistration> courseRegistrations = new HashSet<>();

	public Course() {
		super();
	}

	public Course(String courseCode, String courseTitle, int year, String semester, int creditUnits, byte online) {
		this();
		this.courseCode = courseCode;
		this.courseTitle = courseTitle;
		this.year = year;
		this.semester = semester;
		this.creditUnits = creditUnits;
		this.online = online;
	}

	public Course setCourse(String courseCode, String courseTitle, int year, String semester, int creditUnits, byte online) {
		setCourseCode(courseCode);
		setCourseTitle(courseTitle);
		setYear(year);
		setSemester(semester);
		setCreditUnits(creditUnits);
		setOnline(online);
		return this;
	}

	public String getCourseCode() {
		return courseCode;
	}

	public void setCourseCode(String courseCode) {
		this.courseCode = courseCode;
	}

	public String getCourseTitle() {
		return courseTitle;
	}

	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getSemester() {
		return semester;
	}

	public void setSemester(String semester) {
		this.semester = semester;
	}

	public int getCreditUnits() {
		return creditUnits;
	}

	public void setCreditUnits(int creditUnits) {
		this.creditUnits = creditUnits;
	}

	public byte getOnline() {
		return online;
	}

	public void setOnline(byte online) {
		this.online = online;
	}
	
	@JsonManagedReference("course-courseRegistration")
	@JsonInclude(value = Include.NON_EMPTY)
	public Set<CourseRegistration> getCourseRegistrations() {
		return courseRegistrations;
	}

	@JsonInclude(value = Include.NON_EMPTY)
	public void setCourseRegistrations(Set<CourseRegistration> courseRegistrations) {
		this.courseRegistrations = courseRegistrations;
	}

	//Inherited hashCode/equals is sufficient for this Entity class

}
