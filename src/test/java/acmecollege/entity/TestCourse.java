/**
 * File:  TestCourse.java Course materials (23S) CST 8277
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

import org.apache.commons.lang3.StringUtils;
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
public class TestCourse extends JUnitBase {

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
	public void testGetAllCourse() {
		Response response = webTarget.path(COURSE_RESOURCE_NAME).request(MediaType.APPLICATION_JSON).get();

		List<Course> courses = response.readEntity(new GenericType<List<Course>>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The course list should not be empty.", courses, is(not(empty())));
	}

	@Test
	@Order(2)
	public void testAdminGetAllCourse() {
		Response response = webTarget.path(COURSE_RESOURCE_NAME).register(adminAuth).request(MediaType.APPLICATION_JSON).get();

		List<Course> courses = response.readEntity(new GenericType<List<Course>>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The course list should not be empty.", courses, is(not(empty())));
		}

	@Test
	@Order(3)
	public void testUserGetAllCourse() {
		Response response = webTarget.path(COURSE_RESOURCE_NAME).register(userAuth).request(MediaType.APPLICATION_JSON).get();

		List<Course> courses = response.readEntity(new GenericType<List<Course>>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The course list should not be empty.", courses, is(not(empty())));
		}

	@Test
	@Order(4)
	public void testGetCourseById() {
		String courseId = "1";
		Response response = webTarget.path(COURSE_RESOURCE_NAME).path(courseId).request(MediaType.APPLICATION_JSON).get();

		Course course = response.readEntity(new GenericType<Course>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The course should not be null.", course, is(not(equalTo(nullValue()))));
		assertThat("The course's code should not be null.", course.getCourseCode(), is(not(equalTo(nullValue()))));
		assertThat("The course's title should not be null.", course.getCourseTitle(), is(not(equalTo(nullValue()))));
		assertThat("The course's semester should not be null.", course.getSemester(), is(not(equalTo(nullValue()))));
	}

	@Test
	@Order(5)
	public void testAdminGetCourseById() {
		String courseId = "1";
		Response response = webTarget.path(COURSE_RESOURCE_NAME).path(courseId).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).get();

		Course course = response.readEntity(new GenericType<Course>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The course should not be null.", course, is(not(equalTo(nullValue()))));
		assertThat("The course's code should not be null.", course.getCourseCode(), is(not(equalTo(nullValue()))));
		assertThat("The course's title should not be null.", course.getCourseTitle(), is(not(equalTo(nullValue()))));
		assertThat("The course's semester should not be null.", course.getSemester(), is(not(equalTo(nullValue()))));
	}

	@Test
	@Order(6)
	public void testUserGetCourseById() {
		String courseId = "1";
		Response response = webTarget.path(COURSE_RESOURCE_NAME).path(courseId).register(userAuth)
				.request(MediaType.APPLICATION_JSON).get();

		Course course = response.readEntity(new GenericType<Course>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The course should not be null.", course, is(not(equalTo(nullValue()))));
		assertThat("The course's code should not be null.", course.getCourseCode(), is(not(equalTo(nullValue()))));
		assertThat("The course's title should not be null.", course.getCourseTitle(), is(not(equalTo(nullValue()))));
		assertThat("The course's semester should not be null.", course.getSemester(), is(not(equalTo(nullValue()))));
	}

	@Test
	@Order(7)
	public void testPostNewCourse() throws JsonMappingException, JsonProcessingException {
		final String codeNum = StringUtils.leftPad(String.valueOf(random.nextInt(10000)), 4, "0");
		final String courseCode = "CST" + codeNum;
		final String courseTitle = "Web Programming";
		final int year = 2024;
		final String semester = "SPRING";
		final int creditUnits = 3;
		final byte online = 0b0000;

		Course newCourse = new Course(courseCode, courseTitle, year, semester, creditUnits, online);
		Response response = webTarget.path(COURSE_RESOURCE_NAME).request(MediaType.APPLICATION_JSON).post(Entity.json(newCourse));

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 401 (Unauthorized)", errorResponse.getStatusCode(), is(equalTo(401)));
	}

	@Test
	@Order(8)
	public void testAdminPostNewCourse() throws JsonMappingException, JsonProcessingException {
		final String codeNum = StringUtils.leftPad(String.valueOf(random.nextInt(10000)), 4, "0");
		final String courseCode = "CST" + codeNum;
		final String courseTitle = "Web Programming";
		final int year = 2024;
		final String semester = "SPRING";
		final int creditUnits = 3;
		final byte online = 0b0000;

		Course newCourse = new Course(courseCode, courseTitle, year, semester, creditUnits, online);
		Response response = webTarget.path(COURSE_RESOURCE_NAME).register(adminAuth).request(MediaType.APPLICATION_JSON)
				.post(Entity.json(newCourse));

		Course course = response.readEntity(new GenericType<Course>() {
		});

		assertThat("The response should be 200 (ok)", response.getStatus(), is(equalTo(200)));
		assertThat("The course should not be null.", course, is(not(equalTo(nullValue()))));
		assertThat(String.format("The course's code should be %s.", courseCode), course.getCourseCode(), is(equalTo(courseCode)));
		assertThat(String.format("The course's title should be %s.", courseTitle), course.getCourseTitle(),
				is(equalTo(courseTitle)));
		assertThat(String.format("The course's year should be %d.", year), course.getYear(), is(equalTo(year)));
		assertThat(String.format("The course's semester should be %s.", semester), course.getSemester(), is(equalTo(semester)));
		assertThat(String.format("The course's credit units should be %d.", creditUnits), course.getCreditUnits(),
				is(equalTo(creditUnits)));
		assertThat(String.format("The course's online or not should be %s.", online), course.getOnline(), is(equalTo(online)));

		// The latest created course's id
		latestCreatedId = course.getId();
	}

	@Test
	@Order(9)
	public void testUserPostNewCourse() throws JsonMappingException, JsonProcessingException {
		final String codeNum = StringUtils.leftPad(String.valueOf(random.nextInt(10000)), 4, "0");
		final String courseCode = "CST" + codeNum;
		final String courseTitle = "Web Programming";
		final int year = 2024;
		final String semester = "SPRING";
		final int creditUnits = 3;
		final byte online = 0b0000;

		Course newCourse = new Course(courseCode, courseTitle, year, semester, creditUnits, online);
		Response response = webTarget.path(COURSE_RESOURCE_NAME).register(userAuth).request(MediaType.APPLICATION_JSON)
				.post(Entity.json(newCourse));

		HttpErrorResponse errorResponse = response.readEntity(new GenericType<HttpErrorResponse>() {
		});

		assertThat("The response should be 403 (Forbidden)", errorResponse.getStatusCode(), is(equalTo(403)));
	}

	@Test
	@Order(10)
	public void testDeleteCourse() throws JsonMappingException, JsonProcessingException {
		String courseId = String.valueOf(latestCreatedId);

		Response response = webTarget.path(COURSE_RESOURCE_NAME).path(courseId).request(MediaType.APPLICATION_JSON).delete();

		int statusCode = response.getStatus();
		assertThat("The response should be 401 (Unauthorized)", statusCode, is(equalTo(401)));
	}

	@Test
	@Order(11)
	public void testAdminDeleteNonExistingProfessor() throws JsonMappingException, JsonProcessingException {
		String courseId = "111";

		Response response = webTarget.path(COURSE_RESOURCE_NAME).path(courseId).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).delete();

		int statusCode = response.getStatus();
		assertThat("The response should be 400 (bad request)", statusCode, is(equalTo(400)));
	}

	@Test
	@Order(12)
	public void testAdminDeleteExistingProfessor() throws JsonMappingException, JsonProcessingException {
		String courseId = String.valueOf(latestCreatedId);

		Response response = webTarget.path(COURSE_RESOURCE_NAME).path(courseId).register(adminAuth)
				.request(MediaType.APPLICATION_JSON).delete();

		int statusCode = response.getStatus();
		assertThat("The response should be 200 (ok)", statusCode, is(equalTo(200)));
	}

	@Test
	@Order(13)
	public void testUserDeleteCourse() throws JsonMappingException, JsonProcessingException {
		String courseId = String.valueOf(latestCreatedId);

		Response response = webTarget.path(COURSE_RESOURCE_NAME).path(courseId).register(userAuth).request(MediaType.APPLICATION_JSON).delete();

		int statusCode = response.getStatus();
		assertThat("The response should be 403 (Forbidden)", statusCode, is(equalTo(403)));
	}

}
