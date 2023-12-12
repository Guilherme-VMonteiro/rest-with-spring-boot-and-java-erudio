package br.com.erudio.integrationtests.controller.withjson;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonParseException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.DeserializationFeature;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonMappingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import br.com.erudio.configs.TestConfigs;
import br.com.erudio.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.erudio.integrationtests.vo.PersonVO;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class PersonControllerJsonTest extends AbstractIntegrationTest{

	private static RequestSpecification specification;
	private static ObjectMapper objectMapper;
	
	private static PersonVO person;
	
	@BeforeAll
	public static void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		person = new PersonVO();
	}
	
	@Test
	@Order(1)
	public void testCreate() throws JsonParseException, JsonMappingException, IOException {
		mockPerson();
		
		specification = new RequestSpecBuilder()
				.addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_TESTE)
				.setBasePath("/person")
				.setPort(TestConfigs.SERVER_PORT)
					.addFilter(new RequestLoggingFilter(LogDetail.ALL))
					.addFilter(new ResponseLoggingFilter(LogDetail.ALL))
				.build();
		
		var content = given().spec(specification)
				.contentType(TestConfigs.CONTENT_TYPE_JSON)
					.body(person)
					.when()
					.post()
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
		
		assertTrue(persistedPerson.getId() > 0);
		
		assertEquals("Richard", persistedPerson.getFirstName());
		assertEquals("Stallman", persistedPerson.getLastName());
		assertEquals("New York City - New York", persistedPerson.getAddress());
		assertEquals("Male", persistedPerson.getGender());
}
		@Test
		@Order(2)
		public void testCreateWithWrongOrigin() throws JsonParseException, JsonMappingException, IOException {
			mockPerson();
			
			specification = new RequestSpecBuilder()
					.addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_TESTE_UNAUTHORIZED)
					.setBasePath("/person")
					.setPort(TestConfigs.SERVER_PORT)
						.addFilter(new RequestLoggingFilter(LogDetail.ALL))
						.addFilter(new ResponseLoggingFilter(LogDetail.ALL))
					.build();
			
			var content = given().spec(specification)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
						.body(person)
						.when()
						.post()
					.then()
						.statusCode(403)
					.extract()
						.body()
							.asString();
			

			
			assertNotNull(content);

			assertEquals("Invalid CORS request", content);
	}
		
	@Test
	@Order(3)
	public void testFindByIdWithWrongOrigin() throws JsonParseException, JsonMappingException, IOException {
		mockPerson();
			
			specification = new RequestSpecBuilder()
					.addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_TESTE_UNAUTHORIZED)
					.setBasePath("/person")
					.setPort(TestConfigs.SERVER_PORT)
						.addFilter(new RequestLoggingFilter(LogDetail.ALL))
						.addFilter(new ResponseLoggingFilter(LogDetail.ALL))
					.build();
			
			var content = given().spec(specification)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
						.pathParam("id",person.getId())
						.when()
						.get("{id}")
					.then()
						.statusCode(403)
					.extract()
						.body()
							.asString();
			
			assertNotNull(content);
			assertEquals("Invalid CORS request", content);
			

	}
		
	@Test
	@Order(4)
	public void testFindById() throws JsonParseException, JsonMappingException, IOException {
		mockPerson();
			
			specification = new RequestSpecBuilder()
					.addHeader(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_TESTE)
					.setBasePath("/person")
					.setPort(TestConfigs.SERVER_PORT)
					.addFilter(new RequestLoggingFilter(LogDetail.ALL))
					.addFilter(new ResponseLoggingFilter(LogDetail.ALL))
					.build();
			
			var content = given().spec(specification)
					.contentType(TestConfigs.CONTENT_TYPE_JSON)
					.pathParam("id",person.getId())
					.when()
					.get("{id}")
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
			
			assertTrue(persistedPerson.getId() > 0);
			
			assertEquals("Richard", persistedPerson.getFirstName());
			assertEquals("Stallman", persistedPerson.getLastName());
			assertEquals("New York City - New York", persistedPerson.getAddress());
			assertEquals("Male", persistedPerson.getGender());
		}

	private void mockPerson() {
		person.setFirstName("Richard");
		person.setLastName("Stallman");
		person.setAddress("New York City - New York");
		person.setGender("Male");	
	}

}
