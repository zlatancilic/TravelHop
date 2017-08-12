package com.cilic.zlatan.travelhop;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseService extends FirebaseInstanceIdService {

    public static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    public static DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(MyFirebaseService.class.getName(), "Refreshed token: " + refreshedToken);
    }

    public static void sendRegistrationToServer(final String uid) {
        final String myToken = FirebaseInstanceId.getInstance().getToken();
        System.out.println("TOKEN FETCHED: " + myToken);
        if(myToken != null) {
            databaseReference.child("notificationTokens").child(uid).child(myToken).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    System.out.println("DATA SNAPSHOT NULL" + (dataSnapshot.exists()));
                    if(!dataSnapshot.exists()) {

                        databaseReference.child("notificationTokens").child(uid).child(myToken).setValue(myToken);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public static void removeRegistration(String uid) {
        String myToken = FirebaseInstanceId.getInstance().getToken();
        System.out.println("TOKEN FETCHED: " + myToken);
        if(myToken != null) {
            databaseReference.child("notificationTokens").child(uid).child(myToken).removeValue();
        }
    }
}
