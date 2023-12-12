package br.com.erudio.integrationtests.controller.withyaml;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonParseException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonMappingException;

import br.com.erudio.configs.TestConfigs;
import br.com.erudio.integrationtests.controller.withyaml.mapper.YAMLMapper;
import br.com.erudio.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.erudio.integrationtests.vo.AccountCredentialsVO;
import br.com.erudio.integrationtests.vo.PersonVO;
import br.com.erudio.integrationtests.vo.TokenVO;
import br.com.erudio.integrationtests.vo.pagedModels.PagedModelPerson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class PersonControllerYamlTest extends AbstractIntegrationTest {

	private static RequestSpecification specification;
	private static YAMLMapper objectMapper;

	private static PersonVO person;

	@BeforeAll
	public static void setup() {
		objectMapper = new YAMLMapper();
		person = new PersonVO();
	}

	@Test
	@Order(0)
	public void authorization() throws JsonParseException, JsonMappingException, IOException {

		AccountCredentialsVO user = new AccountCredentialsVO("guilherme", "admin123");

		var accessToken = given()
				.config(RestAssuredConfig.config()
						.encoderConfig(EncoderConfig.encoderConfig().encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML,
								ContentType.TEXT)))
				.basePath("/auth/signin")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_YML)
					.accept(TestConfigs.CONTENT_TYPE_YML)
						.body(user, objectMapper)
							.when()
								.post()
									.then()
										.statusCode(200)
											.extract()
											.body()
												.as(TokenVO.class, objectMapper)
													.getAccessToken();

		specification = new RequestSpecBuilder()
				.addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + accessToken).setBasePath("/person")
				.setPort(TestConfigs.SERVER_PORT).addFilter(new RequestLoggingFilter(LogDetail.ALL))
				.addFilter(new ResponseLoggingFilter(LogDetail.ALL)).build();
	}

	@Test
	@Order(1)
	public void testCreate() throws JsonParseException, JsonMappingException, IOException {
		mockPerson();

		var persistedPerson = given().spec(specification)
				.config(RestAssuredConfig.config()
						.encoderConfig(EncoderConfig.encoderConfig().encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML,
								ContentType.TEXT)))
								.contentType(TestConfigs.CONTENT_TYPE_YML)
									.accept(TestConfigs.CONTENT_TYPE_YML)
									.body(person, objectMapper)
										.when()
											.post()
												.then()
													.statusCode(200)
														.extract()
															.body().as(PersonVO.class, objectMapper);

		person = persistedPerson;

		assertNotNull(persistedPerson);
		assertNotNull(persistedPerson.getId());
		assertNotNull(persistedPerson.getFirstName());
		assertNotNull(persistedPerson.getLastName());
		assertNotNull(persistedPerson.getAddress());
		assertNotNull(persistedPerson.getGender());
		assertTrue(persistedPerson.getEnabled());

		assertTrue(persistedPerson.getId() > 0);

		assertEquals("Richard", persistedPerson.getFirstName());
		assertEquals("Stallman", persistedPerson.getLastName());
		assertEquals("New York City - New York", persistedPerson.getAddress());
		assertEquals("Male", persistedPerson.getGender());
	}

	@Test
	@Order(2)
	public void testUpdate() throws JsonParseException, JsonMappingException, IOException {
		person.setLastName("Stallman Updated");

		var persistedPerson = given().spec(specification)
				.config(RestAssuredConfig.config().encoderConfig(
						EncoderConfig.encoderConfig().encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))
				.contentType(TestConfigs.CONTENT_TYPE_YML)
				.accept(TestConfigs.CONTENT_TYPE_YML)
					.body(person, objectMapper)
					.when().post()
					.then().statusCode(200).extract().body().as(PersonVO.class, objectMapper);

		person = persistedPerson;

		assertNotNull(persistedPerson);
		assertNotNull(persistedPerson.getId());
		assertNotNull(persistedPerson.getFirstName());
		assertNotNull(persistedPerson.getLastName());
		assertNotNull(persistedPerson.getAddress());
		assertNotNull(persistedPerson.getGender());
		assertTrue(persistedPerson.getEnabled());

		assertEquals(person.getId(), persistedPerson.getId());

		assertEquals("Richard", persistedPerson.getFirstName());
		assertEquals("Stallman Updated", persistedPerson.getLastName());
		assertEquals("New York City - New York", persistedPerson.getAddress());
		assertEquals("Male", persistedPerson.getGender());
	}

	@Test
	@Order(3)
	public void testDisableById() throws JsonParseException, JsonMappingException, IOException {

		var persistedPerson = given().spec(specification)
				.config(
						RestAssuredConfig.config()
						.encoderConfig(EncoderConfig.encoderConfig()
								.encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))
				.contentType(TestConfigs.CONTENT_TYPE_YML)
				.accept(TestConfigs.CONTENT_TYPE_YML)
					.pathParam("id", person.getId())
						.when()
							.patch("{id}")
								.then()
									.statusCode(200)
										.extract()
											.body()
												.as(PersonVO.class, objectMapper);

		person = persistedPerson;

		assertNotNull(persistedPerson);
		assertNotNull(persistedPerson.getId());
		assertNotNull(persistedPerson.getFirstName());
		assertNotNull(persistedPerson.getLastName());
		assertNotNull(persistedPerson.getAddress());
		assertNotNull(persistedPerson.getGender());
		assertFalse(persistedPerson.getEnabled());

		assertEquals(person.getId(), persistedPerson.getId());

		assertEquals("Richard", persistedPerson.getFirstName());
		assertEquals("Stallman Updated", persistedPerson.getLastName());
		assertEquals("New York City - New York", persistedPerson.getAddress());
		assertEquals("Male", persistedPerson.getGender());
	}
	
	@Test
	@Order(4)
	public void testFindById() throws JsonParseException, JsonMappingException, IOException {
		mockPerson();

		var persistedPerson = given().spec(specification)
				.config(RestAssuredConfig.config().encoderConfig(
						EncoderConfig.encoderConfig().encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))
				.contentType(TestConfigs.CONTENT_TYPE_YML)
					.accept(TestConfigs.CONTENT_TYPE_YML)
						.header(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_TESTE)
							.pathParam("id", person.getId())
								.when()
									.get("{id}")
										.then()
										.statusCode(200)
											.extract()
												.body()
													.as(PersonVO.class, objectMapper);

		person = persistedPerson;

		assertNotNull(persistedPerson);
		assertNotNull(persistedPerson.getId());
		assertNotNull(persistedPerson.getFirstName());
		assertNotNull(persistedPerson.getLastName());
		assertNotNull(persistedPerson.getAddress());
		assertNotNull(persistedPerson.getGender());
		assertFalse(persistedPerson.getEnabled());

		assertEquals(person.getId(), persistedPerson.getId());

		assertEquals("Richard", persistedPerson.getFirstName());
		assertEquals("Stallman Updated", persistedPerson.getLastName());
		assertEquals("New York City - New York", persistedPerson.getAddress());
		assertEquals("Male", persistedPerson.getGender());
	}

	@Test
	@Order(5)
	public void testDelete() throws JsonParseException, JsonMappingException, IOException {

		given().spec(specification)
			.config(RestAssuredConfig.config().encoderConfig(
					EncoderConfig.encoderConfig().encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))
			.contentType(TestConfigs.CONTENT_TYPE_YML)
			.accept(TestConfigs.CONTENT_TYPE_YML)
			.pathParam("id", person.getId()).when()
				.delete("{id}").then().statusCode(204);

	}

	@Test
	@Order(6)
	public void testFindAll() throws JsonParseException, JsonMappingException, IOException {

		var wrapper = given().spec(specification)
				.config(RestAssuredConfig.config().encoderConfig(
						EncoderConfig.encoderConfig().encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))
				.contentType(TestConfigs.CONTENT_TYPE_YML)
				.accept(TestConfigs.CONTENT_TYPE_YML)
				.queryParams("page", 3, "size", 10, "direction", "asc")
					.when()
					.get()
				.then()
					.statusCode(200)
						.extract()
						.body().as(PagedModelPerson.class, objectMapper);
		
		var people = wrapper.getContent();

		PersonVO foundPersonOne = people.get(0);

		assertNotNull(foundPersonOne.getId());
		assertNotNull(foundPersonOne.getFirstName());
		assertNotNull(foundPersonOne.getLastName());
		assertNotNull(foundPersonOne.getAddress());
		assertNotNull(foundPersonOne.getGender());

		assertFalse(foundPersonOne.getEnabled());
		
		assertEquals(913, foundPersonOne.getId());

		assertEquals("Allis", foundPersonOne.getFirstName());
		assertEquals("Denham", foundPersonOne.getLastName());
		assertEquals("9603 Charing Cross Way", foundPersonOne.getAddress());
		assertEquals("Female", foundPersonOne.getGender());
		
		
		PersonVO foundPersonSix = people.get(5);

		assertNotNull(foundPersonSix.getId());
		assertNotNull(foundPersonSix.getFirstName());
		assertNotNull(foundPersonSix.getLastName());
		assertNotNull(foundPersonSix.getAddress());
		assertNotNull(foundPersonSix.getGender());
		
		assertTrue(foundPersonSix.getEnabled());

		assertEquals(266, foundPersonSix.getId());

		assertEquals("Alwin", foundPersonSix.getFirstName());
		assertEquals("Lisamore", foundPersonSix.getLastName());
		assertEquals("531 Bunker Hill Court", foundPersonSix.getAddress());
		assertEquals("Male", foundPersonSix.getGender());
	}
	
	@Test
	@Order(7)
	public void testFindAllWithoutToken() throws JsonParseException, JsonMappingException, IOException {

		RequestSpecification specificationWithoutToken = new RequestSpecBuilder()
				.setBasePath("/person")
				.setPort(TestConfigs.SERVER_PORT)
					.addFilter(new RequestLoggingFilter(LogDetail.ALL))
					.addFilter(new ResponseLoggingFilter(LogDetail.ALL))
				.build();
		
		given().spec(specificationWithoutToken)
				.config(RestAssuredConfig.config().encoderConfig(
						EncoderConfig.encoderConfig().encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))
				.contentType(TestConfigs.CONTENT_TYPE_YML)
				.accept(TestConfigs.CONTENT_TYPE_XML)
					.when()
					.get()
				.then()
					.statusCode(403);
	}

	private void mockPerson() {
		person.setFirstName("Richard");
		person.setLastName("Stallman");
		person.setAddress("New York City - New York");
		person.setGender("Male");
		person.setEnabled(true);
	}

}
