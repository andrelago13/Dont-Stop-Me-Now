package com.sdis.g0102.dsmn.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.location.Location;
import android.media.Image;

import com.sdis.g0102.dsmn.api.domain.StreetEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
    private Context context;
    private URL url;
    private SSLContext sslContext;
    public API(Context context, URL url) throws GeneralSecurityException, IOException {
        this.context = context;
        this.url = url;

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

    public List<StreetEvent> listEvents() {
        try {
            APIResponse response = sendRequest(new URL(this.url + "events/"), "GET", null);
            if (isHTTPResponseCodeSuccess(response.getCode())) {
                JSONArray ja = new JSONArray(new String(response.getMessage()));
                List<StreetEvent> list = new LinkedList<StreetEvent>();
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);
                    StreetEvent streetEvent = new StreetEvent();
                    streetEvent.creator = jo.getInt("creator");
                    int type = jo.getInt("type");
                    StreetEvent.Type[] seTypes = StreetEvent.Type.values();
                    if (type > seTypes.length)
                        return null;
                    streetEvent.type = seTypes[type];
                    streetEvent.description = jo.getString("description");
                    if (jo.has("location"))
                        streetEvent.location = jo.getString("location");
                    if (jo.has("latitude") && jo.has("longitude"))
                        streetEvent.coords = new PointF((float)jo.getDouble("latitude"), (float)jo.getDouble("longitude"));
                    streetEvent.dateTime = new Timestamp(jo.getLong("datetime"));
                    streetEvent.positiveConfirmations = jo.getInt("positiveConfirmations");
                    streetEvent.negativeConfirmations = jo.getInt("negativeConfirmations");
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

    public Bitmap getEventImage(int eventID) {
        try {
            APIResponse response = sendRequest(new URL(this.url + "events/" + eventID + "photo/"), "GET", null);
            Bitmap bmp = BitmapFactory.decodeByteArray(response.getMessage(), 0, response.getMessage().length);
            return bmp;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private APIResponse sendRequest(URL url, String method, byte[] msg) throws GeneralSecurityException {
        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            urlConnection.setRequestMethod(method);

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
