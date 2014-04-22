package org.columbia.sel.facilitator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Facility {
    private String _id;
    private String name;

    public String getId() {
        return this._id;
    }

    public String getName() {
        return this.name;
    }

}
