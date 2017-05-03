package com.insurance.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.location.Location;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.itextpdf.text.pdf.PdfObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;

@SuppressLint({"NewApi"})
public class ShowMap extends Activity {
    public WebView appWebView;
    public String incidentID;
    private String key;
    public String latitude;
    public String longitude;
    public Context myContext;
    public ShowMapWrapper objWrapper;

    /* renamed from: com.insurance.app.ShowMap.1 */
    class C00161 extends WebChromeClient {
        private final /* synthetic */ Activity val$activity;

        /* renamed from: com.insurance.app.ShowMap.1.1 */
        class C00131 implements OnClickListener {
            private final /* synthetic */ JsResult val$myresult;

            C00131(JsResult jsResult) {
                this.val$myresult = jsResult;
            }

            public void onClick(DialogInterface arg0, int arg1) {
                this.val$myresult.confirm();
            }
        }

        /* renamed from: com.insurance.app.ShowMap.1.2 */
        class C00142 implements OnClickListener {
            private final /* synthetic */ JsResult val$res;

            C00142(JsResult jsResult) {
                this.val$res = jsResult;
            }

            public void onClick(DialogInterface dialog, int id) {
                this.val$res.confirm();
            }
        }

        /* renamed from: com.insurance.app.ShowMap.1.3 */
        class C00153 implements OnClickListener {
            private final /* synthetic */ JsResult val$res;

            C00153(JsResult jsResult) {
                this.val$res = jsResult;
            }

            public void onClick(DialogInterface dialog, int id) {
                this.val$res.cancel();
            }
        }

        C00161(Activity activity) {
            this.val$activity = activity;
        }

