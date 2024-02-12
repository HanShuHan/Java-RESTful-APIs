/**
 * File:  TestCourseRegistration.java Course materials (23S) CST 8277
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
public class TestCourseRegistration extends JUnitBase {

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
	public void testGetAllCourseRegistration() {
		Response response = webTarget.path(COURSE_REGISTRATION_RESOURCE_NAME).request(MediaType.APPLICATION_JSON).get();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 401 (Unauthorized)", errorResponse.getStatusCode(), is(equalTo(401)));
	}

	@Test
	@Order(2)
	public void testAdminGetAllCourseRegistration() {
		Response response = webTarget.path(COURSE_REGISTRATION_RESOURCE_NAME).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).get();

		List<CourseRegistration> courseRegistration = response.readEntity(new GenericType<List<CourseRegistration>>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The course list should not be empty.", courseRegistration, is(not(empty())));
	}

	@Test
	@Order(2)
	public void testAdminGetAllCourseRegistrationByStudentId() {
		final int STUDENT_ID = 1;

		Response response = webTarget.path(COURSE_REGISTRATION_RESOURCE_NAME).queryParam("student_id", STUDENT_ID)
				.register(adminAuth).request(MediaType.APPLICATION_JSON).get();

		List<CourseRegistration> courseRegistration = response.readEntity(new GenericType<List<CourseRegistration>>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The course list should not be empty.", courseRegistration, is(not(empty())));
	}

	@Test
	@Order(2)
	public void testAdminGetAllCourseRegistrationByCourseId() {
		final int COURSE = 1;

		Response response = webTarget.path(COURSE_REGISTRATION_RESOURCE_NAME).queryParam("course_id", COURSE).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).get();

		List<CourseRegistration> courseRegistration = response.readEntity(new GenericType<List<CourseRegistration>>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The course list should not be empty.", courseRegistration, is(not(empty())));
	}

	@Test
	@Order(2)
	public void testAdminGetAllCourseRegistrationByStudentIdAndCourseId() {
		final int STUDENT_ID = 1;
		final int COURSE_ID = 1;

		Response response = webTarget.path(COURSE_REGISTRATION_RESOURCE_NAME).queryParam("student_id", STUDENT_ID).queryParam("course_id", COURSE_ID).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).get();

		List<CourseRegistration> courseRegistration = response.readEntity(new GenericType<List<CourseRegistration>>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The course list should not be empty.", courseRegistration, is(not(empty())));
	}

	@Test
	@Order(3)
	public void testUserGetAllCourseRegistration() {
		Response response = webTarget.path(COURSE_REGISTRATION_RESOURCE_NAME).register(userAuth)
				.request(MediaType.APPLICATION_JSON).get();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 403 (Forbidden)", errorResponse.getStatusCode(), is(equalTo(403)));
	}

	@Test
	@Order(4)
	public void testPostNewCourseRegistration() throws JsonMappingException, JsonProcessingException {
		final int STUDENT_ID = 1;
		final int COURSE_ID = 3;
		final int PROFESSOR_ID = 1;
		final int GRADE = 96;
		final String LETTER_GRADE = "A+";
		CourseRegistration courseRegistration = new CourseRegistration();
		courseRegistration.setNumericGrade(GRADE);
		courseRegistration.setLetterGrade(LETTER_GRADE);

		Response response = webTarget.path(COURSE_REGISTRATION_RESOURCE_NAME).queryParam("student_id", STUDENT_ID)
				.queryParam("course_id", COURSE_ID).queryParam("professor_id", PROFESSOR_ID).request(MediaType.APPLICATION_JSON)
				.post(Entity.json(courseRegistration));

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});
		assertThat("The response should be 401 (Unauthorized)", errorResponse.getStatusCode(), is(equalTo(401)));
	}

	@Test
	@Order(5)
	public void testAdminPostNewCourseRegistration() throws JsonMappingException, JsonProcessingException {
		final int STUDENT_ID = 1;
		final int COURSE_ID = 3;
		final int PROFESSOR_ID = 1;
		final int GRADE = 96;
		final String LETTER_GRADE = "A+";
		CourseRegistration courseRegistration = new CourseRegistration();
		courseRegistration.setNumericGrade(GRADE);
		courseRegistration.setLetterGrade(LETTER_GRADE);

		Response response = webTarget.path(COURSE_REGISTRATION_RESOURCE_NAME).queryParam("student_id", STUDENT_ID)
				.queryParam("course_id", COURSE_ID).queryParam("professor_id", PROFESSOR_ID).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).post(Entity.json(courseRegistration));
		CourseRegistration newCourseRegistration = response.readEntity(new GenericType<CourseRegistration>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat(String.format("The course registration's student id should be %d.", STUDENT_ID),
				newCourseRegistration.getId().getStudentId(), is(equalTo(STUDENT_ID)));
		assertThat(String.format("The course registration's course id should be %d.", COURSE_ID),
				newCourseRegistration.getId().getCourseId(), is(equalTo(COURSE_ID)));
		assertThat(String.format("The course registration's numeric grade should be %d.", GRADE),
				newCourseRegistration.getNumericGrade(), is(equalTo(GRADE)));
		assertThat(String.format("The course registration's letter grade should be %s.", LETTER_GRADE),
				newCourseRegistration.getLetterGrade(), is(equalTo(LETTER_GRADE)));

		latestCreatedId = STUDENT_ID;
		latestCreatedId2 = COURSE_ID;
	}

	@Test
	@Order(6)
	public void testUserPostNewCourseRegistration() throws JsonMappingException, JsonProcessingException {
		final int STUDENT_ID = 1;
		final int COURSE_ID = 3;
		final int PROFESSOR_ID = 1;
		final int GRADE = 96;
		final String LETTER_GRADE = "A+";
		CourseRegistration courseRegistration = new CourseRegistration();
		courseRegistration.setNumericGrade(GRADE);
		courseRegistration.setLetterGrade(LETTER_GRADE);

		Response response = webTarget.path(COURSE_REGISTRATION_RESOURCE_NAME).queryParam("student_id", STUDENT_ID)
				.queryParam("course_id", COURSE_ID).queryParam("professor_id", PROFESSOR_ID).register(userAuth)
				.request(MediaType.APPLICATION_JSON).post(Entity.json(courseRegistration));

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});
		assertThat("The response should be 403 (Forbidden)", errorResponse.getStatusCode(), is(equalTo(403)));
	}

	@Test
	@Order(7)
	public void testDeleteCourseRegistration() throws JsonMappingException, JsonProcessingException {
		Response response = webTarget.path(COURSE_REGISTRATION_RESOURCE_NAME).request(MediaType.APPLICATION_JSON).delete();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 401 (Unauthorized)", errorResponse.getStatusCode(), is(equalTo(401)));
	}

	@Test
	@Order(8)
	public void testAdminUserDeleteCourseRegistrationByStudentIdAndCourseId()
			throws JsonMappingException, JsonProcessingException {

		final int STUDENT_ID = latestCreatedId;
		final int COURSE_ID = latestCreatedId2;

		Response response = webTarget.path(COURSE_REGISTRATION_RESOURCE_NAME).queryParam("student_id", STUDENT_ID)
				.queryParam("course_id", COURSE_ID).register(adminAuth).request(MediaType.APPLICATION_JSON).delete();
		
		List<CourseRegistration> deletedCourseRegistration = response.readEntity(new GenericType<List<CourseRegistration>>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat(String.format("The course registration with student id = %d and course id = %d should have been deleted.", STUDENT_ID, COURSE_ID),
				deletedCourseRegistration, is(not(empty())));
	}

	@Test
	@Order(9)
	public void testUserDeleteCourseRegistration() throws JsonMappingException, JsonProcessingException {
		Response response = webTarget.path(COURSE_REGISTRATION_RESOURCE_NAME).register(userAuth)
				.request(MediaType.APPLICATION_JSON).delete();

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 403 (Forbidden)", errorResponse.getStatusCode(), is(equalTo(403)));
	}

}
