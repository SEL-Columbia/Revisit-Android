package org.columbia.sel.facilitator.api;

import org.columbia.sel.facilitator.model.FacilityList;

import retrofit.http.GET;
//import retrofit.http.POST;
import retrofit.http.Path;

public interface FacilitatorApi {
	@GET("/facilities/near/{lat}/{lng}/{rad}")
	FacilityList facilitiesNear(@Path("lat") String lat, @Path("lng") String lng, @Path("rad") String rad);
	
	@GET("/facilities/within/{swlat}/{swlng}/{nelat}/{nelng}")
	FacilityList facilitiesWithin(@Path("swlat") String swlat, @Path("swlng") String swlng, @Path("nelat") String nelat, @Path("nelng") String nelng);
	
//	@POST("/facilities")
//	void createUser(@Body Facility facility, Callback<Facility> cb);
}
