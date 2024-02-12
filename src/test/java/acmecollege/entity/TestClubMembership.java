/**
 * File:  TestClubMembership.java Course materials (23S) CST 8277
 * 
 * @author Shu-Han Han
 * 
 * Updated by:  Group 11
 *   041060762, Shu-Han Han
 *   041060761, Wan-Hsuan Lee
 *   041061567, Syedmoinuddi Hassan
 *   041066323, Gurarman Singh
 *
 */

package acmecollege.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.time.LocalDateTime;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import acmecollege.JUnitBase;
import acmecollege.rest.resource.HttpErrorResponse;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestClubMembership extends JUnitBase {

	@BeforeAll
	public static void setUpAll() {
		JUnitBase.setUpAll();
	}

	@BeforeEach
	public void setUpEach() {
		super.setUpEach();
	}

	@Test
	@Order(1)
	public void testGetClubMembership() {
		Response response = webTarget.path(CLUB_MEMBERSHIP_RESOURCE_NAME).request(MediaType.APPLICATION_JSON).get();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 401 (Unauthorized)", errorResponse.getStatusCode(), is(equalTo(401)));
	}

	@Test
	@Order(2)
	public void testAdminGetAllClubMembership() {
		Response response = webTarget.path(CLUB_MEMBERSHIP_RESOURCE_NAME).register(adminAuth).request(MediaType.APPLICATION_JSON)
				.get();

		List<ClubMembership> clubMemberships = response.readEntity(new GenericType<List<ClubMembership>>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The course list should not be empty.", clubMemberships, is(not(empty())));
	}

	@Test
	@Order(2)
	public void testAdminGetAllClubMembershipByClubId() {
		final int CLUB_ID = 2;

		Response response = webTarget.path(CLUB_MEMBERSHIP_RESOURCE_NAME).queryParam("club_id", CLUB_ID).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).get();

		List<ClubMembership> clubMemberships = response.readEntity(new GenericType<List<ClubMembership>>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The club membership list should not be empty.", clubMemberships, is(not(empty())));
	}

	@Test
	@Order(2)
	public void testAdminGetAllClubMembershipByNonExistingClubId() {
		final int CLUB_ID = -1;

		Response response = webTarget.path(CLUB_MEMBERSHIP_RESOURCE_NAME).queryParam("club_id", CLUB_ID).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).get();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 400 (Bad Request)", errorResponse.getStatusCode(), is(equalTo(400)));
	}

	@Test
	@Order(3)
	public void testUserGetClubMembership() {
		Response response = webTarget.path(CLUB_MEMBERSHIP_RESOURCE_NAME).register(userAuth).request(MediaType.APPLICATION_JSON)
				.get();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 403 (Forbidden)", errorResponse.getStatusCode(), is(equalTo(403)));
	}

	@Test
	@Order(4)
	public void testPostNewCourseRegistration() throws JsonMappingException, JsonProcessingException {
		LocalDateTime currentTime = LocalDateTime.now();
		LocalDateTime currentTimePlusOneMonth = currentTime.plusMonths(1);
		DurationAndStatus durationAndStatus = new DurationAndStatus(currentTime, currentTimePlusOneMonth, "+");
		final int CLUB_ID = 1;

		Response response = webTarget.path(CLUB_MEMBERSHIP_RESOURCE_NAME).queryParam("club_id", CLUB_ID)
				.request(MediaType.APPLICATION_JSON).post(Entity.json(durationAndStatus));
		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});
		// Asserts the result.
		assertThat("The response should be 401 (Unauthorized)", errorResponse.getStatusCode(), is(equalTo(401)));
	}

	@Test
	@Order(5)
	public void testAdminPostNewClubMembership() throws JsonMappingException, JsonProcessingException {
		LocalDateTime currentTime = LocalDateTime.now();
		LocalDateTime currentTimePlusOneMonth = currentTime.plusMonths(1);
		DurationAndStatus durationAndStatus = new DurationAndStatus(currentTime, currentTimePlusOneMonth, "+");
		ClubMembership clubMembership = new ClubMembership();
		clubMembership.setDurationAndStatus(durationAndStatus);
		final int CLUB_ID = 1;

		Response response = webTarget.path(CLUB_MEMBERSHIP_RESOURCE_NAME).queryParam("club_id", CLUB_ID).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).post(Entity.json(clubMembership));
		ClubMembership newClubMembership = response.readEntity(new GenericType<ClubMembership>() {
		});

		// Asserts the result.
		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat(String.format("The new club membership's duration and status should be %s.", durationAndStatus),
				newClubMembership.getDurationAndStatus(), is(equalTo(durationAndStatus)));

		// Record the newest create club membership's id
		latestCreatedId = newClubMembership.getId();
	}

	@Test
	@Order(6)
	public void testUserPostNewClubMembership() throws JsonMappingException, JsonProcessingException {
		LocalDateTime currentTime = LocalDateTime.now();
		LocalDateTime currentTimePlusOneMonth = currentTime.plusMonths(1);
		DurationAndStatus durationAndStatus = new DurationAndStatus(currentTime, currentTimePlusOneMonth, "+");
		final int CLUB_ID = 1;

		Response response = webTarget.path(CLUB_MEMBERSHIP_RESOURCE_NAME).queryParam("club_id", CLUB_ID).register(userAuth)
				.request(MediaType.APPLICATION_JSON).post(Entity.json(durationAndStatus));
		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		// Asserts the result.
		assertThat("The response should be 403 (Forbidden)", errorResponse.getStatusCode(), is(equalTo(403)));
	}

