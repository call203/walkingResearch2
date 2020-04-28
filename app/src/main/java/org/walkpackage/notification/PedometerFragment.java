package org.walkpackage.notification;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.techtown.walkingresearch.R;
import org.walkpackage.notification.adapters.AdapterSpeed;
import org.walkpackage.notification.models.recordmodel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */

//가속도 센서 만보기 계산 API
//-->  https://copycoding.tistory.com/5?category=1014356
public class PedometerFragment extends Fragment implements SensorEventListener {

    private static final String TAG = "PedometerRagment";
    Button mResetBtn, mStartBtn, mStopBtn, mBluetoothBtn;
    TextView mwalknum;
    TextView mGetspeed, mCalspeed, mNow, mLastTime, mGap;
    //현재 걸음 수
    private int mSteps = 0;
    //리스너가 등록되고 난 후의 step count
    private int mCounterSteps = 0;


    //센서 연결을 위한 변수
    private SensorManager mSensorManager;
    private Sensor mStepCountSensor, mLocationSensor;

    //속도 센서를 위한 변수
    protected LocationManager mLocationManager;
    private Location mLastlocation = null;
    long startTime, endTime;
    private double totalDistance = 0D;
    Date startdate, enddate;

    //기록을 위한 변수
    FirebaseAuth firebaseAuth;
    String uid;
    RecyclerView RecordrecyclerView;
    List<recordmodel> RecordModellist;
    AdapterSpeed adapterSpeed;
    List<String> keylist;

    double speed;
    double distance;
    double calspeed;
    double diff;

    private View view;


