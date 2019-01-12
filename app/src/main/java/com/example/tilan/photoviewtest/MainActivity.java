package com.example.tilan.photoviewtest;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
    static final int NO_DATA = -1;

    static final int MANUAL = 1;
    static final int AUTO_NO_MAN = 0;
    static final int AUTO_WITH_MAN = 2;

    static final int GOAL_EXECUTING=1;
    static final int GOAL_ERROR=2;
    static final int GOAL_IDLE=0;
    static final int GOAL_SUCCESS=3;


    int MODE = AUTO_NO_MAN;
    double AZIMUTH = NO_DATA, STRENGTH = NO_DATA;

    int ID = 1;
    double X_LOC = 200;
    double Y_LOC = 200;
    String SPD = "0";
    int NAV_STATE;

    double cx = 800;
    double cy = 800;
    double OLDcx = 0;
    double OLDcy = 0;
    double cxuser = NULL;
    double cyuser = NULL;
    double OLDcxuser = 0;
    double OLDcyuser = 0;

    //static String IP="10.10.15.186";
    //static String IP="192.168.1.101";
    static String IP="10.42.0.1";

    ImageView mImageView;
    Bitmap mutableBitmap;
    Bitmap mBitmap ;
    Canvas canvas;
    AwesomeSpeedometer SpeedMeter;

    PhotoViewAttacher mAttacher;

    FloatingActionButton manual, autoWithMan, autoNoMan, menu, devMenu;

    LinearLayout manualLayout, nomanLayout, withmanLayout, speedView, devLayout;

    JoystickView js1;

    Animation showButtons;
    Animation hideButtons;
    Animation showLayout;
    Animation hideLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.iv_test);

        SpeedMeter = findViewById(R.id.awesomeSpeedometer);
        speedView = findViewById(R.id.speed_view);

        manual = findViewById(R.id.manual);
        autoWithMan = findViewById(R.id.autowithman);
        autoNoMan = findViewById(R.id.autonoman);
        devMenu=findViewById(R.id.dev);

        manualLayout = findViewById(R.id.manual_layout);
        nomanLayout = findViewById(R.id.autonoman_layout);
        withmanLayout = findViewById(R.id.autowithman_layout);
        devLayout=findViewById(R.id.dev_layout);

        menu = findViewById(R.id.menu);

        js1 = findViewById(R.id.js1);


        mBitmap= BitmapFactory.decodeResource(getResources(), R.drawable.three_and_half);

        mutableBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        showButtons = AnimationUtils.loadAnimation(MainActivity.this, R.anim.show_button);
        hideButtons = AnimationUtils.loadAnimation(MainActivity.this, R.anim.hide_button);
        showLayout = AnimationUtils.loadAnimation(MainActivity.this, R.anim.show_layout);
        hideLayout = AnimationUtils.loadAnimation(MainActivity.this, R.anim.hide_layout);

        SetImage();
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (withmanLayout.getVisibility() == View.VISIBLE && nomanLayout.getVisibility() == View.VISIBLE && manualLayout.getVisibility() == View.VISIBLE) {
                    withmanLayout.setVisibility(View.GONE);
                    nomanLayout.setVisibility(View.GONE);
                    manualLayout.setVisibility(View.GONE);
                    devLayout.setVisibility(View.GONE);

                    withmanLayout.startAnimation(hideLayout);
                    nomanLayout.startAnimation(hideLayout);
                    manualLayout.startAnimation(hideLayout);
                    devLayout.startAnimation(hideLayout);
                    menu.startAnimation(hideButtons);
                } else {
                    withmanLayout.setVisibility(View.VISIBLE);
                    nomanLayout.setVisibility(View.VISIBLE);
                    manualLayout.setVisibility(View.VISIBLE);
                    devLayout.setVisibility(View.VISIBLE);

                    withmanLayout.startAnimation(showLayout);
                    nomanLayout.startAnimation(showLayout);
                    manualLayout.startAnimation(showLayout);
                    devLayout.startAnimation(showLayout);
                    menu.startAnimation(showButtons);

                }
            }
        });
        devMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                withmanLayout.setVisibility(View.GONE);
                nomanLayout.setVisibility(View.GONE);
                manualLayout.setVisibility(View.GONE);
                devLayout.setVisibility(View.GONE);

                withmanLayout.startAnimation(hideLayout);
                nomanLayout.startAnimation(hideLayout);
                manualLayout.startAnimation(hideLayout);
                devLayout.startAnimation(hideLayout);

                menu.startAnimation(hideButtons);

                Intent intent=new Intent(MainActivity.this,nodeStateActivity.class);
                startActivity(intent);
            }
        });
        autoNoMan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                builder2.setMessage("Are you sure you want switch to self driving mode?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MODE = AUTO_NO_MAN;
                        js1.setVisibility(View.INVISIBLE);
                        STRENGTH = NO_DATA;
                        AZIMUTH = NO_DATA;
                        drawTheMap(true, true);
                        new SendData(NO_DATA, NO_DATA).execute();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
                withmanLayout.setVisibility(View.GONE);
                nomanLayout.setVisibility(View.GONE);
                manualLayout.setVisibility(View.GONE);
                devLayout.setVisibility(View.GONE);

                devLayout.startAnimation(hideLayout);
                withmanLayout.startAnimation(hideLayout);
                nomanLayout.startAnimation(hideLayout);
                manualLayout.startAnimation(hideLayout);


                menu.startAnimation(hideButtons);
            }
        });
        autoWithMan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                builder2.setMessage("Are you sure you want switch to ZDrive mode?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MODE = AUTO_WITH_MAN;
                        js1.setVisibility(View.INVISIBLE);
                        STRENGTH = NO_DATA;
                        AZIMUTH = NO_DATA;
                        new SendData(NO_DATA, NO_DATA).execute();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
                withmanLayout.setVisibility(View.GONE);
                nomanLayout.setVisibility(View.GONE);
                manualLayout.setVisibility(View.GONE);
                devLayout.setVisibility(View.GONE);

                devLayout.startAnimation(hideLayout);
                withmanLayout.startAnimation(hideLayout);
                nomanLayout.startAnimation(hideLayout);
                manualLayout.startAnimation(hideLayout);
                menu.startAnimation(hideButtons);
            }
        });
        manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                builder2.setMessage("Are you sure you want switch to manual mode?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        js1.setVisibility(View.VISIBLE);
                        MODE = MANUAL;
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
                withmanLayout.setVisibility(View.GONE);
                nomanLayout.setVisibility(View.GONE);
                manualLayout.setVisibility(View.GONE);
                devLayout.setVisibility(View.GONE);

                devLayout.startAnimation(hideLayout);
                withmanLayout.startAnimation(hideLayout);
                nomanLayout.startAnimation(hideLayout);
                manualLayout.startAnimation(hideLayout);
                menu.startAnimation(hideButtons);
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
                }, 0, 10);
            }
        });

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new GetData().execute();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SetSpeed();
                    }
                });
                if (OLDcy != cy || OLDcx != cx) {
                    drawTheMap(false, true);
                }
                //SetSpeed();
            }
        }, 0, 100);
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

    public void drawTheMap(boolean userIn, boolean pmv) {

//        Log.d("imageWidyh",String.valueOf(mBitmap.getWidth()));
//        Log.d("imageImvHeight",String.valueOf(mImageView.getHeight()));

        if(NAV_STATE==GOAL_EXECUTING){
            userIn=true;
        }
        mutableBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        canvas = new Canvas(mutableBitmap);

        Paint paintuser = new Paint();
        paintuser.setStyle(Paint.Style.FILL);
        paintuser.setColor(Color.GREEN);
        paintuser.setAntiAlias(true);

        Paint paintzeg = new Paint();
        paintzeg.setStyle(Paint.Style.FILL);
        paintzeg.setColor(Color.RED);
        paintzeg.setAntiAlias(true);
        if (pmv) {
            canvas.drawCircle((float) cx, (float) cy, 10, paintzeg);
            Log.d("imagecanvas",String.valueOf(canvas.getWidth()));
            Log.d("imagecanvas",String.valueOf(canvas.getHeight()));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SetImage();
                }
            });
        }
        if (userIn) {
            canvas.drawCircle((float) cxuser, (float) cyuser, 10, paintuser);
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

    private class PhotoTapListener implements PhotoViewAttacher.OnPhotoTapListener {
        @Override
        public void onPhotoTap(View view, float x, float y) {
            double xPercentage = x * 100f;
            double yPercentage = y * 100f;

            menu.startAnimation(hideButtons);
            if (MODE != MANUAL) {
                SendData up = new SendData(xPercentage, yPercentage);
                up.execute();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Please Select a mode other than manual").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
            }
        }
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
                cxuser = TO_X_LOC * mutableBitmap.getWidth() / 100;
                cyuser = TO_Y_LOC * mutableBitmap.getHeight() / 100;
                drawTheMap(true, true);

            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String str = "http://"+IP+"/update_from_app.php?TO_X_LOC=" + TO_X_LOC + "&TO_Y_LOC=" + TO_Y_LOC + "&MODE=" + MODE + "&STRENGTH=" + STRENGTH + "&AZIMUTH=" + AZIMUTH + "";
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
        protected JSONArray doInBackground(Void... voids) {
            String str = "http://"+IP+"/select.php";
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
                        NAV_STATE=jsonobject.getInt("NAV_STATE");
                        Log.d("State",String.valueOf(NAV_STATE));

                        if(NAV_STATE==GOAL_ERROR){
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("The location is unreachable").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                        }

                        if(NAV_STATE==GOAL_SUCCESS){
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("You have arrived").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                        }
                        cx=X_LOC * mutableBitmap.getWidth() / 100;
                        cy=Y_LOC * mutableBitmap.getHeight() / 100;
                        Log.d("App", String.valueOf(SPD));
                    }
                } catch (JSONException ex) {
                    Log.e("App", "Failure", ex);
                }
            }
        }
    }
}
