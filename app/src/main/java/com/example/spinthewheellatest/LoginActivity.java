package com.example.spinthewheellatest;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;

public class LoginActivity extends AppCompatActivity {
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private ProgressDialog mProgressDialog;

    private String ReturnCodee;
    private String text;

    public static String Userlogin ="";
    public static final String IP = "https://www.africellportal.sl"; //"http://192.168.1.96"; //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.activity_login);
        Button spinbtn = (Button) findViewById(R.id.btnlogin);
        final EditText username = (EditText) findViewById(R.id.etusername);
        final EditText password = (EditText) findViewById(R.id.etpassword);
        boolean isConnected = isNetworkAvailable();
        if (isConnected) {

            spinbtn.setOnClickListener(new View.OnClickListener() {
                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    //showAlertDialog(LoginActivity.this, "Error", "Connectiing", false);

                    String Username = username.getText().toString();
                    String Password = password.getText().toString();
                    Userlogin = Username.toString();


                    if (Username.equals("")) {
                        showAlertDialog(LoginActivity.this, "Error", "Username cannot be empty.", false);
                        return;
                    }

                    if (Password.equals("")) {
                        showAlertDialog(LoginActivity.this, "Error", "Password cannot be empty.", false);
                        return;
                    }

                    String _HTTP;
                    _HTTP = ""+IP+"/SpinTheWheel_Login.aspx?Username=" + Username + "&Password=" + Password + "";
                    new LoginActivity.DownloadAndParseSubmitWinnerTask().execute(_HTTP);
//                    if
                    return;
                }
            });
        } else {
            showAlertDialog(LoginActivity.this, "Error", "You Are Not Connected To The Internet", false);
        }

    }

    public class DownloadAndParseSubmitWinnerTask extends AsyncTask<String, Void, String[]> {
        private ProgressDialog dialog;

        public DownloadAndParseSubmitWinnerTask() {
            this.dialog = new ProgressDialog(LoginActivity.this);
        }

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public String[] doInBackground(String... urls) {
            return LoginActivity.this.parseSubmitWinner(urls[0]);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(String[] result) {
            if (result[0].equals("0")) {
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                i.putExtra("UserName",Userlogin.toString());
                startActivity(i);

                //startActivity(new Intent(LoginActivity.this, MainActivity.class));
                //Toast.makeText(LoginActivity.this, result[0], Toast.LENGTH_LONG).show();
            } else {
                showAlertDialog(LoginActivity.this , "Error Login", "Wrong UserName or Password", Boolean.FALSE);
                //Toast.makeText(LoginActivity.this, "Wrong UserName or Password", Toast.LENGTH_LONG).show();
            }
            this.dialog.dismiss();
        }
    }

    public String[] parseSubmitWinner(String strURL) {
        String _RESULT = "";
        String[] strParseResult = new String[1];
        InputStream is = null;
        try {
            is = OpenHttpConnection(strURL);
        } catch (IOException e1) {
            Log.d("NetworkingActivity", e1.getLocalizedMessage());
        }
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(is, null);
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                String tagname = parser.getName();
                switch (eventType) {
                    case 2:
                        if (tagname.equalsIgnoreCase("SpinTheWheel")) {
                            _RESULT = "";
                            break;
                        }
                        break;
                    case 3:
                        if (tagname.equalsIgnoreCase("ReturnCode")) {
                            _RESULT = this.text;


//                            if (!_RESULT.equals("0")){
//                              // showAlertDialog(this, "Error", "Error UserName", false);
//                            } else {
//                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                            }
                        }
                        this.text = "";
                        break;
                    case 4:
                        this.text = parser.getText();
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e2) {
            e2.printStackTrace();
        }
        strParseResult[0] = _RESULT;
        return strParseResult;
    }

     public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() { // from class: com.example.SpinTheWheel.SpinTheWheel.3
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }
    
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

     private InputStream OpenHttpConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        if (!(conn instanceof HttpURLConnection)) {
            throw new IOException("Not an HTTP connection");
        }
        try {
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            int response = httpConn.getResponseCode();
            if (response != 200) {
                return null;
            }
            InputStream in = httpConn.getInputStream();
            return in;
        } catch (Exception ex) {
            Log.d("Networking", ex.getLocalizedMessage());
            throw new IOException("Error connecting");
        }
    }

}