//	@Test
//	@Order(7)
//	public void testDeleteAllClubMemberships() throws JsonMappingException, JsonProcessingException {
//		Response response = webTarget.path(CLUB_MEMBERSHIP_RESOURCE_NAME).request(MediaType.APPLICATION_JSON).delete();
//
//		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
//		});
//
//		assertThat("The response should be 401 (Unauthorized)", errorResponse.getStatusCode(), is(equalTo(401)));
//	}
//
//	@Test
//	@Order(8)
//	public void testAdminUserDeleteCourseRegistrationByStudentIdAndCourseId()
//			throws JsonMappingException, JsonProcessingException {
//		Response response = webTarget.path(CLUB_MEMBERSHIP_RESOURCE_NAME).register(adminAuth).request(MediaType.APPLICATION_JSON)
//				.delete();
//
//		List<ClubMembership> deletedClubMemberships = response.readEntity(new GenericType<List<ClubMembership>>() {
//		});
//
//		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
//		assertThat("All club memberships should have been deleted.", deletedClubMemberships, is(not(empty())));
//	}
//
//	@Test
//	@Order(9)
//	public void testUserDeleteAllClubMemberships() throws JsonMappingException, JsonProcessingException {
//		Response response = webTarget.path(CLUB_MEMBERSHIP_RESOURCE_NAME).register(userAuth).request(MediaType.APPLICATION_JSON)
//				.delete();
//
//		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
//		});
//
//		assertThat("The response should be 403 (Forbidden)", errorResponse.getStatusCode(), is(equalTo(403)));
//	}

	@Test
	@Order(10)
	public void testDeleteAllClubMembershipsById() throws JsonMappingException, JsonProcessingException {
		final String ID = String.valueOf(latestCreatedId);

		Response response = webTarget.path(CLUB_MEMBERSHIP_RESOURCE_NAME).path(ID).request(MediaType.APPLICATION_JSON).delete();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 401 (Unauthorized)", errorResponse.getStatusCode(), is(equalTo(401)));
	}

	@Test
	@Order(11)
	public void testAdminUserDeleteCourseRegistrationByStudentIdAndCourseId()
			throws JsonMappingException, JsonProcessingException {

		final String ID = String.valueOf(latestCreatedId);

		Response response = webTarget.path(CLUB_MEMBERSHIP_RESOURCE_NAME).path(ID).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).delete();

		ClubMembership deletedClubMembership = response.readEntity(new GenericType<ClubMembership>() {
		});
		// Asserts the result.
		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat(String.format("The club membership with student id = %s should have been deleted.", ID), deletedClubMembership,
				is(not(nullValue())));
		assertThat(String.format("The deleted club membership's id should be %s.", ID), String.valueOf(deletedClubMembership.getId()),
				is(equalTo(ID)));
	}

	@Test
	@Order(12)
	public void testUserDeleteAllClubMemberships() throws JsonMappingException, JsonProcessingException {
		Response response = webTarget.path(CLUB_MEMBERSHIP_RESOURCE_NAME).register(userAuth).request(MediaType.APPLICATION_JSON)
				.delete();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 403 (Forbidden)", errorResponse.getStatusCode(), is(equalTo(403)));
	}

}
