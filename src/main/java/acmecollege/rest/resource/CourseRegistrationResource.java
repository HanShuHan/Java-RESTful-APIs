/**
 * File:  CourseRegistrationResource.java Course materials (23S) CST 8277
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
package acmecollege.rest.resource;

import static acmecollege.utility.MyConstants.ADMIN_ROLE;
import static acmecollege.utility.MyConstants.COURSE_REGISTRATION_RESOURCE_NAME;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.TransactionRequiredException;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

import acmecollege.ejb.ACMECollegeService;
import acmecollege.entity.Course;
import acmecollege.entity.CourseRegistration;
import acmecollege.entity.Professor;
import acmecollege.entity.Student;
import acmecollege.utility.ObjectIntegrityChecker;

@Path(COURSE_REGISTRATION_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CourseRegistrationResource {

	private static final Logger LOG = LogManager.getLogger();

	@EJB
	protected ACMECollegeService service;

	@Inject
	protected SecurityContext sc;

	protected static ObjectIntegrityChecker objectChecker;

	static {
		objectChecker = new ObjectIntegrityChecker();
	}

	@GET
	@RolesAllowed({ ADMIN_ROLE })
	public Response getCourseRegistration(@QueryParam("student_id") int studentId, @QueryParam("course_id") int courseId)
			throws JsonProcessingException {

		LOG.debug("Retrieving course registration...");

		List<CourseRegistration> courseRegistrations = null;

		if (studentId < 0 || courseId < 0) {
			HttpErrorResponse errorResponseEntity = new HttpErrorResponse(Status.BAD_REQUEST);
			return Response.status(Status.BAD_REQUEST).entity(errorResponseEntity).build();
		} else if (studentId == 0 && courseId == 0) {
			courseRegistrations = service.findAll(CourseRegistration.class, CourseRegistration.FIND_ALL);
		} else if (studentId > 0 && courseId == 0) {
			courseRegistrations = service.findAllBy(CourseRegistration.class, CourseRegistration.FIND_ALL_BY_STUDENT_ID,
					studentId);
		} else if (studentId == 0 && courseId > 0) {
			courseRegistrations = service.findAllBy(CourseRegistration.class, CourseRegistration.FIND_ALL_BY_COURSE_ID, courseId);
		} else { // studentId > 0 && courseId > 0
			courseRegistrations = service.findAllBy(CourseRegistration.class, CourseRegistration.FIND_BY_STUDENT_ID_AND_COURSE_ID_FETCH_ALL,
					studentId, courseId);
		}
		// Check the returning result list
		if (courseRegistrations.isEmpty()) {
			return Response.noContent().build();
		} else {
			return Response.ok(courseRegistrations).build();
		}

//		Response response = Response.noContent().build();
//		List<CourseRegistration> courseRegistrations = service.findAll(CourseRegistration.class, CourseRegistration.FIND_ALL);
//		if (sc.isCallerInRole(ADMIN_ROLE)) {
//			if (!courseRegistrations.isEmpty()) {
//				response = Response.ok(courseRegistrations).build();
//			}
//		} else { // Role is user
//			SecurityUser securityUser = (SecurityUser) (((WrappingCallerPrincipal) sc.getCallerPrincipal()).getWrapped());
//			Student student = securityUser.getStudent();
//			if (student != null) {
//				int studentId = student.getId();
//				List<CourseRegistration> returningList = new ArrayList<>();
//				if (!courseRegistrations.isEmpty()) {
//					courseRegistrations.stream().forEach(cr -> { // Return the registration if the user is
//						if (cr.getId().getStudentId() == studentId) {
//							returningList.add(cr);
//						}
//					});
//					// If the returning list is not empty
//					if (!returningList.isEmpty()) {
//						response = Response.ok(returningList).build();
//					}
//				} // else, response = noContent
//			} // else, response = noContent
//		}
//		return response;
	}

	@POST
	@RolesAllowed({ ADMIN_ROLE })
	public Response addCourseRegistrationByStudentIdAndCourseIdOrProfessorId(@QueryParam("student_id") int studentId,
			@QueryParam("course_id") int courseId, @QueryParam("professor_id") int professorId,
			CourseRegistration postCourseRegistration) {

		HttpErrorResponse errorResponseEntity = new HttpErrorResponse(Status.BAD_REQUEST);
		Response response = Response.status(Status.BAD_REQUEST).entity(errorResponseEntity).build();

		// Ids integrity check
		if (studentId > 0 && courseId > 0 && professorId >= 0) {
			Student student = service.findSingleEntityBy(Student.class, Student.FIND_BY_ID, studentId);
			Course course = service.findSingleEntityBy(Course.class, Course.FIND_BY_ID, courseId);
			CourseRegistration courseRegistration = service.findSingleEntityBy(CourseRegistration.class,
					CourseRegistration.FIND_BY_STUDENT_ID_AND_COURSE_ID, studentId, courseId);
			// Create a new course registration only if the student and the course exist,
			// and not registered before
			if (student != null && course != null && courseRegistration == null) {
				Professor professor = service.findSingleEntityBy(Professor.class, Professor.FIND_BY_ID, professorId);
				CourseRegistration newCourseRegistration = null;
				if (professorId > 0 && professor != null) {
					try { 
						// Add
						newCourseRegistration = service.addCourseRegistrationBy(studentId, courseId, professorId);
						// Set detail
						if (postCourseRegistration != null) {
							newCourseRegistration.setNumericGrade(postCourseRegistration.getNumericGrade());
							newCourseRegistration.setLetterGrade(postCourseRegistration.getLetterGrade());
							service.update(newCourseRegistration);
						}
					} catch (Exception e) { 
						// If an error happened during creation
						response = Response.serverError().build();
					}
					response = Response.ok(newCourseRegistration).build();
				} else if (professorId == 0) {
					try {
						// Add
						newCourseRegistration = service.addCourseRegistrationBy(studentId, courseId);
						// Set detail
						if (postCourseRegistration != null) {
							newCourseRegistration.setNumericGrade(postCourseRegistration.getNumericGrade());
							newCourseRegistration.setLetterGrade(postCourseRegistration.getLetterGrade());
							service.update(newCourseRegistration);
						}
					} catch (Exception e) { 
						// If an error happened during creation
						response = Response.serverError().build();
					}
					response = Response.ok(newCourseRegistration).build();
				} // else
			} // else
		} // else
		return response;
	}

	@DELETE
	@RolesAllowed(ADMIN_ROLE)
	public Response deleteCourseRegistration(@QueryParam("student_id") int studentId, @QueryParam("course_id") int courseId) {
		LOG.debug("Deleting course with id = {}", courseId);
		
		List<CourseRegistration> courseRegistrations = null;
		
		try {
			if (studentId < 0 || courseId < 0) {
				HttpErrorResponse errorResponseEntity = new HttpErrorResponse(Status.BAD_REQUEST);
				return Response.status(Status.BAD_REQUEST).entity(errorResponseEntity).build();
			} else if (studentId == 0 && courseId == 0) {
				courseRegistrations = service.deleteCourseRegistration();
			} else if (studentId > 0 && courseId == 0) {
				courseRegistrations = service.deleteCourseRegistrationBy(CourseRegistration.FIND_ALL_BY_STUDENT_ID, studentId);
			} else if (studentId == 0 && courseId > 0) {
				courseRegistrations = service.deleteCourseRegistrationBy(CourseRegistration.FIND_ALL_BY_COURSE_ID, courseId);
			} else { // studentId > 0 && courseId > 0
				courseRegistrations = service.deleteCourseRegistrationBy(CourseRegistration.FIND_BY_STUDENT_ID_AND_COURSE_ID_FETCH_ALL,
						studentId, courseId);
			}
		} catch (TransactionRequiredException e) { // Error happened during deleting.
			return Response.serverError().build();
		}
		
		// Check the returning result list
		if (courseRegistrations.isEmpty()) {
			return Response.noContent().build();
		} else {
			return Response.ok(courseRegistrations).build();
		}
	}

}
