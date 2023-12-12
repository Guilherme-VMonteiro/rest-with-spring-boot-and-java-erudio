package br.com.erudio.integrationtests.controller.withxml;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonParseException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonMappingException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import br.com.erudio.configs.TestConfigs;
import br.com.erudio.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.erudio.integrationtests.vo.AccountCredentialsVO;
import br.com.erudio.integrationtests.vo.PersonVO;
import br.com.erudio.integrationtests.vo.TokenVO;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class PersonControllerXmlTest extends AbstractIntegrationTest {

	private static RequestSpecification specification;
	private static XmlMapper objectMapper;

	private static PersonVO person;

	@BeforeAll
	public static void setup() {
		objectMapper = new XmlMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		person = new PersonVO();
	}

	@Test
	@Order(0)
	public void authorization() throws JsonParseException, JsonMappingException, IOException {

		AccountCredentialsVO user = new AccountCredentialsVO("guilherme", "admin123");

		var accessToken = given().basePath("/auth/signin")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_XML)
					.accept(TestConfigs.CONTENT_TYPE_XML)
						.body(user)
						.when()
						.post()
						.then()
						.statusCode(200)
							.extract()
							.body()
								.as(TokenVO.class).getAccessToken();

		specification = new RequestSpecBuilder()
				.addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + accessToken).setBasePath("/person")
				.setPort(TestConfigs.SERVER_PORT).addFilter(new RequestLoggingFilter(LogDetail.ALL))
				.addFilter(new ResponseLoggingFilter(LogDetail.ALL)).build();
	}

	@Test
	@Order(1)
	public void testCreate() throws JsonParseException, JsonMappingException, IOException {
		mockPerson();

		var content = given().spec(specification)
				.contentType(TestConfigs.CONTENT_TYPE_XML)
				.accept(TestConfigs.CONTENT_TYPE_XML)
				.body(person)
				.when()
				.post()
				.then().statusCode(200).extract().body().asString();

		PersonVO persistedPerson = objectMapper.readValue(content, PersonVO.class);
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

		var content = given().spec(specification)
				.contentType(TestConfigs.CONTENT_TYPE_XML)
				.accept(TestConfigs.CONTENT_TYPE_XML)
					.body(person).when().post()
					.then().statusCode(200).extract().body().asString();

		PersonVO persistedPerson = objectMapper.readValue(content, PersonVO.class);
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

		var content = given().spec(specification)
				.contentType(TestConfigs.CONTENT_TYPE_XML)
				.accept(TestConfigs.CONTENT_TYPE_XML)
					.pathParam("id", person.getId())
						.when()
							.patch("{id}")
								.then()
									.statusCode(200)
										.extract()
											.body()
											.asString();

		PersonVO persistedPerson = objectMapper.readValue(content, PersonVO.class);
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

		var content = given().spec(specification)
				.contentType(TestConfigs.CONTENT_TYPE_XML)
				.accept(TestConfigs.CONTENT_TYPE_XML)
				.header(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_TESTE)
				.pathParam("id", person.getId())
				.when().get("{id}").then().statusCode(200).extract().body().asString();

		PersonVO persistedPerson = objectMapper.readValue(content, PersonVO.class);
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
			.contentType(TestConfigs.CONTENT_TYPE_XML)
			.accept(TestConfigs.CONTENT_TYPE_XML)
			.pathParam("id", person.getId()).when()
				.delete("{id}").then().statusCode(204);

	}

	@Test
	@Order(6)
	public void testFindAll() throws JsonParseException, JsonMappingException, IOException {

		var content = given().spec(specification)
				.contentType(TestConfigs.CONTENT_TYPE_XML)
				.accept(TestConfigs.CONTENT_TYPE_XML)
					.when()
					.get()
				.then()
					.statusCode(200)
						.extract()
						.body()
							.asString();
		
		List<PersonVO> people = objectMapper.readValue(content, new TypeReference<List<PersonVO>>() {});

		PersonVO foundPersonOne = people.get(0);

		assertNotNull(foundPersonOne.getId());
		assertNotNull(foundPersonOne.getFirstName());
		assertNotNull(foundPersonOne.getLastName());
		assertNotNull(foundPersonOne.getAddress());
		assertNotNull(foundPersonOne.getGender());
		
		assertTrue(foundPersonOne.getEnabled());

		assertEquals(1, foundPersonOne.getId());

		assertEquals("Guilherme", foundPersonOne.getFirstName());
		assertEquals("Monteiro", foundPersonOne.getLastName());
		assertEquals("Laguna - Santa Catarina", foundPersonOne.getAddress());
		assertEquals("Male", foundPersonOne.getGender());
		
		PersonVO foundPersonSix = people.get(5);

		assertNotNull(foundPersonSix.getId());
		assertNotNull(foundPersonSix.getFirstName());
		assertNotNull(foundPersonSix.getLastName());
		assertNotNull(foundPersonSix.getAddress());
		assertNotNull(foundPersonSix.getGender());
		
		assertTrue(foundPersonSix.getEnabled());

		assertEquals(9, foundPersonSix.getId());

		assertEquals("Miguel", foundPersonSix.getFirstName());
		assertEquals("Machado", foundPersonSix.getLastName());
		assertEquals("Laguna - Santa Catarina", foundPersonSix.getAddress());
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
				.contentType(TestConfigs.CONTENT_TYPE_XML)
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
