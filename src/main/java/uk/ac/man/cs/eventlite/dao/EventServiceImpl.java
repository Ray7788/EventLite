package uk.ac.man.cs.eventlite.dao;

import org.springframework.stereotype.Service;


import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
@Service
public class EventServiceImpl implements EventService {

	@Autowired
	private EventRepository eventRepository;

	@Override
	public long count() {
		return eventRepository.count();
	}

	@Override
	public Iterable<Event> findAll() {
		
		return eventRepository.findAllByOrderByDateAscTimeAsc();
	}
	
	
	@Override
	public Iterable<Event> findPast(){
		Iterable<Event> allEvents = eventRepository.findAllByOrderByDateDescNameAsc();
		List<Event> filteredEvents = new ArrayList<>();
		for (Event event: allEvents) {
			if (LocalDate.now().isAfter(event.getDate())) {
				filteredEvents.add(event);
			}
		}
		return filteredEvents;
	}
	
	@Override
	public Iterable<Event> findFuture(){
		Iterable<Event> allEvents = eventRepository.findAllByOrderByDateAscNameAsc();
		List<Event> filteredEvents = new ArrayList<>();
		for (Event event: allEvents) {
			if (LocalDate.now().isBefore(event.getDate())) {
				filteredEvents.add(event);
			}
		}
		return filteredEvents;
	}
	
	@Override
	public Iterable<Event> findPast(String name){
		LocalDate currentDate = LocalDate.now();
	    String[] noTrailingWhiteSpaceWord = name.toLowerCase().split("\\s+"); // " name " -> ["name"]
		
		
		Iterable<Event> allResults =eventRepository.findByNameContainingIgnoreCaseOrderByDateDescNameAsc(name); //all the results
		List<Event> correctResults = new ArrayList<>(); //filtered results
		
		for (Event event : allResults) { //go through the results
			boolean match = true;
			String[] AllWordsInName = event.getName().toLowerCase().split("\\s+"); //"Kilburn Building" -> ["Kilburn", "Building"]
			if (currentDate.isAfter(event.getDate())){
				for (String searchTerm : noTrailingWhiteSpaceWord) {
					if (!(Arrays.asList(AllWordsInName).contains(searchTerm))) { //if "name" in ["Kilburn", "Building"]
						match = false;
					}
				}
			}else {
				match = false;
			}
			if (match == true) {
				correctResults.add(event);
			}
				
		}
	    return correctResults;
	    
	}
	
	@Override
	public Iterable<Event> findFuture(String name){
		LocalDate currentDate = LocalDate.now();
	    String[] noTrailingWhiteSpaceWord = name.toLowerCase().split("\\s+"); // " name " -> ["name"]
		
		
		Iterable<Event> allResults =eventRepository.findByNameContainingIgnoreCaseOrderByDateAscNameAsc(name); //all the results
		List<Event> correctResults = new ArrayList<>(); //filtered results
		for (Event event : allResults) { //go through the results
			boolean match = true;
			String[] AllWordsInName = event.getName().toLowerCase().split("\\s+"); //"Kilburn Building" -> ["Kilburn", "Building"]
			if (currentDate.isBefore(event.getDate())){
				for (String searchTerm : noTrailingWhiteSpaceWord) {
					if (!(Arrays.asList(AllWordsInName).contains(searchTerm))) { //if "name" in ["Kilburn", "Building"]
						match = false;
					}
				}
			}else {
				match = false;
			}
			if (match == true) {
				correctResults.add(event);
			}
				
		}
	    return correctResults;
	}
	
	@Override
	public Event save(Event event) {
		return eventRepository.save(event);
	}
	
	@Override
	public void delete(Event event) {
		eventRepository.delete(event);
	}

	
	@Override
	public void deleteById(long eid) {
		eventRepository.deleteById(eid);
	}
	
	@Override
	public boolean existsById(long id) {
		return eventRepository.existsById(id);
	}

	@Override
	public Event getEventById(long id) {
		return eventRepository.findById(id).get();
	}

	@Override
	public Event updateEvent(Event event) {
		return eventRepository.save(event);
	}
	
	@Override
	public Optional<Event> find(Long id) {
		return eventRepository.findById(id);
	}
	@Override
	public Iterable<Event> findVenueEvents(Venue venue){
		return eventRepository.findByVenueOrderByDateAscNameAsc(venue);
	}
	@Override
	public Iterable<Event> findFutureByVenue(Venue venue) {
		Iterable<Event> results = eventRepository.findByVenueOrderByDateAscNameAsc(venue);
		List<Event> correctResults = new ArrayList<>();
		for (Event event: results) {
			boolean match = true;
			if (!LocalDate.now().isBefore(event.getDate())){
				match = false;
			}
			if (match == true) {
				correctResults.add(event);
			}
		}
	    return correctResults;
	}
}
 