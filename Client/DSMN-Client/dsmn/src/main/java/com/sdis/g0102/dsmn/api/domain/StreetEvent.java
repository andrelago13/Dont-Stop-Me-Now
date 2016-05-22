package com.sdis.g0102.dsmn.api.domain;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.media.Image;

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
    public int creator;
    public Type type;
    public String description;
    public String location;
    public PointF coords;
    public Bitmap photo;
    public Timestamp dateTime;
    public int positiveConfirmations;
    public int negativeConfirmations;

    public boolean hasLocation() {
        return location != null;
    }

    public boolean hasCoords() {
        return coords != null;
    }
}
