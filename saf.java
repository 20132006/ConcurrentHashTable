import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sv2x.googlemap3.LoginAndRegister.SendPacketToMainThread;

import org.json.JSONArray;
import org.json.JSONException;
@@ -58,11 +59,9 @@ import java.util.Vector;

// cjoo: we need the implementations,
//which also requires functions of onConnected, onConnectionSuspended, onConnectionFailed
public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, Parser.ParsingFinishedListener {
public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,SendPacketToMainThread {


    int inpor = 0;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
@@ -70,6 +69,7 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
    private static boolean wifiConnected = false;   // state of WiFi
    private static boolean mobileConnected = false; // state of LTE/mobile
    public LatLng mLatLng;      // variable for (latitude, longitude)
    private double close_lat, close_lon,fLat,fLng;
    ////////////////////////written by me start/////////////////////////////

    MainActivity activity;
@@ -116,6 +116,9 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
    Vector<Double> Learder_lat = null;
    Vector<Double> Learder_lon = null;

    Vector<Double> Simulation_lat = null;
    Vector<Double> Simulation_lng = null;

    Vector<Integer> instruction_code = null;
    Vector<Integer> instruction_map = null;

@@ -164,30 +167,143 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
        //httpAsyncTask = (HttpAsyncTask) new HttpAsyncTask(this);
        activity=this;

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                // TODO Auto-generated method stub
                double lat = point.latitude;
                double lng = point.longitude;
                int i;
                double smallest_cost = -1.0;
                double cur_cost;
                Location str = new Location("Starting");
                Location end = new Location("Ending");

                Location location = new Location("Selected point");

                location.setLatitude(lat);
                location.setLongitude(lng);



                for (i = 0; i < Simulation_lat.size() - 1; i++) {
                    str.setLatitude(Simulation_lat.elementAt(i));
                    str.setLongitude(Simulation_lng.elementAt(i));

                    end.setLatitude(Simulation_lat.elementAt(i + 1));
                    end.setLongitude(Simulation_lng.elementAt(i + 1));

                    cur_cost = MatchingCost(str, end, location);
                    if (smallest_cost < 0) {
                        smallest_cost = cur_cost;
                        fLat = close_lat;
                        fLng = close_lon;
                    } else if (smallest_cost > cur_cost) {
                        smallest_cost = cur_cost;
                        fLat = close_lat;
                        fLng = close_lon;
                    }
                }
                Toast.makeText(MainActivity.this, "Selected Latitute and Logitute:" + lat + ", " + lng, Toast.LENGTH_SHORT).show();
                MarkerOptions marker = new MarkerOptions().position(new LatLng(fLat, fLng)).title("Matching selected point");

                map.addMarker(marker);
            }
        });

    }

    protected static double bearing(double lat1, double lon1, double lat2, double lon2){
    public double MatchingCost(Location starting, Location ending, Location loc)
    {
        double angle_A, angle_B;
        double a,b,c,h;
        double s,Area;
        double lat_dis,lon_dis;
        double c1,c2;

        lat_dis = ending.getLatitude() - starting.getLatitude();
        lon_dis = ending.getLongitude() - starting.getLongitude();

        a = ending.distanceTo(loc);
        b = starting.distanceTo(loc);
        c = starting.distanceTo(ending);

        a=Math.abs(a);
        b=Math.abs(b);
        c=Math.abs(c);

        s = (a+b+c)/2;

        Area = Math.sqrt( s * (s-a) * (s-b) * (s-c) );

        h = Area*2/c;

        angle_A = Math.toDegrees(Math.acos( ((b*b) + (c*c) - (a*a)) / (2*b*c) ));
        angle_B = Math.toDegrees(Math.acos( ((a*a) + (c*c) - (b*b)) / (2*a*c) ));

        c1 = Math.sqrt((b*b)-(h*h));
        c2 = Math.sqrt((a*a)-(h*h));

        double extra_lat,extra_lon,extra_ratio;

        if (90.0 - angle_A < 0.0)
        {
            close_lat = starting.getLatitude();
            close_lon = starting.getLongitude();
        }
        else if (90.0 - angle_B < 0.0)
        {
            close_lat = ending.getLatitude();
            close_lon = ending.getLongitude();
        }

        double longDiff= lon2-lon1;
        else
        {
            extra_ratio = c1/c;
            extra_lat = lat_dis * extra_ratio;
            extra_lon = lon_dis * extra_ratio;
            close_lat = starting.getLatitude() + extra_lat;
            close_lon = starting.getLongitude() + extra_lon;
        }

        double y = Math.sin(Math.toRadians(longDiff))*Math.cos(Math.toRadians(lat2));
        Location matched_location = new Location("Matched");

        double x = Math.cos(Math.toRadians(lat1))*Math.sin(Math.toRadians(lat2))-Math.sin(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(longDiff));
        matched_location.setLatitude(close_lat);
        matched_location.setLongitude(close_lon);

        return ( Math.toDegrees(Math.atan2(y, x)) + 360 ) % 360;
        return matched_location.distanceTo(loc);
    }

    private void initCamera(Location location, double angle)
    public void locate_all_points() throws JSONException
    {
        CameraPosition position = CameraPosition.builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .zoom(18)
                .bearing((float) angle)
                .tilt(90.0f)
                .build();
        boolean first_pisition=true;
        LatLng last_position=null;
        for (int i = 0; i < array_of_points.length(); i++) {
            double loc_lat, loc_lng;

        map.animateCamera(CameraUpdateFactory.newCameraPosition(position), null);
        //map.getUiSettings().setZoomControlsEnabled(true);
            loc_lat = (double) ((JSONArray) array_of_points.get(i)).get(0);
            loc_lng = (double) ((JSONArray) array_of_points.get(i)).get(1);
            LatLng marking_location = new LatLng(loc_lat, loc_lng);

            if (first_pisition == true) {
                MarkerOptions marker = new MarkerOptions().position(marking_location);
                map.addMarker(marker);
                last_position = marking_location;
                first_pisition = false;
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(marking_location, 18));
            } else {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(marking_location, 18));
                PolylineOptions line = new PolylineOptions()
                        .add(last_position)
                        .add(marking_location);
                map.addPolyline(line);
                last_position = marking_location;
            }
        }
        MarkerOptions marker = new MarkerOptions().position(last_position);

        map.addMarker(marker);
    }

    @Override
