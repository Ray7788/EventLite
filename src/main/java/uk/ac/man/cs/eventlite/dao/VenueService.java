package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueService {

	public long count();

	public Iterable<Venue> findAll();

	public Venue save(Venue venue);

	public Venue getbyId(Long venueId);

	public Optional<Venue> find(Long id);
	
	public Iterable<Venue> search(String name);
	
	public Venue updateVenue(Venue venue);
	
	public void delete(Venue venue);
	
	public void deleteById(long vid);
}
