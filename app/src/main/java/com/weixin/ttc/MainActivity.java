package com.weixin.ttc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity {
    String[] list_of_route;
    final static int parse_code_parse_direction = 1;
    final static int parse_code_parse_stops = 2;
    final static int parse_code_parse_prediction = 3;
    final static int parse_code_parse_routes = 5;
    int direction_code;//if the direction is north and south, direction_code is set 0; else direction code is set 1;
    ListView lv;
    String strUrl;
    String tempS;
    String[] tag;
    String stop_ID[];
    ProgressDialog dialog;
    int route_number;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        new routesTask().execute();
        lv = (ListView) findViewById(R.id.Route_list_view_id);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                strUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&terse&a=ttc&r=";
                //get the route number for the vehicle
                route_number = extractInt(list_of_route[position]);
                strUrl += route_number;
                new MyTask().execute(strUrl);
            }
        });
        sharedPreferences = getSharedPreferences("savedInfo",MODE_PRIVATE);
        rate_my_app();
    }

    private void rate_my_app(){
        int temp=sharedPreferences.getInt("rate_my_app",-1);
        if (temp ==-1){
            sharedPreferences.edit().putInt("rate_my_app",0).commit();
        }else if (temp>=0){
            temp+=1;
            sharedPreferences.edit().putInt("rate_my_app", temp).commit();
        }
        if (temp >6&& temp !=-2){
            Dialog dialog = new Dialog(MainActivity.this);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            String Message = "If you enjoy using this app, please take a moment to rate this app, Thank you for your time and support!";
            builder.setTitle("Rate the App");
            builder.setMessage(Message);
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setPositiveButton("Rate now", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("market://details?id=" + getPackageName()));
                    sharedPreferences.edit().putInt("rate_my_app", -2).commit();
                    startActivity(i);

                }
            });
            builder.setNegativeButton("No, Thanks", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sharedPreferences.edit().putInt("rate_my_app", -2).commit();

                }
            });
            dialog = builder.create();
            dialog.show();
        }
    }

    public String DialogShow(int direction){
        String dir[] = new String[2];
        final String[] direction_chosen = new String[1];
        if (direction ==0){
            dir[0] = "North";
            dir[1] = "South";
        }
        else {
            dir[0] = "East";
            dir[1] = "West";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(true);

        builder.setTitle("Select the direction");

        builder.setPositiveButton(dir[0], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (direction_code == 1)
                    tempS = "East";
                else
                    tempS = "North";
                Intent intent = new Intent(getBaseContext(), VehicleStops.class);
                intent.putExtra("address", strUrl);
                //intent.putExtra("stop_ID",stop_ID);
                intent.putExtra("direction",tempS);
                intent.putExtra("route_number",route_number);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(dir[1], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (direction_code == 1)
                    tempS = "West";
                else
                    tempS = "South";
                Intent intent = new Intent(getBaseContext(), VehicleStops.class);
                intent.putExtra("address", strUrl);
                intent.putExtra("direction",tempS);
                intent.putExtra("stop_ID",stop_ID);
                intent.putExtra("route_number",route_number);
                startActivity(intent);
            }
        });
        builder.show();
        return direction_chosen[0];
    }
    public class routesTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            String url = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeList&a=ttc";
            try {
                list_of_route = parse(url,parse_code_parse_routes,null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            lv.setAdapter(new ArrayAdapter<String>(getBaseContext(), R.layout.list_text_view, list_of_route));

        }
    }
    public class MyTask extends AsyncTask<String,Void,String[]> {
        @Override
        protected String[] doInBackground(String... params) {
            String temp[];
            try {
                temp = parse(params[0],1,null);
            } catch (Exception e) {
                temp =null;
                e.printStackTrace();
            }
            return temp;
        }
        @Override
        protected void onPreExecute() {
            dialog.show();
        }
        @Override
        protected void onPostExecute(String[] strings) {
            if (strings == null) {
                Toast.makeText(getBaseContext(),"Error,check the internet connection", Toast.LENGTH_SHORT).show();
                dialog.hide();
            } else {
                dialog.hide();
                if (strings[0].equals("South")) {
                    direction_code = 0;
                    DialogShow(direction_code);
                } else if (strings[0].equals("East")) {
                    direction_code = 1;
                    DialogShow(direction_code);
                } else {
                    //do it later;

                }
            }
        }
    }
    public String[] parse(String strUrl,int parse_code,String direction) throws Exception {

        String []temp;
        URL url = new URL(strUrl);

        URLConnection urlConnection = url.openConnection();
        //String qwe = urlConnection.getHeaderField("Accept-Encoding:");

        //Log.d("JSON", qwe);
        InputStream inputStream = null;

        if (!(urlConnection instanceof HttpURLConnection)){
            Log.i("JSON","Error in the if statement, instanceof");

        }
        HttpURLConnection httpURLConnection;
        try {

            httpURLConnection = (HttpURLConnection) urlConnection;
            httpURLConnection.setRequestMethod("GET");

            httpURLConnection.connect();
            inputStream = httpURLConnection.getInputStream();

        }catch (Exception e){
            Log.i("JSON","Error in parse");
            makeToast("error");

        }
        if (parse_code==0){
            //process the list of route
            temp = null;
        }
        else if (parse_code == parse_code_parse_direction) {
            temp = getDirection(inputStream);
        }
        else if (parse_code==parse_code_parse_stops){
            temp = getStops(inputStream,direction);
        }
        else if (parse_code ==parse_code_parse_prediction){
            temp = getPrediction(inputStream);
        }
        else if (parse_code==4){
            temp = stop_ID;
        }else if (parse_code == parse_code_parse_routes){
            temp = getRoutes(inputStream);
        }
        else{
            temp=null;
        }
        return temp;
    }

    private String[] getRoutes(InputStream inputStream) throws Exception {
        String[] returnValue = null;
        List<String> listElement = new ArrayList<String>();
        if (inputStream==null)
            return null;
        else {
            Element element = rootElement(inputStream);
            NodeList routes = element.getElementsByTagName("route");
            int length = routes.getLength();
            for (int i = 0 ; i < length;i++){
                listElement.add(((Element)routes.item(i)).getAttribute("title"));
            }
            returnValue = listElement.toArray(new String[listElement.size()]);
        }
        return returnValue;
    }

    public String[] getPrediction(InputStream inputStream){
        String returnValue[];
        try {
            Element element = rootElement(inputStream);
            List<String> listElement = new ArrayList<String>();
            NodeList nodeList_prediction = element.getElementsByTagName("predictions");
            for (int i = 0 ; i < nodeList_prediction.getLength();i++){
                NodeList nodeList = ((Element)nodeList_prediction.item(i)).getElementsByTagName("direction");
                for (int k = 0 ; k < nodeList.getLength();k++){
                    NodeList nl = ((Element)nodeList.item(k)).getElementsByTagName("prediction");
                    int length = nl.getLength();
                    if (length>3){ length =3;}
                    for (int j = 0 ; j < length ; j ++){
                        String routeNumber;
                        routeNumber= ((Element)nl.item(j)).getAttribute("branch");
                        listElement.add(routeNumber+"          "+((Element)nl.item(j)).getAttribute("minutes") +" Minutes");

                    }
                    listElement.add("--------------------------------------------------");
                }
            }
            returnValue = listElement.toArray(new String[listElement.size()]);
            return returnValue;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(),"Error",Toast.LENGTH_LONG).show();
            return null;
        }
    }
    public String[] getStops(InputStream inputStream,String direction) throws Exception {

        String names;
        String temp_element;
        Element element = rootElement(inputStream);
        int length=0;
        NodeList direction_nodeList = element.getElementsByTagName("direction");

        for (int j =0; j < direction_nodeList.getLength();j++){
            Element ele = (Element) direction_nodeList.item(j);
            if(ele.getAttribute("name").equals(direction)) {
                NodeList stop_nodelist = ele.getElementsByTagName("stop");
                length+=stop_nodelist.getLength();
            }
        }
        tag = new String[length];
        stop_ID = new String[length];
        String stop_name[] = new String[length];
        int k = 0;
        for (int j =0; j < direction_nodeList.getLength();j++){
            Element ele = (Element) direction_nodeList.item(j);
            if(ele.getAttribute("name").equals(direction)) {
                NodeList stop_nodelist = ele.getElementsByTagName("stop");
                for (int count =0 ;count < stop_nodelist.getLength(); count++,k++) {
                    temp_element = ((Element) stop_nodelist.item(count)).getAttribute("tag");
                    tag[k]= temp_element;
                    //find the stop name from the tag number
                    NodeList nodelist_of_route = element.getElementsByTagName("route");
                    NodeList nodelist_of_stops_in_route = ((Element)nodelist_of_route.item(0)).getElementsByTagName("stop");
                    for(int a =0; a < nodelist_of_stops_in_route.getLength();a++){
                        Element stop_element = (Element)nodelist_of_stops_in_route.item(a);
                        String check = stop_element.getAttribute("tag");
                        if (check.equals(temp_element)) {
                            names=stop_element.getAttribute("title");
                            stop_name[k]=names;
                            if (check.contains("ar")) {
                                stop_ID[k] = "";
                            }else {
                                stop_ID[k]=stop_element.getAttribute("stopId");
                            }
                            break;
                        }

                    }
                }
            }
        }

        return stop_name;

    }
    //if the direction is north and south, direction_code is set 0; else direction code is set 1;
    public String[] getDirection(InputStream inputStream) throws Exception {
        Element element = rootElement(inputStream);
        String direction[] = {"",""};

        NodeList nodeList = element.getElementsByTagName("direction");

        Element temp = (Element) nodeList.item(0);
        String dire = temp.getAttribute("name");
        if (dire.equals("South")||dire.equals("North")){
            direction[0]="South";
            direction[1]="North";
        } else {
            direction[0]="East";
            direction[1]="West";
        }
        return direction;
    }
    public int extractInt(String s){
        return (new Scanner(s).useDelimiter("[^\\d]+").nextInt());
    }

    public Element rootElement(InputStream inputStream) throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document dom = db.parse(inputStream);
        Element element = dom.getDocumentElement();
        return element;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        if (id == R.id.action_settings) {
            Toast.makeText(MainActivity.this, "Please contact by sending email to weixin.wu6@gmail.com", Toast.LENGTH_LONG).show();
            return true;
        }
        else if (id==R.id.Subway_map){
            startActivity(new Intent(getBaseContext(),Subway_map.class));
        }
        else if (id ==R.id.Save_routes){
            startActivity(new Intent(getBaseContext(),saveRoute.class));
        }

        return super.onOptionsItemSelected(item);
    }
    public void makeToast(String s){
        Toast.makeText(getBaseContext(),s,Toast.LENGTH_SHORT).show();
    }


}
