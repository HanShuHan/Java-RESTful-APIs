/**
 * File:  MembershipCardResource.java Course materials (23S) CST 8277
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
import static acmecollege.utility.MyConstants.MEMBERSHIP_CARD_RESOURCE_NAME;
import static acmecollege.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static acmecollege.utility.MyConstants.USER_ROLE;

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
import org.glassfish.soteria.WrappingCallerPrincipal;

import com.fasterxml.jackson.core.JsonProcessingException;

import acmecollege.ejb.ACMECollegeService;
import acmecollege.entity.ClubMembership;
import acmecollege.entity.MembershipCard;
import acmecollege.entity.SecurityUser;
import acmecollege.entity.Student;
import acmecollege.utility.ObjectIntegrityChecker;

@Path(MEMBERSHIP_CARD_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MembershipCardResource {

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
	public Response getAllMembershipCard() throws JsonProcessingException {
		LOG.debug("Retrieving all membership cards...");

		List<MembershipCard> membershipCards = service.findAll(MembershipCard.class, MembershipCard.FIND_ALL_FETCH_ALL);
		// Check the returning result list
		if (!membershipCards.isEmpty()) {
			return Response.ok(membershipCards).build();
		} else {
			return Response.noContent().build();
		}
	}

	@GET
	@RolesAllowed({ ADMIN_ROLE, USER_ROLE })
	@Path(RESOURCE_PATH_ID_PATH)
	public Response getMembershipCardById(@PathParam("id") int id) throws JsonProcessingException {
		LOG.debug("Retrieving membership card with id = {}", id);

		HttpErrorResponse errorResponseEntity = new HttpErrorResponse(Status.BAD_REQUEST);
		Response response = Response.status(Status.BAD_REQUEST).entity(errorResponseEntity).build();
		if (id > 0) {
			MembershipCard card = service.findSingleEntityBy(MembershipCard.class, MembershipCard.FIND_BY_ID_FETCH_ALL, id);
			if (sc.isCallerInRole(ADMIN_ROLE)) {
				// Check the returning result list
				if (card != null) {
					response = Response.ok(card).build();
				} else {
					response = Response.noContent().build();
				}
			} else { // USER_ROLE
				errorResponseEntity = new HttpErrorResponse(Status.FORBIDDEN);
				response = Response.status(Status.FORBIDDEN).entity(errorResponseEntity).build();

				WrappingCallerPrincipal wrappingCallerPrincipal = (WrappingCallerPrincipal) sc.getCallerPrincipal();
				SecurityUser securityUser = (SecurityUser) (wrappingCallerPrincipal.getWrapped());
				Student student = securityUser.getStudent();
				if (student != null) {
					int studentId = student.getId();
					if (studentId == id) {
						if (card != null) {
							response = Response.ok(card).build();
						} else {
							response = Response.noContent().build();
						}
					} // else, forbidden
				} // else, forbidden
			}
		} // else id <= 0, bad request
		return response;
	}

	@POST
	@RolesAllowed({ ADMIN_ROLE })
	public Response addMembershipCard(@QueryParam("student_id") int studentId, @QueryParam("signed") byte signed,
			@QueryParam("membership_id") int membershipId) {
		LOG.debug("Adding a new membership card with student id = {}, signed = {}, membership id = {}", studentId, signed,
				membershipId);

		HttpErrorResponse errorResponseEntity = new HttpErrorResponse(Status.BAD_REQUEST);
		Response response = Response.status(Status.BAD_REQUEST).entity(errorResponseEntity).build();
		if (studentId > 0) {
			Student student = service.findSingleEntityBy(Student.class, Student.FIND_BY_ID, studentId);
			// Check if the club membership when its given id is greater than zero
			ClubMembership clubMembership = null;
			if (membershipId > 0) {
				clubMembership = service.findSingleEntityBy(ClubMembership.class, ClubMembership.FIND_BY_ID, membershipId);
			}
			// Check only if the student exists, then we add a new membership card.
			try {
				MembershipCard newMembershipCard = null;
				if (student != null && clubMembership != null) {
					newMembershipCard = service.addMembershipCardBy(studentId, membershipId, signed);
				} else if (student != null) {
					newMembershipCard = service.addMembershipCardBy(studentId, signed);
				} // else, student does not exist, bad request
				response = Response.ok(newMembershipCard).build();
			} catch (Exception e) {
				response = Response.serverError().build();
			}
		} // else, student's id <= 0, bad request
		return response;
	}

	@DELETE
	@RolesAllowed(ADMIN_ROLE)
	public Response deleteAllClubMemberships() {
		LOG.debug("Deleting all membership cards");

		Response response = Response.noContent().build();

		long count = service.findSingleEntityBy(Long.class, MembershipCard.COUNT_ALL);
		if (count > 0) {
			List<MembershipCard> deletedMembershipCards = null;
			try {
				deletedMembershipCards = service.deleteAllEntityFrom(MembershipCard.class);
			} catch (Exception e) {
				response = Response.serverError().build();
			}
			response = Response.ok(deletedMembershipCards).build();
		}
		return response;
	}

	@DELETE
	@RolesAllowed(ADMIN_ROLE)
	@Path(RESOURCE_PATH_ID_PATH)
	public Response deleteClubMembership(@PathParam("id") int id) {
		LOG.debug("Deleting a membership card with id = {}", id);

		HttpErrorResponse errorResponseEntity = new HttpErrorResponse(Status.BAD_REQUEST);
		Response response = Response.status(Status.BAD_REQUEST).entity(errorResponseEntity).build();

		if (id > 0) {
			MembershipCard membershipCard = service.findSingleEntityBy(MembershipCard.class, MembershipCard.FIND_BY_ID,
					id);
			if (membershipCard != null) {
				try {
					service.deleteMembershipCardById(id);
				} catch (Exception e) {
					response = Response.serverError().build();
				}
				response = Response.ok(membershipCard).build();
			} // the club membership is not found.
		} // else, id <= 0 (Bad Request).
		return response;
	}

}
