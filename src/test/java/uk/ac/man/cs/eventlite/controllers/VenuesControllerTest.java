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
@WebMvcTest(VenuesController.class)
@Import(Security.class)
public class VenuesControllerTest {

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
	public void getIndexWhenNoVenues() throws Exception {
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

		verify(venueService).findAll();
		verifyNoInteractions(event);
		verifyNoInteractions(venue);
	}

	@Test
	public void getIndexWithVenues() throws Exception {
		when(venue.getName()).thenReturn("Kilburn Building");
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));

		when(event.getVenue()).thenReturn(venue);
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

		verify(venueService).findAll();
	}

	@Test
	public void getVenueNotFound() throws Exception {
		
		
		mvc.perform(get("/venues/99").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound())
				.andExpect(view().name("venues/not_found")).andExpect(handler().methodName("getVenue"));
	}
	
	@Test
    public void testGetVenueWithValidId() throws Exception {
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
		
		List<Event> futureEvents = Arrays.asList(e);
		
		when(venueService.find(id)).thenReturn(Optional.of(v));
		when(eventService.findFutureByVenue(v)).thenReturn(futureEvents);
		
        mvc.perform(get("/venues/{id}", id))
               .andExpect(status().isOk())
               .andExpect(view().name("venues/venue"))
               .andExpect(handler().methodName("getVenue"))
               .andExpect(model().attribute("venue", v))
               .andExpect(model().attribute("events", futureEvents));
    }
	
	@Test
    public void addVenueValid() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name");
		form.add("capacity", "10");
		form.add("roadName", "oxford");
		form.add("postcode", "M14");
		form.add("cityName", "Manchester");
		
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
		
        when(venueService.save(v)).thenReturn(v);
		
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound());
		
		verify(venueService).save(any());
		

    }
	
	@Test
    public void addVenueNoName() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "");
		form.add("capacity", "10");
		form.add("roadName", "oxford");
		form.add("postcode", "M14");
		form.add("cityName", "Manchester");
		
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
		
        when(venueService.save(v)).thenReturn(v);
		
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("venue"));
		
		verifyNoInteractions(venueService);
		

    }
	
	@Test
    public void addVenueNoCapacity() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name");
		form.add("capacity", "");
		form.add("roadName", "oxford");
		form.add("postcode", "M14");
		form.add("cityName", "Manchester");
		
		
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
		
        when(venueService.save(v)).thenReturn(v);
		
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("venue"));
		
		verifyNoInteractions(venueService);

    }
	
	@Test
    public void addVenueNoRoad() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name");
		form.add("capacity", "10");
		form.add("roadName", "");
		form.add("postcode", "M14");
		form.add("cityName", "Manchester");
		
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
		
        when(venueService.save(v)).thenReturn(v);
		
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("venue"));
		
		verifyNoInteractions(venueService);

    }
	
	@Test
    public void addVenueNoPost() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name");
		form.add("capacity", "10");
		form.add("roadName", "oxford");
		form.add("postcode", "");
		form.add("cityName", "Manchester");
		
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
		
        when(venueService.save(v)).thenReturn(v);
		
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("venue"));
		
		verifyNoInteractions(venueService);
    }
	
	@Test 
	public void addBadPostcode() throws Exception{
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name");
		form.add("capacity", "10");
		form.add("roadName", "oxford");
		form.add("postcode", ";");
		form.add("cityName", "Manchester");
		
		long id = 1;
        Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode(";");
		v.setCityName("Manchester");
		v.setAddress();
		
        when(venueService.save(v)).thenReturn(v);
		
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(view().name("venues/add_venue_invalid_address")).andExpect(status().isOk());
		
		verifyNoInteractions(venueService);
	}


	
	@Test
    public void addVenueLongName() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "bjcoetfwpuyujlbczwqsfvsqqlnxnrhcivlddecgwaqwxuvtvsgdsvyrwjcertxycbvrmkxnqccdervzwcpznihbferyzzzdxgndeibimztcfqmmhjrsmynphvoltwalkgzmwkxoeohzbivmmnhmenddtihcrrcstmrztynqctsueckppgjlymgeygshkkaftijthqltcefjhsfzcqkexnrvmughksmsvwxxqyditghjoxnjxgatxtwjylrdyxbeklccxfhuxucjppcdcidaykzvwrtmfvysvedablrqznowpkjlojdwprkrjtjmclegbxfqvrwrbvcqgusus");
		form.add("capacity", "10");
		form.add("roadName", "oxford");
		form.add("postcode", "M14");
		form.add("cityName", "Manchester");
		
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
		
        when(venueService.save(v)).thenReturn(v);
		
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("venue"));
		
		verifyNoInteractions(venueService);
		

    }
	
	
	@Test
    public void updateVenueValid() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name");
		form.add("capacity", "10");
		form.add("roadName", "oxford");
		form.add("postcode", "M14");
		form.add("cityName", "Manchester");
		
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
		
        when(venueService.updateVenue(v)).thenReturn(v);
        when(venueService.getbyId(id)).thenReturn(v);
		
		mvc.perform(post("/venues/{id}", id).with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound());
		
		verify(venueService).updateVenue(any());
		

    }
	
	@Test
    public void updateVenueBadPostcode() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "Kilburn");
		form.add("capacity", "10");
		form.add("roadName", "oxford");
		form.add("postcode", ";");
		form.add("cityName", "Manchester");
		
		long id = 1;
        Venue v = new Venue();
		v.setId(id);
		v.setCapacity(10);
		v.setRoadName("kilburn");
		v.setName("tootill");
		v.setPostcode("m11a1");
		v.setCityName("Manchester");
		v.setAddress();
		
		
		
        when(venueService.updateVenue(v)).thenReturn(v);
        when(venueService.getbyId(id)).thenReturn(v);
		
		mvc.perform(post("/venues/{id}", id).with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(view().name("venues/edit_venue_invalid_address"));
		
		verify(venueService, Mockito.never()).updateVenue(v);
		

    }
	
	@Test
    public void updateVenueNoName() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "");
		form.add("capacity", "10");
		form.add("roadName", "oxford");
		form.add("postcode", "M14");
		form.add("cityName", "Manchester");
		
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
		
        when(venueService.updateVenue(v)).thenReturn(v);
        when(venueService.getbyId(id)).thenReturn(v);
		
		mvc.perform(post("/venues/{id}", id).with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("venue"));
		
		verify(venueService, Mockito.never()).updateVenue(v);
		

    }
	
	@Test
    public void updateVenueNoCapacity() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name");
		form.add("capacity", "");
		form.add("roadName", "oxford");
		form.add("postcode", "M14");
		form.add("cityName", "Manchester");
		
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
		
        when(venueService.updateVenue(v)).thenReturn(v);
        when(venueService.getbyId(id)).thenReturn(v);
		
		mvc.perform(post("/venues/{id}", id).with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("venue"));
		
		verify(venueService, Mockito.never()).updateVenue(v);

    }
	
	@Test
    public void updateVenueNoRoad() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name");
		form.add("capacity", "10");
		form.add("roadName", "");
		form.add("postcode", "M14");
		
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
		
        when(venueService.updateVenue(v)).thenReturn(v);
        when(venueService.getbyId(id)).thenReturn(v);
        
		mvc.perform(post("/venues/{id}", id).with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("venue"));
		
		verify(venueService, Mockito.never()).updateVenue(v);

    }
	
	@Test
    public void updateVenueNoPost() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "name");
		form.add("capacity", "10");
		form.add("roadName", "oxford");
		form.add("postcode", "");
		
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
		
        when(venueService.updateVenue(v)).thenReturn(v);
        when(venueService.getbyId(id)).thenReturn(v);
		
		mvc.perform(post("/venues/{id}", id).with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("venue"));
		
		verify(venueService, Mockito.never()).updateVenue(v);
    }
	
	@Test
    public void updateVenueLongName() throws Exception {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "bjcoetfwpuyujlbczwqsfvsqqlnxnrhcivlddecgwaqwxuvtvsgdsvyrwjcertxycbvrmkxnqccdervzwcpznihbferyzzzdxgndeibimztcfqmmhjrsmynphvoltwalkgzmwkxoeohzbivmmnhmenddtihcrrcstmrztynqctsueckppgjlymgeygshkkaftijthqltcefjhsfzcqkexnrvmughksmsvwxxqyditghjoxnjxgatxtwjylrdyxbeklccxfhuxucjppcdcidaykzvwrtmfvysvedablrqznowpkjlojdwprkrjtjmclegbxfqvrwrbvcqgusus");
		form.add("capacity", "10");
		form.add("roadName", "oxford");
		form.add("postcode", "M14");
		
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
		
        when(venueService.updateVenue(v)).thenReturn(v);
        when(venueService.getbyId(id)).thenReturn(v);
		
		mvc.perform(post("/venues/{id}", id).with(user("Rob").roles(Security.ADMIN_ROLE))
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).params(form)
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("venue"));
		
		verify(venueService, Mockito.never()).updateVenue(v);
		

    }
	
	@Test
	public void deleteVenue() throws Exception {
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
		
		Iterable<Event> eventsList = Collections.emptyList();
		
		when(venueService.find(id)).thenReturn(Optional.of(v));
		
		when(eventService.findFutureByVenue(v)).thenReturn(eventsList);
		
		mvc.perform(delete("/venues/{id}",id).with(user("Rob").roles(Security.ADMIN_ROLE))
				.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound());
		verify(venueService).find(id);
		verify(venueService).deleteById(id);
		verify(eventService).findFutureByVenue(v);
	}
	
	@Test
	public void deleteVenueDoesntExist() throws Exception {
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
		
		when(venueService.find(id)).thenReturn(Optional.ofNullable(null));
		
		mvc.perform(delete("/venues/{id}",id).with(user("Rob").roles(Security.ADMIN_ROLE))
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isNotFound())
		.andExpect(view().name("venues/not_found")).andExpect(handler().methodName("deleteVenue"));
		verify(venueService).find(id);
		verify(venueService, Mockito.never()).deleteById(id);
	}
	
	@Test
	public void searchVenue() throws Exception {
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
		
		List<Venue> venues = Arrays.asList(v);
		
		when(venueService.search("venue")).thenReturn(venues);
		
		mvc.perform(get("/venues/searchVenues").accept(MediaType.TEXT_HTML)
		.contentType(MediaType.APPLICATION_FORM_URLENCODED).param("name", "venue").with(csrf())).andExpect(status().isOk())
		.andExpect(view().name("venues/index")).andExpect(handler().methodName("getVenueByName")).andExpect(model().attribute("venues", venues));
		
		verify(venueService).search("venue");
	}
	
	@Test
	public void updateVenueForm() throws Exception {
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
		
		when(venueService.getbyId((long) 1)).thenReturn(v);
		
		mvc.perform(get("/venues/edit/{id}", id).with(user("Rob").roles(Security.ADMIN_ROLE))
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk()).andExpect(model().attribute("venue", v));
		
		verify(venueService).getbyId(id);
	}
	
	@Test
	public void addVenueForm() throws Exception {
		long id = 1;
		
		mvc.perform(get("/venues/add_venue", id).with(user("Rob").roles(Security.ADMIN_ROLE))
		.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isOk());
		
	}
	
	
}
