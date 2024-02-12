/**
 * File:  TestStudent.java Course materials (23S) CST 8277
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
public class TestStudent extends JUnitBase {

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
	public void testAdminGetAllStudent() {
		Response response = webTarget.path(STUDENT_RESOURCE_NAME).register(adminAuth).request(MediaType.APPLICATION_JSON).get();

		List<Student> students = response.readEntity(new GenericType<List<Student>>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The student list should not be empty.", students, is(not(empty())));
}

	@Test
	@Order(2)
	public void testUserGetAllStudent() {
		Response response = webTarget.path(STUDENT_RESOURCE_NAME).register(userAuth).request(MediaType.APPLICATION_JSON).get();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		int statusCode = errorResponse.getStatusCode();
		assertThat("The response should be 403 (forbidden)", statusCode, is(equalTo(403)));
	}

	@Test
	@Order(3)
	public void testAdminGetStudentById() {
		String sId = "1";
		Response response = webTarget.path(STUDENT_RESOURCE_NAME).path(sId).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).get();

		Student student = response.readEntity(new GenericType<Student>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The student should not be null.", student, is(not(equalTo(nullValue()))));
		assertThat("The student's first name should not be null.", student.getFirstName(), is(not(equalTo(nullValue()))));
		assertThat("The student's last name should not be null.", student.getLastName(), is(not(equalTo(nullValue()))));
	}

	@Test
	@Order(4)
	public void testUserGetAuthorizedStudentById() {
		String sId = "1";
		Response response = webTarget.path(STUDENT_RESOURCE_NAME).path(sId).register(userAuth).request(MediaType.APPLICATION_JSON)
				.get();

		Student student = response.readEntity(new GenericType<Student>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The student should not be null.", student, is(not(equalTo(nullValue()))));
		assertThat("The student's first name should not be null.", student.getFirstName(), is(not(equalTo(nullValue()))));
		assertThat("The student's last name should not be null.", student.getLastName(), is(not(equalTo(nullValue()))));
	}

	@Test
	@Order(5)
	public void testUserGetUnauthorizedStudentById() {
		String sId = "111";
		Response response = webTarget.path(STUDENT_RESOURCE_NAME).path(sId).register(userAuth).request(MediaType.APPLICATION_JSON)
				.get();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 403 (forbidden)", errorResponse.getStatusCode(), is(equalTo(403)));
	}

	@Test
	@Order(6)
	public void testAdminPostNewStudent() throws JsonMappingException, JsonProcessingException {
		String firstName = "Mickey" + random.nextInt(1000);
		String lastName = "Mouse" + random.nextInt(1000);
		Student student = new Student(firstName, lastName);

		Response response = webTarget.path(STUDENT_RESOURCE_NAME).register(adminAuth).request(MediaType.APPLICATION_JSON)
				.post(Entity.json(student));

		Student newStudent = response.readEntity(new GenericType<Student>() {
		});
		latestCreatedId = newStudent.getId();

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The student should not be null.", newStudent, is(not(equalTo(nullValue()))));
		assertThat(String.format("The student's first name should be %s", firstName), student.getFirstName(),
				is((equalTo(firstName))));
		assertThat(String.format("The student's last name should be %s", lastName), student.getLastName(),
				is((equalTo(lastName))));

		Long numOfSecurityUser = newStudent.getSecurityUsers().stream().count();
		assertThat("There should be one security user for the new user.", numOfSecurityUser, is(equalTo(1L)));

		int numOfRoles = newStudent.getSecurityUsers().stream().findFirst().get().getRoles().size();
		assertThat("There should be one role for the security user.", numOfRoles, is(equalTo(1)));

		String roleName = newStudent.getSecurityUsers().stream().findFirst().get().getRoles().stream().findFirst().get()
				.getRoleName();
		assertThat(String.format("The role name should be %s.", USER_ROLE), roleName, is(equalTo(USER_ROLE)));
	}

	@Test
	@Order(7)
	public void testUserPostNewStudent() throws JsonMappingException, JsonProcessingException {
		String firstName = "Mickey" + random.nextInt(1000);
		String lastName = "Mouse" + random.nextInt(1000);
		Student student = new Student(firstName, lastName);

		Response response = webTarget.path(STUDENT_RESOURCE_NAME).register(userAuth).request(MediaType.APPLICATION_JSON)
				.post(Entity.json(student));

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 403 (forbidden)", errorResponse.getStatusCode(), is(equalTo(403)));
	}

	@Test
	@Order(8)
	public void testAdminPutProfessorByCourseId() throws JsonMappingException, JsonProcessingException {
		String sId = "1";
		String cId = "2";
		String firstName = "Avigail" + random.nextInt(1000);
		String lastName = "Lee" + random.nextInt(1000);
		String department = "Tourism" + random.nextInt(1000);
		Professor professor = new Professor(firstName, lastName, department);

		Response response = webTarget.path(STUDENT_RESOURCE_NAME).path(sId).path(COURSE_RESOURCE_NAME).path(cId)
				.path(PROFESSOR_SUBRESOURCE_NAME).register(adminAuth).request(MediaType.APPLICATION_JSON)
				.put(Entity.json(professor));

		Professor updatedProfessor = response.readEntity(new GenericType<Professor>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The professor should not be null.", updatedProfessor, is(not(equalTo(nullValue()))));
		assertThat(String.format("The professor's first name should be %s", firstName), updatedProfessor.getFirstName(),
				is((equalTo(firstName))));
		assertThat(String.format("The professor's last name should be %s", lastName), updatedProfessor.getLastName(),
				is((equalTo(lastName))));
		assertThat(String.format("The professor's department should be %s", department), updatedProfessor.getDepartment(),
				is((equalTo(department))));
	}

	@Test
	@Order(9)
	public void testUserPutProfessorByCourseId() throws JsonMappingException, JsonProcessingException {
		String sId = "1";
		String cId = "2";
		String firstName = "Avigail" + random.nextInt(1000);
		String lastName = "Lee" + random.nextInt(1000);
		String department = "Tourism" + random.nextInt(1000);
		Professor professor = new Professor(firstName, lastName, department);

		Response response = webTarget.path(STUDENT_RESOURCE_NAME).path(sId).path(COURSE_RESOURCE_NAME).path(cId)
				.path(PROFESSOR_SUBRESOURCE_NAME).register(userAuth).request(MediaType.APPLICATION_JSON)
				.put(Entity.json(professor));

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 403 (forbidden)", errorResponse.getStatusCode(), is(equalTo(403)));
	}

	@Test
	@Order(10)
	public void testAdminDeleteNonRegisteredStudent() throws JsonMappingException, JsonProcessingException {
		String id = String.valueOf(latestCreatedId);

		Response response = webTarget.path(STUDENT_RESOURCE_NAME).path(id).register(adminAuth).request(MediaType.APPLICATION_JSON)
				.delete();

		int statusCode = response.getStatus();
		assertThat("The response should be 200 (ok)", statusCode, is(equalTo(200)));
	}

	@Test
	@Order(11)
	public void testAdminDeleteRegisteredStudent() throws JsonMappingException, JsonProcessingException {
		String id = String.valueOf(latestCreatedId);

		Response response = webTarget.path(STUDENT_RESOURCE_NAME).path(id).register(adminAuth).request(MediaType.APPLICATION_JSON)
				.delete();

		int statusCode = response.getStatus();
		assertThat("The response should be 400 (Bad Request)", statusCode, is(equalTo(400)));
	}

	@Test
	@Order(12)
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
