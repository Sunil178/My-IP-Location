package com.evanhe.myiplocation;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.content.Context;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaDrm;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private String device_name, android_OS, android_device, android_model, android_brand, android_product, unique_device_id, build_id, display_id, locale, manufaturer, network, abi, tags, android_id, address, city, htmlText;
    private String imei = "Not Supported";
    public static String proxy_string, location_string = "", ip_string = "", ip_city = "";
    private boolean googlePlayServicesAvailable;
    private int sdk_version;
    String gid = "";
    public static WebView browser;
    public static int REQUEST_CODE_CHECK_SETTINGS = 101;
    public static int REQUEST_CODE_READ_PHONE = 100;
    FusedLocationProviderClient mFusedLocationClient;
    Geocoder geocoder;
    List<Address> addresses;
    public static EditText proxy;
    public static Button set_proxy;
    public static Handler handler;
    public static Runnable runnable;
    public static boolean server_status;
    public static boolean location_status;
    public static boolean ip_status;

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainActivity.location_status = false;
        MainActivity.ip_status = false;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());
        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        this.network = telephonyManager.getNetworkOperatorName();

        browser = (WebView) findViewById(R.id.webview);
        browser.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                try {
                    new GetPublicIP().execute();

                    if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_CHECK_SETTINGS);
                    }
                    else {
                        getLastLocation();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        browser.setWebChromeClient(new WebChromeClient());
        browser.getSettings().setJavaScriptEnabled(true);
        htmlText = "<!DOCTYPE html><html><head><style type=\"text/css\">\n" +
                "#toast {" +
                "position: fixed;" +
                "display: block;" +
                "bottom: 2em;" +
                "height: 2em;" +
                "width: 10em;" +
                "left: calc(50% - 5em);" +
                "animation: toast-fade-in 1s 2 alternate;" +
                "background-color: black;" +
                "border-radius: 2em;" +
                "color: white;" +
                "text-align: center;" +
                "padding: 1em;" +
                "line-height: 2em;" +
                "opacity: 0;" +
                "}" +
                "@keyframes toast-fade-in {" +
                "from {" +
                "opacity: 0;" +
                "}" +
                "to {" +
                "opacity: 1;" +
                "}" +
                "}" +
                "</style></head><body><b>Network:</b> <i>" + network + "</i><span id='pip'><br><br><b>IP Address:</b> Searching...</span><span id='ipregion'><br><br><b>IP Region:</b> Searching...</span><span id='ipcity'><br><br><b>IP City:</b> Searching...</span><span id='location'><br><br><b>Location:</b> Searching...</span><span id='address'><br><br><b>Address:</b> Searching...</span><span id='city'><br><br><b>Location City:</b> Searching...</span>" +
                "<script type=\"text/javascript\">" +
                "function updateLocation(lat, long) {" +
                "document.getElementById('location').innerHTML = \"<br><br><b>Location:</b> <i>\" + lat + \", \" + long + \"</i>\";" +
                "}" +
                "function updateAddress(address) {" +
                "document.getElementById('address').innerHTML = \"<br><br><b>Address:</b> <i>\" + address + \"</i>\";" +
                "}" +
                "function updateCity(city) {" +
                "document.getElementById('city').innerHTML = \"<br><br><b>Location City:</b> <i>\" + city + \"</i>\";" +
                "}" +
                "function updateIP(ip) {" +
                "document.getElementById('pip').innerHTML = \"<br><br><b>IP Address:</b> <i>\" + ip + \"</i>\";" +
                "}" +
                "function updateIpRegion(region) {" +
                "document.getElementById('ipregion').innerHTML = \"<br><br><b>IP Region:</b> <i>\" + region + \"</i>\";" +
                "}" +
                "function updateIpCity(city) {" +
                "document.getElementById('ipcity').innerHTML = \"<br><br><b>IP City:</b> <i>\" + city + \"</i>\";" +
                "}" +
                "</script>" +
                "<script type=\"text/javascript\">" +
                "function copyTextToClipboard(text) {" +
                "  var textArea = document.createElement(\"textarea\");" +
                "  textArea.style.position = 'fixed';" +
                "  textArea.style.top = 0;" +
                "  textArea.style.left = 0;" +
                "  textArea.style.width = '2em';" +
                "  textArea.style.height = '2em';" +
                "  textArea.style.padding = 0;" +
                "  textArea.style.border = 'none';" +
                "  textArea.style.outline = 'none';" +
                "  textArea.style.boxShadow = 'none';" +
                "  textArea.style.background = 'transparent';" +
                "  textArea.value = text;" +
                "  document.body.appendChild(textArea);" +
                "  textArea.focus();" +
                "  textArea.select();" +
                "  try {" +
                "    var successful = document.execCommand('copy');" +
                "    showToast(\"Copied!!\");" +
                "  } catch (err) {" +
                "    showToast(\"Failed to copy!!\");" +
                "  }" +
                "  document.body.removeChild(textArea);" +
                "}" +
                "function showToast(text) {" +
                "  span = document.createElement('span');" +
                "  text = document.createTextNode(text);" +
                "  span.append(text);" +
                "  span.setAttribute(\"id\", \"toast\");" +
                "  document.body.appendChild(span);" +
                "  setTimeout(function(){" +
                "    document.getElementById('toast').remove();" +
                "  },2000);" +
                "}; " +
                "function copyText(e) {" +
                "  if (e.target.tagName == \"I\") {" +
                "    elementData = e.target.innerText;" +
                "    copyTextToClipboard(elementData);" +
                "  }" +
                "}" +
                "document.addEventListener(\"click\", copyText);" +
                "</script>" +
                "</body>" +
                "</html>";
        browser.loadDataWithBaseURL("file:///android_asset/www/", htmlText, "text/html", "UTF-8", null);
    }

    @SuppressLint({"MissingPermission", "WrongConstant"})
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_CODE_CHECK_SETTINGS) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation();
                } else {
                    Toast.makeText(MainActivity.this, "Permission denied to get your Location", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }


    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (isLocationEnabled()) {
            mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    requestNewLocationData();
                    if (location == null) {
                        requestNewLocationData();
                    } else {
                        try {
                            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            address = addresses.get(0).getAddressLine(0);
                            city = addresses.get(0).getLocality();
                            MainActivity.browser.loadUrl("javascript:(updateLocation(\"" + location.getLatitude() + "\", \"" + location.getLongitude() + "\"))");
                            MainActivity.browser.loadUrl("javascript:(updateAddress(\"" + address + "\"))");
                            MainActivity.browser.loadUrl("javascript:(updateCity(\"" + city + "\"))");
                            MainActivity.location_string = location.getLatitude() + "," + location.getLongitude();
                            MainActivity.location_status = true;
                        } catch (IOException e) {
                            MainActivity.browser.post(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.browser.loadUrl("javascript:(updateLocation(\"No Internet\", \"No Internet\"))");
                                    MainActivity.browser.loadUrl("javascript:(updateAddress(\"No Internet\"))");
                                    MainActivity.browser.loadUrl("javascript:(updateCity(\"No Internet\"))");
                                    MainActivity.handler.removeCallbacks(MainActivity.runnable);
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else {
            Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(100);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            try {
                addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                address = addresses.get(0).getAddressLine(0);
                city = addresses.get(0).getLocality();
                MainActivity.browser.loadUrl("javascript:(updateLocation(\"" + mLastLocation.getLatitude() + "\", \"" + mLastLocation.getLongitude() + "\"))");
                MainActivity.browser.loadUrl("javascript:(updateAddress(\"" + address + "\"))");
                MainActivity.browser.loadUrl("javascript:(updateCity(\"" + city + "\"))");
                MainActivity.location_string = mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();
                MainActivity.location_status = true;
            } catch (IOException e) {
                MainActivity.browser.post(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.browser.loadUrl("javascript:(updateLocation(\"No Internet\", \"No Internet\"))");
                        MainActivity.browser.loadUrl("javascript:(updateAddress(\"No Internet\"))");
                        MainActivity.browser.loadUrl("javascript:(updateCity(\"No Internet\"))");
                        MainActivity.handler.removeCallbacks(MainActivity.runnable);
                    }
                });
                e.printStackTrace();
            }
        }
    };

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}

class GetPublicIP extends AsyncTask<String, String, String> {
    URLConnection socket;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected String doInBackground(String... strings) {
        String publicIP = "";

        try  {
            if (MainActivity.proxy_string == null || MainActivity.proxy_string.trim().equals("") || MainActivity.proxy_string.trim().equals(":0")) {
                socket = new URL("http://ip-api.com/json").openConnection();
            }
            else {
                String[] arrayString = MainActivity.proxy_string.split(":");
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(arrayString[0], Integer.parseInt(arrayString[1])));
                socket = new URL("http://ip-api.com/json").openConnection(proxy);
            }

            socket.setUseCaches( false );
            socket.setDefaultUseCaches( false );
            HttpURLConnection conn = ( HttpURLConnection )socket;
            conn.setUseCaches( false );
            conn.setDefaultUseCaches( false );
            conn.setRequestProperty( "Cache-Control",  "no-cache" );
            conn.addRequestProperty("Cache-Control", "max-age=0");
            conn.setRequestProperty( "Pragma",  "no-cache" );
            conn.setRequestProperty( "Expires",  "0" );
            conn.setRequestMethod( "GET" );
            conn.connect();
            java.util.Scanner s = new java.util.Scanner(conn.getInputStream(), "UTF-8").useDelimiter("\\A");
            publicIP = s.next();
            conn.disconnect();
        } catch (IOException e) {
            MainActivity.browser.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.browser.loadUrl("javascript:(updateIpRegion(\"No Internet\"))");
                    MainActivity.browser.loadUrl("javascript:(updateIpCity(\"No Internet\"))");
                    MainActivity.browser.loadUrl("javascript:(updateIP(\"No Internet\"))");
                    MainActivity.handler.removeCallbacks(MainActivity.runnable);
                }
            });
            e.printStackTrace();
        }
        return publicIP;
    }

    @Override
    protected void onPostExecute(String publicIp) {
        super.onPostExecute(publicIp);
        try {
            JSONObject obj = new JSONObject(publicIp);
            MainActivity.browser.loadUrl("javascript:(updateIpRegion(\"" + obj.get("regionName") + "\"))");
            MainActivity.browser.loadUrl("javascript:(updateIpCity(\"" + obj.get("city") + "\"))");
            MainActivity.browser.loadUrl("javascript:(updateIP(\"" + obj.get("query") + "\"))");
            MainActivity.ip_string = obj.getString("query");
            MainActivity.ip_city = obj.getString("city");
            MainActivity.ip_status = true;
        } catch (JSONException e) {
            e.printStackTrace();
            MainActivity.handler.removeCallbacks(MainActivity.runnable);
        }
    }
}
