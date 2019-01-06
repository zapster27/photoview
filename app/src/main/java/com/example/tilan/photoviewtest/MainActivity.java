package com.example.tilan.photoviewtest;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.github.anastr.speedviewlib.AwesomeSpeedometer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import io.github.controlwear.virtual.joystick.android.JoystickView;
import uk.co.senab.photoview.PhotoViewAttacher;

import static java.sql.Types.NULL;


public class MainActivity extends AppCompatActivity {
//    static final String PHOTO_TAP_TOAST_STRING = "Photo Tap! X: %.2f %% Y:%.2f %% ID: %d";
    static final int NO_DATA = -1;
    static final int MANUAL = 1;
    static final int AUTO_NO_MAN = 0;
    static final int AUTO_WITH_MAN = 2;


    int MODE = AUTO_NO_MAN;
    double AZIMUTH = NO_DATA, STRENGTH = NO_DATA;

    int ID=1;
    double X_LOC = 200;
    double Y_LOC = 200;
    String SPD = "0";

    double cx = 800;
    double cy = 800;
    double OLDcx = 0;
    double OLDcy = 0;
    double cxuser = NULL;
    double cyuser = NULL;
    double OLDcxuser = 0;
    double OLDcyuser = 0;

    ImageView mImageView;
    Bitmap mutableBitmap;
    AwesomeSpeedometer SpeedMeter;
    PhotoViewAttacher mAttacher;
    FloatingActionButton manual, autoWithMan, autoNoMan;
    JoystickView js1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.iv_test);
        SpeedMeter = findViewById(R.id.awesomeSpeedometer);
        manual = findViewById(R.id.manual);
        autoWithMan = findViewById(R.id.autowithman);
        autoNoMan = findViewById(R.id.autonoman);
        js1 = findViewById(R.id.js1);
        final Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map);
        mutableBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);

        SetImage();
        autoNoMan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MODE = AUTO_NO_MAN;
                js1.setVisibility(View.INVISIBLE);
                STRENGTH = NO_DATA;
                AZIMUTH = NO_DATA;
                drawTheMap(true,true);
                new SendData(NO_DATA, NO_DATA).execute();
            }
        });
        autoWithMan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MODE = AUTO_WITH_MAN;
                js1.setVisibility(View.INVISIBLE);
                STRENGTH = NO_DATA;
                AZIMUTH = NO_DATA;
                new SendData(NO_DATA, NO_DATA).execute();
            }
        });
        manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                js1.setVisibility(View.VISIBLE);
                MODE = MANUAL;
                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        js1.setOnMoveListener(new JoystickView.OnMoveListener() {
                            @Override
                            public void onMove(int angle, int strength) {
                                STRENGTH = strength;
                                AZIMUTH = angle;
                                Log.d("STRENGTH", String.valueOf(STRENGTH));
                                Log.d("AZIMUTH", String.valueOf(AZIMUTH));
                                new SendData(NO_DATA, NO_DATA).execute();
                            }
                        });
                    }
                }, 0, 500);
            }
        });

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new GetData().execute();
                if(X_LOC!=NULL){
                    cx=(X_LOC/480)*mBitmap.getWidth();
                    cy=(Y_LOC/480)*mBitmap.getHeight();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SetSpeed();
                    }
                });
                if(OLDcy!=cy || OLDcx!=cx){
                    drawTheMap(false,true);
                }
                //SetSpeed();
            }
        },0,1100);
    }

    private void SetSpeed() {
        SpeedMeter.speedTo(Float.parseFloat(SPD));
    }

    private void SetImage() {
        mImageView.setImageDrawable(new BitmapDrawable(getResources(), mutableBitmap));        // The MAGIC happens here!
        mAttacher = new PhotoViewAttacher(mImageView);

        // Lets attach some listeners, not required though!
        mAttacher.setOnPhotoTapListener(new PhotoTapListener());
    }

    private class PhotoTapListener implements PhotoViewAttacher.OnPhotoTapListener {

        @Override
        public void onPhotoTap(View view, float x, float y) {
            double xPercentage = x * 100f;
            double yPercentage = y * 100f;

            if (MODE != MANUAL) {
                SendData up = new SendData(xPercentage, yPercentage);
                up.execute();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Please Select a mode other than manual").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
            }
        }
    }

    public void drawTheMap(boolean userIn, boolean pmv) {
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map);
        mutableBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        Paint paintuser = new Paint();
        paintuser.setStyle(Paint.Style.FILL);
        paintuser.setColor(Color.GREEN);
        paintuser.setAntiAlias(true);

        Paint paintzeg = new Paint();
        paintzeg.setStyle(Paint.Style.FILL);
        paintzeg.setColor(Color.RED);
        paintzeg.setAntiAlias(true);
        if (pmv) {
            canvas.drawCircle((float) cx, (float) cy, 40, paintzeg);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SetImage();
                }
            });
        }
        if (OLDcxuser != cxuser || OLDcyuser != cyuser && userIn) {
            canvas.drawCircle((float) cxuser, (float) cyuser, 40, paintuser);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SetImage();
                }
            });
        }
        OLDcxuser = cxuser;
        OLDcyuser = cyuser;
        OLDcx = cx;
        OLDcy = cy;
    }

    private class SendData extends AsyncTask<Void, Void, Void> {
        double TO_X_LOC;
        double TO_Y_LOC;

        private SendData(double TO_X_LOC, double TO_Y_LOC) {
            this.TO_X_LOC = TO_X_LOC;
            this.TO_Y_LOC = TO_Y_LOC;
        }


        @Override
        protected void onPreExecute() {
                if (TO_X_LOC != NULL) {
                    cxuser = TO_X_LOC * 2560 / 100;
                    cyuser = TO_Y_LOC * 2560 / 100;
                    drawTheMap(true, true);

            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String str = "http://192.168.1.101/update_from_app.php?TO_X_LOC=" + TO_X_LOC + "&TO_Y_LOC=" + TO_X_LOC + "&MODE=" + MODE + "&STRENGTH=" + STRENGTH + "&AZIMUTH=" + AZIMUTH + "";
            try {
                URL url = new URL(str);
//                Conn = url.openConnection();
                HttpURLConnection Conn = (HttpURLConnection) url.openConnection();
                InputStream in = Conn.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);
                int data = isw.read();
                while (data != -1) {
                    char current = (char) data;
                    data = isw.read();
                    Log.d("current", String.valueOf(current));
                }
            } catch (Exception e) {
                Log.e("App", "yourDataTask", e);
                return null;
            }
            return null;
        }
    }

    public class GetData extends AsyncTask<Void, Void, JSONArray> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected JSONArray doInBackground(Void... voids) {
            String str = "http://192.168.1.101/select.php";
            URLConnection urlConn = null;
            BufferedReader bufferedReader = null;
            try {
                URL url = new URL(str);
                urlConn = url.openConnection();
                bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

                StringBuffer stringBuffer = new StringBuffer();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }

                return new JSONArray(stringBuffer.toString());
            } catch (Exception ex) {
                Log.e("App", "yourDataTask", ex);
                return null;
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        @Override
        protected void onPostExecute(JSONArray response) {
            if (response != null) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject jsonobject = response.getJSONObject(i);
                        ID = jsonobject.getInt("ID");
                        X_LOC = jsonobject.getDouble("X_LOC");
                        Y_LOC = jsonobject.getDouble("Y_LOC");
                        SPD = jsonobject.getString("SPD");
                        Log.d("App", String.valueOf(SPD));
                    }
                } catch (JSONException ex) {
                    Log.e("App", "Failure", ex);
                }
            }
        }
    }
}
