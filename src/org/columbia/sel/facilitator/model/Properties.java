package org.columbia.sel.facilitator.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Properties POJO used by the Facility objects.
 * 
 * @author Jonathan Wohl
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "sector", "type", "checkins" })
public class Properties {

	@JsonProperty("sector")
	private String sector;
	@JsonProperty("type")
	private String type;
	@JsonProperty("checkins")
	private Integer checkins;
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

	@JsonProperty("checkins")
	public Integer getCheckins() {
		return checkins;
	}

	@JsonProperty("checkins")
	public void setCheckins(Integer checkins) {
		this.checkins = checkins;
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
