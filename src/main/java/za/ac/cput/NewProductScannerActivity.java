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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import za.ac.cput.classes.Product;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class NewProductScannerActivity extends Activity {
  public String tempBarcode;
  private ProgressBar spinner;
  private AsyncTimer timer;
  private TextView responseText;
  private Product product;
  EditText id, name, barcode;


  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_product_scanner); // this works without the scanner app
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
            // String text = new HttpAsyncTask().execute("http://41.185.26.97:8080/mobile-scanner-server/rest/product?barcode="+tempBarcode).toString();
            new HttpAsyncTask().execute("http://41.185.26.97:8080/mobile-scanner-server/rest/product");
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

    barcode = (EditText) findViewById(R.id.barcodeText);
    name = (EditText) findViewById(R.id.productName);

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
      Toast.makeText(this, "Already adding...", Toast.LENGTH_SHORT).show();
      return;
    }
    timer = new AsyncTimer(this, spinner);
    timer.execute();
  }

  private void stopThread() {
    if (timer == null || timer.getIsRunning() == false) {
      Toast.makeText(this, "Not adding!", Toast.LENGTH_SHORT).show();
      return;
    }
    timer.cancel(true);
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

  public boolean isConnected() {
    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isConnected()) {
      return true;
    } else {
      return false;
    }
  }

  private boolean validate() {
    if (name.getText().toString().trim().equals("")) {
      return false;
    } else if (barcode.getText().toString().trim().equals("")) {
      return false;
    } else {
      return true;
    }
  }

  private class HttpAsyncTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... urls) {

      product = new Product();
      // product.setId();
      product.setBarcode(barcode.getText().toString());
      product.setName(name.getText().toString());

      return POST(urls[0], product);
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
      // Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
      responseText.setTextSize(45);
      responseText.setText("Successful!");
    }
  }  public static String POST(String url, Product product) {
    InputStream inputStream = null;
    String result = "";
    try {

      // 1. create HttpClient
      HttpClient httpclient = new DefaultHttpClient();

      // 2. make POST request to the given URL
      HttpPost httpPost = new HttpPost(url);

      String json = "";

      // 3. build jsonObject
      JSONObject jsonObject = new JSONObject();
      jsonObject.accumulate("id", product.getId());
      jsonObject.accumulate("barcode", product.getBarcode());
      jsonObject.accumulate("name", product.getName());

      // 4. convert JSONObject to JSON to String
      json = jsonObject.toString();

      // ** Alternative way to convert Person object to JSON string usin Jackson Lib
      // ObjectMapper mapper = new ObjectMapper();
      // json = mapper.writeValueAsString(person);

      // 5. set json to StringEntity
      StringEntity se = new StringEntity(json);

      // 6. set httpPost Entity
      httpPost.setEntity(se);

      // 7. Set some headers to inform server about the type of the content
      httpPost.setHeader("Accept", "application/json");
      httpPost.setHeader("Content-type", "application/json");

      // 8. Execute POST request to the given URL
      HttpResponse httpResponse = httpclient.execute(httpPost);

      // 9. receive response as inputStream
      inputStream = httpResponse.getEntity().getContent();

      // 10. convert inputstream to string
      if (inputStream != null) {
        result = convertInputStreamToString(inputStream);
      } else {
        result = "Did not work!";
      }

    } catch (Exception e) {
      Log.d("InputStream", e.getLocalizedMessage());
    }

    // 11. return result
    return result;
  }

}

