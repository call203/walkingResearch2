package org.walkpackage.notification.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import org.techtown.walkingresearch.R;
import org.walkpackage.notification.models.recordmodel;

import java.util.List;

/* 데이터 집합을 관리하고 뷰를 생성*/
public class AdapterSpeed extends RecyclerView.Adapter<AdapterSpeed.MyHolder> {

    Context context;
    List<recordmodel> RecordModellist;
    String Uid;

    public AdapterSpeed(Context context, List<recordmodel> recordModellist) {
        this.context = context;
        this.RecordModellist = recordModellist;
        if(Uid !=null)
            Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


    @NonNull
    //아이템 뷰를 관리하는 viewHolder 객체 생성
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_recordspeed,parent,false);
        return new AdapterSpeed.MyHolder(view);
    }
    //position에 해당하는 데이터를 viewholder가 관리하는 view에 바인딩
    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        final String uid = RecordModellist.get(i).getUid();
        final String getspeed = RecordModellist.get(i).getMgetSpeed();
        final String calspeed = RecordModellist.get(i).getMcalSpeed();
        final String getdistance = RecordModellist.get(i).getMdistance();
        final String gettimegab = RecordModellist.get(i).getMtimediff();

        Log.d(uid, " " + getspeed+" "+ calspeed+" " + getdistance+" " + gettimegab+ " ");
        if(getspeed!=null) {

            myHolder.mGetspeed.setText(getspeed);
            myHolder.mCalspeed.setText(calspeed);
            myHolder.mDistance.setText(getdistance);
            myHolder.mTimegab.setText(gettimegab);
        }
    }

    @Override
    public int getItemCount() {

        return RecordModellist.size();
    }

    //각 list에 들어갈 객체의 맴버 변수
    class MyHolder extends RecyclerView.ViewHolder{
        TextView mGetspeed, mCalspeed, mTimegab, mDistance;

        public MyHolder(View itemView) {

            super(itemView);

            mGetspeed = itemView.findViewById(R.id.speedtxt);
            mCalspeed = itemView.findViewById(R.id.calSpeedtxt);
            mTimegab = itemView.findViewById(R.id.timegabtxt);
            mDistance = itemView.findViewById(R.id.disttxt);


        }


    }

}

