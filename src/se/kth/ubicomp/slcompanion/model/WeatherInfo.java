package se.kth.ubicomp.slcompanion.model;

public class WeatherInfo {
	
	private String temperature;
	
	private String condition;
	
	private int conditionCode;
	
	

	public WeatherInfo(String temperature, String condition, int conditionCode) {
		this.temperature = temperature;
		this.condition = condition;
		this.conditionCode = conditionCode;
	}

	public String getTemperature() {
		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public int getConditionCode() {
		return conditionCode;
	}

	public void setConditionCode(int conditionCode) {
		this.conditionCode = conditionCode;
	}
	
	

}
