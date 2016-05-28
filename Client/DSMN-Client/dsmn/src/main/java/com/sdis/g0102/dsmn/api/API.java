package com.sdis.g0102.dsmn.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.location.Location;
import android.media.Image;
import android.util.Log;

import com.facebook.AccessToken;
import com.sdis.g0102.dsmn.api.domain.Comment;
import com.sdis.g0102.dsmn.api.domain.StreetEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by Gustavo on 19/05/2016.
 */
public class API {

    public static final String url_string = "https://172.30.23.148/api/";

    private static API instance = null;

    private Context context;
    private URL url;
    private SSLContext sslContext;
    private String facebookHash;

    public static API getInstance() {
        return instance;
    }

    public static API getInstance(Context context) throws IOException, GeneralSecurityException {
        if(instance != null)
            return instance;

        instance = new API(context, AccessToken.getCurrentAccessToken().getToken());
        return instance;
    }

    private API(Context context, String facebookHash) throws GeneralSecurityException, IOException {
        this.context = context;
        this.url = new URL(url_string);
        this.facebookHash = facebookHash;
        initSSLContext();
    }

    static {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier()
        {
            public boolean verify(String hostname, SSLSession session)
            {
                // ip address of the service URL(like.23.28.244.244)
                //if (hostname.equals("172.30.24.106"))
                    return true;
                //return false;
            }
        });
    }

    public List<StreetEvent> listEvents(boolean onlyMine) {
        try {
            APIResponse response = sendRequest(new URL(this.url + "events?onlymine=" + onlyMine), "GET", null);
            if (isHTTPResponseCodeSuccess(response.getCode())) {
                JSONArray ja = new JSONArray(new String(response.getMessage()));
                List<StreetEvent> list = new LinkedList<StreetEvent>();
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);
                    StreetEvent streetEvent = new StreetEvent(jo);
                    list.add(streetEvent);
                }
                return list;
            } else
                return null;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param type
     * @param duration
     * @param location
     * @return the event ID on success, null otherwise
     */
    public Integer createEvent(StreetEvent.Type type, String duration, String location) {
        return createEvent(type, duration, location, null, null);
    }

    /**
     *
     * @param type
     * @param duration
     * @param longitude
     * @param latitude
     * @return the event ID on success, null otherwise
     */
    public Integer createEvent(StreetEvent.Type type, String duration, Float longitude, Float latitude) {
        return createEvent(type, duration, null, longitude, latitude);
    }

    /**
     *
     * @param type
     * @param description
     * @param location
     * @param longitude
     * @param latitude
     * @return the event ID on success, null otherwise
     */
    public Integer createEvent(StreetEvent.Type type, String description, String location, Float longitude, Float latitude) {
        try {
            JSONObject jo = new JSONObject();
            JSONObject joCreateEvent = new JSONObject();
            joCreateEvent.put("type", type.ordinal());
            joCreateEvent.put("description", description);
            if (location != null)
                joCreateEvent.put("location", location);
            if (longitude != null)
                joCreateEvent.put("longitude", longitude);
            if (latitude != null)
                joCreateEvent.put("latitude", latitude);
            jo.put("create_event", joCreateEvent);
            APIResponse response = sendRequest(new URL(this.url + "events/"), "POST", jo.toString().getBytes());
            if (!isHTTPResponseCodeSuccess(response.getCode())) return null;
            jo = new JSONObject(new String(response.getMessage()));
            return jo.getJSONObject("success").getInt("eventid");
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public StreetEvent getEvent(int eventID) {
        return getEvent(eventID, null, null);
    }

    public StreetEvent getEvent(int eventID, Float longitude, Float latitude) {
        return getEvent(eventID, longitude, latitude, null);
    }

    public StreetEvent getEvent(int eventID, Float longitude, Float latitude, Float radius) {
        try {
            String s = this.url + "events/" + eventID;
            if (longitude != null && latitude != null) {
                s += "?longitude=" + longitude + "&latitude=" + latitude;
                if (radius != null)
                    s += "radius=" + radius;
            }
            APIResponse response = sendRequest(new URL(s), "GET", null);
            if (isHTTPResponseCodeSuccess(response.getCode())) {
                return new StreetEvent(new JSONObject(new String(response.getMessage())));
            } else
                return null;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteEvent(int eventID) {
        try {
            APIResponse response = sendRequest(new URL(this.url + "events/" + eventID), "DELETE", null);
            return isHTTPResponseCodeSuccess(response.getCode());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Bitmap getEventPhoto(int eventID) {
        try {
            APIResponse response = sendRequest(new URL(this.url + "events/" + eventID + "/photo"), "GET", null);
            if (isHTTPResponseCodeSuccess(response.getCode())) {
                Log.d("test", "test1  " + response.getMessage().length);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;
                Bitmap bmp = BitmapFactory.decodeByteArray(response.getMessage(), 0, response.getMessage().length, options);
                Log.d("test", "test2");
                return bmp;
            } else {
                return null;
            }
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param eventID
     * @return true on success, false otherwise
     */
    public boolean setEventPhoto(int eventID, Bitmap photo) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            APIResponse response = sendRequest(new URL(this.url + "events/" + eventID + "/photo"), "PUT", imageBytes);
            return isHTTPResponseCodeSuccess(response.getCode());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     *
     * @param eventID
     * @return true on success, false otherwise
     */
    public boolean deleteEventPhoto(int eventID) {
        try {
            APIResponse response = sendRequest(new URL(this.url + "events/" + eventID + "/photo"), "DELETE", null);
            return isHTTPResponseCodeSuccess(response.getCode());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Comment> listEventComments(int eventID) {
        try {
            APIResponse response = sendRequest(new URL(this.url + "events/" + eventID + "/comments"), "GET", null);
            if (!isHTTPResponseCodeSuccess(response.getCode()))
                return null;
            JSONArray ja = new JSONArray(new String(response.getMessage()));
            List<Comment> comments = new LinkedList<Comment>();
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                Comment comment = new Comment();
                comment.id = jo.getInt("id");
                comment.writer = jo.getInt("writer");
                comment.event = jo.getInt("event");
                comment.message = jo.getString("message");
                comment.datetime = jo.getLong("datetime");
                comments.add(comment);
            }
            return comments;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addComment(int eventID, String message) {
        try {
            JSONObject jo = new JSONObject();
            JSONObject joCreateComment = new JSONObject();
            jo.put("create_comment", joCreateComment);
            joCreateComment.put("eventid", eventID);
            joCreateComment.put("message", message);
            APIResponse response = sendRequest(new URL(this.url + "events/" + eventID + "/comments"), "POST", jo.toString().getBytes());
            return isHTTPResponseCodeSuccess(response.getCode());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addConfirmation(int eventID, boolean type) {
        try {
            JSONObject jo = new JSONObject();
            JSONObject joCreateComment = new JSONObject();
            jo.put("event_confirm", joCreateComment);
            joCreateComment.put("eventid", eventID);
            joCreateComment.put("type", type);
            APIResponse response = sendRequest(new URL(this.url + "events/" + eventID + "/confirmations"), "PUT", jo.toString().getBytes());
            return isHTTPResponseCodeSuccess(response.getCode());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteConfirmation(int eventID) {
        try {
            APIResponse response = sendRequest(new URL(this.url + "events/" + eventID + "/confirmations"), "DELETE", null);
            return isHTTPResponseCodeSuccess(response.getCode());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean requestNotifications(InetAddress address, int port, float longitude, float latitude, float radius) {
        try {
            JSONObject jo = new JSONObject();
            JSONObject joRequestNotification = new JSONObject();
            jo.put("request_notification", joRequestNotification);
            joRequestNotification.put("address", address.getHostAddress());
            joRequestNotification.put("port", port);
            joRequestNotification.put("longitude", longitude);
            joRequestNotification.put("latitude", latitude);
            joRequestNotification.put("radius", radius);
            APIResponse response = sendRequest(new URL(this.url + "notifications/"), "PUT", jo.toString().getBytes());
            return isHTTPResponseCodeSuccess(response.getCode());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean stopNotifications() {
        try {
            APIResponse response = sendRequest(new URL(this.url + "notifications/"), "DELETE", null);
            return isHTTPResponseCodeSuccess(response.getCode());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private APIResponse sendRequest(URL url, String method, byte[] msg) throws GeneralSecurityException {
        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            urlConnection.setRequestMethod(method);
            urlConnection.setRequestProperty("Authorization", this.facebookHash);
            if(msg != null)
                urlConnection.getOutputStream().write(msg);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream is = urlConnection.getInputStream();
            int n;
            while ((n = is.read()) != -1) baos.write(n);
            is.close();

            return new APIResponse(urlConnection.getResponseCode(), baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initSSLContext() throws GeneralSecurityException, IOException {
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(context.getAssets().open("keys/truststore.bks"), "123456".toCharArray());

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);
    }

    private boolean isHTTPResponseCodeSuccess(int code) {
        return code / 100 == 2;
    }
}
