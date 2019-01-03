package com.example.tilan.photoviewtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.senab.photoview.PhotoViewAttacher;


public class MainActivity extends AppCompatActivity {
    static final String PHOTO_TAP_TOAST_STRING = "Photo Tap! X: %.2f %% Y:%.2f %% ID: %d";
    static final String SCALE_TOAST_STRING = "Scaled to: %.2ff";


    private PhotoViewAttacher mAttacher;

    private Toast mCurrentToast;

    private Matrix mCurrentDisplayMatrix = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                yourDataTask f=new yourDataTask();
                f.execute();
            }
        },0,1000);

        setContentView(R.layout.activity_main);

        ImageView mImageView = (ImageView) findViewById(R.id.iv_test);
        Bitmap mBitmap= BitmapFactory.decodeResource(getResources(),R.drawable.map);
        Bitmap mutableBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        int size=20;
        int Width=mBitmap.getWidth();
        int Height=mBitmap.getHeight();
        Paint paint=new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        int radius = Math.min(canvas.getWidth(),canvas.getHeight()/2);
        int padding = 5;
        canvas.drawCircle(1280, 1280, 20, paint );


//        Drawable bitmap = getResources().getDrawable(R.drawable.map);
        mImageView.setImageDrawable(new BitmapDrawable(getResources(),mutableBitmap));

        // The MAGIC happens here!
        mAttacher = new PhotoViewAttacher(mImageView);

        // Lets attach some listeners, not required though!
        mAttacher.setOnMatrixChangeListener(new MatrixChangeListener());
        mAttacher.setOnPhotoTapListener(new PhotoTapListener());



    }
    private class PhotoTapListener implements PhotoViewAttacher.OnPhotoTapListener{

        @Override
        public void onPhotoTap(View view, float x, float y) {
            float xPercentage = x * 100f;
            float yPercentage = y * 100f;

            float X_LOC=xPercentage;
            float Y_LOC=xPercentage;

            showToast(String.format(PHOTO_TAP_TOAST_STRING, X_LOC, Y_LOC, view == null ? 0 : view.getId()));
        }
    }
    private void showToast(CharSequence text) {
        if (null != mCurrentToast) {
            mCurrentToast.cancel();
        }

        mCurrentToast = Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT);
        mCurrentToast.show();
    }
    private class MatrixChangeListener implements PhotoViewAttacher.OnMatrixChangedListener{

        @Override
        public void onMatrixChanged(RectF rect) {
        }
    }
    private class yourDataTask extends AsyncTask<Void, Void, JSONArray>
    {
        @Override
        protected JSONArray doInBackground(Void... params)
        {

            String str="http://10.10.6.156/select.php";
            URLConnection urlConn = null;
            BufferedReader bufferedReader = null;
            try
            {
                URL url = new URL(str);
                urlConn = url.openConnection();
                bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

                StringBuffer stringBuffer = new StringBuffer();
                String line;
                while ((line = bufferedReader.readLine()) != null)
                {
                    stringBuffer.append(line);
                }

                return new JSONArray(stringBuffer.toString());
            }
            catch(Exception ex)
            {
                Log.e("App", "yourDataTask", ex);
                return null;
            }
            finally
            {
                if(bufferedReader != null)
                {
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
            if(response != null)
            {
                try {
                    for(int i=0; i < response.length(); i++) {
                        JSONObject jsonobject = response.getJSONObject(i);
                        String ID       = jsonobject.getString("ID");
                        String X_LOC    = jsonobject.getString("X_LOC");
                        String Y_LOC  = jsonobject.getString("Y_LOC");
                        String SPD = jsonobject.getString("SPD");
                        Log.d("App",  X_LOC);
                    }

                } catch (JSONException ex) {
                    Log.e("App", "Failure", ex);
                }
            }
        }


    }
}
