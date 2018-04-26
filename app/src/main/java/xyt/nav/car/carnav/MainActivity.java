package xyt.nav.car.carnav;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Context;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.io.File;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.Config;

public class MainActivity extends AppCompatActivity {
    ImageView map;
    ProgressDialog pDialog;

    private static final String PHOTOSERVICE_URL = "http://imgsv.imaging.nikon.com/lineup/lens/zoom/normalzoom/af-s_dx_18-140mmf_35-56g_ed_vr/img/sample/img_01.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        System.out.println("--------------------Working Directory = " + this.getApplicationInfo().dataDir );

        map = (ImageView) findViewById(R.id.Map);

        map.setBackgroundResource(R.drawable.map);
//        map.setImageResource(R.drawable.car_symbol);
        Bitmap bitmap = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        canvas.drawCircle(10, 10, 1, paint);
        map.setImageBitmap(bitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class LoadImage extends AsyncTask<String, String, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Fetching Image from Minio Server....");
            pDialog.show();
        }

        protected Bitmap doInBackground(String... args) {
            InputStream inputStream = null;
            String result = "";

            try {
                URL url = new URL(args[0]);

                HttpURLConnection httpCon =
                        (HttpURLConnection) url.openConnection();

                if (httpCon.getResponseCode() != 200)
                    throw new Exception("Failed to connect");

                // Fetch the content as an inputStream.
                inputStream = httpCon.getInputStream();

                // Convert the fetched inputstream to string.
                if (inputStream != null)
                    result = convertInputStreamToString(inputStream);
                else
                    result = "Did not work!";

                if (result != null) System.out.println(result);

                // convert String to JSONObject.
                JSONObject json = new JSONObject(result);

                // get the array of photos.
                JSONArray imageJSON = json.getJSONArray("Album");
                int index = imageJSON.length()-1;

                Random rand = new Random();

                // Let's get a randomly pic a picture to load.
                int rindex = rand.nextInt((index - 0) + 1) + 0;


                // Return the image.
                return BitmapFactory.decodeStream(new URL(imageJSON.getJSONObject(rindex).getString("url")).openStream());
            } catch (Exception e) {

                System.out.println(e.getMessage());
            }
            return null;

        }


        protected void onPostExecute(Bitmap image) {
            System.out.println("In Post Execute");
            if (image != null) {
                pDialog.dismiss();
                // Place the image on the ImageView.
                map.setImageBitmap(image);

            } else
            {
                pDialog.dismiss();
                Toast.makeText(MainActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();
            }

        }

    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{

        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";

        // Loop through the stream line by line and convert to a String.
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}
