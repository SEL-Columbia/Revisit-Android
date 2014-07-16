package edu.columbia.sel.revisit.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Properties POJO used by the Site objects.
 * 
 * @author Jonathan Wohl
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "sector", "type", "visits" })
public class Properties {

	@JsonProperty("sector")
	private String sector;
	@JsonProperty("type")
	private String type;
	@JsonProperty("visits")
	private Integer visits;
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("sector")
	public String getSector() {
		return sector;
	}

	@JsonProperty("sector")
	public void setSector(String sector) {
		this.sector = sector;
	}

	@JsonProperty("type")
	public String getType() {
		return type;
	}

	@JsonProperty("type")
	public void setType(String type) {
		this.type = type;
	}

	@JsonProperty("visits")
	public Integer getVisits() {
		return visits;
	}

	@JsonProperty("visits")
	public void setVisits(Integer visits) {
		this.visits = visits;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}
