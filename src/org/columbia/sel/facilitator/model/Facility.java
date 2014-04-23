package org.columbia.sel.facilitator.model;

import java.util.Date;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A POJO representing a Facility.
 * @author jmw
 *
 */

// This tells Jackson to ignore extra keys in the JSON that's being mapped.
@JsonIgnoreProperties(ignoreUnknown = true)
public class Facility implements Parcelable {
    public String name;
    
    public String uuid;
    
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="EST")
    public Date createdAt;
    // 2014-04-23T14:04:52.221Z
    
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="EST")
    public Date updatedAt;
    
    public Map<String, Object> properties;
    
    
    
    // Dummy constructor so that the Parcel constructor isn't used by default
    private Facility() {
    }
    
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        //out.writeInt();
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
		//mData = in.readInt();
	}
	

}
