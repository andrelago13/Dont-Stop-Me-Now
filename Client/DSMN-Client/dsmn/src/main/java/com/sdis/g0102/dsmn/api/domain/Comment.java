package com.sdis.g0102.dsmn.api.domain;

import java.util.Comparator;

/**
 * Created by Gustavo on 23/05/2016.
 */
public class Comment {
    public int id;
    public int writer;
    public int event;
    public String message;
    public long datetime;

    public static Comparator<Comment> datetime_comparator = new Comparator<Comment>() {
        @Override
        public int compare(Comment lhs, Comment rhs) {
            if(lhs.datetime < rhs.datetime) {
                return -1;
            } else if (lhs.datetime > rhs.datetime) {
                return 1;
            }
            return 0;
        }
    };
}
