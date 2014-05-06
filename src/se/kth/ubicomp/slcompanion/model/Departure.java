/**
 * 
 */
package se.kth.ubicomp.slcompanion.model;

/**
 * Describes a departure at a nearby station. Bundling the information into one object facilitates listing departures.
 * 
 * @author jasperh
 *
 */
public class Departure {

	private String destination;
	
	private String line;
	
	private SlLineColor color;
	
	private String minsUntilDeparture;
	
	private int departureHours;
	
	private int departureMinutes;
	

	public Departure(String destination, String line, String minsUntilDeparture) {
		this.destination = destination;
		this.line = line;
		this.minsUntilDeparture = minsUntilDeparture;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getLine() {
		return line;
	}

	public SlLineColor getColor() {
		return color;
	}

	public void setColor(SlLineColor color) {
		this.color = color;
	}

	public int getDepartureHours() {
		return departureHours;
	}

	public void setDepartureHours(int departureHours) {
		this.departureHours = departureHours;
	}

	public int getDepartureMinutes() {
		return departureMinutes;
	}

	public void setDepartureMinutes(int departureMinutes) {
		this.departureMinutes = departureMinutes;
	}

	public String getMinsUntilDeparture() {
		return minsUntilDeparture;
	}

}
