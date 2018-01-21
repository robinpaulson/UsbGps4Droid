package org.broeuschmeul.android.gps.usb.provider;

import android.app.Application;
import android.location.Location;
import android.os.Handler;
import org.broeuschmeul.android.gps.nmea.util.USBGpsSatellite;
import android.support.v7.app.AppCompatDelegate;

import java.util.ArrayList;

/**
 * Created by freshollie on 15/05/17.
 */

public class USBGpsApplication extends Application {
    private static boolean locationAsked = true;

    private int MAX_LOG_SIZE = 100;

    private final ArrayList<ServiceDataListener> serviceDataListeners = new ArrayList<>();
    private Location lastLocation;
    private ArrayList<String> logLines = new ArrayList<>();

    private USBGpsSatellite[] lastSatelliteList;

    private Handler mainHandler;

    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    public interface ServiceDataListener {
        void onNmeaReceived(String sentence);
        void onLocationNotified(Location location);
        void onSatelittesUpdated(USBGpsSatellite[] satellites);
    }

    @Override
    public void onCreate() {
        com.android.gpstest.Application.initalise(this);
        locationAsked = false;
        mainHandler = new Handler(getMainLooper());
        for (int i = 0; i < MAX_LOG_SIZE; i++) {
            logLines.add("");
        }
        super.onCreate();
    }

    public static void setLocationAsked() {
        locationAsked = true;
    }

    public static boolean wasLocationAsked() {
        return locationAsked;
    }

    public static void setLocationNotAsked() {
        locationAsked = false;
    }

    public String[] getLogLines() {
        return logLines.toArray(new String[logLines.size()]);
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void registerServiceDataListener(ServiceDataListener listener) {
        serviceDataListeners.add(listener);
    }

    public void unregisterServiceDataListener(ServiceDataListener listener) {
        serviceDataListeners.remove(listener);
    }

    public void notifyNewSentence(final String sentence) {
        if (logLines.size() > MAX_LOG_SIZE) {
            logLines.remove(0);
        }

        logLines.add(sentence);

        synchronized (serviceDataListeners) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (ServiceDataListener dataListener: serviceDataListeners) {
                        dataListener.onNmeaReceived(sentence);
                    }
                }
            });
        }
    }

    public void notifyNewLocation(final Location location) {
        lastLocation = location;
        synchronized (serviceDataListeners) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (ServiceDataListener dataListener: serviceDataListeners) {
                        dataListener.onLocationNotified(location);
                    }
                }
            });

        }
    }

    public void notifySatellitesUpdated(final USBGpsSatellite[] satellites) {
        lastSatelliteList = satellites;
        synchronized (serviceDataListeners) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (ServiceDataListener dataListener: serviceDataListeners) {
                        dataListener.onSatelittesUpdated(satellites);
                    }
                }
            });
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        com.android.gpstest.Application.terminate();
    }
}