    public PedometerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_pedometer, container, false);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Toolbar PedometerTool = view.findViewById(R.id.pedometerTool);
        ((AppCompatActivity) getActivity()).setSupportActionBar(PedometerTool);

        //센서 연결 [ 걸음수 센서 & 위치 센서 ]
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mStepCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        loadRecordSpeed();

        mResetBtn = view.findViewById(R.id.reset_btn);
        mStartBtn = view.findViewById(R.id.start_btn);
        mStopBtn = view.findViewById(R.id.stop_btn);
        mBluetoothBtn = view.findViewById(R.id.Bluetooth_Btn);
        mGetspeed = view.findViewById(R.id.getSpeed);
        mCalspeed = view.findViewById(R.id.calSpeed);
        mNow = view.findViewById(R.id.now);
        mLastTime = view.findViewById(R.id.LastTime);
        mGap = view.findViewById(R.id.dist_gap);
        mwalknum = view.findViewById(R.id.walknum);

        //recycler view
        firebaseAuth = FirebaseAuth.getInstance();
        RecordrecyclerView = view.findViewById(R.id.recordspeed);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //지금까지의 기록
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        RecordModellist = new ArrayList<>();
        keylist = new ArrayList<>();
        //loadRecordSpeed();

        mBluetoothBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), BluetoothActivity.class));
            }
        });


        //초기화 버튼 : 다시 숫자를 0으로 만들어준다.
        mResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSteps = 0;
                mCounterSteps = 0;
                mwalknum.setText(Integer.toString(mSteps));

                mGetspeed.setText("속도 : ");
                mCalspeed.setText("계산 속도(속 = 거 /시 ) : ");
                mNow.setText("시작시간 : ");
                mLastTime.setText("멈춘시간 : ");
                mGap.setText("이동거리 : ");
                totalDistance = 0D;

            }
        });


        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 사용자 권한 요청
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);


        }


        //시작 버튼  [위치 센서]
        mStartBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //위치 권한이 없을 시
                if (Build.VERSION.SDK_INT >= 23 &&
                        ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            0);
                } else {
                    //시작 시간 구하기
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    startdate = new Date();
                    String formatDate = sdf.format(startdate);
                    mNow.setText(" 시작 시간: " + formatDate);
                    //시작 버튼 누른 초 시간
                    startTime = System.currentTimeMillis();


                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.i(TAG, "이건 실패");

                        return;
                    }
                    Log.i(TAG, "이건 여기까지 잘 넘어옴");
                    //시작 위치를 받아옴
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
                    Log.i(TAG, "이건 여기까지는?");


                }
            }
        });

        //멈출때
        mStopBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //stop버튼 누른 초 시간
                endTime = System.currentTimeMillis();

                //멈춘 시간
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                enddate = new Date();
                String formatDate = sdf.format(enddate);
                mLastTime.setText(" 멈춘 시간: " + formatDate);
                mGetspeed.setText("속도 : 수신중...");

            }


        });

        return view;
    }

    //기록들 불러오기
    private void loadRecordSpeed() {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RecordSpeed");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                RecordModellist.clear();
                   for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        //데이터를 담기위한 객체 모델
                       recordmodel Recordmodel = ds.getValue(recordmodel.class);
                       //이 객체를 리스트에 넣는다.
                       RecordModellist.add(Recordmodel);
                       //이 리스트들을 어댑터에 넣어진다.
                       adapterSpeed = new AdapterSpeed(getActivity(), RecordModellist);
                       RecordrecyclerView.setAdapter(adapterSpeed);
                   }
                }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //stop 버튼 누를 후 기록 하기
    private void uploadData() {


        //테이블 만들기
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("mgetspeed", Double.toString(speed));
        hashMap.put("mcalspeed", Double.toString( Double.parseDouble(String.format("%.3f", calspeed))));
        hashMap.put("mdistance", Double.toString(distance));
        hashMap.put("mtimediff", Double.toString(diff));

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RecordSpeed");
        ref.push().setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "recordSpeed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Fail_recordSpeed", Toast.LENGTH_SHORT).show();
                    }
                });


    }

    //위치 리스너로 속도 이동거리 받아 계산하기
    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {

            if (mLastlocation != null) {

                //속도
                speed = Double.parseDouble(String.format("%.3f", location.getSpeed()));
                mGetspeed.setText("속도 : " + speed + "m/s");
                float disance[] = new float[1];
                Log.d(TAG, mLastlocation.getLatitude() + " " + mLastlocation.getLongitude() + " " + location.getLatitude() + " " + location.getLongitude());
                Location.distanceBetween(mLastlocation.getLatitude(), mLastlocation.getLongitude(), location.getLatitude(), location.getLongitude(), disance);
                PedometerFragment.this.totalDistance += disance[0];
                distance = Double.parseDouble(String.format("%.3f", totalDistance));

                mGap.setText("이동거리 : " + distance + "m");
                //계산속도
                diff = (endTime - startTime) /1000.0;  //초단위
                calspeed = distance / diff; //m/s를 만들어 주기 위해서

                mCalspeed.setText("계산 속도(속 = 거 /시 ) : " + Double.parseDouble(String.format("%.3f", calspeed)) + "m/s");

            }

            // Update stored location
            PedometerFragment.this.mLastlocation = location;
            Log.d(TAG, String.valueOf(totalDistance) + "!!!!!!");

            if (!mGetspeed.getText().equals("속도 : 수신중...") && !mGetspeed.getText().equals("속도 : ")) {
                Log.i(TAG, speed+" " + distance +" " + calspeed+" " + diff);
                uploadData();
                loadRecordSpeed();
                Log.i(TAG, "수신 꺼짐");
                mLocationManager.removeUpdates(locationListener);
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    //SensorEventListener [걸음수센서] 의 메소드들
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

    }

    public void onStart() {
        super.onStart();
        if (mStepCountSensor != null) {
            //센서의 속도 설정
            mSensorManager.registerListener(this, mStepCountSensor, SensorManager.SENSOR_DELAY_GAME);

        }

    }

    public void onStop() {
        super.onStop();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //[걸음 수 센서]
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

            //stepcountsenersor는 앱이 꺼지더라도 초기화 되지않는다. 그러므로 우리는 초기값을 가지고 있어야한다.
            if (mCounterSteps < 1) {
                // initial value
                mCounterSteps = (int) event.values[0];
            }
            //리셋 안된 값 + 현재값 - 리셋 안된 값
            mSteps = (int) event.values[0] - mCounterSteps;
            mwalknum.setText(Integer.toString(mSteps));

        }


    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
