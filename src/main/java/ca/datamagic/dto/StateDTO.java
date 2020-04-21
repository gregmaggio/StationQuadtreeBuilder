/**
 * 
 */
package ca.datamagic.dto;

/**
 * @author Greg
 *
 */
public class StateDTO {
	private String name = null;
	private String abbreviation = null;
	private String code = null;
	
	public StateDTO() {
		
	}
	
	public StateDTO(String name, String abbreviation, String code) {
		this.name = name;
		this.abbreviation = abbreviation;
		this.code = code;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getAbbreviation() {
		return this.abbreviation;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public void setName(String newVal) {
		this.name = newVal;
	}
	
	public void setAbbreviation(String newVal) {
		this.abbreviation = newVal;
	}
	
	public void setCode(String newVal) {
		this.code = newVal;
	}
}
