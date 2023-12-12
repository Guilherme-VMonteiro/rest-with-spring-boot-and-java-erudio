package br.com.erudio.integrationtests.controller.withyaml;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javax.management.modelmbean.XMLParseException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.erudio.configs.TestConfigs;
import br.com.erudio.integrationtests.controller.withyaml.mapper.YAMLMapper;
import br.com.erudio.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.erudio.integrationtests.vo.AccountCredentialsVO;
import br.com.erudio.integrationtests.vo.TokenVO;
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
public class AuthControllerYamlTest  extends AbstractIntegrationTest{

	private static YAMLMapper yamlMapper;
	private static TokenVO tokenVO;
	
	@BeforeAll
	public static void setUp() {
		yamlMapper = new YAMLMapper();
	}
	
	@Test
	@Order(1)
	public void testSignin() throws XMLParseException, IOException {
		AccountCredentialsVO user = new AccountCredentialsVO("guilherme", "admin123");
		
		
		RequestSpecification specification = new RequestSpecBuilder()
									.addFilter(new RequestLoggingFilter(LogDetail.ALL))
									.addFilter(new ResponseLoggingFilter(LogDetail.ALL))
								.build();
		
		tokenVO = given()
				.spec(specification)
				.config(RestAssuredConfig.config().encoderConfig(
							EncoderConfig.encoderConfig().encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))
					.accept(TestConfigs.CONTENT_TYPE_YML)
					.basePath("/auth/signin")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_YML)
						.body(user, yamlMapper)
							.when().post()
								.then()
									.statusCode(200)
										.extract()
											.body()
												.as(TokenVO.class, yamlMapper);
		
		assertNotNull(tokenVO.getAccessToken());
		assertNotNull(tokenVO.getRefreshToken());
	}
	
	@Test
	@Order(2)
	public void testRefresh() throws XMLParseException, IOException {
		
		var newTokenVO = given()
						.config(RestAssuredConfig.config().encoderConfig(
									EncoderConfig.encoderConfig().encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YML, ContentType.TEXT)))
							.accept(TestConfigs.CONTENT_TYPE_YML)
							.basePath("/auth/refresh")
							.port(TestConfigs.SERVER_PORT)
							.contentType(TestConfigs.CONTENT_TYPE_YML)
								.pathParam("username", tokenVO.getUsername())
								.header(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenVO.getRefreshToken())
									.when().put("{username}")
									.then()
										.statusCode(200)
											.extract()
												.body()
													.as(TokenVO.class, yamlMapper);
		
		assertNotNull(tokenVO.getAccessToken());
		assertNotNull(tokenVO.getRefreshToken());
	}
}
