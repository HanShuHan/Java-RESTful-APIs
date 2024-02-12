/**
 * File:  ProfessorResource.java Course materials (23S) CST 8277
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
import static acmecollege.utility.MyConstants.PROFESSOR_SUBRESOURCE_NAME;

import java.util.List;
import java.util.Set;

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

import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

import acmecollege.ejb.ACMECollegeService;
import acmecollege.entity.CourseRegistration;
import acmecollege.entity.Professor;

@Path(PROFESSOR_SUBRESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProfessorResource {

	private static final Logger LOG = LogManager.getLogger();

	@EJB
	protected ACMECollegeService service;

	@Inject
	protected SecurityContext sc;

	@GET
	@RolesAllowed({ ADMIN_ROLE })
	public Response getProfessor() throws JsonProcessingException {
		LOG.debug("Retrieving all professors...");

		Response response = Response.noContent().build();
		List<Professor> professors = service.findAll(Professor.class, Professor.FIND_ALL);

		if (!professors.isEmpty()) {
			response = Response.ok(professors).build();
		}

		LOG.debug("Professors found = {}", professors);

		return response;
	}

	@GET
	@RolesAllowed({ ADMIN_ROLE })
	@Path("/{professorId}")
	public Response getProfessorById(@PathParam("professorId") int professorId) {
		LOG.debug("Retrieving professor with id = {}", professorId);

		Professor professor = service.findSingleEntityBy(Professor.class, Professor.FIND_BY_ID_FETCH_ALL, professorId);
		if (professor != null) {
			return Response.ok(professor).build();
		} else {
			return Response.noContent().build();
		}
	}

	@RolesAllowed({ ADMIN_ROLE })
	@POST
	public Response addProfessor(Professor professor) {
		LOG.debug("Adding a new professor = {}", professor);

		String firstName = professor.getFirstName();
		String lastName = professor.getLastName();
		String department = professor.getDepartment();
		boolean isValid = ObjectUtils.allNotNull(firstName, lastName, department);
		if (isValid) {
			service.save(professor);

			return Response.ok(professor).build();
		} else {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@DELETE
	@Path("/{professorId}")
	@RolesAllowed(ADMIN_ROLE)
	public Response deleteProfessorById(@PathParam("professorId") int pId) {
		LOG.debug("Deleting professor with id = {}", pId);

		Professor professor = service.findSingleEntityBy(Professor.class, Professor.FIND_BY_ID_FETCH_ALL, pId);
		if (professor != null) { // If the professor exists
			Set<CourseRegistration> courseRegistrations = professor.getCourseRegistrations();
			if (courseRegistrations != null && courseRegistrations.size() > 0) { // If the professor has course(s)
				courseRegistrations.stream().forEach(cR -> {
					cR.setProfessor(null);
					service.update(cR);
				});
			} // End of reseting the course registrations
			boolean isDeleted = (service.deleteProfessorById(pId) == 1); // Delete the professor
			if (isDeleted) {
				return Response.ok(professor).build();
			} else {
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		} else {
			HttpErrorResponse errEntity =  new HttpErrorResponse(Status.BAD_REQUEST);
			return Response.status(Status.BAD_REQUEST).entity(errEntity).build();
		}
	}

}
