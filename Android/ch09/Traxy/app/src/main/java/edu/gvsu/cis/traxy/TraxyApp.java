package edu.gvsu.cis.traxy;

import android.app.Application;
import net.danlew.android.joda.JodaTimeAndroid;

/*
 * Created by engeljo on 3/21/17.
 */

public class TraxyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
    }
}

