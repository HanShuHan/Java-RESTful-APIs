/**
 * File:  ClubMembershipResource.java Course materials (23S) CST 8277
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
import static acmecollege.utility.MyConstants.CLUB_MEMBERSHIP_RESOURCE_NAME;
import static acmecollege.utility.MyConstants.RESOURCE_PATH_ID_PATH;

import java.util.List;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

import acmecollege.ejb.ACMECollegeService;
import acmecollege.entity.ClubMembership;
import acmecollege.entity.StudentClub;
import acmecollege.utility.ObjectIntegrityChecker;

@Path(CLUB_MEMBERSHIP_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ClubMembershipResource {

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
	public Response getAllClubMembership(@QueryParam("club_id") int clubId) throws JsonProcessingException {
		LOG.debug("Retrieving club membership...");

		List<ClubMembership> clubMemberships = null;
		if (clubId < 0) {
			HttpErrorResponse errorResponseEntity = new HttpErrorResponse(Status.BAD_REQUEST);
			return Response.status(Status.BAD_REQUEST).entity(errorResponseEntity).build();
		} else if (clubId == 0) {
			LOG.debug("Retrieving all club membership...");
			clubMemberships = service.findAll(ClubMembership.class, ClubMembership.FIND_ALL);
		} else { // clubId > 0
			LOG.debug("Retrieving all club membership with club id = {}", clubId);
			clubMemberships = service.findAllBy(ClubMembership.class, ClubMembership.FIND_ALL_BY_CLUB_ID, clubId);
		}
		
		// Check the returning result list
		if (clubMemberships.isEmpty()) {
			return Response.noContent().build();
		} else {
			return Response.ok(clubMemberships).build();
		}
	}

	@GET
	@RolesAllowed({ ADMIN_ROLE })
	@Path(RESOURCE_PATH_ID_PATH)
	public Response getClubMembershipById(@PathParam("id") int id) throws JsonProcessingException {
		LOG.debug("Retrieving club membership with id = {}", id);

		ClubMembership clubMembership = service.findSingleEntityBy(ClubMembership.class, ClubMembership.FIND_BY_ID_FETCH_ALL, id);
		// Check the returning result list
		if (clubMembership != null) {
			return Response.noContent().build();
		} else {
			return Response.ok(clubMembership).build();
		}
	}

	@POST
	@RolesAllowed({ ADMIN_ROLE })
	public Response addClubMembership(@QueryParam("club_id") int clubId, ClubMembership clubMembership) {
		LOG.debug("Adding a new club membership with club id = {}", clubId);

		HttpErrorResponse errorResponseEntity = new HttpErrorResponse(Status.BAD_REQUEST);
		Response response = Response.status(Status.BAD_REQUEST).entity(errorResponseEntity).build();
		if (clubId > 0 && objectChecker.check(clubMembership)) {
			StudentClub club = service.findSingleEntityBy(StudentClub.class, StudentClub.FIND_BY_ID, clubId);
			// Add a new club membership only if the given club id exists.
			if (club != null) {
				try {
					ClubMembership newClubMembership = service.addClubMembershipBy(clubId, clubMembership.getDurationAndStatus());
					response = Response.ok(newClubMembership).build();
				} catch (Exception e) {
					LOG.debug("Error happened when adding the new club membership with club id = {} and duration and status = {}",
							clubId, clubMembership.getDurationAndStatus());
					response = Response.serverError().build();
				}
			}
		}
		return response;
	}

	@DELETE
	@RolesAllowed(ADMIN_ROLE)
	public Response deleteAllClubMemberships() {
		LOG.debug("Deleting all club memberships");

		Response response = Response.noContent().build();
		
		List<ClubMembership> clubMemberships = service.findAll(ClubMembership.class, ClubMembership.FIND_ALL);
		if (!clubMemberships.isEmpty()) {
			try {
				clubMemberships.stream().forEach(clubMembership -> {
					service.deleteClubMembershipById(clubMembership.getId());
				});
			} catch (Exception e) {
				response = Response.serverError().build();
			}
			response = Response.ok(clubMemberships).build();
		}
		return response;
	}

	@DELETE
	@RolesAllowed(ADMIN_ROLE)
	@Path(RESOURCE_PATH_ID_PATH)
	public Response deleteClubMembership(@PathParam("id") int id) {
		LOG.debug("Deleting a club membership with id = {}", id);

		HttpErrorResponse errorResponseEntity = new HttpErrorResponse(Status.BAD_REQUEST);
		Response response = Response.status(Status.BAD_REQUEST).entity(errorResponseEntity).build();

		if (id > 0) {
			ClubMembership clubMembership = service.findSingleEntityBy(ClubMembership.class, ClubMembership.FIND_BY_ID_FETCH_ALL, id);
			if (clubMembership != null) {
				try {
					service.deleteClubMembershipById(id);
				} catch (Exception e) {
					response = Response.serverError().build();
				}
				response = Response.ok(clubMembership).build();
			} // the club membership is not found.
		} // else, id <= 0 (Bad Request).
		return response;
	}

}
