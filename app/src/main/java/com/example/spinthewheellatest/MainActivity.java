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
import android.os.Handler;
import android.security.keystore.UserNotAuthenticatedException;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;


public class MainActivity extends AppCompatActivity {
    private String Data1GB;
    private String Cash100;
    private String Cash30;
    private String Cash50;
    private String HandBand;
    private String KeyRing;
    private String Tshirt;


    private int degree = 0;
    private boolean isSpinning = false;
    private String text;
    private ImageView wheel;
    private static final String[] sectors = {"Data1GB", "30,000 LE", "Key ring", "T-Shirt", "50,000 LE", "100,000 LE", "Hand Band"};
    private static final int[] sectorDegress = new int[sectors.length];
    private static final Random random = new Random();

    private static String UserLogin = "";

    public static String IP =  "https://www.africellportal.sl"; //"http://192.168.1.96"; //
    public String SubsCount ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.activity_main);
        ImageView spinbtn = (ImageView) findViewById(R.id.Spinbtn);

        UserLogin = extras.getString("UserName");
        final EditText phone = (EditText) findViewById(R.id.editTextPhone);

        boolean isConnected = isNetworkAvailable();
        if (isConnected) {
            this.wheel = (ImageView) findViewById(R.id.wheel);
            getdegreeforSectors();
            spinbtn.setOnClickListener(new View.OnClickListener() { // from class: com.example.SpinTheWheel.SpinTheWheel.1
                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    String PhoneNumber = phone.getText().toString();
                    if (PhoneNumber.equals("")) {
                        MainActivity spinTheWheel = MainActivity.this;
                        spinTheWheel.showAlertDialog(spinTheWheel, "Error", "Phone number cannot be empty.", false);
                        MainActivity.this.isSpinning = false;
                        return;
                    }
                    new DownloadAndParseLoginTask().execute(""+IP+"/SpinTheWheel_Get_WithMSISDN.aspx?MSISDN=" + PhoneNumber + "");
                    Handler mainHandler = new Handler(MainActivity.this.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (SubsCount.equals("2")) {
//                                Toast.makeText(MainActivity.this, "", Toast.LENGTH_LONG).show();
                                showAlertDialog(MainActivity.this, "Info", "customer exceeded your number of Spins allowed per day.", false);
                                MainActivity.this.isSpinning = false;
                                return;
                            }
                            if (!isSpinning) {
                                 spin();
                                 isSpinning = true;
                            }
                        } // This is your code
                    };
                    mainHandler.postDelayed(myRunnable,500);

//                    if (SubsCount.equals("2")) {
//                        Toast.makeText(MainActivity.this, "Subscriber reached his limit.", Toast.LENGTH_LONG).show();
//                        isSpinning = false;
//                        return;
//                    }

                }
            });
            return;
        }
        phone.setVisibility(View.GONE);
        spinbtn.setVisibility(View.GONE);
        showAlertDialog(this, "Error", "You Are Not Connected To The Internet", false);
        this.isSpinning = false;

    }

    private void getdegreeforSectors() {
        int sectordegrees = 360 / sectors.length;
        for (int i = 0; i < sectors.length; i++) {
            sectorDegress[i] = (i + 1) * sectordegrees;
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public void spin() {
        this.degree = random.nextInt(sectors.length - 1);
        RotateAnimation rotateAnimation = new RotateAnimation(0.0f, (sectors.length * 360) + sectorDegress[this.degree], 1, 0.5f, 1, 0.5f);
        rotateAnimation.setDuration(3600L);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setInterpolator(new DecelerateInterpolator());
        rotateAnimation.setAnimationListener(new Animation.AnimationListener() { // from class: com.example.SpinTheWheel.SpinTheWheel.2
            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationStart(Animation animation) {
            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationEnd(Animation animation) {
//                if (MainActivity.this.SubsCount.equals("2")) {
//                    MainActivity.this.isSpinning = false;
//                    Toast.makeText(MainActivity.this, "Subscriber reached his Limit.", Toast.LENGTH_LONG).show();
//                    MainActivity.this.isSpinning = false;
//                    return;
//                } else {

                String award = MainActivity.sectors[MainActivity.sectors.length - (MainActivity.this.degree + 1)];
                EditText phone = (EditText) MainActivity.this.findViewById(R.id.editTextPhone);
                String PhoneNumber = phone.getText().toString();

                String _HTTP = ""+IP+"/SpinTheWheel_Insert.aspx?MSISDN=" + PhoneNumber + "&Item=" + award.toString() + "&UserName="+ UserLogin.toString() +"";
                MainActivity spinTheWheel = MainActivity.this;
                  if (award.equals("Key ring")) {
                      if (MainActivity.this.KeyRing.equals("0")) {
                          MainActivity.this.spin();
                      } else {
                          MainActivity.this.isSpinning = false;
                          Toast.makeText(MainActivity.this, "you won " + award.toString() + ".", Toast.LENGTH_LONG).show();
                          // spinTheWheel.showAlertDialog(spinTheWheel, "Winner", "you won " + award.toString() + ".", false);
                          new DownloadAndParseSubmitWinnerTask().execute(_HTTP);
                          MainActivity.this.isSpinning = false;
                      }
                  }
                  if (award.equals("Hand Band")) {
                      if (MainActivity.this.HandBand.equals("0")) {
                          MainActivity.this.spin();
                      } else {
                          MainActivity.this.isSpinning = false;
                          Toast.makeText(MainActivity.this, "you won " + award.toString() + ".", Toast.LENGTH_LONG).show();
                          // spinTheWheel.showAlertDialog(spinTheWheel, "Winner", "you won " + award.toString() + ".", false);
                          new DownloadAndParseSubmitWinnerTask().execute(_HTTP);
                          MainActivity.this.isSpinning = false;
                      }
                  }
                  if (award.equals("T-Shirt")) {
                      if (MainActivity.this.Tshirt.equals("0")) {
                          MainActivity.this.spin();
                      } else {
                          MainActivity.this.isSpinning = false;
                          Toast.makeText(MainActivity.this, "you won " + award.toString() + ".", Toast.LENGTH_LONG).show();
                          // spinTheWheel.showAlertDialog(spinTheWheel, "Winner", "you won " + award.toString() + ".", false);
                          new DownloadAndParseSubmitWinnerTask().execute(_HTTP);
                          MainActivity.this.isSpinning = false;
                      }
                  }
                  if (award.equals("Data1GB")) {
                      if (MainActivity.this.Data1GB.equals("0")) {
                          MainActivity.this.spin();
                      } else {
                          MainActivity.this.isSpinning = false;
                          Toast.makeText(MainActivity.this, "you won " + award.toString() + ".", Toast.LENGTH_LONG).show();
                          // spinTheWheel.showAlertDialog(spinTheWheel, "Winner", "you won " + award.toString() + ".", false);
                          new DownloadAndParseSubmitWinnerTask().execute(_HTTP);
                          MainActivity.this.isSpinning = false;
                      }
                  }
                  if (award.equals("30,000 LE")) {
                      if (MainActivity.this.Cash30.equals("0")) {
                          MainActivity.this.spin();
                      } else {
                          MainActivity.this.isSpinning = false;
                          Toast.makeText(MainActivity.this, "you won " + award.toString() + ".", Toast.LENGTH_LONG).show();
                          // spinTheWheel.showAlertDialog(spinTheWheel, "Winner", "you won " + award.toString() + ".", false);
                          new DownloadAndParseSubmitWinnerTask().execute(_HTTP);
                          MainActivity.this.isSpinning = false;
                      }
                  }
                  if (award.equals("50,000 LE")) {
                      if (MainActivity.this.Cash50.equals("0")) {
                          MainActivity.this.spin();
                      } else {
                          MainActivity.this.isSpinning = false;
                          Toast.makeText(MainActivity.this, "you won " + award.toString() + ".", Toast.LENGTH_LONG).show();
                          // spinTheWheel.showAlertDialog(spinTheWheel, "Winner", "you won " + award.toString() + ".", false);
                          new DownloadAndParseSubmitWinnerTask().execute(_HTTP);
                          MainActivity.this.isSpinning = false;
                      }
                  }
                  if (award.equals("100,000 LE")) {
                      if (MainActivity.this.Cash100.equals("0")) {
                          MainActivity.this.spin();
                      } else {
                          MainActivity.this.isSpinning = false;
                          Toast.makeText(MainActivity.this, "you won " + award.toString() + ".", Toast.LENGTH_LONG).show();
                          // spinTheWheel.showAlertDialog(spinTheWheel, "Winner", "you won " + award.toString() + ".", false);
                          new DownloadAndParseSubmitWinnerTask().execute(_HTTP);
                          MainActivity.this.isSpinning = false;
                      }
                  }
              }
//            }

            @Override // android.view.animation.Animation.AnimationListener
            public void onAnimationRepeat(Animation animation) {
            }
        });
        this.wheel.startAnimation(rotateAnimation);
    }

    /* loaded from: classes.dex */
    public class DownloadAndParseLoginTask extends AsyncTask<String, Void, String[]> {
        private ProgressDialog dialog;

        public DownloadAndParseLoginTask() {
            this.dialog = new ProgressDialog(MainActivity.this);
        }

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public String[] doInBackground(String... urls) {
            return MainActivity.this.parseLogin(urls[0]);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(String[] result) {

            if (result[0].equals("0")) {
                MainActivity.this.KeyRing = "0";
            } else {
                MainActivity.this.KeyRing = result[0].toString();
            }
            if (result[1].equals("0")) {
                MainActivity.this.HandBand = "0";
            } else {
                MainActivity.this.HandBand = result[1].toString();
            }
            if (result[2].equals("0")) {
                MainActivity.this.Tshirt = "0";
            } else {
                MainActivity.this.Tshirt = result[2].toString();
            }
            if (result[3].equals("0")) {
                MainActivity.this.Data1GB = "0";
            } else {
                MainActivity.this.Data1GB = result[3].toString();
            }
            if (result[4].equals("0")) {
                MainActivity.this.Cash30 = "0";
            } else {
                MainActivity.this.Cash30 = result[4].toString();
            }
            if (result[5].equals("0")) {
                MainActivity.this.Cash50 = "0";
            } else {
                MainActivity.this.Cash50 = result[5].toString();
            }
            if (result[6].equals("0")) {
                MainActivity.this.Cash100 = "0";
            } else {
                MainActivity.this.Cash100 = result[6].toString();
            }
            if (result[7].equals("0")) {
                MainActivity.this.SubsCount = "0";
            } else {
                MainActivity.this.SubsCount = result[7].toString();
            }

            this.dialog.dismiss();
        }
    }

    public String[] parseLogin(String strURL) {
        String _KeyRing = "";
        String _handBand = "";
        String _Tshirt = "";
        String _Data1GB = "";
        String _Cash30 = "";
        String _Cash50 = "";
        String _Cash100 = "";
        String _SubsCount = "";
        String[] strParseResult = new String[8];
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
                            _KeyRing = "";
                            _handBand = "";
                            _Tshirt = "";
                            _Data1GB = "";
                            _Cash30 = "";
                            _Cash50 = "";
                            _Cash100 = "";
                            _SubsCount= "";
                            break;
                        }
                        break;
                    case 3:
                        if (tagname.equalsIgnoreCase("KeyRing")) {
                            _KeyRing = this.text;
                        } else if (tagname.equalsIgnoreCase("handBand")) {
                            _handBand = this.text;
                        } else if (tagname.equalsIgnoreCase("Tshirt")) {
                            _Tshirt = this.text;
                        } else if (tagname.equalsIgnoreCase("Data1GB")) {
                            _Data1GB = this.text;
                        } else if (tagname.equalsIgnoreCase("Cash30")) {
                            _Cash30 = this.text;
                        } else if (tagname.equalsIgnoreCase("Cash50")) {
                            _Cash50 = this.text;
                        } else if (tagname.equalsIgnoreCase("Cash100")) {
                            _Cash100 = this.text;
                        } else if (tagname.equalsIgnoreCase("Subscount")) {
                            _SubsCount = this.text;
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
        strParseResult[0] = _KeyRing;
        strParseResult[1] = _handBand;
        strParseResult[2] = _Tshirt;
        strParseResult[3] = _Data1GB;
        strParseResult[4] = _Cash30;
        strParseResult[5] = _Cash50;
        strParseResult[6] = _Cash100;
        strParseResult[7] = _SubsCount;
        return strParseResult;
    }


    public class DownloadAndParseSubmitWinnerTask extends AsyncTask<String, Void, String[]> {
        private ProgressDialog dialog;

        public DownloadAndParseSubmitWinnerTask() {
            this.dialog = new ProgressDialog(MainActivity.this);
        }

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public String[] doInBackground(String... urls) {
            return MainActivity.this.parseSubmitWinner(urls[0]);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(String[] result) {
            if (result[0].equals("0")) {
//                Intent i = new Intent(LoginActivity.this, MainActivity.class);
//                i.putExtra("UserName",Userlogin.toString());
//                startActivity(i);

                //startActivity(new Intent(LoginActivity.this, MainActivity.class));
                //Toast.makeText(LoginActivity.this, result[0], Toast.LENGTH_LONG).show();
            } else {
//                showAlertDialog(LoginActivity.this , "Error Login", "Wrong UserName or Password", Boolean.FALSE);
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
                        if (tagname.equalsIgnoreCase("SubmitReport")) {
                            _RESULT = "";
                            break;
                        }
                        break;
                    case 3:
                        if (tagname.equalsIgnoreCase("Reply")) {
                            _RESULT = this.text;
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

}