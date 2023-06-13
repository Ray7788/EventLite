package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

@RestController
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {

	private static final String NOT_FOUND_MSG = "{ \"error\": \"%s\", \"id\": %d }";

	@Autowired
	private VenueService venueService;
	@Autowired
	private EventService eventService;
	@Autowired
	private VenueModelAssembler venueAssembler;
	@Autowired
	private EventModelAssembler eventAssembler;

	@ExceptionHandler(VenueNotFoundException.class)
	public ResponseEntity<?> venueNotFoundHandler(VenueNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(String.format(NOT_FOUND_MSG, ex.getMessage(), ex.getId()));
	}

	@GetMapping("/{id}")
	public EntityModel<Venue> getVenue(@PathVariable("id") long id) {
		Venue venue = venueService.find(id).orElseThrow(() -> new VenueNotFoundException(id));
		return venueAssembler.toModel(venue);
	}

	@GetMapping
	public CollectionModel<EntityModel<Venue>> getAllVenues() {
		return venueAssembler.toCollectionModel(venueService.findAll())
				.add(linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel())
				.add(linkTo(Profile.class).slash("/api/profile/venues").withRel("profile"));
	}
	
	@GetMapping("/{id}/events")
	public CollectionModel<EntityModel<Event>> getVenueEvents(@PathVariable("id") long id) {
		//return eventAssembler.toCollectionModel(something that gets
		//all events from a venue, use service classes or something?);
		Venue venue = venueService.getbyId(id); //gets the venue name
		Iterable<Event> venueEvents = eventService.findVenueEvents(venue); //gets the venue events based on the venue name
		return eventAssembler.toCollectionModel(venueEvents);
	}
	
	@GetMapping("/{id}/next3events")
	public CollectionModel<EntityModel<Event>> getVenueNext3Events(@PathVariable("id") long id) {
		// best approach is to get all the future events and if it has the same venue id 
		//as the id of the method then add it to a list until it is max 3
		//return eventAssembler.toCollectionModel
		//(something that gets the next 3 events from a venue)
		Venue venue = venueService.getbyId(id); //gets the venue name
		Iterable<Event> futureEvents = eventService.findFutureByVenue(venue);
		List<Event> topThreeEvents = new ArrayList<>();
		for (Event event: futureEvents) {
			if (topThreeEvents.size()==3) {
				break;
			}
			topThreeEvents.add(event);
		}
		return eventAssembler.toCollectionModel(topThreeEvents);
	}

}
