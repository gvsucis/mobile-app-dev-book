package edu.gvsu.cis.traxy.webservice;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;

import edu.gvsu.cis.traxy.BuildConfig;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class WeatherService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_FOO = "edu.gvsu.cis.traxy.webservice.action.FOO";
    public static final String ACTION_BAZ = "edu.gvsu.cis.traxy.webservice.action.BAZ";

    // TODO: Rename parameters
    public static final String EXTRA_KEY = "traxy.webservice.extra.KEY";
    public static final String EXTRA_LAT = "traxy.webservice.extra.LAT";
    public static final String EXTRA_LNG = "traxy.webservice.extra.LNG";
    public static final String EXTRA_TIME = "traxy.webservice.extra.TIME";
    public static final String EXTRA_TEMP = "traxy.webservice.extra.TEMPERATURE";
    public static final String EXTRA_ICON = "traxy.webservice.extra.ICON";
    public static final String BROADCAST_WEATHER = "traxy.webservice" +
            ".action.BROADCAST";
    private static final String BASE_URL =
            "https://api.darksky.net/forecast/" + BuildConfig.DARK_SKY_API_KEY;

    public WeatherService() {
        super("WeatherService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String key = intent.getStringExtra(EXTRA_KEY);
        final double lat = intent.getDoubleExtra(EXTRA_LAT, 0.0f);
        final double lng = intent.getDoubleExtra(EXTRA_LNG, 0.0f);
        final long when = intent.getLongExtra(EXTRA_TIME, 0L);
        HttpURLConnection conn;
        BufferedInputStream bis;
        ByteArrayOutputStream baos;
        try {
            URL url = new URL(BASE_URL + String.format("/%g,%g,%d", lat,
             lng, when));
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int resp = conn.getResponseCode();
            if (resp == HttpURLConnection.HTTP_OK) {
                bis = new BufferedInputStream(conn.getInputStream());
                baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int len;
                while ((len = bis.read(buffer)) > 0)
                    baos.write(buffer, 0, len);

                JSONObject data = new JSONObject(new String(baos
                        .toByteArray()));
                JSONObject current = data.getJSONObject("currently");
//                String condition = current.getString("summary");
                String icon = current.getString("icon");
                double temp = current.getDouble("temperature");

                Intent result = new Intent(BROADCAST_WEATHER);
                result.putExtra(EXTRA_KEY, key);
                result.putExtra(EXTRA_TEMP, temp);
                result.putExtra(EXTRA_ICON, icon);
                LocalBroadcastManager.getInstance(this).sendBroadcast
                        (result);
            }
        } catch (MalformedURLException m) {
            m.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
