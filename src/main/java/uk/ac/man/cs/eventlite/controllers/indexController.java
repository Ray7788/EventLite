package uk.ac.man.cs.eventlite.controllers;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.PriorityQueue;
import java.util.function.Function;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;



@Controller
@RequestMapping(value = "/", produces = { MediaType.TEXT_HTML_VALUE })
public class indexController {

	@Autowired
	private EventService eventService;
	
	@Autowired
	private VenueService venueService;


	@GetMapping
	public String getAllEvents(Model model) {
		Iterable<Venue> venues = venueService.findAll();
		PriorityQueue<Map.Entry<Venue, Integer>> venueQueue = new PriorityQueue<Map.Entry<Venue, Integer>>(Map.Entry.comparingByValue(Comparator.reverseOrder()));
		Integer freq;
		for(Venue v : venues) {
			freq = getFrequency(v);
			venueQueue.add(new AbstractMap.SimpleEntry<Venue, Integer>(v, freq));
		}
		ArrayList<Venue> venueList = new ArrayList<Venue>();
		for(int i = 0; i < 3; i++) {
			if(venueQueue.peek() == null) {
				break;
			}
			venueList.add(venueQueue.poll().getKey());	
		}
		
		model.addAttribute("futureEvents", eventService.findFuture());
		model.addAttribute("popularVenues", venueList);
		return "home/home";
	}
	
	private Integer getFrequency(Venue venue) {
		Iterable<Event> events = eventService.findAll();
		Integer count =  0;
		for(Event e: events) {
			if(e.getVenue().getId() == venue.getId()) {
				count += 1;
			}
		}
		return count;
	}

}
