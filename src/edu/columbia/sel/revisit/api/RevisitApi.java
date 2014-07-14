package edu.columbia.sel.revisit.api;

import edu.columbia.sel.revisit.model.Site;
import edu.columbia.sel.revisit.model.SiteList;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Retrofit REST API definitions.
 * 
 * @author Jonathan Wohl
 *
 */
public interface RevisitApi {
	@GET("/facilities/near/{lat}/{lng}/{rad}")
	SiteList sitesNear(@Path("lat") String lat, @Path("lng") String lng, @Path("rad") String rad);
	
	@GET("/facilities/within/{swlat}/{swlng}/{nelat}/{nelng}")
	SiteList sitesWithin(@Path("swlat") String swlat, @Path("swlng") String swlng, @Path("nelat") String nelat, @Path("nelng") String nelng, @Query("sector") String sector);
	
	// RoboSpice is taking care of the async/thread management stuff, so we can tell Rotrofit to act synchronously
	@POST("/facilities")
	Site addSite(@Body Site site);
	
	// Update
	@PUT("/facilities/{id}")
	Site updateSite(@Body Site site, @Path("id") String id);
}
