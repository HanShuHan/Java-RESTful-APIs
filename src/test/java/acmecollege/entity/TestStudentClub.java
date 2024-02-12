/**
 * File:  TestStudentClub.java Course materials (23S) CST 8277
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

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

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
public class TestStudentClub extends JUnitBase {

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
	public void testAdminGetStudentClubById() {
		String scId = "2";
		Response response = webTarget.path(STUDENT_CLUB_RESOURCE_NAME).path(scId).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).get();

		StudentClub studentClub = response.readEntity(new GenericType<StudentClub>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The student club should not be null.", studentClub, is(not(equalTo(nullValue()))));
		assertThat("The student club's name should not be null.", studentClub.getName(), is(not(equalTo(nullValue()))));
	}

	@Test
	@Order(2)
	public void testUserGetStudentClubById() {
		String scId = "2";
		Response response = webTarget.path(STUDENT_CLUB_RESOURCE_NAME).path(scId).register(userAuth)
				.request(MediaType.APPLICATION_JSON).get();

		StudentClub studentClub = response.readEntity(new GenericType<StudentClub>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The student club should not be null.", studentClub, is(not(equalTo(nullValue()))));
		assertThat("The student club's name should not be null.", studentClub.getName(), is(not(equalTo(nullValue()))));
	}

	@Test
	@Order(3)
	public void testAdminPostNewStudentClub() throws JsonMappingException, JsonProcessingException {
		final String newClubName = "C Programming " + random.nextInt(1000);
		StudentClub newStudentClub = new AcademicStudentClub(newClubName);

		Response response = webTarget.path(STUDENT_CLUB_RESOURCE_NAME).register(adminAuth).request(MediaType.APPLICATION_JSON)
				.post(Entity.json(newStudentClub));

		StudentClub studentClub = response.readEntity(new GenericType<StudentClub>() {
		});
		latestCreatedId = studentClub.getId();

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The student club should not be null.", studentClub, is(not(equalTo(nullValue()))));
		assertThat(String.format("The student club's name should be %s", newClubName), studentClub.getName(),
				is((equalTo(newClubName))));
	}

	@Test
	@Order(4)
	public void testUserPostNewStudentClub() throws JsonMappingException, JsonProcessingException {
		final String newClubName = "C Programming " + random.nextInt(1000);
		StudentClub newStudentClub = new AcademicStudentClub(newClubName);

		Response response = webTarget.path(STUDENT_CLUB_RESOURCE_NAME).register(userAuth).request(MediaType.APPLICATION_JSON)
				.post(Entity.json(newStudentClub));

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		int statusCode = errorResponse.getStatusCode();
		assertThat("The response should be 403 (forbidden)", statusCode, is(equalTo(403)));
	}

	@Test
	@Order(5)
	public void testAdminPutStudentClub() throws JsonMappingException, JsonProcessingException {
		String scId = "2";
		final String newClubName = "Basketball Club " + random.nextInt(1000);
		StudentClub updatingStudentClub = new AcademicStudentClub(newClubName);

		Response response = webTarget.path(STUDENT_CLUB_RESOURCE_NAME).path(scId).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).put(Entity.json(updatingStudentClub));

		StudentClub studentClub = response.readEntity(new GenericType<StudentClub>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The student club should not be null.", studentClub, is(not(equalTo(nullValue()))));
		assertThat(String.format("The student club's name should be %s", newClubName), studentClub.getName(),
				is((equalTo(newClubName))));
	}

	@Test
	@Order(6)
	public void testUserPutStudentClub() throws JsonMappingException, JsonProcessingException {
		String scId = "2";
		final String newClubName = "Socker Club " + random.nextInt(1000);
		StudentClub updatingStudentClub = new AcademicStudentClub(newClubName);

		Response response = webTarget.path(STUDENT_CLUB_RESOURCE_NAME).path(scId).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).put(Entity.json(updatingStudentClub));

		StudentClub studentClub = response.readEntity(new GenericType<StudentClub>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The student club should not be null.", studentClub, is(not(equalTo(nullValue()))));
		assertThat(String.format("The student club's name should be %s", newClubName), studentClub.getName(),
				is((equalTo(newClubName))));
	}
	
	@Test
	@Order(7)
	public void testAdminDeleteExistingStudentClub() throws JsonMappingException, JsonProcessingException {
		String scId = String.valueOf(latestCreatedId);

		Response response = webTarget.path(STUDENT_CLUB_RESOURCE_NAME).path(scId).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).delete();
		
		StudentClub deletedSC = response.readEntity(new GenericType<StudentClub>() {
		});
		
		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The deleted student club should not be null.", deletedSC, is(not(equalTo(nullValue()))));
	}
	
	@Test
	@Order(8)
	public void testAdminDeleteNonExistingStudentClub() throws JsonMappingException, JsonProcessingException {
		String scId = "100";
		
		Response response = webTarget.path(STUDENT_CLUB_RESOURCE_NAME).path(scId).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).delete();
		
		StatusType statusInfo = response.getStatusInfo();
		assertThat("The response should be 400 (Bad request)", statusInfo.getStatusCode(), is(equalTo(400)));
	}

	@Test
	@Order(9)
	public void testUserDeleteStudentClub() throws JsonMappingException, JsonProcessingException {
		String scId = "2";

		Response response = webTarget.path(STUDENT_CLUB_RESOURCE_NAME).path(scId).register(userAuth)
				.request(MediaType.APPLICATION_JSON).delete();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		int statusCode = errorResponse.getStatusCode();
		assertThat("The response should be 403 (forbidden)", statusCode, is(equalTo(403)));
	}

}
