package acmecollege.utility;

import org.apache.commons.lang3.ObjectUtils;

import acmecollege.entity.ClubMembership;
import acmecollege.entity.Course;

public class ObjectIntegrityChecker {

	public boolean check(Object obj) {
		if (obj == null) {	
			return false;
		} else if (obj instanceof Course course) { // Check if the object is an instance of Course
			String code = course.getCourseCode();
			String title = course.getCourseTitle();
			int year = course.getYear();
			String semester = course.getSemester();
			int credits = course.getCreditUnits();
			byte online = course.getOnline();
			
			boolean areStringsAllNotNull = ObjectUtils.allNotNull(code, title, semester);
			if (!areStringsAllNotNull) {
				return false;
			}
			if (year <= 0 || credits <= 0 || online < 0) {
				return false;
			}
			return true;
		} else if (obj instanceof ClubMembership clubMembership) { // Check if the object is an instance of ClubMembership
			if (clubMembership != null && clubMembership.getDurationAndStatus() != null) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}
	
}