@@ -222,35 +338,12 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
                .show();
    }


    protected void sendEmail()
    {
        Log.i("Send email", "");
        String[] TO = {""};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message goes here");
    int until_index = 0;
    public void run_file(String file_name) {
        Simulation_lng = new Vector<>();
        Simulation_lat = new Vector<>();

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.i("Finished sending email...", "");
        }
        catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }


    public void run_file(String file_name)
    {
        try {
            fileInputStream = openFileInput(file_name.toString());
            inputStreamReader = new InputStreamReader(fileInputStream);
            char[] data = new char[data_block];
@@ -267,15 +360,18 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
                int i,index;
                LatLng last_position = null;
                boolean first_pisition = true;
                String info = final_data;
                //String info = final_data;

                //String info= "35.572361,129.191254,35.572666,129.191422,35.572853,129.191467,35.572868,129.19165,35.572891,129.191788,35.572922,129.191986,35.572929,129.192123,35.572922,129.192261,35.572895,129.192383,35.572716,129.193054,35.572864,129.192501,35.572835,129.192648,35.572811,129.192785,35.572785,129.192909,35.572594,129.193436,35.572548,129.193588";
                String info= "35.573284,129.191605,35.573196,129.191574,35.573128,129.191559,35.57304,129.191528,35.572853,129.191467,35.572868,129.19165,35.572891,129.191788,35.572891,129.191788,35.572922,129.191986,35.572926,129.192047,";
                while (info.length() > 0)
                {
                    i = info.indexOf(";");
                    i = info.indexOf(",");
                    String cLatitude,cLongitude;
                    cLatitude = info.substring(0, i);
                    info = info.substring(i + 1, info.length());

                    i = info.indexOf(";");
                    i = info.indexOf(",");
                    cLongitude = info.substring(0, i);
                    info = info.substring(i+1,info.length());
                    LatLng marking_location = new LatLng(Double.parseDouble(cLatitude), Double.parseDouble(cLongitude));
@@ -296,10 +392,14 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
                        map.addPolyline(line);
                        last_position = marking_location;
                    }
                    Simulation_lat.add(Double.parseDouble(cLatitude));
                    Simulation_lng.add(Double.parseDouble(cLongitude));
                }
                MarkerOptions marker = new MarkerOptions().position(last_position).title(info);
                if (last_position!=null) {
                    MarkerOptions marker = new MarkerOptions().position(last_position).title(info);

                map.addMarker(marker);
                    map.addMarker(marker);
                }

            } catch (IOException e) {
                e.printStackTrace();
@@ -309,9 +409,7 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
        }
    }


    public boolean remove_file(String file_name)
    {
    public boolean remove_file(String file_name) {
        int size =0;
        File dir = getFilesDir();//new File(path);
        File listFile[] = dir.listFiles();
@@ -332,41 +430,6 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
        return false;
    }



    public void locate_all_points() throws JSONException
    {
        boolean first_pisition=true;
        LatLng last_position=null;
        for (int i = 0; i < array_of_points.length(); i++) {
            double loc_lat, loc_lng;

            loc_lat = (double) ((JSONArray) array_of_points.get(i)).get(0);
            loc_lng = (double) ((JSONArray) array_of_points.get(i)).get(1);
            LatLng marking_location = new LatLng(loc_lat, loc_lng);

            if (first_pisition == true) {
                MarkerOptions marker = new MarkerOptions().position(marking_location);
                map.addMarker(marker);
                last_position = marking_location;
                first_pisition = false;
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(marking_location, 18));
            } else {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(marking_location, 18));
                PolylineOptions line = new PolylineOptions()
                        .add(last_position)
                        .add(marking_location);
                map.addPolyline(line);
                last_position = marking_location;
            }
        }
        MarkerOptions marker = new MarkerOptions().position(last_position);

        map.addMarker(marker);
    }



    public CharSequence [] get_arrayAdapter() {

        int size =0;
@@ -397,14 +460,11 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
        return filelist;
    }


    public boolean fileExistance(String fname) {
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }



    public void create_file() {
        getFilesDir();
        if (fileOutputStream != null)//We need to close file if we have using previouse file
@@ -427,14 +487,11 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
        }
    }


    public void write_data(String data) {
        Own_locations += data;
    }


    public void close_and_save()
    {
    public void close_and_save() {
        try {
            outputStreamWriter.write(Own_locations);
            outputStreamWriter.flush();
@@ -459,6 +516,7 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
            sendRegisterRequest();

            rxThread = new Receive(gSocket, MyState, uList, this);
            rxThread.addListener(this);
            MyState = rxThread.getUser();
            new Thread(rxThread).start();
        } else {
@@ -474,7 +532,6 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
@@ -633,8 +690,7 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
    ///cjoo

    @Override
    public void onConnected(Bundle bundle)
    {
    public void onConnected(Bundle bundle) {
        MyState.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (MyState.mLastLocation != null)
        {
@@ -659,8 +715,7 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co


    // cjoo: Need to use map and location services
    protected synchronized void buildGoogleApiClient()
    {
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
@@ -668,8 +723,7 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
                .build();
    }
    // cjoo: periodic location updates
    protected void createLocationRequest()
    {
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
@@ -685,7 +739,7 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co

                if (MyState.mLastLocation != null)
                {
                    initCamera(location,bearing(MyState.mLastLocation.getLatitude(), MyState.mLastLocation.getLongitude(),location.getLatitude(),location.getLongitude()));
                    //initCamera(location,bearing(MyState.mLastLocation.getLatitude(), MyState.mLastLocation.getLongitude(),location.getLatitude(),location.getLongitude()));
                }

                Location previous_location = MyState.mLastLocation;
@@ -696,7 +750,7 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
                showInstruction(location);

                mLatLng = new LatLng(MyState.mLastLocation.getLatitude(), MyState.mLastLocation.getLongitude());
                //map.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, MyState.mCurrentCameraLevel));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, MyState.mCurrentCameraLevel));

                if ( outputStreamWriter != null )
                {
@@ -719,8 +773,7 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
        };
    }

    public void showInstruction(Location location)
    {
    public void showInstruction(Location location) {
        MyState.mLastLocation = location;
        MyState.mLastUpdateTime = System.currentTimeMillis()/1000;
        EditText distanceBTW = (EditText) findViewById(R.id.distance);
@@ -729,89 +782,73 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
        {
            ImageButton inst_sign = (ImageButton) findViewById(R.id.image_sign);

            location.setLatitude(35.57287);
            location.setLongitude(129.191483);
            //location.setLatitude(35.57287);
            //location.setLongitude(129.191483);

            String instruction = ShowingInstruction.QueryInstructions(Learder_lat,Learder_lon,instruction_code,instruction_map,location);
            String distance = null;

            if (instruction.indexOf(",")>=0) {
                distance = instruction.substring(instruction.indexOf(",") + 1);
            }
            if (instruction != null) {

            if (instruction.indexOf("Left") >= 0)
            {
                inst_sign.setBackgroundResource(R.drawable.turnleft);
                inst_sign.setVisibility(View.VISIBLE);
                distanceBTW.setText(distance);
            }
            else if (instruction.indexOf("Right") >= 0)
            {
                inst_sign.setBackgroundResource(R.drawable.turnright);
                inst_sign.setVisibility(View.VISIBLE);
                distanceBTW.setText(distance);
            }
            else if (instruction.indexOf("GoStraight") >= 0)
            {
                inst_sign.setBackgroundResource(R.drawable.gostraight);
                inst_sign.setVisibility(View.VISIBLE);
                distanceBTW.setText(distance);
            }
            else if (instruction.indexOf("RoundAbout") >=0 )
            {
                inst_sign.setBackgroundResource(R.drawable.roundaboutsign);
                inst_sign.setVisibility(View.VISIBLE);
                distanceBTW.setText(distance);
            }
            else if (instruction.indexOf("UTurn") >=0 )
            {
                inst_sign.setBackgroundResource(R.drawable.u_turn);
                inst_sign.setVisibility(View.VISIBLE);
                distanceBTW.setText(distance);
            }
            else if (instruction.indexOf("ReachViaLocation") >=0 )
            {
                inst_sign.setBackgroundResource(R.drawable.reach_via_location);
                inst_sign.setVisibility(View.VISIBLE);
                distanceBTW.setText(distance);
            }
            else if (instruction.indexOf("NoTurn") >=0 )
            {
                inst_sign.setBackgroundResource(R.drawable.no_turn);
                inst_sign.setVisibility(View.VISIBLE);
                distanceBTW.setText(distance);
            }
            else if (instruction.indexOf("HeadOn") >=0 )
            {
                inst_sign.setBackgroundResource(R.drawable.head_on);
                inst_sign.setVisibility(View.VISIBLE);
                distanceBTW.setText(distance);
            }
            else{
                inst_sign.setVisibility(View.INVISIBLE);
                distanceBTW.setText("No Instruction");
                if (instruction.indexOf(",") >= 0) {
                    distance = instruction.substring(instruction.indexOf(",") + 1);
                }

                if (instruction.indexOf("Left") >= 0) {
                    inst_sign.setBackgroundResource(R.drawable.turnleft);
                    inst_sign.setVisibility(View.VISIBLE);
                    distanceBTW.setText(distance);
                } else if (instruction.indexOf("Right") >= 0) {
                    inst_sign.setBackgroundResource(R.drawable.turnright);
                    inst_sign.setVisibility(View.VISIBLE);
                    distanceBTW.setText(distance);
                } else if (instruction.indexOf("GoStraight") >= 0) {
                    inst_sign.setBackgroundResource(R.drawable.gostraight);
                    inst_sign.setVisibility(View.VISIBLE);
                    distanceBTW.setText(distance);
                } else if (instruction.indexOf("RoundAbout") >= 0) {
                    inst_sign.setBackgroundResource(R.drawable.roundaboutsign);
                    inst_sign.setVisibility(View.VISIBLE);
                    distanceBTW.setText(distance);
                } else if (instruction.indexOf("UTurn") >= 0) {
                    inst_sign.setBackgroundResource(R.drawable.u_turn);
                    inst_sign.setVisibility(View.VISIBLE);
                    distanceBTW.setText(distance);
                } else if (instruction.indexOf("ReachViaLocation") >= 0) {
                    inst_sign.setBackgroundResource(R.drawable.reach_via_location);
                    inst_sign.setVisibility(View.VISIBLE);
                    distanceBTW.setText(distance);
                } else if (instruction.indexOf("NoTurn") >= 0) {
                    inst_sign.setBackgroundResource(R.drawable.no_turn);
                    inst_sign.setVisibility(View.VISIBLE);
                    distanceBTW.setText(distance);
                } else if (instruction.indexOf("HeadOn") >= 0) {
                    inst_sign.setBackgroundResource(R.drawable.head_on);
                    inst_sign.setVisibility(View.VISIBLE);
                    distanceBTW.setText(distance);
                } else {
                    inst_sign.setVisibility(View.INVISIBLE);
                    distanceBTW.setText("No Instruction");
                }
            }

        }
    }

    protected void startLocationUpdates()
    {
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, mLocationListener);
        MyState.mRequestLocationUpdates = true;
    }

    protected void stopLocationUpdate()
    {
    protected void stopLocationUpdate() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, mLocationListener);
        MyState.mRequestLocationUpdates = false;
    }

    // cjoo: to restore previous value
    private void updateValuesFromBundle(Bundle savedInstanceState)
    {
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null)
        {
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY))
@@ -832,8 +869,7 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co

    // cjoo: check connectivity
    //      (Soon, we will not support Wi-Fi...)
    private boolean checkConnection()
    {
    private boolean checkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected())
@@ -849,34 +885,6 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
        }
    }

    /////////////////////////////////////////// This is an example (remove)
    private void runUDPExample() {
        new Thread(new Runnable() {
            String msg = "Hello Server";
            byte[] recvMsg = new byte[1000];
            //int port = 8001;

            @Override
            public void run() {
                try {
                    DatagramSocket s = new DatagramSocket();
                    //MyState.serverAddr = InetAddress.getByName("140.254.222.199");
                    DatagramPacket p = new DatagramPacket(msg.getBytes(), msg.length(), MyState.serverAddr, MyState.serverPort);
                    s.send(p);
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    /////////////////////////////////////////////

    // get Id & name
    public void getIdName(String ID, String NAME) {
        // cjoo: initial user input
@@ -891,14 +899,12 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
        MyState.send(gSocket, text.toString());
    }

    public void sendSpaceJoinRequest()
    {
    public void sendSpaceJoinRequest() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Space name");
        alert.setMessage("Type the space name to join");

        // Set an EditText view to get user input

        final EditText input = new EditText(this);
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
@@ -912,7 +918,7 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
                instruction_code = new Vector<>();
                instruction_map = new Vector<>();

                temp_First("{\"status\":200,\"status_message\":\"Found matchings\",\"matchings\":[{\"matched_names\":[\"\",\"\",\"유니스트길 (UNIST-gil)\",\"유니스트길 (UNIST-gil)\"],\"matched_points\":[[35.573284,129.191605],[35.573128,129.191559],[35.572891,129.191788],[35.572926,129.192047]],\"route_summary\":{\"total_time\":16,\"total_distance\":103},\"indices\":[0,1,2,3],\"instructions\":[[\"10\",\"\",18,0,1,\"18m\",\"S\",196,1,\"N\",16],[\"9\",\"\",32,2,10,\"31m\",\"S\",196,1,\"N\",16],[\"7\",\"유니스트길 (UNIST-gil)\",29,4,2,\"28m\",\"E\",83,1,\"W\",263],[\"9\",\"유니스트길 (UNIST-gil)\",24,6,1,\"23m\",\"N\",0,1,\"N\",0],[\"15\",\"\",0,9,0,\"0m\",\"N\",0,\"N\",0]],\"geometry\":[[35.573284,129.191605],[35.573196,129.191574],[35.573128,129.191559],[35.57304,129.191528],[35.572853,129.191467],[35.572868,129.19165],[35.572891,129.191788],[35.572891,129.191788],[35.572922,129.191986],[35.572926,129.192047]],\"hint_data\":{\"locations\":[\"_____w4aBwAAAAAADAAAAAMAAAAuAAAAFAAAAJZGBABOAQAAI84eAq1OswcCAAEB\",\"_____w4aBwAAAAAACwAAAAkAAAAZAAAAJAAAAJZGBABOAQAAhs0eAoFOswcBAAEB\",\"ABoHAP____8KHQAAFAAAABQAAAAZAAAAygEAAE9HBABOAQAAm8weAnJPswcBAAEB\",\"ABoHAP____8KHQAACAAAABMAAABLAAAAmQEAAE9HBABOAQAAvcweAnFQswcDAAEB\"],\"checksum\":1726661290}}]}\n");
                //temp_First("{\"status\":200,\"status_message\":\"Found matchings\",\"matchings\":[{\"matched_names\":[\"\",\"\",\"유니스트길 (UNIST-gil)\",\"유니스트길 (UNIST-gil)\"],\"matched_points\":[[35.573284,129.191605],[35.573128,129.191559],[35.572891,129.191788],[35.572926,129.192047]],\"route_summary\":{\"total_time\":16,\"total_distance\":103},\"indices\":[0,1,2,3],\"instructions\":[[\"10\",\"\",18,0,1,\"18m\",\"S\",196,1,\"N\",16],[\"9\",\"\",32,2,10,\"31m\",\"S\",196,1,\"N\",16],[\"7\",\"유니스트길 (UNIST-gil)\",29,4,2,\"28m\",\"E\",83,1,\"W\",263],[\"9\",\"유니스트길 (UNIST-gil)\",24,6,1,\"23m\",\"N\",0,1,\"N\",0],[\"15\",\"\",0,9,0,\"0m\",\"N\",0,\"N\",0]],\"geometry\":[[35.573284,129.191605],[35.573196,129.191574],[35.573128,129.191559],[35.57304,129.191528],[35.572853,129.191467],[35.572868,129.19165],[35.572891,129.191788],[35.572891,129.191788],[35.572922,129.191986],[35.572926,129.192047]],\"hint_data\":{\"locations\":[\"_____w4aBwAAAAAADAAAAAMAAAAuAAAAFAAAAJZGBABOAQAAI84eAq1OswcCAAEB\",\"_____w4aBwAAAAAACwAAAAkAAAAZAAAAJAAAAJZGBABOAQAAhs0eAoFOswcBAAEB\",\"ABoHAP____8KHQAAFAAAABQAAAAZAAAAygEAAE9HBABOAQAAm8weAnJPswcBAAEB\",\"ABoHAP____8KHQAACAAAABMAAABLAAAAmQEAAE9HBABOAQAAvcweAnFQswcDAAEB\"],\"checksum\":1726661290}}]}\n");

                MyState.isLeader = false;
                String value = input.getText().toString();
@@ -933,8 +939,7 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
        alert.show();
    }

    public void sendLeaveSpaceRequest()
    {
    public void sendLeaveSpaceRequest() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Do you want from space?");

@@ -964,8 +969,7 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
        alert.show();
    }

    void Init_space_info()
    {
    void Init_space_info() {
        MyState.isLeader = false;
        MyState.mLeadersLastLatLng = null;
        MyState.mySpaceId="";
@@ -975,8 +979,7 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
        map.clear();
    }
    // request Space Creation to the server
    private void sendSpaceCreateRequest()
    {
    private void sendSpaceCreateRequest() {
        final SpaceRegisterDialog dialog = new SpaceRegisterDialog();

        final Thread first = new Thread(new Runnable() {
@@ -1049,8 +1052,7 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
    /**************************************************************************************************************************/


    private void sendLeadersLastLocations(String Locations)
    {
    private void sendLeadersLastLocations(String Locations) {

        final CharSequence text;
        text = String.valueOf(MyState.id) + ";" + "Leader's Locations" + ";"
@@ -1064,8 +1066,7 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
    /**************************************************************************************************************************/


    private void FileHandling_Sharing()
    {
    private void FileHandling_Sharing() {
        final CharSequence List_of_files[] = get_arrayAdapter();
        final CharSequence List_of_handling[] = {"Email","Show up in map","SMS", "Edit","Remove","Share"};

@@ -1136,8 +1137,7 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
        builder_listOFfiles.show();
    }

    public void showToast(final String toast)
    {
    public void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
@@ -1145,8 +1145,7 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
        });
    }

    public void showMessage(String title, String Message)
    {
    public void showMessage(String title, String Message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
@@ -1154,8 +1153,7 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
        builder.show();
    }

    public void temp_First(String message)
    {
    public void temp_First(String message) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(message);
@@ -1271,7 +1269,6 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co

    @Override
    public void onTextParsed(JSONArray array_of_point, JSONArray array_of_instruc) {

        JSONArray instructionOnIndex = null;
        JSONArray latlon = null;
        LatLng latLng = null;
@@ -1297,26 +1294,6 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co

        }

        for (int i=0;i<array_of_point.length();i++)
        {

            try {
                latlon = (JSONArray) array_of_point.get(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                Learder_lat.add((double) latlon.get(0));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                Learder_lon.add((double) latlon.get(1));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        for (int i=0;i<array_of_instruc.length();i++)
        {
            int code_inst = 0,mapped_instructions = 0;
@@ -1340,6 +1317,5 @@ public class MainActivity extends FragmentActivity implements GoogleApiClient.Co
            instruction_map.add(old_len + mapped_instructions);

        }

    }
} 
