package com.sdis.g0102.dsmn.api;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
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
            HttpsURLConnection urlConnection = sendRequest(new URL(this.url + "events/"), "GET", null);
            if (isHTTPResponseCodeSuccess(urlConnection.getResponseCode()))
                return null; // TODO
            else
                return null;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private HttpsURLConnection sendRequest(URL url, String method, byte[] msg) throws GeneralSecurityException {
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

            // TODO return code and message

            return urlConnection;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isHTTPResponseCodeSuccess(int code) {
        return code / 100 == 2;
    }
}
