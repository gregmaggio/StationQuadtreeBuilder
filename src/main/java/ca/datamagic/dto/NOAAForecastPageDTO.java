/**
 * 
 */
package ca.datamagic.dto;

/**
 * @author Greg
 *
 */
public class NOAAForecastPageDTO {
	private String wfo = null;
	private String radar = null;
	
	public NOAAForecastPageDTO() {
		
	}
	
	public NOAAForecastPageDTO(String wfo, String radar) {
		this.wfo = wfo;
		this.radar = radar;
	}

	public String getWFO() {
		return this.wfo;
	}
	
	public String getRadar() {
		return this.radar;
	}
	
	public void setWFO(String newVal) {
		this.wfo = newVal;
	}
	
	public void setRadar(String newVal) {
		this.radar = newVal;
	}
}
