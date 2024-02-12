/**
 * File:  StudentClubResource.java Course materials (23S) CST 8277
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
import static acmecollege.utility.MyConstants.STUDENT_CLUB_RESOURCE_NAME;
import static acmecollege.utility.MyConstants.USER_ROLE;

import java.util.HashSet;
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
import javax.ws.rs.PUT;
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
import acmecollege.entity.ClubMembership;
import acmecollege.entity.DurationAndStatus;
import acmecollege.entity.StudentClub;

@Path(STUDENT_CLUB_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StudentClubResource {

	private static final Logger LOG = LogManager.getLogger();

	@EJB
	protected ACMECollegeService service;

	@Inject
	protected SecurityContext sc;

	@GET
	public Response getStudentClubs() throws JsonProcessingException {
		LOG.debug("Retrieving all student clubs...");

		Response response = Response.noContent().build();
		List<StudentClub> studentClubs = service.getAllStudentClubs();

		if (!studentClubs.isEmpty()) {
//			SimpleModule module = new SimpleModule().addSerializer(new StudentClubListSerializer());
//			ObjectMapper objectMapper = new ObjectMapper().registerModule(module);
//
//			String json = objectMapper.writeValueAsString(studentClubs);

			response = Response.ok(studentClubs).build();
		}

		LOG.debug("Student clubs found = {}", studentClubs);

		return response;
	}

	@GET
	// SCR01 - Specify the roles allowed for this method
	@Path("/{studentClubId}")
	public Response getStudentClubById(@PathParam("studentClubId") int studentClubId) {
		LOG.debug("Retrieving student club with id = {}", studentClubId);

		Response response = Response.noContent().build();
		StudentClub studentClub = service.getStudentClubById(studentClubId);

		if (studentClub != null) {
			response = Response.ok(studentClub).build();
		}

		return response;
	}

	@DELETE
	// SCR02 - Specify the roles allowed for this method
	@RolesAllowed(ADMIN_ROLE)
	@Path("/{studentClubId}")
	public Response deleteStudentClub(@PathParam("studentClubId") int scId) {
		LOG.debug("Deleting student club with id = {}", scId);

		StudentClub sc = service.deleteStudentClub(scId);
		if (sc != null) {
			return Response.ok(sc).build();
		} else {
			HttpErrorResponse errResponseEntity = new HttpErrorResponse(Status.BAD_REQUEST);
			return Response.status(Status.BAD_REQUEST).entity(errResponseEntity).build();		
		}
	}

	// Please try to understand and test the below methods:
	@RolesAllowed({ ADMIN_ROLE })
	@POST
	public Response addStudentClub(StudentClub newStudentClub) {
		LOG.debug("Adding a new student club = {}", newStudentClub);

		if (service.isDuplicated(newStudentClub)) {
			HttpErrorResponse err = new HttpErrorResponse(Status.CONFLICT.getStatusCode(), "Entity already exists");

			return Response.status(Status.CONFLICT).entity(err).build();
		} else {
			StudentClub tempStudentClub = service.save(newStudentClub);

			return Response.ok(tempStudentClub).build();
		}
	}

	@RolesAllowed({ ADMIN_ROLE })
	@POST
	@Path("/{studentClubId}/clubmembership")
	public Response addClubMembershipToStudentClub(@PathParam("studentClubId") int scId, ClubMembership newClubMembership) {
		LOG.debug("Adding a new ClubMembership to student club with id = {}", scId);

		DurationAndStatus durationAndStatus = newClubMembership.getDurationAndStatus();
		if (durationAndStatus == null || durationAndStatus.getStartDate() == null || durationAndStatus.getEndDate() == null) {
			return Response.status(Status.BAD_REQUEST.getStatusCode(), "Duration date(s) cannot be null.").build();
		}

		StudentClub sc = service.getStudentClubById(scId);
		Response response = Response.status(Status.NOT_FOUND.getStatusCode(), "Student club not found.").build();

		if (sc != null) {
			newClubMembership.setStudentClub(sc);
			service.save(newClubMembership);

			Set<ClubMembership> clubMemberships = sc.getClubMemberships();
			if (clubMemberships == null) {
				sc.setClubMembership(new HashSet<>());
			}
			sc.getClubMemberships().add(newClubMembership);
			service.update(sc);

			response = Response.ok(sc).build();
		}

		return response;
	}

	@RolesAllowed({ USER_ROLE, ADMIN_ROLE })
	@PUT
	@Path("/{studentClubId}")
	public Response updateStudentClub(@PathParam("studentClubId") int scId, StudentClub updatingStudentClub) {
		LOG.debug("Updating a specific student club with id = {}", scId);

		StudentClub studentClub = service.getStudentClubById(scId);
		if (studentClub != null) {
			StudentClub updatedStudentClub = service.updateStudentClubById(scId, updatingStudentClub);
			return Response.ok(updatedStudentClub).build();
		}

		return Response.status(Status.NOT_FOUND.getStatusCode(), "Student club not found.").build();
	}

}
