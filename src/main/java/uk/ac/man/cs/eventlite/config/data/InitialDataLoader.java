package uk.ac.man.cs.eventlite.config.data;

import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueRepository;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Configuration
@Profile("default")
public class InitialDataLoader {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);
	private Venue venue1;
	private Venue venue2;
	private Venue venue3;

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@Bean
	CommandLineRunner initDatabase() {
		return args -> {
			if (venueService.count() > 0) {
				log.info("Database already populated with venues. Skipping venue initialization.");
			} else {
				// Build and save initial venues here.
				venue1 = new Venue();
				venue1.setName("Alan Turing Building");
				venue1.setCapacity(30);
				venue1.setRoadName("Upper Brook St.");
				venue1.setPostcode("M13 9PY");
				venue1.setCityName("Manchester");
				
				venue1.setAddress();
				venue1.setCoordinates();
				
				venue2 = new Venue();
				venue2.setName("Kilburn Building");
				venue2.setCapacity(100);
				venue2.setRoadName("Oxford Road");
				venue2.setPostcode("M13 9PL");
				venue2.setCityName("Manchester");
				
				venue2.setAddress();
				venue2.setCoordinates();


				venue3 = new Venue();
				venue3.setName("Stopford Building");
				venue3.setCapacity(300);
				venue3.setRoadName("Oxford Road South");
				venue3.setPostcode("M13 9PP");
				venue3.setCityName("Manchester");
				
				venue3.setAddress();
				venue3.setCoordinates();
				
				venueService.save(venue1);
				venueService.save(venue2);
				venueService.save(venue3);
			}

			if (eventService.count() > 0) {
				log.info("Database already populated with events. Skipping event initialization.");
			} else {
				// Build and save initial events here.
				Event event1 = new Event();
				event1.setName("COMP23412 Showcase 01");
				event1.setDate(LocalDate.of(2023, 5, 8));
				event1.setTime(LocalTime.of(12,0));
				event1.setDescription("waffle waffle waffle");
				event1.setVenue(venue1);
				
				Event event2 = new Event();
				event2.setName("COMP23412 Showcase 02");
				event2.setDate(LocalDate.of(2023, 5, 9));
				event2.setTime(LocalTime.of(11,0));
				event2.setDescription("chips chips chips");
				event2.setVenue(venue2);
				
				Event event3 = new Event();
				event3.setName("COMP23412 Showcase 03");
				event3.setDate(LocalDate.of(2023, 5, 11));
				event3.setTime(LocalTime.of(10,0));
				event3.setVenue(venue3);
				event3.setDescription("burger burger burger");
				
				Event event4 = new Event();
				event4.setName("COMP23412 Showcase 07");
				event4.setDate(LocalDate.of(2022, 4, 8));
				event4.setTime(LocalTime.of(11,0));
				event4.setDescription("sushi sushi sushi");
				event4.setVenue(venue2);
				
				eventService.save(event1);
				eventService.save(event2);
				eventService.save(event3);
				eventService.save(event4);

			}
		};
	}
}
