package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsController.class)
@Import(Security.class)
public class EventsControllerTest {

	@Autowired
	private MockMvc mvc;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

	@MockBean
	private EventService eventService;

	@MockBean
	private VenueService venueService;

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
//		verify(venueService).findAll();
		verifyNoInteractions(event);
		verifyNoInteractions(venue);
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		when(venue.getName()).thenReturn("Kilburn Building");
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));

		when(event.getVenue()).thenReturn(venue);
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAll();
	}

	@Test
	public void getEventNotFound() throws Exception {
		mvc.perform(get("/events/99").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound())
				.andExpect(view().name("events/not_found")).andExpect(handler().methodName("getEvent"));
	}
	
	@Test
    public void testGetEventWithValidId() throws Exception {
		long id = 1;
        Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
		when(eventService.find(id)).thenReturn(Optional.of(e));
        mvc.perform(get("/events/{id}", id))
               .andExpect(status().isOk())
               .andExpect(view().name("events/event"))
               .andExpect(handler().methodName("getEvent"));
    }
	
	@Test
    public void addEventValid() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name");
		form.add("venueID", "1");
		form.add("date", "2024-01-02");
		form.add("time", "16:00");
		form.add("description", "hello");
		
		long id = 1;
        Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
        when(venueService.getbyId((long) 1)).thenReturn(v);
        when(eventService.save(e)).thenReturn(e);
		
		mvc.perform(post("/events/add_event").with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound());
		
		verify(eventService).save(any());
		

    }
	
	@Test
    public void addEventNoName() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "");
		form.add("venueID", "1");
		form.add("date", "2024-01-02");
		form.add("time", "16:00");
		form.add("description", "hello");
		
		long id = 1;
        Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
        when(venueService.getbyId((long) 1)).thenReturn(v);
        when(eventService.save(e)).thenReturn(e);
		
		mvc.perform(post("/events/add_event").with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("event"));
		
		verifyNoInteractions(eventService);
		

    }
	
	@Test
    public void addEventNoVenue() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name");
		form.add("venueID", "");
		form.add("date", "2024-01-02");
		form.add("time", "16:00");
		form.add("description", "hello");
		
		long id = 1;
        Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
        when(venueService.getbyId((long) 1)).thenReturn(v);
        when(eventService.save(e)).thenReturn(e);
		
		mvc.perform(post("/events/add_event").with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isBadRequest());
		
		verifyNoInteractions(eventService);
		

    }
	
	@Test
    public void addEventNoDate() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name");
		form.add("venueID", "1");
		form.add("date", "");
		form.add("time", "16:00");
		form.add("description", "hello");
		
		long id = 1;
        Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
        when(venueService.getbyId((long) 1)).thenReturn(v);
        when(eventService.save(e)).thenReturn(e);
		
		mvc.perform(post("/events/add_event").with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("event"));
		
		verifyNoInteractions(eventService);
		

    }
	
	@Test
    public void addEventLongName() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "bjcoetfwpuyujlbczwqsfvsqqlnxnrhcivlddecgwaqwxuvtvsgdsvyrwjcertxycbvrmkxnqccdervzwcpznihbferyzzzdxgndeibimztcfqmmhjrsmynphvoltwalkgzmwkxoeohzbivmmnhmenddtihcrrcstmrztynqctsueckppgjlymgeygshkkaftijthqltcefjhsfzcqkexnrvmughksmsvwxxqyditghjoxnjxgatxtwjylrdyxbeklccxfhuxucjppcdcidaykzvwrtmfvysvedablrqznowpkjlojdwprkrjtjmclegbxfqvrwrbvcqgusus");
		form.add("venueID", "1");
		form.add("date", "2024-01-02");
		form.add("time", "16:00");
		form.add("description", "describe");
		
		long id = 1;
        Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
        when(venueService.getbyId((long) 1)).thenReturn(v);
        when(eventService.save(e)).thenReturn(e);
		
		mvc.perform(post("/events/add_event").with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("event"));
		
		verifyNoInteractions(eventService);
		

    }
	
	@Test
    public void addEventLongDescription() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name");
		form.add("venueID", "1");
		form.add("date", "2024-01-02");
		form.add("time", "16:00");
		form.add("description", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		
		long id = 1;
        Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
        when(venueService.getbyId((long) 1)).thenReturn(v);
        when(eventService.save(e)).thenReturn(e);
		
		mvc.perform(post("/events/add_event").with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("event"));
		
		verifyNoInteractions(eventService);
	
	}
	
	@Test
	public void updateEventValid() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name");
		form.add("venueId", "1");
		form.add("date", "2024-01-02");
		form.add("time", "16:00");
		form.add("description", "hello");
		
		long id = 1;
        Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
		when(eventService.getEventById(id)).thenReturn(e);
		
		mvc.perform(post("/events/{id}",id).with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound());
		
	}
	
	@Test
	public void updateEventNoName() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "");
		form.add("venueId", "1");
		form.add("date", "2024-01-02");
		form.add("time", "16:00");
		form.add("description", "hello");
		
		long id = 1;
        Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
		when(eventService.getEventById(id)).thenReturn(e);
		
		mvc.perform(post("/events/{id}",id).with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("event"));
		
		verifyNoInteractions(eventService);
	}
	
	@Test
	public void updateEventNoDate() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name");
		form.add("venueId", "1");
		form.add("date", "");
		form.add("time", "16:00");
		form.add("description", "hello");
		
		long id = 1;
        Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
		when(eventService.getEventById(id)).thenReturn(e);
		
		mvc.perform(post("/events/{id}",id).with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("event"));
		
		verifyNoInteractions(eventService);
	}
	
	@Test
	public void updateEventLongName() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "bjcoetfwpuyujlbczwqsfvsqqlnxnrhcivlddecgwaqwxuvtvsgdsvyrwjcertxycbvrmkxnqccdervzwcpznihbferyzzzdxgndeibimztcfqmmhjrsmynphvoltwalkgzmwkxoeohzbivmmnhmenddtihcrrcstmrztynqctsueckppgjlymgeygshkkaftijthqltcefjhsfzcqkexnrvmughksmsvwxxqyditghjoxnjxgatxtwjylrdyxbeklccxfhuxucjppcdcidaykzvwrtmfvysvedablrqznowpkjlojdwprkrjtjmclegbxfqvrwrbvcqgusus");
		form.add("venueId", "1");
		form.add("date", "");
		form.add("time", "16:00");
		form.add("description", "hello");
		
		long id = 1;
        Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
		when(eventService.getEventById(id)).thenReturn(e);
		
		mvc.perform(post("/events/{id}",id).with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("event"));
		
		verifyNoInteractions(eventService);
	}
	
	@Test
	public void updateEventLongDescription() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name");
		form.add("venueId", "1");
		form.add("date", "");
		form.add("time", "16:00");
		form.add("description", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		
		long id = 1;
        Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
		when(eventService.getEventById(id)).thenReturn(e);
		
		mvc.perform(post("/events/{id}",id).with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("event"));
		
		verifyNoInteractions(eventService);
	}
	
	@Test
	public void deleteEvent() throws Exception {
		long id = 1;
		
		Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
		
		when(eventService.existsById(id)).thenReturn(true);
		
		mvc.perform(delete("/events/{id}",id).with(user("Rob").roles(Security.ADMIN_ROLE))
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound());
		verify(eventService).deleteById(id);
		verify(eventService).existsById(id);
	}
	
	@Test
	public void deleteEventDoesntExist() throws Exception {
		long id = 1;
		
		Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
		
		when(eventService.existsById(id)).thenReturn(false);
		
		mvc.perform(delete("/events/{id}",id).with(user("Rob").roles(Security.ADMIN_ROLE))
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isNotFound())
		.andExpect(view().name("events/not_found")).andExpect(handler().methodName("deleteEvent"));
		verify(eventService).existsById(id);
		verify(eventService, Mockito.never()).deleteById(id);
	}
	
	@Test
	public void searchEvent() throws Exception {
		long id = 1;
		
		Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
		List<Event> futureEvents = Arrays.asList(e);
		when(eventService.findFuture("event")).thenReturn(futureEvents);
		when(eventService.findPast("event")).thenReturn(futureEvents);
		
		mvc.perform(get("/events/searchEvents").accept(MediaType.TEXT_HTML)
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("name", "event").with(csrf())).andExpect(status().isOk())
		.andExpect(view().name("events/index")).andExpect(handler().methodName("getEventByName")).andExpect(model().attribute("pastEvents", futureEvents))
        .andExpect(model().attribute("futureEvents", futureEvents));
		
		verify(eventService).findFuture("event");
		verify(eventService).findPast("event");
	}
	
	@Test
	public void updateEventForm() throws Exception {
		long id = 1;
		
		Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
		List<Venue> venues = Arrays.asList(v);
		when(venueService.findAll()).thenReturn(venues);
		when(eventService.getEventById((long) 1)).thenReturn(e);
		
		mvc.perform(get("/events/edit/{id}", id).with(user("Rob").roles(Security.ADMIN_ROLE))
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attribute("venues", venues))
        .andExpect(model().attribute("event", e));
		
		verify(venueService).findAll();
		verify(eventService).getEventById(id);
	}
	
	@Test
	public void addEventForm() throws Exception {
		long id = 1;
		
		Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setAddress();
		
		Event e = new Event();
		e.setId(id);
		e.setDate(LocalDate.of(2021, 4, 19));
		e.setTime(LocalTime.of(19, 11));
		e.setName("Event 1");
		e.setDescription("cool");
		e.setVenue(v);
		
		List<Venue> venues = Arrays.asList(v);
		when(venueService.findAll()).thenReturn(venues);
		
		mvc.perform(get("/events/add_event", id).with(user("Rob").roles(Security.ADMIN_ROLE))
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attribute("venues", venues));
		
		verify(venueService).findAll();
	}
	
	
}
