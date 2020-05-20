package org.walkpackage.notification;


import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;
import org.techtown.walkingresearch.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FitbitFragment extends Fragment {

    private TextView tv_outPut;
    private StringBuffer sb = new StringBuffer();

    public FitbitFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.fragment_fitbit, container, false);
         //위젯에 대한 참조
         tv_outPut = (TextView) view.findViewById(R.id.tv_outPut);

         //URL 설정
         String url = "https://01c78603.ngrok.io/settingweb1/restex";

         //AsyncTask를 통해 HttpURLConnection 수행.
        NetworkTask networkTask = new NetworkTask(url, null);
        networkTask.execute();
        return view;
    }

    public class NetworkTask extends AsyncTask<Void, Void, String>{

        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result;
            RequestHttpConnection requestHttpURLConnection = new RequestHttpConnection();
            result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try{

                JSONArray fitbitArray = new JSONArray(s);
                Log.d("배열","ok");

                for(int i=0; i<fitbitArray.length(); i++) {
                    JSONObject fitbitObject = fitbitArray.getJSONObject(i);
                    Log.d("배열원소","ok");

                    String a = fitbitObject.getString("director");
                    String d = fitbitObject.getString("types");
                    String b = fitbitObject.getString("movie_id");
                    String c = fitbitObject.getString("movie_name");


                    sb.append(a +"  " + b +"  "+ c + "  " + d + "  " );
                }
                 tv_outPut.setText(sb);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

}
