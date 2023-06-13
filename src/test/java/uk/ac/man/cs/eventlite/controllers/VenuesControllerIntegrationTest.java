package uk.ac.man.cs.eventlite.controllers;


import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.hamcrest.CoreMatchers.not;

import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.regex.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;

	private WebTestClient client;
	
	private static Pattern CSRF = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
	private static String SESSION_KEY = "JSESSIONID";
	private static String CSRF_HEADER = "X-CSRF-TOKEN";

	private int currentRows;
	
	@Autowired
	private VenueService venueService;
	
	
	private String[] login() {
		String[] tokens = new String[2];

		// Although this doesn't POST the log in form it effectively logs us in.
		// If we provide the correct credentials here, we get a session ID back which
		// keeps us logged in.
		EntityExchangeResult<String> result = client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get()
				.uri("/sign-in").accept(MediaType.TEXT_HTML).exchange().expectBody(String.class).returnResult();
		tokens[0] = getCsrfToken(result.getResponseBody());
		tokens[1] = result.getResponseCookies().getFirst(SESSION_KEY).getValue();

		return tokens;
	}

	private String getCsrfToken(String body) {
		Matcher matcher = CSRF.matcher(body);

		// matcher.matches() must be called; might as well assert something as well...
		assertThat(matcher.matches(), equalTo(true));

		return matcher.group(1);
	}

	    
	   

	@BeforeEach
	public void setup() {
		currentRows = countRowsInTable("venues");
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}
	
	@Test
	@DirtiesContext
	public void testCoordinates() {
		String[] tokens = login();
		MultiValueMap<String,String> form = new LinkedMultiValueMap<String,String>();
		assertThat(3,equalTo(countRowsInTable("venues")));
		form.add("name", "Kilburn");
		form.add("capacity","100");
		form.add("roadName","Oxford Road");
		form.add("postcode","M13 9PL");
		form.add("cityName","Manchester");
		
		client.post().uri("/venues").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(form).cookies(cookies->{cookies.add(SESSION_KEY,tokens[1]);}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/venues"));
		assertThat(currentRows+1,equalTo(countRowsInTable("venues")));
		
		Venue venue  = venueService.getbyId(7L);
		
		assertThat(venue.getName(),is("Kilburn"));
		assertThat(venue.getAddress(),is("Oxford Road, Manchester, M13 9PL"));
		assertThat(venue.getLatitude(),equalTo(53.46691)); //51.765915
		assertThat(venue.getLongitude(),equalTo(-2.23358)); //-1.200179

		
	}

	@Test
	public void testGetAllVenues() {
		client.get().uri("/venues").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
	}

	@Test
	public void getVenueNotFound() {
		client.get().uri("/venues/99").accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("99"));
				});
	}
	
	@Test
	public void getEvent() {
		client.get().uri("/venues/1").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectHeader()
		.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("Venue Details"));
		});
	}
	
	@Test
	@DirtiesContext
	public void addVenueLoggedIn() {
		
		String[] tokens = login();
		
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		
		form.add("name", "name");
		form.add("capacity", "10");
		form.add("roadName", "oxford");
		form.add("postcode", "M14");
		form.add("cityName","Manchester");
		client.post().uri("/venues").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(form).cookies(cookies -> {
			cookies.add(SESSION_KEY, tokens[1]);
		}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/venues"));
		
		assertThat(currentRows + 1, equalTo(countRowsInTable("venues")));

	}
	
	@Test
	@DirtiesContext
	public void addVenueLoggedOut() {
		
		String[] tokens = login();
		
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		
		form.add("name", "name");
		form.add("capacity", "10");
		form.add("roadName", "oxford");
		form.add("postcode", "M14");
		form.add("cityName","Manchester");
		client.post().uri("/venues").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/sign-in"));
		
		assertThat(currentRows, equalTo(countRowsInTable("venues")));

	}
	
	@Test
	@DirtiesContext
	public void addVenueLoggedBlankName() {
		
		String[] tokens = login();
		
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		
		form.add("name", "");
		form.add("capacity", "10");
		form.add("roadName", "oxford");
		form.add("postcode", "M14");
		form.add("cityName","Manchester");
		client.post().uri("/venues").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(form).cookies(cookies -> {
			cookies.add(SESSION_KEY, tokens[1]);
		}).exchange().expectStatus().isOk().expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("Venue name can not be empty"));
		});
		
		assertThat(currentRows, equalTo(countRowsInTable("venues")));

	}
	
	@Test
	@DirtiesContext
	public void addVenueLoggedNegativeCapacity() {
		
		String[] tokens = login();
		
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		
		form.add("name", "name");
		form.add("capacity", "-1");
		form.add("roadName", "oxford");
		form.add("postcode", "M14");
		form.add("cityName","Manchester");
		client.post().uri("/venues").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(form).cookies(cookies -> {
			cookies.add(SESSION_KEY, tokens[1]);
		}).exchange().expectStatus().isOk().expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("The value must be a positive integer"));
		});
		
		assertThat(currentRows, equalTo(countRowsInTable("venues")));

	}
	
	@Test
	@DirtiesContext
	public void addVenueLoggedBlankRoad() {
		
		String[] tokens = login();
		
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		
		form.add("name", "name");
		form.add("capacity", "1");
		form.add("roadName", "");
		form.add("postcode", "M14");
		form.add("cityName","Manchester");
		client.post().uri("/venues").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(form).cookies(cookies -> {
			cookies.add(SESSION_KEY, tokens[1]);
		}).exchange().expectStatus().isOk().expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("Road name can not be empty"));
		});
		
		assertThat(currentRows, equalTo(countRowsInTable("venues")));

	}
	
	@Test
	@DirtiesContext
	public void addVenueLoggedBlankPost() {
		
		String[] tokens = login();
		
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		
		form.add("name", "name");
		form.add("capacity", "1");
		form.add("roadName", "oxford");
		form.add("postcode", "");
		form.add("cityName","Manchester");
		client.post().uri("/venues").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(form).cookies(cookies -> {
			cookies.add(SESSION_KEY, tokens[1]);
		}).exchange().expectStatus().isOk().expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("Postcode can not be empty"));
		});
		
		assertThat(currentRows, equalTo(countRowsInTable("venues")));

	}
	@Test
	@DirtiesContext
	public void addVenueLoggedBlankCity() {
		
		String[] tokens = login();
		
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		
		form.add("name", "name");
		form.add("capacity", "1");
		form.add("roadName", "oxford");
		form.add("postcode", "M13 9PL");
		form.add("cityName","");
		client.post().uri("/venues").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(form).cookies(cookies -> {
			cookies.add(SESSION_KEY, tokens[1]);
		}).exchange().expectStatus().isOk().expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("City name can not be empty"));
		});
		
		assertThat(currentRows, equalTo(countRowsInTable("venues")));

	}
	
	@Test
	@DirtiesContext
	public void deleteVenueLoggedIn() {
		String[] tokens = login();
		long id = 2;
		client.delete().uri("venues/{id}", id).accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).cookies(cookies -> {
			cookies.add(SESSION_KEY, tokens[1]);
		}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/venues"));
		
		assertThat(currentRows - 1, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	@DirtiesContext
	public void deleteVenueLoggedOut() {
		String[] tokens = login();
		long id = 1;
		client.delete().uri("venues/{id}", id).accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0])
		.exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/sign-in"));
		
		assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	@DirtiesContext
	public void deleteVenueNotFound() {
		String[] tokens = login();
		long id = 99;
		client.delete().uri("venues/{id}", id).accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).cookies(cookies -> {
			cookies.add(SESSION_KEY, tokens[1]);
		}).exchange().expectStatus().isNotFound();
		
		assertThat(currentRows, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	@DirtiesContext
	public void updateVenueLoggedIn() {
		String[] tokens = login();
		long id = 1;
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "names");
		form.add("capacity", "2");
		form.add("roadName", "oxfords");
		form.add("postcode", "M15");
		form.add("cityName","Manchester");
		client.post().uri("venues/{id}", id).accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(form).cookies(cookies -> {
			cookies.add(SESSION_KEY, tokens[1]);
		}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/venues"));
		
		assertThat(currentRows, equalTo(countRowsInTable("venues")));
		Venue venue = venueService.getbyId(id);
		
		assertThat(venue.getName(), equalTo("names"));
		assertThat(venue.getCapacity(), equalTo(2));
		assertThat(venue.getRoadName(), equalTo("oxfords"));
		assertThat(venue.getPostcode(), equalTo("M15"));

	}
	
	@Test
	@DirtiesContext
	public void updateVenueLoggedOut() {
		String[] tokens = login();
		long id = 1;
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "names");
		form.add("capacity", "2");
		form.add("roadName", "oxfords");
		form.add("postcode", "M15");
		form.add("cityName","London");
		client.post().uri("venues/{id}", id).accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(form)
		.exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/sign-in"));
		
		assertThat(currentRows, equalTo(countRowsInTable("venues")));
		Venue venue = venueService.getbyId(id);
		
		assertThat(venue.getName(), not(equalTo("names")));
		assertThat(venue.getCapacity(), not(equalTo(2)));
		assertThat(venue.getRoadName(), not(equalTo("oxfords")));
		assertThat(venue.getPostcode(), not(equalTo("M15")));
		assertThat(venue.getPostcode(), not(equalTo("London")));

	}
	
	
}
