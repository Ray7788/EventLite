package uk.ac.man.cs.eventlite.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class VenueServiceImpl implements VenueService {

	@Autowired
	private VenueRepository venueRepository;

	@Override
	public long count() {
		return venueRepository.count();
	}

	@Override
	public Iterable<Venue> findAll() {
		return venueRepository.findAll();
	}
	
	@Override
	public Venue save(Venue venue) {
		return venueRepository.save(venue);
	}

	@Override
	public Venue getbyId(Long venueId) {
		return venueRepository.findById(venueId).orElse(null);
	}
	@Override
	public Optional<Venue> find(Long id) {
		return venueRepository.findById(id);
	}
	
	@Override
	public Venue updateVenue(Venue venue) {
		return venueRepository.save(venue);
	}
	
	@Override
    public Iterable<Venue> search(String name){
		String[] splitName = name.toLowerCase().split("\\s+");
		
		Iterable<Venue> allResults = venueRepository.findByNameContainingIgnoreCaseOrderByNameAsc(name);
		
		List<Venue> filteredResults = new ArrayList<>();
		
		for (Venue venue: allResults) {
			boolean match = true;
			String[] AllWordsInVenueName = venue.getName().toLowerCase().split("\\s+");
			for (String searchTerm : splitName) {
				if (!(Arrays.asList(AllWordsInVenueName).contains(searchTerm))) { //if "name" in ["Kilburn", "Building"]
					match = false;
				}
			}
			if (match == true) {
				filteredResults.add(venue);
			}
		}
		return filteredResults;
	    
    }
	
	@Override
	public void delete(Venue venue) {
		venueRepository.delete(venue);
	}

	
	@Override
	public void deleteById(long vid) {
		venueRepository.deleteById(vid);
	}
}
