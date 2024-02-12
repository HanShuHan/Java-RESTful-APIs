/**
 * File:  CourseResource.java Course materials (23S) CST 8277
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
import static acmecollege.utility.MyConstants.COURSE_RESOURCE_NAME;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

import acmecollege.ejb.ACMECollegeService;
import acmecollege.entity.Course;
import acmecollege.entity.CourseRegistration;
import acmecollege.entity.Course_;
import acmecollege.utility.ObjectIntegrityChecker;

@Path(COURSE_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CourseResource {

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
	public Response getAllCourse() throws JsonProcessingException {
		LOG.debug("Retrieving all courses...");

		List<Course> courses = service.findAll(Course.class, Course.FIND_ALL);
		if (courses.isEmpty()) {
			return Response.noContent().build();
		} else {
			List<Course> returningList = courses;
			if (!sc.isCallerInRole(ADMIN_ROLE)) {
				returningList = courses.stream().peek(course -> {
					service.detach(course);
					course.getCourseRegistrations().clear();
				}).collect(Collectors.toList());
			}
			return Response.ok(returningList).build();
		}
	}

	@GET
	@Path("/{courseId}")
	public Response getCourseById(@PathParam("courseId") int courseId) {
		LOG.debug("Retrieving course with id = {}", courseId);
		
		Course course = service.findSingleEntityBy(Course.class, Course.FIND_BY_ID_FETCH_ALL, courseId);
		if (course == null) {
			return Response.noContent().build();
		} else {
			Course returningCourse = course;
			if (!sc.isCallerInRole(ADMIN_ROLE)) {
				service.detach(course);
				course.getCourseRegistrations().clear();
			}
			return Response.ok(returningCourse).build();
		}
		
	}
	
	@GET
	@Path("/{courseId}" + COURSE_REGISTRATION_RESOURCE_NAME)
	public Response getAllCourseRegistrationByCourseId(@PathParam("courseId") int courseId) {
		LOG.debug("Retrieving all course registrations with course id = {}", courseId);

		List<CourseRegistration> courseRegistrations = service.findAllBy(CourseRegistration.class,
				CourseRegistration.FIND_ALL_BY_COURSE_ID, courseId);
		if (courseRegistrations.isEmpty()) {
			return Response.noContent().build();
		} else {
			return Response.ok(courseRegistrations).build();
		}
	}

	@RolesAllowed({ ADMIN_ROLE })
	@POST
	public Response addCourse(Course course) {
		LOG.debug("Adding a new course = {}", course);

		boolean isCourseValid = objectChecker.check(course);
		if (isCourseValid) {
			Course newCourse = new Course(course.getCourseCode(), course.getCourseTitle(), course.getYear(), course.getSemester(),
					course.getCreditUnits(), course.getOnline());
			service.save(newCourse);

			return Response.ok(newCourse).build();
		} else {
			HttpErrorResponse errorEntity = new HttpErrorResponse(Status.BAD_REQUEST);
			return Response.status(Status.BAD_REQUEST).entity(errorEntity).build();
		}
	}

	@DELETE
	@Path("/{courseId}")
	@RolesAllowed(ADMIN_ROLE)
	public Response deleteProfessorById(@PathParam("courseId") int courseId) {
		LOG.debug("Deleting course with id = {}", courseId);

		Course course = service.findSingleEntityBy(Course.class, Course.FIND_BY_ID_FETCH_ALL, courseId);
		if (course == null) {
			HttpErrorResponse errResponseEntity = new HttpErrorResponse(Status.BAD_REQUEST);
			return Response.status(Status.BAD_REQUEST).entity(errResponseEntity).build();
		} else {
			service.detach(course);
			int count = service.deleteEntityFrom(Course.class, Course_.id, Integer.class, courseId);
			if (count == 1) {
				return Response.ok(course).build();
			} else {
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}

}
