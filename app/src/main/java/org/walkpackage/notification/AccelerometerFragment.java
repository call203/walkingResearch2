package org.walkpackage.notification;


import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    TextView xValue, yValue, zValue,stepCount;
    private Thread thread;
    private Button btnStart, btnStop, btnReset;
    private LineGraphSeries<DataPoint> mSeriesAccelX, mSeriesAccelY, mSeriesAccelZ;
    private GraphView mGraphAccel;
    private double graphLastAccelXValue = 10d;

    //현재 걸음 수
    private int mSteps = 0;
//    //리스너가 등록되고 난 후의 step count
//    private int mCounterSteps = 0;

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
        mStepCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);



        mGraphAccel = initGraph((GraphView) view.findViewById(R.id.graph), "X, Y, Z direction Acceleration");




        xValue = (TextView) view.findViewById(R.id.xValue);
        yValue = (TextView) view.findViewById(R.id.yValue);
        zValue = (TextView) view.findViewById(R.id.zValue);
        stepCount = (TextView) view.findViewById(R.id.stepcount);

        xValue.setText("xValue: " + 0.0);
        yValue.setText("yValue: " + 0.0);
        zValue.setText("zValue: " + 0.0);
        stepCount.setText("걸음수: " + 0 +" 걸음");

        //버튼추가
        btnStart = (Button)view.findViewById(R.id.btnStart);
        btnStop = (Button)view.findViewById(R.id.btnStop);
        btnReset = (Button)view.findViewById(R.id.btnReset);

        //버튼 제어
        btnStart.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              startAccel();
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
        if (mAccelerometer != null) {
            mSensorManager.registerListener((SensorEventListener) this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (mStepCountSensor != null) {
            //센서의 속도 설정
            mSensorManager.registerListener(this, mStepCountSensor, SensorManager.SENSOR_DELAY_GAME);

        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float x, y, z;
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];

            xValue.setText("xValue: " + x);
            yValue.setText("yValue: " + y);
            zValue.setText("zValue: " + z);

            //파이어베이스에 저장
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RecordAccelerometer");
            ref.child(uid).child("x").push().setValue(Double.toString(x));
            ref.child(uid).child("y").push().setValue(Double.toString(y));
            ref.child(uid).child("z").push().setValue(Double.toString(z));

            graphLastAccelXValue += 0.05d;

            mSeriesAccelX.appendData(new DataPoint(graphLastAccelXValue, x), true, 100);
            mSeriesAccelY.appendData(new DataPoint(graphLastAccelXValue, y), true, 100);
            mSeriesAccelZ.appendData(new DataPoint(graphLastAccelXValue, z), true, 100);
        }

        //[걸음 수 센서]
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

            //stepcountsenersor는 앱이 꺼지더라도 초기화 되지않는다. 그러므로 우리는 초기값을 가지고 있어야한다.
//            if (mCounterSteps < 1) {
//                // initial value
//                mCounterSteps = (int) event.values[0];
//            }
            //리셋 안된 값 + 현재값 - 리셋 안된 값
            mSteps = (int) event.values[0];
            stepCount.setText("걸음수: " + Integer.toString(mSteps)+" 걸음");

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not in use
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
        Toast.makeText(getActivity(), "Record Accelerometer Data!", Toast.LENGTH_SHORT).show();
        mSensorManager.unregisterListener((SensorEventListener) this); // 센서 반납
    }
    //리셋버튼
    public void ResetAccel(){

        xValue.setText("xValue: " + 0.0);
        yValue.setText("yValue: " + 0.0);
        zValue.setText("zValue: " + 0.0);

        mGraphAccel.removeAllSeries();


    }
}