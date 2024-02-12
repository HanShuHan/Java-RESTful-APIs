/**
 * File:  TestProfessor.java Course materials (23S) CST 8277
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
public class TestProfessor extends JUnitBase {

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
	public void testAdminGetAllProfessor() {
		Response response = webTarget.path(PROFESSOR_SUBRESOURCE_NAME).register(adminAuth).request(MediaType.APPLICATION_JSON)
				.get();

		List<Professor> professors = response.readEntity(new GenericType<List<Professor>>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The professor list should not be empty.", professors, is(not(empty())));
	}

	@Test
	@Order(2)
	public void testUserGetAllProfessor() {
		Response response = webTarget.path(PROFESSOR_SUBRESOURCE_NAME).register(userAuth).request(MediaType.APPLICATION_JSON)
				.get();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 403 (forbidden)", errorResponse.getStatusCode(), is(equalTo(403)));
	}

	@Test
	@Order(3)
	public void testAdminGetProfessorById() {
		String pId = "1";
		Response response = webTarget.path(PROFESSOR_SUBRESOURCE_NAME).path(pId).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).get();

		Professor professor = response.readEntity(new GenericType<Professor>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The professor should not be null.", professor, is(not(equalTo(nullValue()))));
		assertThat("The professor's first name should not be null.", professor.getFirstName(), is(not(equalTo(nullValue()))));
		assertThat("The professor's last name should not be null.", professor.getLastName(), is(not(equalTo(nullValue()))));
		assertThat("The professor's department should not be null.", professor.getDepartment(), is(not(equalTo(nullValue()))));
	}

	@Test
	@Order(4)
	public void testUserGetProfessorById() {
		String pId = "1";
		Response response = webTarget.path(PROFESSOR_SUBRESOURCE_NAME).path(pId).register(userAuth)
				.request(MediaType.APPLICATION_JSON).get();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 403 (forbidden)", errorResponse.getStatusCode(), is(equalTo(403)));
	}

	@Test
	@Order(3)
	public void testAdminPostNewProfessor() throws JsonMappingException, JsonProcessingException {
		final String firstName = "Louis" + random.nextInt(1000);
		final String lastName = "Huang" + random.nextInt(1000);
		final String department = "Economics" + random.nextInt(1000);
		Professor newProfessor = new Professor(firstName, lastName, department);

		Response response = webTarget.path(PROFESSOR_SUBRESOURCE_NAME).register(adminAuth).request(MediaType.APPLICATION_JSON)
				.post(Entity.json(newProfessor));

		Professor professor = response.readEntity(new GenericType<Professor>() {
		});
		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The professor should not be null.", professor, is(not(equalTo(nullValue()))));
		assertThat(String.format("The professor's first name should be %s.", firstName), professor.getFirstName(),
				is(equalTo(firstName)));
		assertThat(String.format("The professor's last name should be %s.", lastName), professor.getLastName(),
				is(equalTo(lastName)));
		assertThat(String.format("The professor's department should be %s.", department), professor.getDepartment(),
				is(equalTo(department)));

		latestCreatedId = professor.getId();
	}

	@Test
	@Order(4)
	public void testUserPostNewProfessor() throws JsonMappingException, JsonProcessingException {
		final String firstName = "Louis" + random.nextInt(1000);
		final String lastName = "Huang" + random.nextInt(1000);
		final String department = "Economics" + random.nextInt(1000);
		Professor newProfessor = new Professor(firstName, lastName, department);

		Response response = webTarget.path(PROFESSOR_SUBRESOURCE_NAME).register(userAuth).request(MediaType.APPLICATION_JSON)
				.post(Entity.json(newProfessor));

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});
		assertThat("The response should be 403 (forbidden)", errorResponse.getStatusCode(), is(equalTo(403)));
	}

	@Test
	@Order(5)
	public void testAdminDeleteExistingProfessor() throws JsonMappingException, JsonProcessingException {
		String pId = String.valueOf(latestCreatedId);

		Response response = webTarget.path(PROFESSOR_SUBRESOURCE_NAME).path(pId).register(adminAuth).request().delete();

		int statusCode = response.getStatus();
		assertThat("The response should be 200 (ok)", statusCode, is(equalTo(200)));
	}

	@Test
	@Order(6)
	public void testAdminDeleteNonExistingProfessor() throws JsonMappingException, JsonProcessingException {
		String pId = "111";

		Response response = webTarget.path(PROFESSOR_SUBRESOURCE_NAME).path(pId).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).delete();

		int statusCode = response.getStatus();
		assertThat("The response should be 400 (bad request)", statusCode, is(equalTo(400)));
	}

	@Test
	@Order(7)
	public void testUserDeleteStudent() throws JsonMappingException, JsonProcessingException {
		String id = "1";

		Response response = webTarget.path(STUDENT_RESOURCE_NAME).path(id).register(userAuth).request(MediaType.APPLICATION_JSON)
				.delete();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		int statusCode = errorResponse.getStatusCode();
		assertThat("The response should be 403 (forbidden)", statusCode, is(equalTo(403)));
	}

}
