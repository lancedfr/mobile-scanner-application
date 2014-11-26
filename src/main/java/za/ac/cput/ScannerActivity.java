package za.ac.cput;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ScannerActivity extends Activity {
    public String tempBarcode;
    private ProgressBar spinner;
    private AsyncTimer timer;
    private TextView responseText;

    public static String GET(String url) {
        InputStream inputStream = null;
        String result = "";
        try {
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if (inputStream != null) {
                result = convertInputStreamToString(inputStream);
            } else {
                result = "Did not work!";
            }
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }
        inputStream.close();
        return result;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner); // this works without the scanner app
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        spinner = (ProgressBar) findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
        OnClickListener listnr = new OnClickListener() {

            @Override
            public void onClick(View v) {
                //  Intent intent = new Intent(getApplication(), MainActivity.class);
                //  startActivity(intent);
                responseText = (TextView) findViewById(R.id.resultText);
                if (tempBarcode != null) {
                    // check if you are connected or not
                    if (isConnected()) {
                        responseText.setTextColor(0xFF00CC00);
                        responseText.setText("Connection to api established....");
                        // Toast.makeText(getBaseContext(),"connected",Toast.LENGTH_LONG).show();
                        String text = new HttpAsyncTask().execute("http://41.185.26.97:8080/mobile-scanner-server/rest/product?barcode=" + tempBarcode).toString();
                    } else {
                        responseText.setText("No connection established");
                        // Toast.makeText(getBaseContext(),"not connected",Toast.LENGTH_LONG).show();
                    }
                    spinner.setVisibility(View.VISIBLE);
                    startThread();
                } else {
                    spinner = (ProgressBar) findViewById(R.id.progressBar1);
                    spinner.setVisibility(View.GONE);
                    responseText.setText("No barcode acquired");
                }
            }
        };

        Button btn = (Button) findViewById(R.id.buttonSearch);

        btn.setOnClickListener(listnr);
        //hide action bar
        final ActionBar actionBar = getActionBar();
        actionBar.hide();
        //hide status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //load scanner
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String barcode;
            String type;

            barcode = scanResult.getContents();
            type = scanResult.getFormatName();

            tempBarcode = barcode;

            EditText barcodeText = (EditText) findViewById(R.id.barcodeText);
            EditText typeText = (EditText) findViewById(R.id.typeText);

            barcodeText.setText(barcode);
            typeText.setText(type);
        }
    }

    private void startThread() {
        if (timer != null && timer.getIsRunning() == true) {
            Toast.makeText(this, "Already searching...", Toast.LENGTH_SHORT).show();
            return;
        }
        timer = new AsyncTimer(this, spinner);
        timer.execute();
    }

    private void stopThread() {
        if (timer == null || timer.getIsRunning() == false) {
            Toast.makeText(this, "Not searching!", Toast.LENGTH_SHORT).show();
            return;
        }
        timer.cancel(true);
    }

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return GET(urls[0]);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            // Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();

            if (result.contains("{")) {
                responseText.setTextSize(45);
                responseText.setText("Found");
            } else {
                responseText.setTextSize(45);
                responseText.setText("Not Found");
            }
            // responseText.setText(result);
        }
    }
}