package com.vikendu.chat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationService extends Service {

    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "1";
    private static final String NOTIF_CHANNEL_ID_2 = "2";
    private int initial_count = 0;
    private String message_recv;

    private DatabaseReference mReference;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

//    private ChildEventListener mListner = new ChildEventListener() {
//        @Override
//        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//
////            Log.d("Notif", "OnChildAdded");
////            newMessageRecieved();
//
//        }
//
//        @Override
//        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            Log.d("Notif", "OnChildRemoved");
//            newMessageRecieved();
//        }
//
//        @Override
//        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
////            Log.d("Notif", "OnChildRemoved");
////            newMessageRecieved();
//
//        }
//
//        @Override
//        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//        }
//
//        @Override
//        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//        }
//    };


    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(initial_count == 0)
            {
                //To ignore the connection initialisation
                initial_count++;
            }
            else if(initial_count >= 1)
            {
                message_recv = (String)dataSnapshot.child("author").getValue();
                newMessageRecieved(message_recv);
//                Log.d("Notif",message_recv+"new Message");
            }
            Log.d("Notif", "OnChildAdded");

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            //Log.d(TAG, databaseError.getMessage()); //Log errors
        }
    };




//    private void createNotificationChannel() {
//        // Create the NotificationChannel, but only on API 26+ because
//        // the NotificationChannel class is new and not in the support library
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = getString(R.string.channel_name);
//            String description = getString(R.string.channel_description);
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
//            channel.setDescription(description);
//            // Register the channel with the system; you can't change the importance
//            // or other notification behaviors after this
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mReference = FirebaseDatabase.getInstance().getReference();

        mReference = mReference.child("message");
        //mReference.keepSynced(false);
//        mReference.addChildEventListener(mListner);
        mReference.addValueEventListener(valueEventListener);

        startForeground();
        return super.onStartCommand(intent, flags, startId);

    }

    private void startForeground() {
        Intent notificationIntent = new Intent(this, MainChatActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                NOTIF_CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.drawable.speech_bubble)
                .setContentTitle("Notification Service")
                .setContentIntent(pendingIntent)
                .build());
    }

    private void newMessageRecieved(String author)
    {

        Intent notificationIntent = new Intent(this, MainChatActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIF_CHANNEL_ID_2)
                .setSmallIcon(R.drawable.speech_bubble)
                .setContentTitle("New Message")
                .setContentText(author)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(2, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mReference.removeEventListener(valueEventListener);

    }
}
