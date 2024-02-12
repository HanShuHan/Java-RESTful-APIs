/***************************************************************************
 * File:  CourseRegistration.java Course materials (23S) CST 8277
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
import java.util.Objects;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("unused")
/**
 * The persistent class for the course_registration database table.
 */
@Entity
@Table(name = "course_registration")
//@Access(AccessType.FIELD)
@NamedQuery(name = CourseRegistration.FIND_ALL, query = "SELECT cr FROM CourseRegistration cr LEFT JOIN FETCH cr.course LEFT JOIN FETCH cr.student LEFT JOIN FETCH cr.professor")
@NamedQuery(name = CourseRegistration.FIND_ALL_BY_STUDENT_ID, query = "SELECT cr FROM CourseRegistration cr LEFT JOIN FETCH cr.course LEFT JOIN FETCH cr.professor LEFT JOIN FETCH cr.student WHERE cr.student.id = :param1")
@NamedQuery(name = CourseRegistration.FIND_ALL_BY_COURSE_ID, query = "SELECT cr FROM CourseRegistration cr LEFT JOIN FETCH cr.course LEFT JOIN FETCH cr.professor LEFT JOIN FETCH cr.student WHERE cr.course.id = :param1")
@NamedQuery(name = CourseRegistration.FIND_BY_STUDENT_ID_AND_COURSE_ID, query = "SELECT cr FROM CourseRegistration cr WHERE cr.student.id = :param1 AND cr.course.id = :param2")
@NamedQuery(name = CourseRegistration.FIND_BY_STUDENT_ID_AND_COURSE_ID_FETCH_ALL, query = "SELECT cr FROM CourseRegistration cr LEFT JOIN FETCH cr.course LEFT JOIN FETCH cr.professor LEFT JOIN FETCH cr.student WHERE cr.student.id = :param1 AND cr.course.id = :param2")
public class CourseRegistration extends PojoBaseCompositeKey<CourseRegistrationPK> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String FIND_ALL = "CourseRegistration.findAll";
	public static final String FIND_ALL_BY_STUDENT_ID = "CourseRegistration.findAllByStudentId";
	public static final String FIND_ALL_BY_COURSE_ID = "CourseRegistration.findAllByCourseId";
	public static final String FIND_BY_STUDENT_ID_AND_COURSE_ID = "CourseRegistration.findByStudentIdAndCourseId";
	public static final String FIND_BY_STUDENT_ID_AND_COURSE_ID_FETCH_ALL = "CourseRegistration.findByStudentIdAndCourseIdFetchAll";

	// Hint - What annotation is used for a composite primary key type?
	@EmbeddedId
	private CourseRegistrationPK id;

	// @MapsId is used to map a part of composite key to an entity.
	@MapsId("studentId")
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id", referencedColumnName = "id", nullable = false)
	private Student student;

	// CR01 - Add missing annotations.  Similar to student, this field is a part of the composite key of this entity.  Changes to this class should cascade.  Reference to a course is not optional.
	@MapsId("courseId")
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", referencedColumnName = "course_id", nullable = false)
	private Course course;

	// CR02 - Add missing annotations.  Changes to this class should cascade.
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "professor_id", referencedColumnName = "professor_id")
	private Professor professor;

	@Column(name = "numeric_grade")
	private int numericGrade;

	@Column(name = "letter_grade", length = 3)
	private String letterGrade;


	public CourseRegistration() {
		id = new CourseRegistrationPK();
	}

	@Override
	public CourseRegistrationPK getId() {
		return id;
	}

	@Override
	public void setId(CourseRegistrationPK id) {
		this.id = id;
	}

//	@JsonIgnore
	@JsonBackReference("student-courseRegistration")
	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		if (student != null) {
			id.setStudentId(student.id);			
		} 
//		else {
//			id.setStudentId(0);
//		}
		this.student = student;
		
//		if (student != null) {
//			student.getCourseRegistrations().add(this);
//		}
	}

	@JsonBackReference("course-courseRegistration")
	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		if (course != null) {
			id.setCourseId(course.id);
		}
		
		this.course = course;
		
//		if (course != null) {
//			course.getCourseRegistrations().add(this);
//		}
	}

	@JsonBackReference("prof-courseRegistration")
//	@JsonIgnore
	public Professor getProfessor() {
		return professor;
	}

	public void setProfessor(Professor professor) {
		this.professor = professor;
		
//		if (professor != null) {
//			professor.getCourseRegistrations().add(this);
//		}
	}

	public int getNumericGrade() {
		return numericGrade;
	}
	
	public void setNumericGrade(int numericGrade) {
		this.numericGrade = numericGrade;
	}

	public String getLetterGrade() {
		return letterGrade;
	}

	public void setLetterGrade(String letterGrade) {
		this.letterGrade = letterGrade;
	}

	//Inherited hashCode/equals is sufficient for this entity class

}