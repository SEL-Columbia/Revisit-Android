package org.columbia.sel.facilitator.api;

import org.columbia.sel.facilitator.model.Facility;
import org.columbia.sel.facilitator.model.FacilityList;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
//import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Retrofit REST API definitions.
 * 
 * @author Jonathan Wohl
 *
 */
public interface FacilitatorApi {
	@GET("/facilities/near/{lat}/{lng}/{rad}")
	FacilityList facilitiesNear(@Path("lat") String lat, @Path("lng") String lng, @Path("rad") String rad);
	
	@GET("/facilities/within/{swlat}/{swlng}/{nelat}/{nelng}")
	FacilityList facilitiesWithin(@Path("swlat") String swlat, @Path("swlng") String swlng, @Path("nelat") String nelat, @Path("nelng") String nelng, @Query("sector") String sector);
	
	// RoboSpice is taking care of the async/thread management stuff, so we can tell Rotrofit to act synchronously
	@POST("/facilities")
	Facility addFacility(@Body Facility facility);
//	void addFacility(@Body Facility facility, Callback<Facility> cb);
}
