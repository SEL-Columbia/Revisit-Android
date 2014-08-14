package edu.columbia.sel.revisit.api;

import edu.columbia.sel.revisit.model.Site;
import edu.columbia.sel.revisit.model.SiteList;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * Retrofit REST API definitions.
 * 
 * @author Jonathan Wohl
 *
 */
public interface RevisitApi {
	@GET("/sites/near/{lat}/{lng}/{rad}")
	SiteList sitesNear(@Path("lat") String lat, @Path("lng") String lng, @Path("rad") String rad);
	
	@GET("/sites/within/{swlat}/{swlng}/{nelat}/{nelng}")
	SiteList sitesWithin(@Path("swlat") String swlat, @Path("swlng") String swlng, @Path("nelat") String nelat, @Path("nelng") String nelng, @Query("sector") String sector);
	
	// RoboSpice is taking care of the async/thread management stuff, so we can tell Rotrofit to act synchronously
	@POST("/sites")
	Site addSite(@Body Site site);
	
	@Multipart
	@POST("/sites/{id}/photos")
	Site uploadPhoto(@Part("photo") TypedFile photo, @Path("id") String id);
	
	// Update
	@PUT("/sites/{id}")
	Site updateSite(@Body Site site, @Path("id") String id);
}