        public void onProgressChanged(WebView view, int progress) {
            this.val$activity.setProgress(progress * 1000);
        }

        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            new Builder(ShowMap.this.myContext).setTitle(Constants.STR_APP_NAME).setMessage(message).setPositiveButton("OK", new C00131(result)).setCancelable(false).create().show();
            return true;
        }

        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            JsResult res = result;
            Builder builder = new Builder(ShowMap.this.myContext);
            builder.setMessage(message).setCancelable(false).setTitle(Constants.STR_APP_NAME).setPositiveButton("Yes", new C00142(res)).setNegativeButton("No", new C00153(res));
            builder.create().show();
            return true;
        }
    }

    public class MyBrowser extends WebViewClient {
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.indexOf("tel:") > -1) {
                try {
                    ShowMap.this.startActivity(new Intent("android.intent.action.DIAL", Uri.parse(url)));
                } catch (Exception e) {
                }
            } else if (url.indexOf("mail:") > -1) {
                try {
                    ShowMap.this.startActivity(new Intent("android.intent.action.SEND", Uri.parse(url)));
                } catch (Exception e2) {
                }
            } else {
                view.loadUrl(url);
            }
            return true;
        }
    }

    public class SSLAcceptingWebViewClient extends WebViewClient {
        public SSLAcceptingWebViewClient(Context ctx) {
        }

        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            try {
                handler.proceed();
            } catch (Exception e) {
            }
        }
    }

    public class ShowMapWrapper {
        public String APP_FOLDERNAME;
        public String incidentID;
        public Activity myAct;
        private WebView objWV;

        /* renamed from: com.insurance.app.ShowMap.ShowMapWrapper.1 */
        class C00171 implements Runnable {
            private final /* synthetic */ String val$webResponseURL;

            C00171(String str) {
                this.val$webResponseURL = str;
            }

            public void run() {
                ShowMapWrapper.this.objWV.loadUrl(this.val$webResponseURL);
            }
        }

        public ShowMapWrapper(String lat, String lang, Activity myact, WebView objwv, String strIncidentID) {
            ShowMap.this.latitude = PdfObject.NOTHING;
            ShowMap.this.longitude = PdfObject.NOTHING;
            this.incidentID = PdfObject.NOTHING;
            this.APP_FOLDERNAME = "InsuranceAssist";

            ShowMap.this.latitude = lat;
            ShowMap.this.longitude = lang;

            this.myAct = myact;
            this.objWV = objwv;
            this.incidentID = strIncidentID;
        }

        public void pushLocation(String platitude, String plongitude) {
            Log.e("SHOWMAP WRAPPER", "LAT" + platitude + ",LONG" + plongitude);
            ShowMap.this.latitude = platitude;
            ShowMap.this.longitude = plongitude;
        }
        public String CB_Success;
        public String CB_failure;

        public String getLocation(String incidentID, String successCB, String failureCB) {
            Log.e("WRAPPER", "Request Reached Wrapper- getLocation");
            String retVal = "Done";
            this.CB_Success = successCB;
            this.CB_failure = failureCB;
            try {
                File incidentDir = new File(APP_FOLDERNAME + File.separator + incidentID + File.separator + "data.json");
                Log.e("Path", "Path" + incidentDir.getAbsolutePath() + ", isExists:" + incidentDir.exists());
                if (!incidentDir.isFile() || !incidentDir.exists()) {
                    return retVal;
                }
                FileReader reader = new FileReader(incidentDir.getAbsoluteFile());
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuffer stringBuffer = new StringBuffer();
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    stringBuffer.append(line).append("\n");
                }
                bufferedReader.close();
                reader.close();
                String defaultData = stringBuffer.toString();
                JSONObject obj = new JSONObject(defaultData);
                Log.d("GEOLOCATION", "Before Geo-location Fetch:");
                GetLocation objGetLocation = new GetLocation(myAct);
                if (objGetLocation.displayGpsStatus().booleanValue()) {
                    Location location = objGetLocation.getLocationData();
                    if (location != null)
                    {
                        //Trim the location data
                        String lt = String.valueOf(location.getLatitude());
                        String lng= String.valueOf(location.getLongitude());

                        if(lt.length() >10) {
                            lt = lt.substring(0, 10);
                        }
                        if(lng.length() >10)
                        {
                            lng =lng.substring(0, 10);
                        }

                        obj.put("GPSLatitude", lt);
                        obj.put("GPSLongitude", lng);
                        defaultData = obj.toString();
                        retVal = lt + "," + lng;
                        Log.d("Latitude", lt+"");
                        Log.d("Longitude", lng+"");
                    }
                } else {
                    retVal = "OFF";
                }
                File incidentDataFile = new File(APP_FOLDERNAME + File.separator + incidentID + File.separator + "data.json");
                if (incidentDataFile.isAbsolute() && incidentDataFile.exists()) {
                    incidentDataFile.delete();
                }
                incidentDataFile.createNewFile();
                FileWriter fileWritter = new FileWriter(incidentDataFile, false);
                BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                bufferWritter.write(defaultData);
                bufferWritter.close();
                fileWritter.close();
                invokeSuccessCallback(retVal);
                return retVal;
            } catch (Exception e) {
                e.printStackTrace();
                retVal = "ERROR";
                invokeFailureCallback(retVal);
                return retVal;
            }
        }

        protected void invokeSuccessCallback(Object responseData) {
            try {
                invokeURL("javascript:" + this.CB_Success + "('" + responseData + "')");
            } catch (Exception e) {
            }
        }

        protected void invokeFailureCallback(String errorMessage) {
            try {
                invokeURL("javascript:" + this.CB_failure + "('" + errorMessage + "')");
            } catch (Exception e) {
            }
        }

        /**
         * Used to unvoke the instant WebURL to send detials from Wrapper to Siebel.
         */
        public  void invokeURL(String webUrl) {
            final String webResponseURL = webUrl;
            Log.i("WRAPPER RES", "URL:"+webUrl);
            this.myAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Log.e("IAWRapper", "INV_URL:" + webResponseURL);
                    appWebView.loadUrl(webResponseURL);
                }
            });
        }

        public void saveImage(String imageData) {
            imageData = imageData.replace("data:image/png;base64,", PdfObject.NOTHING);
            Log.e("IMAGE DATA", "DAta:" + this.incidentID);
            try {
                this.APP_FOLDERNAME = Utility.getApplicationFolder(this.myAct);
                String path = this.APP_FOLDERNAME + File.separator + this.incidentID + File.separator + File.separator + "MyPhotos" + File.separator + "myLocation.png";
                File dir = new File(this.APP_FOLDERNAME + File.separator + this.incidentID + File.separator + File.separator + "MyPhotos");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File locationImgPath = new File(path);
                Log.e("Path", "Path" + locationImgPath.getAbsolutePath() + ", isExists:" + locationImgPath.exists());
                if (locationImgPath.isFile() && locationImgPath.exists()) {
                    locationImgPath.delete();
                }
                Options options = new Options();
                options.inDensity = 160;
                options.inScaled = false;
                byte[] decode = Base64.decode(imageData, 0);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length, options);
                FileOutputStream os = new FileOutputStream(locationImgPath);
                bitmap.compress(CompressFormat.PNG, 50, os);
                os.close();
                Log.e("Path", "Path" + locationImgPath.getAbsolutePath() + ", isExists:" + locationImgPath.exists());
                Log.e("Path", "Done");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void getShowLocation() {
            try {
                Log.e("JOY", "Lat:" + ShowMap.this.latitude + ", Long:" + ShowMap.this.longitude + ", IncidentID:" + this.incidentID);
                String webResponseURL = "javascript:SHOW_MYLOCATION.pushLocation('" + ShowMap.this.latitude + "','" + ShowMap.this.longitude
                        + "','" + this.incidentID + "')";
                Log.e("JOY", "URL:" + webResponseURL);
                this.myAct.runOnUiThread(new C00171(webResponseURL));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void GoBack()
        {
            onBackPressed();
        }
    }

    public ShowMap() {
        this.latitude = PdfObject.NOTHING;
        this.longitude = PdfObject.NOTHING;
        this.incidentID = PdfObject.NOTHING;
        this.key = "DESede";
    }

    @SuppressLint({"SetJavaScriptEnabled"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        setContentView(R.layout.webview);
        this.appWebView = (WebView) findViewById(R.id.webView1);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            this.latitude = b.getString("SHOWLAT");
            this.longitude = b.getString("SHOWLONG");
            this.incidentID = b.getString("INCIDENTID");
        }
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(this.appWebView, 1);
        getWindow().setSoftInputMode(16);
        this.appWebView.setNetworkAvailable(true);
        WebSettings settings = this.appWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(true);
        this.appWebView.clearCache(true);
        this.appWebView.clearHistory();
        this.appWebView.clearFormData();
        this.appWebView.clearSslPreferences();
        this.myContext = this;
        this.objWrapper = new ShowMapWrapper(this.latitude, this.longitude, this, this.appWebView, this.incidentID);
        this.appWebView.addJavascriptInterface(this.objWrapper, "Wrapper");
        this.appWebView.setWebViewClient(new MyBrowser());
        this.appWebView.setWebChromeClient(new C00161(this));
        this.appWebView.loadUrl("file:///android_asset/www/showmap.html");
    }

    public void onBackPressed() {
        Log.d("onBackPressed", "onBackPressed in showmap");
        Intent intent = new Intent();
        intent.putExtra("updated_lat", this.latitude);
        intent.putExtra("updated_lng", this.longitude);
        setResult(-1, intent);
        finish();
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy", "onDestroy");
    }

    protected void onPause() {
        Log.d("onPause", "onPause");
        if (AudioRecord.mRecorder != null) {
            AudioRecord.stopRecording();
        }
        super.onPause();
    }

    protected void onRestart() {
        Log.d("onRestart", "onRestart");
        super.onRestart();
    }

    protected void onResume() {
        Log.d("onResume", "onResume");
        super.onResume();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("INSASSIST", "ON ACTION RESULT, ResultCode:" + resultCode + ", Request Code:" + requestCode);
    }

    private String encryptLicence(String inputNormalStr) {
        return inputNormalStr;
    }

    private String decrypteLicence(String inputEncryptedStr) {
        return inputEncryptedStr;
    }

    private boolean ValidateLicence() {
        String dateTemplate = "yyyyMMdd";
        try {
            String fileFolder = Utility.getApplicationFolder(this);
            File file = new File(new StringBuilder(String.valueOf(fileFolder)).append(File.separator).append("lic.dat").toString());
            JSONObject obj = new JSONObject();
            Date currDate = new Date();
            String dateStr = new SimpleDateFormat(dateTemplate).format(currDate);
            file = new File(fileFolder);
            if (!file.exists()) {
                file.mkdirs();
            }
            FileWriter fileWritter;
            BufferedWriter bufferWritter;
            if (file.exists()) {
                Reader fileReader = new FileReader(file.getAbsoluteFile());
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                StringBuffer stringBuffer = new StringBuffer();
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    stringBuffer.append(line);
                }
                bufferedReader.close();
                fileReader.close();
                String jsonStr = stringBuffer.toString();
                JSONObject jsonObj = new JSONObject(jsonStr);
                int count = jsonObj.getInt("Count");
                Log.e("WRAPPER", "LICENCE DATA:" + jsonStr + ", count:" + count);
                if (count > 17) {
                    return false;
                }
                String dateStrFromFile = jsonObj.getString("updateDate");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateTemplate);
                Log.e("WRAPPER", "Start Date Str:" + dateStrFromFile);
                String currDateStr = simpleDateFormat.format(currDate);
                Log.e("WRAPPER", "Curr Date Str:" + currDateStr);
                String str;
                if (Double.parseDouble(dateStrFromFile) != Double.parseDouble(currDateStr)) {
                    Log.e("Wrapper", "First Case");
                    count++;
                    jsonObj = new JSONObject();
                    str = "updateDate";
                    jsonObj.put(str, simpleDateFormat.format(currDate));
                    jsonObj.put("Count", count);
                } else {
                    Log.e("Wrapper", "Second Case");
                    jsonObj = new JSONObject();
                    str = "updateDate";
                    jsonObj.put(str, simpleDateFormat.format(currDate));
                    jsonObj.put("Count", count);
                }
                if (file.exists()) {
                    file.delete();
                }
                fileWritter = new FileWriter(file, false);
                bufferWritter = new BufferedWriter(fileWritter);
                bufferWritter.write(jsonObj.toString());
                bufferWritter.close();
                fileWritter.close();
                return true;
            }
            file.createNewFile();
            obj.put("updateDate", dateStr);
            obj.put("Count", 1);
            if (file.exists()) {
                file.delete();
            }
            fileWritter = new FileWriter(file, false);
            bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(obj.toString());
            bufferWritter.close();
            fileWritter.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void ShowGetLocationProgress(){
        Utility.showActivityIndicator(myContext,"Getting location..","My Location");
    }

    public void HideGetLocationProgress()
    {
        Utility.hideActivityIndicator();
    }

    public boolean LocationStatus()
    {
        GetLocation objGetLocation = new GetLocation(myContext);
        return objGetLocation.displayGpsStatus().booleanValue();
    }


}
