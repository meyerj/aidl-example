package com.afollestad.aidlexamplereceiver;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.afollestad.aidlexample.IMainService;
import com.afollestad.aidlexample.IListener;
import com.afollestad.aidlexample.MainObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Aidan Follestad (afollestad)
 */
public class MainService extends Service {

    Set<IListener> mListeners = new HashSet<IListener>();
    Timer mCallbackTimer;

    private void log(String message) {
        Log.v("MainService", message);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("Received start command.");

        Intent notificationIntent = new Intent(this, MainService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this)
                        .setContentTitle("AIDL Receiver")
                        .setContentText("Running...")
                        .setContentIntent(pendingIntent)
                        .setTicker("Ticker text")
                        .build();

        startForeground(101, notification);

        mCallbackTimer = new Timer("CallbackTimer");
        mCallbackTimer.scheduleAtFixedRate(mCallbackTimerTask, 0, 1000);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        log("Received binding.");
        return mBinder;
    }

    private final IMainService.Stub mBinder = new IMainService.Stub() {
        @Override
        public MainObject[] listFiles(String path) throws RemoteException {
            log("Received list command for: " + path);
            List<MainObject> toSend = new ArrayList<>();
            // Generates a list of 1000 objects that aren't sent back to the binding Activity
            for (int i = 0; i < 1000; i++)
                toSend.add(new MainObject("/example/item" + (i + 1)));
            return toSend.toArray(new MainObject[toSend.size()]);
        }

        @Override
        public void exit() throws RemoteException {
            log("Received exit command.");
            stopSelf();
        }

        @Override
        public void registerCallbacks(IListener listener) {
            log("Received registerCallbacks.");
            mListeners.add(listener);
        }
    };

    private TimerTask mCallbackTimerTask = new TimerTask() {
        @Override
        public void run() {
            log("TimerTask");
            for(IListener listener : mListeners) {
                try {
                    listener.callback();
                } catch (RemoteException e) {
                }
            }
            for (Iterator<IListener> i = mListeners.iterator(); i.hasNext();) {
                IListener listener = i.next();
                try {
                    listener.callback();
                } catch (RemoteException e) {
                    log("Removing listener after RemoteException.");
                    i.remove();
                }
            }
        }
    };
}
