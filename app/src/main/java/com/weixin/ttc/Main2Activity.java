package com.weixin.ttc;

import android.app.ProgressDialog;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;



public class Main2Activity extends AppCompatActivity {

    private String tag,stop_ID;
    private ListView lv;
    private ProgressDialog dialog;
    String prediciton_list[];
    String url;
    MainActivity mainActivity;
    int count;

    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dialog = new ProgressDialog(this);
        sharedPreferences= getSharedPreferences("save_data", MODE_PRIVATE);
        dialog.setMessage("Loading...");

        mainActivity = new MainActivity();
        Bundle extra = getIntent().getExtras();
        //state == 0-> start intent from the list of stops, state == 1 -> start intent from saved_route
        if (extra.getInt("state")==0) {
            tag = extra.getString("tag");
            stop_ID = extra.getString("stop_ID");

            url = extra.getString("url");
            setTitle(extra.getString("stop_name"));
            url = url.replaceFirst("routeConfig", "predictions");
            url = url + "&s=" + tag;
        }
        else {

            url=extra.getString("url");
            stop_ID="";
        }
        final String final_url = url;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new GetPrediction().execute(final_url);

            }
        });
        count = sharedPreferences.getInt("count", -1);
        //set count for the save data, every time data added count +1;
        if (count == -1){
            sharedPreferences.edit().putInt("count",0).commit();
        }
        else {
            //count = sharedPreferences.getInt("count",-1);
        }
        new GetPrediction().execute(url);
    }
    public class GetPrediction extends AsyncTask<String,Void,String[]>{

        @Override
        protected void onPreExecute() {
            dialog.show();
            lv= (ListView)findViewById(R.id.listView_pridiction);
            if (!stop_ID.equals("")) {
                url = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a=ttc&stopId=";
                url += stop_ID;
            }
        }

        @Override
        protected void onPostExecute(String []s) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(),R.layout.list_stops_view,s);
            lv.setAdapter(adapter);
            dialog.hide();
        }

        @Override
        protected String[] doInBackground(String... params) {
            try {
                prediciton_list = mainActivity.parse(url,3,null);
                String return_val[] = new String[prediciton_list.length];
                for(int i =0; i < prediciton_list.length;i++){
                    String temp = prediciton_list[i];
                    if (temp.contains("---"))
                        return_val[i]=temp;
                    else
                        return_val[i]="Route#:" +"  "+ temp;
                }
                return return_val;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getBaseContext(),"Error",Toast.LENGTH_LONG).show();
                return null;
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_prediction,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(Main2Activity.this, "Please contact by sending email to weixin.wu6@gmail.com", Toast.LENGTH_LONG).show();
            return true;
        }
        else if (id == R.id.save){
            count = sharedPreferences.getInt("count",-1);
            boolean isExist = false;
            for (int i =0;i < count ; i ++){
                String temp = sharedPreferences.getString("url"+i,"error");
                if (temp.equals(url)){
                    isExist=true;
                    Toast.makeText(getBaseContext(),"Roote exists",Toast.LENGTH_SHORT).show();
                    break;
                }
            }
            if(!isExist) {
                sharedPreferences.edit().putString("stopName" + count, this.getIntent().getStringExtra("stop_name")).commit();
                sharedPreferences.edit().putString("url" + count, url).commit();
                //sharedPreferences.edit().putInt("route_number" + count, route_number).commit();
                count++;
                sharedPreferences.edit().putInt("count", count).commit();
                Toast.makeText(getBaseContext(), "Route saved", Toast.LENGTH_SHORT).show();
            }


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
