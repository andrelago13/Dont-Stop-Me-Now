package com.sdis.g0102.dsmn.api;

import android.content.Context;

import com.sdis.g0102.dsmn.domain.StreetEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
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
    public API(Context context, URL url) {
        this.context = context;
        this.url = url;
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

    private APIResponse sendRequest(URL url, String method, byte[] msg) throws GeneralSecurityException {
        try {
            // TODO extract this code so it's only run once

            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(context.getAssets().open("keys/truststore.bks"), "123456".toCharArray());

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
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

    private boolean isHTTPResponseCodeSuccess(int code) {
        return code / 100 == 2;
    }
}
