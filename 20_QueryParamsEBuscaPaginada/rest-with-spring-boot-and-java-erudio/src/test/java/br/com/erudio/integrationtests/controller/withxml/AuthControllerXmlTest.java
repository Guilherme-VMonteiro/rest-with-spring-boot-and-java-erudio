package br.com.erudio.integrationtests.controller.withxml;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javax.management.modelmbean.XMLParseException;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.erudio.configs.TestConfigs;
import br.com.erudio.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.erudio.integrationtests.vo.AccountCredentialsVO;
import br.com.erudio.integrationtests.vo.TokenVO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class AuthControllerXmlTest  extends AbstractIntegrationTest{

private static TokenVO tokenVO;
	
	@Test
	@Order(1)
	public void testSignin() throws XMLParseException, IOException {
		AccountCredentialsVO user = new AccountCredentialsVO("guilherme", "admin123");
		
		tokenVO = given()
					.basePath("/auth/signin")
					.port(TestConfigs.SERVER_PORT)
					.contentType(TestConfigs.CONTENT_TYPE_XML)
						.body(user)
							.when().post()
								.then()
									.statusCode(200)
										.extract()
											.body()
												.as(TokenVO.class);
		
		assertNotNull(tokenVO.getAccessToken());
		assertNotNull(tokenVO.getRefreshToken());
	}
	
	@Test
	@Order(2)
	public void testRefresh() throws XMLParseException, IOException {
		AccountCredentialsVO user = new AccountCredentialsVO("guilherme", "admin123");
		
		var newTokenVO = given()
							.basePath("/auth/refresh")
							.port(TestConfigs.SERVER_PORT)
							.contentType(TestConfigs.CONTENT_TYPE_XML)
								.pathParam("username", tokenVO.getUsername())
								.header(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer " + tokenVO.getRefreshToken())
									.when().put("{username}")
									.then()
										.statusCode(200)
											.extract()
												.body()
													.as(TokenVO.class);
		
		assertNotNull(tokenVO.getAccessToken());
		assertNotNull(tokenVO.getRefreshToken());
	}
}
