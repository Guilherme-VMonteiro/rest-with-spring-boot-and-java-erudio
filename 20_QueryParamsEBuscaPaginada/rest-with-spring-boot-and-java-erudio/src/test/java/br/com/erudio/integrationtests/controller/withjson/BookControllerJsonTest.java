package br.com.erudio.integrationtests.controller.withjson;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonParseException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonMappingException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.erudio.configs.TestConfigs;
import br.com.erudio.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.erudio.integrationtests.vo.AccountCredentialsVO;
import br.com.erudio.integrationtests.vo.BookVO;
import br.com.erudio.integrationtests.vo.TokenVO;
import br.com.erudio.integrationtests.vo.wrappers.WrapperBookVO;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class BookControllerJsonTest extends AbstractIntegrationTest {

	private static RequestSpecification specification;
	private static ObjectMapper objectMapper;

	private static BookVO book;
	
	@BeforeAll
	public static void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		book = new BookVO();
		
	}

	@Test
	@Order(0)
	public void authorization() throws JsonParseException, JsonMappingException, IOException {

		AccountCredentialsVO user = new AccountCredentialsVO("guilherme", "admin123");

		var accessToken = given().basePath("/auth/signin").port(TestConfigs.SERVER_PORT)
				.contentType(TestConfigs.CONTENT_TYPE_JSON).body(user).when().post().then().statusCode(200).extract()
				.body().as(TokenVO.class).getAccessToken();

		specification = new RequestSpecBuilder()
				.addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + accessToken).setBasePath("/book")
				.setPort(TestConfigs.SERVER_PORT).addFilter(new RequestLoggingFilter(LogDetail.ALL))
				.addFilter(new ResponseLoggingFilter(LogDetail.ALL)).build();
	}

	@Test
	@Order(1)
	public void testCreate() throws JsonParseException, JsonMappingException, IOException {
		mockBook();

		var content = given().spec(specification).contentType(TestConfigs.CONTENT_TYPE_JSON).body(book).when().post()
				.then().statusCode(200).extract().body().asString();

		BookVO persistedBook = objectMapper.readValue(content, BookVO.class);
		book = persistedBook;

		assertNotNull(persistedBook);
		assertNotNull(persistedBook.getId());
		assertNotNull(persistedBook.getAuthor());
		assertNotNull(persistedBook.getLaunchDate());
		assertNotNull(persistedBook.getPrice());
		assertNotNull(persistedBook.getTitle());

		assertTrue(persistedBook.getId() > 0);

		assertEquals("Nigel Poulton", persistedBook.getAuthor());
		assertEquals(55.99, persistedBook.getPrice());
		assertEquals("Docker Deep Dive", persistedBook.getTitle());
	}

	@Test
	@Order(2)
	public void testUpdate() throws JsonParseException, JsonMappingException, IOException {
		book.setAuthor("Nigel Poulton Updated");

		var content = given().spec(specification).contentType(TestConfigs.CONTENT_TYPE_JSON).body(book).when().post()
				.then().statusCode(200).extract().body().asString();

		BookVO persistedBook = objectMapper.readValue(content, BookVO.class);
		book = persistedBook;

		assertNotNull(persistedBook);
		assertNotNull(persistedBook.getId());
		assertNotNull(persistedBook.getAuthor());
		assertNotNull(persistedBook.getLaunchDate());
		assertNotNull(persistedBook.getPrice());
		assertNotNull(persistedBook.getTitle());

		assertEquals(book.getId(), persistedBook.getId());

		assertEquals("Nigel Poulton Updated", persistedBook.getAuthor());
		assertEquals(55.99, persistedBook.getPrice());
		assertEquals("Docker Deep Dive", persistedBook.getTitle());
	}

	
	
	@Test
	@Order(4)
	public void testFindById() throws JsonParseException, JsonMappingException, IOException {
		mockBook();
		
		var content = given().spec(specification).contentType(TestConfigs.CONTENT_TYPE_JSON)
				.header(TestConfigs.HEADER_PARAM_ORIGIN, TestConfigs.ORIGIN_TESTE).pathParam("id", book.getId())
				.when().get("{id}").then().statusCode(200).extract().body().asString();
		
		BookVO persistedBook = objectMapper.readValue(content, BookVO.class);
		book = persistedBook;
		
		assertNotNull(persistedBook);
		assertNotNull(persistedBook.getId());
		assertNotNull(persistedBook.getAuthor());
		assertNotNull(persistedBook.getLaunchDate());
		assertNotNull(persistedBook.getPrice());
		assertNotNull(persistedBook.getTitle());
		
		assertEquals(book.getId(), persistedBook.getId());
		
		assertEquals("Nigel Poulton Updated", persistedBook.getAuthor());		
		assertEquals(55.99, persistedBook.getPrice());
		assertEquals("Docker Deep Dive", persistedBook.getTitle());
	}

	@Test
	@Order(5)
	public void testDelete() throws JsonParseException, JsonMappingException, IOException {

		given().spec(specification).contentType(TestConfigs.CONTENT_TYPE_JSON).pathParam("id", book.getId()).when()
				.delete("{id}").then().statusCode(204);

	}

	@Test
	@Order(6)
	public void testFindAll() throws JsonParseException, JsonMappingException, IOException {

		var content = given().spec(specification)
				.contentType(TestConfigs.CONTENT_TYPE_JSON)
				.queryParams("page", 1, "size", 6, "direction", "asc")
					.when()
					.get()
				.then()
					.statusCode(200)
				.extract()
				.body()
					.asString();
		
		WrapperBookVO wrapper = objectMapper.readValue(content, WrapperBookVO.class);
		List<BookVO> books = wrapper.getEmbedded().getBooks();
		
		BookVO foundBookOne = books.get(0);

		assertNotNull(foundBookOne.getId());
		assertNotNull(foundBookOne.getAuthor());
		assertNotNull(foundBookOne.getLaunchDate());
		assertNotNull(foundBookOne.getPrice());
		assertNotNull(foundBookOne.getTitle());

		assertEquals(7, foundBookOne.getId());

		assertEquals("Eric Freeman, Elisabeth Freeman, Kathy Sierra, Bert Bates", foundBookOne.getAuthor());
		assertEquals(110.00, foundBookOne.getPrice());
		assertEquals("Head First Design Patterns", foundBookOne.getTitle());
		
		BookVO foundBookSix = books.get(5);

		assertNotNull(foundBookSix.getId());
		assertNotNull(foundBookSix.getAuthor());
		assertNotNull(foundBookSix.getLaunchDate());
		assertNotNull(foundBookSix.getPrice());
		assertNotNull(foundBookSix.getTitle());
		

		assertEquals(13, foundBookSix.getId());

		assertEquals("Richard Hunter e George Westerman", foundBookSix.getAuthor());
		assertEquals(95.00, foundBookSix.getPrice());
		assertEquals("O verdadeiro valor de TI", foundBookSix.getTitle());
	}
	
	@Test
	@Order(7)
	public void testFindAllWithoutToken() throws JsonParseException, JsonMappingException, IOException {

		RequestSpecification specificationWithoutToken = new RequestSpecBuilder()
				.setBasePath("/book")
				.setPort(TestConfigs.SERVER_PORT)
					.addFilter(new RequestLoggingFilter(LogDetail.ALL))
					.addFilter(new ResponseLoggingFilter(LogDetail.ALL))
				.build();
		
		given().spec(specificationWithoutToken)
				.contentType(TestConfigs.CONTENT_TYPE_JSON)
					.when()
					.get()
				.then()
					.statusCode(403);
	}

	private void mockBook(){  
        book.setTitle("Docker Deep Dive");
        book.setAuthor("Nigel Poulton");
        book.setPrice(Double.valueOf(55.99));
        book.setLaunchDate(new Date());
	}

}
