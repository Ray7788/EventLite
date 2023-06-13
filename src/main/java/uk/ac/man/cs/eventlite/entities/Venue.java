package uk.ac.man.cs.eventlite.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.util.List;

import com.mapbox.api.geocoding.v5. MapboxGeocoding; 
import com.mapbox.api.geocoding.v5.models.CarmenFeature; 
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2. Response;

@Entity
@Table(name = "venues") // optional can be omitted 
public class Venue {
	
	@Id
	@GeneratedValue
	private long id;

	@Column(name = "name") // optional can be omitted 
	@NotEmpty(message = "Venue name can not be empty")
	@Size(max = 256, message = "The name should shorter than 256 characters")
	private String name;
	
	@Column(name = "capacity") // optional can be omitted
	@Min(value = 1, message = "The value must be a positive integer")
	private int capacity;	
	
	@Column(name = "address") // optional can be omitted 
	private String address;
	
	@Column (name = "postcode")
	@NotEmpty(message = "Postcode can not be empty")
	private String postcode;

	@Column (name = "roadName")
	@NotEmpty(message = "Road name can not be empty")
	@Size(max = 300, message = "The road name should have a maximum of 300 characters")
	private String roadName;
	
	@Column (name = "cityName")
	@NotEmpty(message = "City name can not be empty")
	@Size(max = 300, message = "The city name should have a maximum of 300 characters")
	private String cityName;
	
	@Column (name = "longitude")
	private double longitude;
	
	@Column (name = "latitude")
	private double latitude;

	public Venue() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	public String getAddress() {
	    return address;
	}
	
	public void setAddress() {
		  this.address = getRoadName() + ", " + getCityName() + ", " + getPostcode();
	}
	
	public String getPostcode() {
	    return postcode;
	}
	
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getRoadName() {
		return roadName;
	}

	public void setRoadName(String roadName) {
		this.roadName = roadName;
	}
	
	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}


	public void setCoordinates() {
		MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
				.accessToken("pk.eyJ1IjoiYWx0YWlzYW5tIiwiYSI6ImNsZmF3bWl6NzA5MTgzcW4xMXRod2lkOHcifQ.xwkHeSIVcmGWcvMWr8DOfQ")
				.query(this.getAddress())
				.geocodingTypes("postcode")
				.country("gb")
				.build();
		Venue venue = this;
		
		mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
			@Override
			public void onResponse (Call<GeocodingResponse> call, Response<GeocodingResponse> response) { 
				List<CarmenFeature> results = response.body().features();
				if (results.size() > 0) {
					Point firstResultPoint = results.get(0).center(); 
					venue.setLongitude(firstResultPoint.longitude());
					venue.setLatitude(firstResultPoint.latitude());
				}
				else {
					venue.setLongitude(600);
					venue.setLatitude(600);
					System.out.println("No result found");
				}
			}
			@Override
			public void onFailure (Call<GeocodingResponse> call, Throwable throwable){
				throwable.printStackTrace();
				}
		});
		
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
