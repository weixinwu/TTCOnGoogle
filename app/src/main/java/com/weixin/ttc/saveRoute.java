package com.weixin.ttc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



public class saveRoute extends AppCompatActivity {

    ListView listView_saveRoute;
    SharedPreferences sf;
    String url;
    TextView tv ;
    String list_of_stops[];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_save_route);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView_saveRoute = (ListView)findViewById(R.id.saved_route_list_view);
        tv = (TextView)findViewById(R.id.textView2);
        sf = getSharedPreferences("save_data", MODE_PRIVATE);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
        if (sf.getInt("count",-1)==-1){
            listView_saveRoute.setVisibility(View.INVISIBLE);
            tv.setVisibility(View.VISIBLE);
        }
        else {

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    sf.edit().clear().commit();
                    Toast.makeText(getBaseContext(), "All routes have been deleted", Toast.LENGTH_SHORT).show();
                    listView_saveRoute.setVisibility(View.INVISIBLE);
                    tv.setVisibility(View.VISIBLE);
                    list_of_stops = null;
                }
            });
            new FillStops().execute();
        }

    }
    private class FillStops extends AsyncTask<Void,Void,String[]>{

        @Override
        protected void onPreExecute() {

            listView_saveRoute.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        url = sf.getString("url" + position, "Error");
                        //int route_number = sf.getInt("route_number" + position, -1);
                        if (url.equals("Error"))
                            Toast.makeText(getBaseContext(), "Error,please clear the saved data", Toast.LENGTH_SHORT).show();
                        else {
                            Intent intent = new Intent(getBaseContext(), Main2Activity.class);
                            intent.putExtra("url", url);
                            intent.putExtra("state", 1);
                            //intent.putExtra("route_number", route_number);
                            startActivity(intent);

                    }
                }
            });
        }

        @Override
        protected void onPostExecute(String []s) {
            if (s ==null){

                tv.setVisibility(View.VISIBLE);
            }
            else {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.list_text_view, s);
                listView_saveRoute.setAdapter(adapter);
            }
        }

        @Override
        protected String[] doInBackground(Void... params) {
            int count = 0;
            count  = sf.getInt("count",-1);
            if (count==-1){
                Toast.makeText(getBaseContext(),"data has been clear return null",Toast.LENGTH_SHORT).show();

                return null;
            }
            else {
                list_of_stops= new String[count];
                for (int i = 0; i < count; i++) {
                    list_of_stops[i] = sf.getString("stopName" + i, "Not found");
                }
                return list_of_stops;
            }

        }
    }

}
