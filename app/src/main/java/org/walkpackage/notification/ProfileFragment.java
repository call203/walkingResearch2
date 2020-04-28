package org.walkpackage.notification;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.techtown.walkingresearch.R;

public class ProfileFragment extends Fragment {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;


    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private TextView mEmail, mName;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Toolbar ProfileTool = view.findViewById(R.id.progileTool);
        ((AppCompatActivity)getActivity()).setSupportActionBar(ProfileTool);
        setHasOptionsMenu(true);

       firebaseAuth = FirebaseAuth.getInstance();
       currentUser = firebaseAuth.getCurrentUser();
       firebaseDatabase = FirebaseDatabase.getInstance();
       databaseReference = firebaseDatabase.getReference("Users");


       mEmail = view.findViewById(R.id.emailline);
       mName = view.findViewById(R.id.profile_name);

        //Users 자식인 email 불러오기
        //email과 지금 사용자의 email과 같은 곳 찾기
        Query query = databaseReference.orderByChild("email").equalTo(currentUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();

                    mName.setText(name);
                    mEmail.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // destroy all menu and re-call onCreateOptionsMenu
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.appbar_action, menu) ;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings :
                firebaseAuth.signOut();
                getActivity().finish();
                startActivity(new Intent(getActivity(), MainActivity.class));



                    //((BottomNaviActivity) getActivity()).logout();
                return true;
            default :
                return super.onOptionsItemSelected(item) ;
        }
    }

    }
