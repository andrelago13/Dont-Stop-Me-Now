package com.sdis.g0102.dsmn.api.domain;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.media.Image;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;

/**
 * Created by Gustavo on 20/05/2016.
 */
public class StreetEvent {
    public enum Type {
        SPEED_RADAR,
        TRAFFIC_STOP,
        HIGH_TRAFFIC,
        CAR_CRASH
    }
    public int id;
    public String creator;
    public Type type;
    public String description;
    public String location;
    public PointF coords;
    public Timestamp dateTime;
    public int positiveConfirmations;
    public int negativeConfirmations;

    public boolean hasLocation() {
        return location != null;
    }

    public boolean hasCoords() {
        return coords != null;
    }

    public StreetEvent() {}

    public StreetEvent(JSONObject jo) throws JSONException {
        id = jo.getInt("id");
        creator = jo.getString("creator");

        int type = jo.getInt("type");
        StreetEvent.Type[] seTypes = StreetEvent.Type.values();
        if (type > seTypes.length)
            throw new JSONException("Invalid Event type.");
        this.type = seTypes[type];

        description = jo.getString("description");
        if (jo.has("location"))
            location = jo.getString("location");

        if (jo.has("latitude") && jo.has("longitude"))
            coords = new PointF((float)jo.getDouble("latitude"), (float)jo.getDouble("longitude"));
        dateTime = new Timestamp(jo.getLong("datetime"));
        positiveConfirmations = jo.getInt("positiveConfirmations");
        negativeConfirmations = jo.getInt("negativeConfirmations");
    }
}
