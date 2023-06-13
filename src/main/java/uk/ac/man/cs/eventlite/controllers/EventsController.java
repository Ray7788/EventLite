package uk.ac.man.cs.eventlite.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.MastodonRequest;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Statuses;
import com.sys1yagi.mastodon4j.api.method.Timelines;

import okhttp3.OkHttpClient;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Timelines;

import okhttp3.OkHttpClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;
	
	@Autowired
	private VenueService venueService;

	@ExceptionHandler(EventNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String eventNotFoundHandler(EventNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "events/not_found";
	}
 
	@GetMapping("/{id}")
	public String getEvent(@PathVariable("id") long id, Model model) {
		Optional<Event> event = eventService.find(id);
		if (event.isEmpty()) {
			throw new EventNotFoundException(id);
		}
		else {
			model.addAttribute("event", event.get());
			return "events/event";
		}
	}

	@GetMapping
	public String getAllEvents(Model model) {
		List<Status> recentPosts = getMastodonPosts();
		model.addAttribute("mastodonPosts", recentPosts);
		model.addAttribute("formatLocalDateTime", (Function<String, String>) this::formatLocalDateTime);
		model.addAttribute("events", eventService.findAll());
		model.addAttribute("pastEvents", eventService.findPast());
		model.addAttribute("futureEvents", eventService.findFuture());
		return "events/index";
	}
	
	@GetMapping(value = "/add_event")
	public String addNewEvent(Model model) {
		Iterable<Venue> venues = venueService.findAll();
		model.addAttribute("venues", venues);
		if (!model.containsAttribute("event")) {
			model.addAttribute("event", new Event());

		}
		return "events/add_event";
	}
	
	@PostMapping(value = "/add_event", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createNewEvent(@RequestBody @Valid @ModelAttribute Event event, BindingResult errors, Model model,  RedirectAttributes redirectAttributes, @RequestParam long venueID) {
		if (errors.hasErrors() && (errors.getErrorCount() - errors.getFieldErrorCount("venue"))>0) {
			Iterable<Venue> venues = venueService.findAll();
			model.addAttribute("venues", venues);
			return "events/add_event";
		}
		event.setVenue(venueService.getbyId(venueID));
		eventService.save(event);
        redirectAttributes.addFlashAttribute("ok_message", "Added the new event successfully.");

		return "redirect:/events";
	}

	@GetMapping("/searchEvents")
	public String getEventByName(Model model, @RequestParam("name") String name) {
		Iterable<Event> listPastEvents = eventService.findPast(name);
		Iterable<Event> listFutureEvents = eventService.findFuture(name);
		model.addAttribute("pastEvents", listPastEvents);
		model.addAttribute("futureEvents", listFutureEvents);
		return "events/index";
	}
	
	@DeleteMapping("/{id}")
	public String deleteEvent(@PathVariable("id") long eid, RedirectAttributes redirectAttrs) {
		if (!eventService.existsById(eid)) {
			throw new EventNotFoundException(eid);
		}

		eventService.deleteById(eid);
		redirectAttrs.addFlashAttribute("ok_message", "Event deleted.");

		return "redirect:/events";
	}
	
	@GetMapping("/edit/{id}")
	public String updateEventForm(@PathVariable Long id, Model model) {
		Iterable<Venue> venues = venueService.findAll();
		model.addAttribute("venues",venues);
		model.addAttribute("event", eventService.getEventById(id));
		return "events/edit_event";
	}
	
	@PostMapping("/{id}")
	public String updateEvent(@RequestParam Long venueId, @PathVariable Long id, @Valid Event event, BindingResult result, Model model) {
		
		if (result.hasErrors() && (result.getErrorCount() - result.getFieldErrorCount("venue")) > 0){
			Iterable<Venue> venues = venueService.findAll();
			event.setVenue(venueService.getbyId(venueId));
			model.addAttribute("event",event);
			model.addAttribute("venues",venues);
		    return "events/edit_event";
		  }
		Event existingEvent = eventService.getEventById(id);
		existingEvent.setId(id);
		existingEvent.setName(event.getName());
		existingEvent.setVenue(venueService.getbyId(venueId));
		existingEvent.setDate(event.getDate());
		existingEvent.setTime(event.getTime());
		existingEvent.setDescription(event.getDescription());
		eventService.updateEvent(existingEvent);
		return "redirect:/events";
	
	}
	
	@PostMapping("/mastPost/{id}")
	public String mastPost(@PathVariable("id") Long id, Model model, @RequestParam("content") String content, RedirectAttributes redirectattributes)throws Mastodon4jRequestException {
		
		String accessToken = "MxI5vPqzyLGcqVZN_4iv2HbqjPu9INiDwk_xK_v4Cuk";
        MastodonClient client = new MastodonClient.Builder("universeodon.com", new OkHttpClient.Builder(), new Gson()) 
        		.accessToken(accessToken)
                .useStreamingApi()
                .build();
                
		Event event = eventService.getEventById(id);
		model.addAttribute("event", event); 		
		model.addAttribute("content", content);
		String error = "0";
        
        Statuses status = new Statuses(client);
        try {
        	status.postStatus(content, null, null, false, null, Status.Visibility.Unlisted).execute();
        } catch(Exception e) {
        	System.out.println(e);
        	error = "1";
        }        
        model.addAttribute("error", error);
        
		return "events/event";
	}
		
	
	
	private List<Status> getMastodonPosts(){
	    List<Status> recentPosts = new ArrayList<>();
		String accessToken = "MxI5vPqzyLGcqVZN_4iv2HbqjPu9INiDwk_xK_v4Cuk";
        MastodonClient client = new MastodonClient.Builder("universeodon.com", new OkHttpClient.Builder(), new Gson()) 
        		.accessToken(accessToken)
                .useStreamingApi()
                .build();

	    Timelines timelines = new Timelines(client);
	    
	    try {
	        Pageable<Status> homeTimeline = timelines.getHome().execute();
	        List<Status> statuses = homeTimeline.getPart();
	        
	        for (Status status : statuses) {
	        	if (Long.toString(status.getAccount().getId()).equals("110264637283349858")) {
		            // Filtering logic to fetch the required posts (if any)
		            recentPosts.add(status);
		            if (recentPosts.size() == 3) {
		                break;
		            }
	        	}
	        }
	    } catch (Mastodon4jRequestException e) {
	    }
	    
	    Collections.sort(recentPosts, Comparator.comparing(Status::getCreatedAt).reversed());
	    return recentPosts;
	}
	
	public String formatLocalDateTime(String dateTimeStr) {
	    Instant instant = Instant.parse(dateTimeStr);
	    LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
	    return localDateTime.toString();
	}
}
