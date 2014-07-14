package edu.columbia.sel.facilitator.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A POJO representing a Facility. Implements Parcelable so that it can be
 * passed around between Activities.
 * 
 * @author jmw
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "_id", "name", "uuid", "active", "coordinates", "properties", "updatedAt", "createdAt" })
public class Facility implements Parcelable {

	private final String TAG = this.getClass().getCanonicalName();
	
	@JsonProperty("_id")
	private String _id;
	@JsonProperty("name")
	private String name;

	// We generate a random UUID on the client when a new Site is created in
	// order to manage offline use and syncing
	@JsonProperty("uuid")
	private String uuid = UUID.randomUUID().toString();
	@JsonProperty("active")
	private Boolean active = false;
	@JsonProperty("coordinates")
	private List<Double> coordinates = new ArrayList<Double>();
	@JsonProperty("properties")
	private Properties properties = new Properties();
	@JsonProperty("updatedAt")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "EST")
	private Date updatedAt;
	@JsonProperty("createdAt")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "EST")
	private Date createdAt;
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();
	private Boolean requestSync = false;

	/**
	 * Default constructor
	 * 
	 * @return
	 */
	public Facility() {
//		this.uuid = UUID.randomUUID().toString();
	}

	@JsonProperty("_id")
	public String get_id() {
		return _id;
	}

	@JsonProperty("_id")
	public void set_id(String _id) {
		this._id = _id;
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("uuid")
	public String getUuid() {
		return uuid;
	}

	@JsonProperty("uuid")
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@JsonProperty("active")
	public Boolean getActive() {
		return active;
	}

	@JsonProperty("active")
	public void setActive(Boolean active) {
		this.active = active;
	}

	@JsonProperty("coordinates")
	public List<Double> getCoordinates() {
		return coordinates;
	}

	@JsonProperty("coordinates")
	public void setCoordinates(List<Double> coordinates) {
		this.coordinates = coordinates;
	}

	@JsonProperty("properties")
	public Properties getProperties() {
		return properties;
	}

	@JsonProperty("properties")
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	@JsonProperty("updatedAt")
	public Date getUpdatedAt() {
		return updatedAt;
	}

	@JsonProperty("updatedAt")
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	@JsonProperty("createdAt")
	public Date getCreatedAt() {
		return createdAt;
	}

	@JsonProperty("createdAt")
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	public Boolean getRequestSync() {
		return requestSync;
	}

	public void setRequestSync(Boolean requestSync) {
		this.requestSync = requestSync;
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_id == null) ? 0 : _id.hashCode());
        return result;
    }
	
	// Use @Override to avoid accidental overloading.
	@Override
	public boolean equals(Object o) {
//		Log.i(TAG, "EQUALS:");
		// Return true if the objects are identical.
		// (This is just an optimization, not required for correctness.)
		if (this == o) {
			return true;
		}

		// Return false if the other object has the wrong type.
		// This type may be an interface depending on the interface's
		// specification.
		if (!(o instanceof Facility)) {
			return false;
		}

		// Cast to the appropriate type.
		// This will succeed because of the instanceof, and lets us access
		// private fields.
		Facility site = (Facility) o;

		
		// Check each field. Primitive fields, reference fields, and nullable
		// reference
		// fields are all treated differently.
		if (site.get_id() != null && site.get_id().equals(this.get_id())) {
			Log.i(TAG, "Server _id Match: " + this.get_id() + " = " + site.get_id());
			return true;
		}

		if (site.getUuid() != null && !site.getUuid().equals("-1") && site.getUuid().equals(this.getUuid())) {
			Log.i(TAG, "UUID Match: " + this.getUuid() + " = " + site.getUuid());
			return true;
		}

		return false;
	}

	//
	// Parcelable Interface
	//

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		Properties props = this.getProperties();
		out.writeString(_id);
		out.writeString(uuid);
		out.writeString(name);
		out.writeByte((byte) (active ? 1 : 0));
		out.writeString(props.getType());
		out.writeString(props.getSector());
		out.writeInt(props.getCheckins());
		out.writeList(coordinates);
	}

	public static final Parcelable.Creator<Facility> CREATOR = new Parcelable.Creator<Facility>() {
		public Facility createFromParcel(Parcel in) {
			return new Facility(in);
		}

		public Facility[] newArray(int size) {
			return new Facility[size];
		}
	};

	private Facility(Parcel in) {
		_id = in.readString();
		uuid = in.readString();
		name = in.readString();
		active = in.readByte() != 0;
		this.properties.setType(in.readString());
		this.properties.setSector(in.readString());
		this.properties.setCheckins(in.readInt());
		in.readList(coordinates, null);
	}

}
