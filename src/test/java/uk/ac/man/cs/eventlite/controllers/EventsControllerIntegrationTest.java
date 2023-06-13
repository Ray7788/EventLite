package uk.ac.man.cs.eventlite.controllers;


import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.hamcrest.CoreMatchers.not;

import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.LocalTime;
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
import uk.ac.man.cs.eventlite.entities.Event;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;

	private WebTestClient client;
	
	private static Pattern CSRF = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
	private static String SESSION_KEY = "JSESSIONID";
	private static String CSRF_HEADER = "X-CSRF-TOKEN";

	private int currentRows;
	
	@Autowired
	private EventService eventService;
	
	
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
		currentRows = countRowsInTable("events");
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}

	@Test
	public void testGetAllEvents() {
		client.get().uri("/events").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
	}

	@Test
	public void getEventNotFound() {
		client.get().uri("/events/99").accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("99"));
				});
	}
	
	@Test
	public void getEvent() {
		client.head().uri("/events/4").exchange()
        .expectStatus().isFound();
	}
	
	@Test
	@DirtiesContext
	public void addEventLoggedIn() {
		
		String[] tokens = login();
		
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name");
		form.add("date", "2024-01-01");
		form.add("time", "15:00");
		form.add("venueID", "1");
		form.add("description", "hello world");
		client.post().uri("/events/add_event").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(form).cookies(cookies -> {
			cookies.add(SESSION_KEY, tokens[1]);
		}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/events"));
		
		assertThat(currentRows + 1, equalTo(countRowsInTable("events")));

	}
	
	@Test
	@DirtiesContext
	public void addEventBlankName() {
		
		String[] tokens = login();
		
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "");
		form.add("date", "2024-01-01");
		form.add("time", "15:00");
		form.add("venueID", "1");
		form.add("description", "hello world");
		client.post().uri("/events/add_event").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(form).cookies(cookies -> {
			cookies.add(SESSION_KEY, tokens[1]);
		}).exchange().expectStatus().isOk().expectHeader()
		.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("An event must have a name."));
		});
		
		assertThat(currentRows , equalTo(countRowsInTable("events")));

	}
	
	@Test
	@DirtiesContext
	public void addEventBlankDate() {
		
		String[] tokens = login();
		
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "event name");
		form.add("date", "");
		form.add("time", "");
		form.add("venueID", "1");
		form.add("description", "hello world");
		client.post().uri("/events/add_event").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(form).cookies(cookies -> {
			cookies.add(SESSION_KEY, tokens[1]);
		}).exchange().expectStatus().isOk().expectHeader()
		.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("An event must have a date."));
		});
		
		assertThat(currentRows , equalTo(countRowsInTable("events")));

	}
	
	@Test
	@DirtiesContext
	public void addEventPastDate() {
		
		String[] tokens = login();
		
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "event name");
		form.add("date", "2023-01-01");
		form.add("time", "");
		form.add("venueID", "1");
		form.add("description", "hello world");
		client.post().uri("/events/add_event").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(form).cookies(cookies -> {
			cookies.add(SESSION_KEY, tokens[1]);
		}).exchange().expectStatus().isOk().expectHeader()
		.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("The date must be in the future."));
		});
		
		assertThat(currentRows , equalTo(countRowsInTable("events")));

	}
	
	
	@Test
	@DirtiesContext
	public void addEventLoggedOut() {
		
		String[] tokens = login();
		
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name");
		form.add("date", "2024-01-01");
		form.add("time", "15:00");
		form.add("venueID", "1");
		form.add("description", "hello world");
		client.post().uri("/events/add_event").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/sign-in"));
		
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	@DirtiesContext
	public void deleteEventLoggedIn() {
		String[] tokens = login();
		long id = 4;
		client.delete().uri("events/{id}", id).accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).cookies(cookies -> {
			cookies.add(SESSION_KEY, tokens[1]);
		}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/events"));
		
		assertThat(currentRows - 1, equalTo(countRowsInTable("events")));
	}
	
	@Test
	@DirtiesContext
	public void deleteEventLoggedOut() {
		String[] tokens = login();
		long id = 4;
		client.delete().uri("events/{id}", id).accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0])
		.exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/sign-in"));
		
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	@DirtiesContext
	public void deleteEventNotFound() {
		String[] tokens = login();
		long id = 99;
		client.delete().uri("events/{id}", id).accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).cookies(cookies -> {
			cookies.add(SESSION_KEY, tokens[1]);
		}).exchange().expectStatus().isNotFound();
		
		assertThat(currentRows, equalTo(countRowsInTable("events")));
	}
	
	@Test
	@DirtiesContext
	public void updateEventLoggedIn() {
		String[] tokens = login();
		long id = 4;
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name");
		form.add("venueId", "2");
		form.add("date", "2024-01-02");
		form.add("time", "16:00");
		form.add("description", "hello");
		client.post().uri("events/{id}", id).accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(form).cookies(cookies -> {
			cookies.add(SESSION_KEY, tokens[1]);
		}).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/events"));
		
		assertThat(currentRows, equalTo(countRowsInTable("events")));
		Event event = eventService.getEventById(id);
		
		assertThat(event.getName(), equalTo("name"));
		assertThat(event.getVenue().getId(), equalTo((long) 2));
		assertThat(event.getDate(), equalTo(LocalDate.parse("2024-01-02")));
		assertThat(event.getTime(), equalTo(LocalTime.parse("16:00")));
		assertThat(event.getDescription(), equalTo("hello"));

	}
	
	@Test
	@DirtiesContext
	public void updateEventLoggedOut() {
		String[] tokens = login();
		long id = 4;
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name2");
		form.add("venueId", "2");
		form.add("date", "2024-01-02");
		form.add("time", "16:00");
		form.add("description", "hello");
		client.post().uri("events/{id}", id).accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(form)
		.exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/sign-in"));
		
		assertThat(currentRows, equalTo(countRowsInTable("events")));
		Event event = eventService.getEventById(id);
		
		assertThat(event.getName(), not(equalTo("name2")));
		assertThat(event.getVenue().getId(), not(equalTo((long) 2)));
		assertThat(event.getDate(), not(equalTo(LocalDate.parse("2024-01-02"))));
		assertThat(event.getTime(), not(equalTo(LocalTime.parse("16:00"))));
		assertThat(event.getDescription(), not(equalTo("hello")));

	}
	
	@Test
	@DirtiesContext
	public void addEventLongName() {
		
		String[] tokens = login();
		
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaasssasasasasasasasasasasasasassssssssssssssssssssss");
		form.add("date", "2024-01-01");
		form.add("time", "15:00");
		form.add("venueID", "1");
		form.add("description", "hello world");
		client.post().uri("/events/add_event").accept(MediaType.TEXT_HTML).header(CSRF_HEADER, tokens[0]).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.bodyValue(form).cookies(cookies -> {
			cookies.add(SESSION_KEY, tokens[1]);
		}).exchange().expectStatus().isOk().expectHeader()
		.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
			assertThat(result.getResponseBody(), containsString("The name must be shorter than 256 characters."));
		});
		
		assertThat(currentRows , equalTo(countRowsInTable("events")));

	}
}
