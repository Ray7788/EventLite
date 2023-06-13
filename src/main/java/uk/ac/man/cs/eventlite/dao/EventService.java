package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	
	public Iterable<Event> findPast();
	
	public Iterable<Event> findFuture();
	
	public Iterable<Event> findPast(String name);
	
	public Iterable<Event> findFuture(String name);
	
	public Optional<Event> find(Long id);
	
	public Iterable<Event> findVenueEvents(Venue venue);
	
	public Event save(Event event);
	
	public void delete(Event event);
	
	public void deleteById(long eid);
	
	public boolean existsById(long id);

	public Event getEventById(long id);
	
	public Event updateEvent(Event event);
	
	public Iterable<Event> findFutureByVenue(Venue venue);
	
}
