package com.example.vivin.chatapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendRequestBtn;

    private DatabaseReference mUsersDatabase;

    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private ProgressDialog mProgressDialog;

    private FirebaseUser mCurrentUser;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_displayName);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView) findViewById(R.id.profile_totalFriends);
        mProfileSendRequestBtn = (Button) findViewById(R.id.profile_send_req_btn);

        mCurrent_state ="not_friends";

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while the page loads");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name= dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.defaultavatar).into(mProfileImage);


                //Check for request
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){

                            String requestType = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(requestType.equals("received")){

                                mCurrent_state= "req_received";
                                mProfileSendRequestBtn.setText("ACCEPT FRIEND REQUEST");

                            }else if (requestType.equals("sent")){

                                mCurrent_state= "req_sent";
                                mProfileSendRequestBtn.setText("CANCEL FRIEND REQUEST");

                            }
                        }

                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileSendRequestBtn.setEnabled(false);
                                         // ----------          NOT FRIEND STATE          ----------//

                if(mCurrent_state.equals("not_friends")){

                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type").setValue("received")
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                mProfileSendRequestBtn.setEnabled(true);
                                                mCurrent_state= "req_sent";
                                                mProfileSendRequestBtn.setText("CANCEL FRIEND REQUEST");

                                                Toast.makeText(ProfileActivity.this, "Request sent.", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }else
                                Toast.makeText(ProfileActivity.this, "Request Not sent.", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                                         // ----------          NOT FRIEND STATE          ----------//


                                     // ----------          CANCEL REQUEST STATE          ----------//

                if(mCurrent_state.equals("req_sent")){

                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendRequestBtn.setEnabled(true);
                                    mCurrent_state="not_friends";
                                    mProfileSendRequestBtn.setText("SEND FRIEND REQUEST");
                                }
                            });

                        }
                    });

                }

                                     // ----------          CANCEL REQUEST STATE          ----------//

                                     // ----------          REQUEST RECEIVED STATE          ----------//
                if(mCurrent_state.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {


                                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    mProfileSendRequestBtn.setEnabled(true);
                                                                    mCurrent_state="friends";
                                                                    mProfileSendRequestBtn.setText("UNFRIEND");
                                                                }
                                                            });

                                                }
                                            });


                                }
                            });
                        }
                    });

                }

                                     // ----------          REQUEST RECEIVED STATE         ----------//
            }
        });

    }
}
