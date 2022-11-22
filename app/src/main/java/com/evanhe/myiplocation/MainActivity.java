package com.evanhe.myiplocation;

import static com.evanhe.myiplocation.MyApplication.AF_DEV_KEY;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.content.Context;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.attribution.AppsFlyerRequestListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private String network, address, city, htmlText;
    public static WebView browser;
    Geocoder geocoder;
    List<Address> addresses;
    public static Runnable runnable;
    public static String LOG_TAG = "BOTICOCEANS";

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        this.network = telephonyManager.getNetworkOperatorName();


        AppsFlyerConversionListener conversionListener =  new AppsFlyerConversionListener() {
            public String LOG_TAG = "AppsFlyer*****";
            @Override
            public void onConversionDataSuccess(Map<String, Object> conversionDataMap) {
                for (String attrName : conversionDataMap.keySet())
                    Log.d(LOG_TAG, "onConversionDataSuccess attribute: " + attrName + " = " + conversionDataMap.get(attrName));
            }

            @Override
            public void onConversionDataFail(String errorMessage) {
                Log.d(LOG_TAG, "error getting conversion data: " + errorMessage);
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> attributionData) {
                for (String attrName : attributionData.keySet())
                    Log.d(LOG_TAG, "onAppOpenAttribution attribute: " + attrName + " = " + attributionData.get(attrName));
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
                Log.d(LOG_TAG, "error onAttributionFailure : " + errorMessage);
            }

        };


        AppsFlyerLib.getInstance().init(AF_DEV_KEY, conversionListener, getApplicationContext());


        browser = (WebView) findViewById(R.id.webview);
        browser.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                try {


                    AppsFlyerLib.getInstance().setDebugLog(true);
                    AppsFlyerLib.getInstance().start(getApplicationContext());
//                    AppsFlyerLib.getInstance();



                    new GetPublicIP().execute();

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
                "left: calc(50% - 6em);" +
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
                "</style></head><body><b>Network:</b> <i>" + network + "</i><span id='pip'><br><br><b>IP Address:</b> Searching...</span><span id='ipcity'><br><br><b>City:</b> Searching...</span><span id='ipregion'><br><br><b>State:</b> Searching...</span><span id='ipcountry'><br><br><b>Country:</b> Searching...</span><span id='isp'><br><br><b>ISP:</b> Searching...</span>" +
                "<script type=\"text/javascript\">" +
                "function updateISP(isp) {" +
                "document.getElementById('isp').innerHTML = \"<br><br><b>ISP:</b> <i>\" + isp + \"</i>\";" +
                "}" +
                "function updateCountry(address) {" +
                "document.getElementById('ipcountry').innerHTML = \"<br><br><b>Country:</b> <i>\" + address + \"</i>\";" +
                "}" +
                "function updateIP(ip) {" +
                "document.getElementById('pip').innerHTML = \"<br><br><b>IP Address:</b> <i>\" + ip + \"</i>\";" +
                "}" +
                "function updateIpRegion(region) {" +
                "document.getElementById('ipregion').innerHTML = \"<br><br><b>State:</b> <i>\" + region + \"</i>\";" +
                "}" +
                "function updateIpCity(city) {" +
                "document.getElementById('ipcity').innerHTML = \"<br><br><b>City:</b> <i>\" + city + \"</i>\";" +
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

    class GetPublicIP extends AsyncTask<String, String, String> {
        URLConnection socket;
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(String... strings) {
            String publicIP = "";

            try  {
                socket = new URL("http://ip-api.com/json").openConnection();
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
                        MainActivity.browser.loadUrl("javascript:(updateCountry(\"No Internet\"))");
                        MainActivity.browser.loadUrl("javascript:(updateISP(\"No Internet\"))");
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
                MainActivity.browser.loadUrl("javascript:(updateCountry(\"" + obj.get("country") + "\"))");
                MainActivity.browser.loadUrl("javascript:(updateIpRegion(\"" + obj.get("regionName") + "\"))");
                MainActivity.browser.loadUrl("javascript:(updateIpCity(\"" + obj.get("city") + "\"))");
                MainActivity.browser.loadUrl("javascript:(updateIP(\"" + obj.get("query") + "\"))");
                MainActivity.browser.loadUrl("javascript:(updateISP(\"" + obj.get("isp") + "\"))");

                Map<String, Object> eventValues = new HashMap<String, Object>();
                eventValues.put("android_id", Settings.Secure.getString(getApplicationContext().getContentResolver(), "android_id"));
                eventValues.put("country",obj.get("country"));
                eventValues.put("regionName",obj.get("regionName"));
                eventValues.put("city",obj.get("city"));
                eventValues.put("ip",obj.get("query"));
                eventValues.put("isp",obj.get("isp"));

                Log.i(LOG_TAG, eventValues.toString());

                AppsFlyerLib.getInstance().logEvent(getApplicationContext(), AFInAppEventType.COMPLETE_REGISTRATION, eventValues, new AppsFlyerRequestListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(LOG_TAG, "AppsFlyer register sent successfully");
                    }
                    @Override
                    public void onError(int i, @NonNull String s) {
                        Log.d(LOG_TAG, "AppsFlyer event failed to be sent:\n" +
                                "Error code: " + i + "\n"
                                + "Error description: " + s);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}