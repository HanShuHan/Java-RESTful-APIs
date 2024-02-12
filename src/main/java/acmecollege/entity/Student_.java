package acmecollege.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2023-12-01T21:09:23.979-0500")
@StaticMetamodel(Student.class)
public class Student_ extends PojoBase_ {
	public static volatile SingularAttribute<Student, String> firstName;
	public static volatile SingularAttribute<Student, String> lastName;
	public static volatile SetAttribute<Student, MembershipCard> membershipCards;
	public static volatile SetAttribute<Student, CourseRegistration> courseRegistrations;
	public static volatile SetAttribute<Student, SecurityUser> securityUsers;
}
