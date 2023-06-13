package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesControllerApi.class)
@Import({ Security.class, VenueModelAssembler.class, EventModelAssembler.class})
public class VenuesControllerApiTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private EventService eventService;
	
	@MockBean
	private VenueService venueService;
	
	@Test
	public void getIndexWhenNoVenues() throws Exception {
		
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());
		
		mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllVenues")).andExpect(jsonPath("$.length()", equalTo(1)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")));
		verify(venueService).findAll();

	}
	
	@Test
	public void getIndexWithVenues() throws Exception {
		long id = 1;
		Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setCityName("Manchester");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(v));

		mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllVenues")).andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")))
				.andExpect(jsonPath("$._embedded.venues.length()", equalTo(1)));

		verify(venueService).findAll();
	}
	
	@Test
	public void getVenueNotFound() throws Exception {
		
		when(venueService.find((long) 99)).thenReturn(Optional.ofNullable(null));
		
		mvc.perform(get("/api/venues/99").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error", containsString("venue 99"))).andExpect(jsonPath("$.id", equalTo(99)))
				.andExpect(handler().methodName("getVenue"));
		verify(venueService).find((long) 99);
	}
	
	@Test
	public void getVenue() throws Exception {
		long id = 1;
		Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setCityName("Manchester");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
		when(venueService.find(id)).thenReturn(Optional.of(v));
		
		mvc.perform(get("/api/venues/1").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON))
		.andExpect(jsonPath("$.id", equalTo(1)));;
		
		verify(venueService).find(id);
	}
	
	@Test
	public void getVenueEvents() throws Exception {
		long id = 1;
		Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setCityName("Manchester");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
		Iterable<Event> iterable = Collections.singleton(e);
		
		when(venueService.getbyId(id)).thenReturn(v);
		when(eventService.findVenueEvents(v)).thenReturn(iterable);
		
		mvc.perform(get("/api/venues/1/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON));
		
		verify(venueService).getbyId(id);
		verify(eventService).findVenueEvents(v);
	}
	
	@Test
	public void getVenue3Events() throws Exception {
		long id = 1;
		Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setCityName("Manchester");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
		Iterable<Event> iterable = Collections.singleton(e);
		
		when(venueService.getbyId(id)).thenReturn(v);
		when(eventService.findFutureByVenue(v)).thenReturn(iterable);
		
		mvc.perform(get("/api/venues/1/next3events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON));
		
		verify(venueService).getbyId(id);
		verify(eventService).findFutureByVenue(v);
	}
}