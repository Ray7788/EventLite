package uk.ac.man.cs.eventlite.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.Optional;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

	
	@Autowired
	private VenueService venueService;
	
	@Autowired
	private EventService eventService;

	@ExceptionHandler(VenueNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String venueNotFoundHandler(VenueNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "venues/not_found";
	}

	@GetMapping("/{id}")
	public String getVenue(@PathVariable("id") long id, Model model) {
		Optional<Venue> venue = venueService.find(id);
		if (venue.isEmpty()) {
			throw new VenueNotFoundException(id);
		}
		else {
			Venue venue1 = venue.get();
			model.addAttribute("venue", venue1);
			model.addAttribute("events", eventService.findFutureByVenue(venue1));
			return "venues/venue";
		}
	}

	@RequestMapping(method = RequestMethod.GET)
	public String getAllVenues(Model model) {
		model.addAttribute("venues", venueService.findAll());
	    return "venues/index";
	}
	
	@RequestMapping(value = "/add_venue", method = RequestMethod.GET)
	public String newVenue(Model model) {
		if (!model.containsAttribute("venue")) {
			model.addAttribute("venue", new Venue());
		}
		return "venues/add_venue";
	}
	
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String createVenue(@RequestBody @Valid @ModelAttribute Venue venue,
		BindingResult errors, Model model, RedirectAttributes redirectAttrs) {
		venue.setAddress();
		venue.setCoordinates();
		if (venue.getLongitude() == 600 && venue.getLatitude() == 600) {
			model.addAttribute("venue", venue);
			return "venues/add_venue_invalid_address";
		}else{
			if (errors.hasErrors()) {
				model.addAttribute("venue", venue);
				return "venues/add_venue";
			}
		}
		venueService.save(venue);
		redirectAttrs.addFlashAttribute("ok_message", "Added new venue succefully.");

		return "redirect:/venues";
	}
	
	@DeleteMapping("/{id}")
	public String deleteVenue(@PathVariable("id") long vid, RedirectAttributes redirectAttrs) {
		Optional<Venue> venue = venueService.find(vid);
		if (venue.isEmpty()) {
			throw new VenueNotFoundException(vid);
		}else {
			Venue this_venue = venue.get();
			if (!(eventService.findFutureByVenue(this_venue).iterator().hasNext())) {
				venueService.deleteById(vid);
				redirectAttrs.addFlashAttribute("ok_message", "Venue deleted.");
		
				return "redirect:/venues";
				}
		}
		return "redirect:/venues/{id}";
		
	}

	@GetMapping("/searchVenues")
    public String getVenueByName(Model model, @RequestParam("name") String name) {
        Iterable<Venue> listVenues = venueService.search(name);
        model.addAttribute("venues", listVenues);
        return "venues/index";
    }
	
	@GetMapping("/edit/{id}")
	public String updateVenueForm(@PathVariable Long id, Model model) {
		model.addAttribute("venue", venueService.getbyId(id));
		return "venues/edit_venue";
	}
	
	@PostMapping("/{id}")
	public String updateVenue(@PathVariable Long id, @Valid @ModelAttribute Venue venue, BindingResult errors, Model model) {
		Venue existingVenue = venueService.getbyId(id);
		existingVenue.setId(id);
		existingVenue.setName(venue.getName());
		existingVenue.setCapacity(venue.getCapacity());
		existingVenue.setPostcode(venue.getPostcode());
		existingVenue.setRoadName(venue.getRoadName());
		existingVenue.setCityName(venue.getCityName());
		existingVenue.setAddress();
		existingVenue.setCoordinates();
		if (existingVenue.getLongitude() == 600 && existingVenue.getLatitude() == 600) {
			model.addAttribute("venue", venue);
			return "venues/edit_venue_invalid_address";
		}else{
			if (errors.hasErrors()) {
				model.addAttribute("venue", venue);
				return "venues/edit_venue";
			}
		}
		venueService.updateVenue(existingVenue);
		return "redirect:/venues";

	}
}
