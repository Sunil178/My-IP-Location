package com.botico.myiplocation;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    private String network, htmlText;
    public static WebView browser;
    public Bundle bundle;
    public FirebaseAnalytics firebaseAnalytics;
    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        bundle = new Bundle();
        bundle.putString("APP_LAUNCH", "SUCCESS");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Param.SUCCESS, bundle);
        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        network = telephonyManager.getNetworkOperatorName();
        browser = (WebView) findViewById(R.id.webview);
        browser.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                new GetPublicIP().execute();
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
