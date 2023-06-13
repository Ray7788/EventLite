package uk.ac.man.cs.eventlite.dao;

import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface EventRepository extends CrudRepository<Event, Long> {

	public Iterable<Event> findByVenueOrderByDateAscNameAsc(Venue venue);
	
	public Iterable<Event> findByNameContainingIgnoreCaseOrderByDateAscNameAsc(String name);
	public Iterable<Event> findByNameContainingIgnoreCaseOrderByDateDescNameAsc(String name);
	
	public Iterable<Event> findAllByOrderByDateAscTimeAsc();
	public Iterable<Event> findAllByOrderByDateDescNameAsc();
	public Iterable<Event> findAllByOrderByDateAscNameAsc();
}