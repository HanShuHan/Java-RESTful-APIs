/**
 * File:  JUnitBase.java Course materials (23S) CST 8277
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

package acmecollege;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Random;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import acmecollege.utility.MyConstants;

public class JUnitBase implements MyConstants {

	protected volatile static Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
	protected volatile static Logger logger = LogManager.getLogger(_thisClaz);

	protected volatile static HttpAuthenticationFeature adminAuth;
	protected volatile static HttpAuthenticationFeature userAuth;
	protected volatile static HttpAuthenticationFeature invalidUserAuth;

	protected volatile static URI uri;
	protected volatile static Client client;
	protected volatile WebTarget webTarget;

	protected volatile static Random random;
	protected volatile static int latestCreatedId;
	protected volatile static int latestCreatedId2;

	@BeforeAll
	public static void setUpAll() {
		client = ClientBuilder
				.newClient(new ClientConfig().register(MyObjectMapperProvider.class).register(new LoggingFeature()));
		uri = UriBuilder.fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION).scheme(HTTP_SCHEMA).host(HOST).port(PORT)
				.build();

		adminAuth = HttpAuthenticationFeature.basic(DEFAULT_ADMIN_USER, DEFAULT_ADMIN_USER_PASSWORD);
		userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER, DEFAULT_USER_PASSWORD);
		invalidUserAuth = HttpAuthenticationFeature.basic(DEFAULT_INVALID_USER, DEFAULT_INVALID_USER_PASSWORD);

		random = new Random();
	}

	@BeforeEach
	public void setUpEach() {
		webTarget = client.target(uri);
	}

}
