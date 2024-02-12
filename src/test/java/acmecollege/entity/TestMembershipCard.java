/**
 * File:  TestMembershipCard.java Course materials (23S) CST 8277
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

import java.util.List;

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
public class TestMembershipCard extends JUnitBase {

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
	public void testGetAllMembershipCard() {
		Response response = webTarget.path(MEMBERSHIP_CARD_RESOURCE_NAME).request(MediaType.APPLICATION_JSON).get();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 401 (Unauthorized)", errorResponse.getStatusCode(), is(equalTo(401)));
	}

	@Test
	@Order(2)
	public void testAdminGetAllMembershipCard() {
		Response response = webTarget.path(MEMBERSHIP_CARD_RESOURCE_NAME).register(adminAuth).request(MediaType.APPLICATION_JSON)
				.get();

		List<MembershipCard> membershipCards = response.readEntity(new GenericType<List<MembershipCard>>() {
		});
		// Asserts the result
		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The membership card list should not be empty.", membershipCards, is(not(empty())));
	}

	@Test
	@Order(3)
	public void testUserGetAllMembershipCard() {
		Response response = webTarget.path(MEMBERSHIP_CARD_RESOURCE_NAME).register(userAuth).request(MediaType.APPLICATION_JSON)
				.get();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 403 (Forbidden)", errorResponse.getStatusCode(), is(equalTo(403)));
	}

	@Test
	@Order(4)
	public void testGetMembershipCardById() {
		Response response = webTarget.path(MEMBERSHIP_CARD_RESOURCE_NAME).request(MediaType.APPLICATION_JSON).get();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 401 (Unauthorized)", errorResponse.getStatusCode(), is(equalTo(401)));
	}

	@Test
	@Order(5)
	public void testAdminGetMembershipCardById() {
		Response response = webTarget.path(MEMBERSHIP_CARD_RESOURCE_NAME).register(adminAuth).request(MediaType.APPLICATION_JSON)
				.get();

		List<MembershipCard> membershipCards = response.readEntity(new GenericType<List<MembershipCard>>() {
		});
		// Asserts the result
		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The membership card list should not be empty.", membershipCards, is(not(empty())));
	}

	@Test
	@Order(6)
	public void testValidUserGetMembershipCardById() {
		final String ID = "1";

		Response response = webTarget.path(MEMBERSHIP_CARD_RESOURCE_NAME).path(ID).register(userAuth)
				.request(MediaType.APPLICATION_JSON).get();

		MembershipCard membershipCard = response.readEntity(new GenericType<MembershipCard>() {
		});
		// Asserts the result
		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The membership card should not be null.", membershipCard, is(not(nullValue())));
	}

	@Test
	@Order(6)
	public void testInvalidUserGetMembershipCardById() {
		Response response = webTarget.path(MEMBERSHIP_CARD_RESOURCE_NAME).register(invalidUserAuth)
				.request(MediaType.APPLICATION_JSON).get();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 403 (Forbidden)", errorResponse.getStatusCode(), is(equalTo(403)));
	}

	@Test
	@Order(7)
	public void testPostNewMembershipCard() throws JsonMappingException, JsonProcessingException {
		final int STUDENT_ID = 1;
		final byte SIGNED = 1;
		final int MEMBERSHIP_ID = 3;

		Response response = webTarget.path(MEMBERSHIP_CARD_RESOURCE_NAME).queryParam("student_id", STUDENT_ID)
				.queryParam("signed", SIGNED).queryParam("membership_id", MEMBERSHIP_ID).request(MediaType.APPLICATION_JSON)
				.post(null);
		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		// Asserts the result.
		assertThat("The response should be 401 (Unauthorized)", errorResponse.getStatusCode(), is(equalTo(401)));
	}

	@Test
	@Order(8)
	public void testAdminPostNewMembershipCard() throws JsonMappingException, JsonProcessingException {
		final int STUDENT_ID = 1;
		final byte SIGNED = 1;
		final int MEMBERSHIP_ID = 3;

		Response response = webTarget.path(MEMBERSHIP_CARD_RESOURCE_NAME).queryParam("student_id", STUDENT_ID)
				.queryParam("signed", SIGNED).queryParam("membership_id", MEMBERSHIP_ID).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).post(null);
		MembershipCard newMembershipCard = response.readEntity(new GenericType<MembershipCard>() {
		});

		// Asserts the result.
		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat(String.format("The new membership card's signed should be %d.", SIGNED), newMembershipCard.getSigned(),
				is(equalTo(SIGNED)));

		// Record the newest create club membership's id
		latestCreatedId = newMembershipCard.getId();
	}

	@Test
	@Order(9)
	public void testUserPostNewClubMembership() throws JsonMappingException, JsonProcessingException {
		final int STUDENT_ID = 1;
		final byte SIGNED = 1;
		final int MEMBERSHIP_ID = 3;

		Response response = webTarget.path(MEMBERSHIP_CARD_RESOURCE_NAME).queryParam("student_id", STUDENT_ID)
				.queryParam("signed", SIGNED).queryParam("membership_id", MEMBERSHIP_ID).register(userAuth)
				.request(MediaType.APPLICATION_JSON).post(null);
		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		// Asserts the result.
		assertThat("The response should be 403 (Forbidden)", errorResponse.getStatusCode(), is(equalTo(403)));
	}

	@Test
	@Order(10)
	public void testDeleteMembershipCardById() throws JsonMappingException, JsonProcessingException {
		final int ID = latestCreatedId;

		Response response = webTarget.path(MEMBERSHIP_CARD_RESOURCE_NAME).path(String.valueOf(ID))
				.request(MediaType.APPLICATION_JSON).delete();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 401 (Unauthorized)", errorResponse.getStatusCode(), is(equalTo(401)));
	}

	@Test
	@Order(11)
	public void testAdminDeleteMembershipCardById() throws JsonMappingException, JsonProcessingException {
		final int ID = latestCreatedId;
		Response response = webTarget.path(MEMBERSHIP_CARD_RESOURCE_NAME).path(String.valueOf(ID)).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).delete();

		MembershipCard deletedMembershipCard = response.readEntity(new GenericType<MembershipCard>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("All club memberships should have been deleted.", deletedMembershipCard, is(not(nullValue())));
	}

	@Test
	@Order(12)
	public void testAdmiUserDeleteMembershipCardById() throws JsonMappingException, JsonProcessingException {
		final int ID = latestCreatedId;
		Response response = webTarget.path(MEMBERSHIP_CARD_RESOURCE_NAME).path(String.valueOf(ID)).register(userAuth)
				.request(MediaType.APPLICATION_JSON).delete();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 403 (Forbidden)", errorResponse.getStatusCode(), is(equalTo(403)));
	}

	@Test
	@Order(13)
	public void testDeleteAllMembershipCards() throws JsonMappingException, JsonProcessingException {
		Response response = webTarget.path(MEMBERSHIP_CARD_RESOURCE_NAME).request(MediaType.APPLICATION_JSON).delete();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 401 (Unauthorized)", errorResponse.getStatusCode(), is(equalTo(401)));
	}

//	@Test
//	@Order(14)
//	public void testAdminDeleteAllMembershipCards() throws JsonMappingException, JsonProcessingException {
//		Response response = webTarget.path(MEMBERSHIP_CARD_RESOURCE_NAME).register(adminAuth).request(MediaType.APPLICATION_JSON)
//				.delete();
//
//		List<MembershipCard> deletedMembershipCards = response.readEntity(new GenericType<List<MembershipCard>>() {
//		});
//
//		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
//		assertThat("All club memberships should have been deleted.", deletedMembershipCards, is(not(empty())));
//	}

	@Test
	@Order(15)
	public void testUserDeleteAllMembershipCards() throws JsonMappingException, JsonProcessingException {
		Response response = webTarget.path(MEMBERSHIP_CARD_RESOURCE_NAME).register(userAuth).request(MediaType.APPLICATION_JSON)
				.delete();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 403 (Forbidden)", errorResponse.getStatusCode(), is(equalTo(403)));
	}

}
