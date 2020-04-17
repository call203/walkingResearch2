package org.walkpackage.notification;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.techtown.walkingresearch.R;

public class BottomNaviActivity extends AppCompatActivity {

    //bottom navigation에 해당 fragment 넣기
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private ProfileFragment profileFragment = new ProfileFragment();
    private PedometerFragment  pedometerFragment= new PedometerFragment();
    private AccelerometerFragment accelerometerFragment = new AccelerometerFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navi);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frameLayout, pedometerFragment).commitAllowingStateLoss();

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){


            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                switch(menuItem.getItemId()){
                    case R.id.Pedometer :
                        transaction.replace(R.id.frameLayout,pedometerFragment).commitAllowingStateLoss();
                        break;
                    case R.id.acceleration :
                        transaction.replace(R.id.frameLayout,accelerometerFragment).commitAllowingStateLoss();
                        break;
                    case R.id.profile :
                        transaction.replace(R.id.frameLayout,profileFragment).commitAllowingStateLoss();
                        break;
                }
                return true;
            }
        });
    }
}
