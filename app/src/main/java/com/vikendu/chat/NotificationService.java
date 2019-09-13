package com.vikendu.chat;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
            }
            Log.d("Notif", "OnChildAdded");

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mReference = FirebaseDatabase.getInstance().getReference();

        mReference = mReference.child("message");
        mReference.addValueEventListener(valueEventListener);

        startForeground();
        return super.onStartCommand(intent, flags, startId);

    }

    private void startForeground() {
        Intent notificationIntent = new Intent(this, MainChatActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        startForeground(NOTIF_ID, new NotificationCompat.Builder(this,
                NOTIF_CHANNEL_ID) //Notif channel created in MainChatActivity's onCreate()
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
        notificationManager.notify(2, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mReference.removeEventListener(valueEventListener);
    }
}
