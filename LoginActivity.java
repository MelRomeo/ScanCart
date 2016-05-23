package melz.barcode;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends ActionBarActivity {
    private final Context context = this;
    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String mimeType = "multipart/form-data;boundary=" + boundary;
    private byte[] multipartBody;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button btnLogin=(Button)findViewById(R.id.btnLogin);
        final EditText password=(EditText)findViewById(R.id.password);
        final EditText email=(EditText)findViewById(R.id.email);
        final EditText domain=(EditText)findViewById(R.id.subdomain);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //http://demo.opencart.com/admin/index.php?route=common/login
               // performLogin(email.getText().toString(), password.getText().toString());
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

                StrictMode.setThreadPolicy(policy);

                if(TextUtils.isEmpty(email.getText().toString())){

                    email.setError("Please Enter UserName");

                }else if(TextUtils.isEmpty(password.getText().toString())){
                    password.setError("Please Enter Password");

                }else if(TextUtils.isEmpty(domain.getText().toString())){
                    domain.setError("Please Enter SubDomain");

                }else{


                    login(email.getText().toString(), password.getText().toString(),domain.getText().toString());

                }

    
            }
        });

    }


    public void putUpdate(String Productid,String quantity) {


        String url = "http://dexjac.com/admin/index.php?route=common/login";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JSONObject Parent = new JSONObject();

        Map<String,String> params = new HashMap<String, String>();
        params.put("username", String.valueOf(Productid));
        params.put("password", String.valueOf(quantity));
        try {
            Parent.put("", new JSONObject(params));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.POST, url,Parent,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Document doc = Jsoup.parse(response.toString());

                        Elements topicList = doc.select("li#dashboard");


                        Elements text = doc.select("div[class=alert alert-danger");
                        Log.d("melz", response.toString());
                        Toast.makeText(LoginActivity.this, "Product Quantity Updated Successfully", Toast.LENGTH_LONG).show();

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("melz", "Error: " + error.getMessage());
                Toast.makeText(LoginActivity.this,"Product Quantity Updated Failed",Toast.LENGTH_LONG).show();


            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        jsonObjReq.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });


        requestQueue.add(jsonObjReq);
    }


    public void login(final String username, final String password,final String domain){

        if(checkInternetConenction()) {

            final ProgressDialog progressBar = new ProgressDialog(LoginActivity.this);
            progressBar.setCancelable(true);
            progressBar.setMessage("Authenticating...");
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setProgress(0);
            progressBar.setCanceledOnTouchOutside(false);
            progressBar.setMax(100);
            progressBar.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String domainurld=domain;

                    if(domain.startsWith("https")||domain.startsWith("http")){


                    }else{
                        domainurld="http://"+domain;
                    }

                    String url = domainurld+"/admin/index.php?route=common/login";

                    StringBuilder urlBuilder = new StringBuilder(url);


                    MultipartEntity mEntity = new MultipartEntity();

                    try {

                        mEntity.addPart("username", new StringBody(username));
                        mEntity.addPart("password", new StringBody(password));

                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }

                    HttpCall httpCall = new HttpCall();
                    httpCall.buildRequest(urlBuilder.toString(), "", "", "POST", "multipart/form-data");
                    httpCall.setMultipartEntity(mEntity);
              
                    final String resposeString = httpCall.executeCallGetString();

                    if (resposeString != null) {

                        progressBar.dismiss();
                        final String finalDomainurld = domainurld;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (resposeString.contains("li#dashboard") || resposeString.contains("You are logged in as")) {

                                   String finalurl= finalDomainurld +"/api/rest_admin/";

                                    SharedPreferences sharedpreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);

                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                    editor.putString("domainurl", finalurl);
                                    editor.commit();

                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                } else if (resposeString.contains("Invalid token session")) {
                                    Toast.makeText(LoginActivity.this, "Invalid token session", Toast.LENGTH_LONG).show();

                                } else if (resposeString.contains("No match for Username and/or Password.")) {

                                    Toast.makeText(LoginActivity.this, "No match for Username and/or Password.", Toast.LENGTH_LONG).show();

                                } else {
                                    Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                       }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.dismiss();
                                Toast.makeText(LoginActivity.this, "Wrong Domain", Toast.LENGTH_LONG).show();
                            }
                        });


                    }
                }
            }).start();

        }else{
            Toast.makeText(LoginActivity.this, "No Internet Connection", Toast.LENGTH_LONG).show();

        }



    }
    private boolean checkInternetConenction() {
 
        ConnectivityManager connec =(ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        if ( connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||

                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {
  
            return true;
        }else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED  ) {
    
            return false;
        }
        return false;
    }

    private void buildTextPart(DataOutputStream dataOutputStream, String parameterName, String parameterValue) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + parameterName + "\"" + lineEnd);
        dataOutputStream.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);
        dataOutputStream.writeBytes(parameterValue + lineEnd);
    }

    public void performLogin(final String username, final String pass){


        RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);

        StringRequest sr = new StringRequest(Request.Method.POST,"http://dexjac.com/admin/index.php?route=common/login",new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Document doc = Jsoup.parse(response);

                Elements topicList = doc.select("li#dashboard");


                Elements text = doc.select("div[class=alert alert-danger");


                Log.d("ra",response.toString());
                if(response.toString().contains("li#dashboard")){
                    Log.d("ra",response.toString());
                }else if(response.toString().contains("Invalid token session")){
                    Log.d("ra",response.toString());
                }else if(response.toString().contains("No match for Username and/or Password.")){
                    Log.d("ra",response.toString());
                }

               else{
                    Log.d("ra",response.toString());
                }
    
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ra",error.toString());

   
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("password", pass);
                params.put("redirect","http://dexjac.com/admin/index.php?route=common/login");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");

                return params;
            }
        };

        sr.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        queue.add(sr);
    }

}
