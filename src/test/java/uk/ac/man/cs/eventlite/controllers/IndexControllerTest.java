package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;


@ExtendWith(SpringExtension.class)
@WebMvcTest(indexController.class)
@Import(Security.class)

public class IndexControllerTest {
	
	@Autowired
	private MockMvc mvc;
	
	@Mock
	private Event event;

	@Mock
	private Venue venue;
	
	@MockBean
	private VenueService venueService;
	
	@MockBean
	private EventService eventService;
	
	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findFuture()).thenReturn(Collections.<Event>emptyList());

		mvc.perform(get("/").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("home/home")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findFuture();
		verifyNoInteractions(event);
		verifyNoInteractions(venue);
	}
	@Test
	public void getIndexWhenNoVenues() throws Exception {
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

		mvc.perform(get("/").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("home/home")).andExpect(handler().methodName("getAllEvents"));

		verify(venueService).findAll();
		verifyNoInteractions(event);
		verifyNoInteractions(venue);
	}
	@Test
	public void getIndex() throws Exception {
		when(venue.getName()).thenReturn("Kilburn Building");
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));

		when(event.getVenue()).thenReturn(venue);
		when(eventService.findFuture()).thenReturn(Collections.<Event>singletonList(event));

		mvc.perform(get("/").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("home/home")).andExpect(handler().methodName("getAllEvents"));

		verify(venueService).findAll();
		verify(eventService, times(1)).findFuture();
		
		
		
	}


}
