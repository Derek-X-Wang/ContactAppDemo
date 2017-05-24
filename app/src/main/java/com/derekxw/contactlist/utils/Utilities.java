package com.derekxw.contactlist.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * Created by Derek on 5/23/2017.
 */

public class Utilities {

    public static String dateToString(Date date) {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        return df.format(date);
    }

    public static int getColor(int id, Context c) {
        return ContextCompat.getColor(c, id);
    }
}
