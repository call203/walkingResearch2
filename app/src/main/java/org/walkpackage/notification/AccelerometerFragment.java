package org.walkpackage.notification;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.jetbrains.annotations.NotNull;
import org.techtown.walkingresearch.R;

import static android.content.Context.SENSOR_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccelerometerFragment extends Fragment implements SensorEventListener{
    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mStepCountSensor;
    TextView xValue, yValue, zValue,time;
    private Thread thread;
    private Button btnStart, btnStop, btnReset;
    private LineGraphSeries<DataPoint> mSeriesAccelX, mSeriesAccelY, mSeriesAccelZ;
    private GraphView mGraphAccel;
    private double graphLastAccelXValue = 10d;
    //가속도 data
    float x = 0;
    float y = 0;
    float z = 0;
    Boolean flag = false;
    //시간 재기
    long start, end;
    //For 파이어베이스
    FirebaseAuth firebaseAuth;
    String uid;


    public AccelerometerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //파이어베이스 설정
        //해당 기록 user id 받기
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final View view = inflater.inflate(R.layout.fragment_accelerometer, container, false);
        //가속도 센서 설정
        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        //pedometer센서 설정
        mStepCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);



        mGraphAccel = initGraph((GraphView) view.findViewById(R.id.graph), "X, Y, Z direction Acceleration");

        //마쉬멜로우 버전 이상에서 권한 신청하기
        if(ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
            //ask for permission
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION},1);
        }



        xValue = (TextView) view.findViewById(R.id.xValue);
        yValue = (TextView) view.findViewById(R.id.yValue);
        zValue = (TextView) view.findViewById(R.id.zValue);
        time = (TextView) view.findViewById(R.id.time);

        xValue.setText("xValue: " + 0.0);
        yValue.setText("yValue: " + 0.0);
        zValue.setText("zValue: " + 0.0);
        time.setText("Time: " + 0.0 + "seconds");

        //버튼추가
        btnStart = (Button)view.findViewById(R.id.btnStart);
        btnStop = (Button)view.findViewById(R.id.btnStop);
        btnReset = (Button)view.findViewById(R.id.btnReset);

        //버튼 제어
        btnStart.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Handler handler = new Handler();
              handler.postDelayed(new Runnable() {
                  @Override
                  public void run() {
                      startAccel();
                  }
              },5000); //5초지연
          }
         });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopAccel();
            }
        });
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResetAccel();
            }
        });


        // Inflate the layout for this fragment
        return view;
    }


    public void startAccel(){


        mSeriesAccelX = initSeries(Color.BLUE, "X"); //라인 그래프를 그림
        mSeriesAccelY = initSeries(Color.RED, "Y");
        mSeriesAccelZ = initSeries(Color.GREEN, "Z");


        //그래프에 x,y,z 추가
        mGraphAccel.addSeries(mSeriesAccelX);
        mGraphAccel.addSeries(mSeriesAccelY);
        mGraphAccel.addSeries(mSeriesAccelZ);

        //시간
        start = System.currentTimeMillis();

        if (mAccelerometer != null) {
            mSensorManager.registerListener((SensorEventListener) this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        }

        if (mStepCountSensor != null) {

            mSensorManager.registerListener((SensorEventListener) this, mStepCountSensor, SensorManager.SENSOR_DELAY_FASTEST);

        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {



        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {


            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
           // long now = System.currentTimeMillis();

            xValue.setText("xValue: " + x);
            yValue.setText("yValue: " + y);
            zValue.setText("zValue: " + z);

            //파이어베이스에 저장
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RecordAccelerometer");
            ref.child(uid).child("x").push().setValue(Double.toString(x));
            ref.child(uid).child("y").push().setValue(Double.toString(y));
            ref.child(uid).child("z").push().setValue(Double.toString(z));
            //ref.child(uid).child("time").push().setValue(Double.toString(now/1000.0)); //초단위

            graphLastAccelXValue += 0.05d;

            mSeriesAccelX.appendData(new DataPoint(graphLastAccelXValue, x), true, 100);
            mSeriesAccelY.appendData(new DataPoint(graphLastAccelXValue, y), true, 100);
            mSeriesAccelZ.appendData(new DataPoint(graphLastAccelXValue, z), true, 100);



        }

        //[걸음 수 센서]
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            Log.d("걸음", String.valueOf(flag));

               //if(flag == false) {
                   //한걸음이 올라갔을때 저장
                   DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RecordAccelerometer");
                   ref.child(uid).child("one_step").child("x").push().setValue(Double.toString(x));
                   ref.child(uid).child("one_step").child("y").push().setValue(Double.toString(y));
                   ref.child(uid).child("one_step").child("z").push().setValue(Double.toString(z));
                   flag = true;
              // }


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not in use
    }

    //대기 함수
    public static class Sleep {
        public static void main(String[] args ) {
            System.out.println("sleep 실행 전");
            try {
                Thread.sleep(5000); //5초 대기
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("sleep 실행 후");
        }
    }

    //그래프 초기화
    public GraphView initGraph(@NotNull GraphView graph, String title) {
        //GraphView graph = getView().findViewById(id);
        //데이터가 늘어날때 x축 scroll이 생기도록
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(5);
        graph.getGridLabelRenderer().setLabelVerticalWidth(100);
        graph.setTitle(title);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        return graph;
    }

    //x,y,z 데이터 그래프 추가
    public LineGraphSeries<DataPoint> initSeries(int color, String title){
        LineGraphSeries<DataPoint> series;
        series = new LineGraphSeries<>();
        series.setDrawDataPoints(true);
        series.setDrawBackground(true);
        series.setColor(color);
        series.setTitle(title);
        return series;
    }

    //멈춤 버튼
    public void StopAccel(){
        end = System.currentTimeMillis();
        double totalTime = (end-start)/1000.0;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RecordAccelerometer");
        ref.child(uid).child("Totaltime").push().setValue(totalTime);

        time.setText("Time: " + totalTime + "seconds");

        Toast.makeText(getActivity(), "Record Accelerometer Data!", Toast.LENGTH_SHORT).show();
        mSensorManager.unregisterListener((SensorEventListener) this); // 센서 반납
    }

    //리셋버튼
    public void ResetAccel(){
        flag = false;
        xValue.setText("xValue: " + 0.0);
        yValue.setText("yValue: " + 0.0);
        zValue.setText("zValue: " + 0.0);
        time.setText("Time: " + 0.0 + "seconds");
        //그래프 초기화
        mGraphAccel.removeAllSeries();


    }
}