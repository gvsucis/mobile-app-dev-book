package edu.gvsu.cis.traxy.webservice;

import edu.gvsu.cis.traxy.BuildConfig;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by dulimarh on 8/15/17.
 */

public interface DarkSkyServices {
    @GET("forecast/" + BuildConfig.DARK_SKY_API_KEY +
            "/{latitude},{longitude},{time}")
    Call<DarkSkyWeather>
    fetchWeatherForDate(@Path("latitude") double lat,
                        @Path("longitude") double lon,
                        @Path("time") long time);
}
