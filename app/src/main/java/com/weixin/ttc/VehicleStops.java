package com.weixin.ttc;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class VehicleStops extends AppCompatActivity {
    String urlAddr;
    String direction;
    MainActivity mainActivity;
    String stops[];
    String tag[],stop_ID[];
    ListView lv_of_stops;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_stops);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mainActivity = new MainActivity();
        urlAddr = null;
        Bundle extra = getIntent().getExtras();
        if(extra!=null){
            urlAddr = extra.getString("address");
            //stop_ID = extra.getStringArray("stop_ID");
            direction= extra.getString("direction");

        }
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading Stops...");
        new getStops().execute(urlAddr);

        lv_of_stops.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getBaseContext(),Main2Activity.class);
                intent.putExtra("tag", tag[position]);
                intent.putExtra("stop_name",stops[position]);
                intent.putExtra("stop_ID",stop_ID[position]);
                intent.putExtra("url",urlAddr);
                intent.putExtra("state",0);

                startActivity(intent);
            }
        });
    }
    public class getStops extends AsyncTask<String,Void,String[]>{
        @Override
        protected String[] doInBackground(String... params) {
            try {
                stops= mainActivity.parse(params[0],2,direction);
                stop_ID = mainActivity.parse(params[0],4,direction);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return stops;
        }
        @Override
        protected void onPreExecute() {
            dialog.show();
            lv_of_stops=(ListView)findViewById(R.id.list_view_of_stops);
        }
        @Override
        protected void onPostExecute(String []s) {

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(),R.layout.list_stops_view,s);
            lv_of_stops.setAdapter(adapter);
            tag=mainActivity.tag;
            dialog.hide();
        }
    }
}
