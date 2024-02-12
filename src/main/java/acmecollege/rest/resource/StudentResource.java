/**
 * File:  StudentResource.java Course materials (23S) CST 8277
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
import static acmecollege.utility.MyConstants.PROFESSOR_SUBRESOURCE_NAME;
import static acmecollege.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static acmecollege.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static acmecollege.utility.MyConstants.STUDENT_COURSE_RESOURCE_PATH;
import static acmecollege.utility.MyConstants.STUDENT_RESOURCE_NAME;
import static acmecollege.utility.MyConstants.USER_ROLE;

import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;

import acmecollege.ejb.ACMECollegeService;
import acmecollege.entity.CourseRegistration;
import acmecollege.entity.Professor;
import acmecollege.entity.SecurityUser;
import acmecollege.entity.Student;

@Path(STUDENT_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StudentResource {

	private static final Logger LOG = LogManager.getLogger();

	@EJB
	protected ACMECollegeService service;

	@Inject
	protected SecurityContext sc;

	@GET
	@RolesAllowed({ ADMIN_ROLE })
	public Response getStudents() {
		LOG.debug("retrieving all students ...");

		List<Student> students = service.getAllStudents();
		if (!students.isEmpty()) {
			return Response.ok(students).build();
		} else {
			return Response.noContent().build();
		}
	}

	@GET
	@RolesAllowed({ ADMIN_ROLE, USER_ROLE })
	@Path(RESOURCE_PATH_ID_PATH)
	public Response getStudentById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
		LOG.debug("try to retrieve specific student " + id);

		Response response = Response.noContent().build();
		Student student = null;

		if (sc.isCallerInRole(ADMIN_ROLE)) {
			student = service.getStudentById(id);

			if (student != null) {
				response = Response.ok().entity(student).build();
			}
		} else if (sc.isCallerInRole(USER_ROLE)) {
			WrappingCallerPrincipal wCallerPrincipal = (WrappingCallerPrincipal) sc.getCallerPrincipal();
			SecurityUser sUser = (SecurityUser) wCallerPrincipal.getWrapped();
			student = service.getStudentById(sUser.getStudent().getId());

			if (student != null) {
				if (student.getId() == id) {
					response = Response.ok().entity(student).build();
				} else {
					throw new ClientErrorException(Status.FORBIDDEN);
//					throw new ClientErrorException(Response.status(Status.FORBIDDEN)
//							.entity(new HttpErrorResponse(Status.FORBIDDEN.getStatusCode(), UNAUTHORIZED_RESOURCE_MSG)).build());
//					throw WebApplicationException(Status.FORBIDDEN.getStatusCode(),
//							"User trying to access resource it does not own (wrong userid).");
//					response = Response.status(Status.FORBIDDEN.getStatusCode(),
//							"User trying to access resource it does not own (wrong userid).").build();
				}
			} else {
				throw new ClientErrorException(Status.FORBIDDEN);
			}
		}
		return response;
	}

	@GET
	@Path(RESOURCE_PATH_ID_PATH + COURSE_REGISTRATION_RESOURCE_NAME)
	@RolesAllowed({ ADMIN_ROLE })
	public Response getAllCourseRegistrationByStudentId(@PathParam("id") int studentId) {
		LOG.debug("Retrieving course registrations with student id = {}", studentId);

		List<CourseRegistration> courseRegistrations = service.findAllBy(CourseRegistration.class,
				CourseRegistration.FIND_ALL_BY_STUDENT_ID, studentId);
		if (courseRegistrations.isEmpty()) {
			return Response.noContent().build();
		} else {
			return Response.ok(courseRegistrations).build();
		}
	}

	@POST
	@RolesAllowed({ ADMIN_ROLE })
	public Response addPerson(Student newStudent) {
		Student newStudentWithIdTimestamps = service.save(newStudent);

		// Build a SecurityUser linked to the new student
		service.buildUserForNewStudent(newStudentWithIdTimestamps);

		return Response.ok(newStudentWithIdTimestamps).build();
	}

	@PUT
	@RolesAllowed({ ADMIN_ROLE })
	@Path(STUDENT_COURSE_RESOURCE_PATH + PROFESSOR_SUBRESOURCE_NAME)
	public Response updateProfessorForStudentCourse(@PathParam("studentId") int studentId, @PathParam("courseId") int courseId,
			Professor newProfessor) {
		Student student = service.getStudentById(studentId);
		if (student == null) {
			return Response.status(Status.BAD_REQUEST.getStatusCode(), "The student does not exist.").build();
		}
		CourseRegistration courseRegistrataion = service.findStudentCourseRegistrationBy(studentId, courseId);
		if (courseRegistrataion == null) {
			return Response.status(Status.BAD_REQUEST.getStatusCode(), "The course does not exist.").build();
		}

		// Checks if the professor does not exist, the user should provide all necessary
		// values.
		Professor foundProfessor = service.findSingleEntityBy(Professor.class, Professor.FIND_BY_ID_FETCH_ALL,
				newProfessor.getId());
		;
		if (foundProfessor == null && (newProfessor.getFirstName() == null || newProfessor.getLastName() == null
				|| newProfessor.getDepartment() == null)) {
			return Response.status(Status.BAD_REQUEST.getStatusCode(), "The professor lacks necessary value(s).").build();
		}

		Professor professor = service.setProfessorForStudentCourse(studentId, courseId, newProfessor);
		return Response.ok(professor).build();
	}

	@DELETE
	@RolesAllowed(ADMIN_ROLE)
	@Path("/{studentId}")
	public Response deleteStudentClub(@PathParam("studentId") int sId) {
		LOG.debug("Deleting student club with id = {}", sId);

		HttpErrorResponse errorResponseEntity = new HttpErrorResponse(Status.BAD_REQUEST);
		Response response = Response.status(Status.BAD_REQUEST).entity(errorResponseEntity).build();
		
		Student student = service.findSingleEntityBy(Student.class, Student.FIND_BY_ID_FETCH_ALL, sId);
		if (student != null) {
			Set<CourseRegistration> courseRegistrations = student.getCourseRegistrations();
			try {
				if (courseRegistrations == null || courseRegistrations.size() == 0) {
					// The student is deletable
					service.deleteStudentById(sId);
					response = Response.ok(student).build();
				} else { // The student is registered in a course (foreign key constraint)
					errorResponseEntity = new HttpErrorResponse(Status.CONFLICT);
					response = Response.status(Status.CONFLICT).entity(errorResponseEntity).build();
				}
			} catch (Exception e) {
				response = Response.serverError().build();
			}
		} // else the student does not exist
		return response;
	}

}