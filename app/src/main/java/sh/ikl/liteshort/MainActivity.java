package sh.ikl.liteshort;

import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.content.ClipboardManager;
import android.webkit.URLUtil;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences settings = getApplicationContext().getSharedPreferences("liteshort", 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText serverUrl = findViewById(R.id.serverUrl);
        serverUrl.setText(settings.getString("Server", "https://ls.ikl.sh"));
    }

    public void onClick(View v) {
        EditText serverUrl = findViewById(R.id.serverUrl);
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);


        SharedPreferences settings = getApplicationContext().getSharedPreferences("liteshort", 0);
        SharedPreferences.Editor editor = settings.edit();


        ClipData data = clipboard.getPrimaryClip();
        String dataUri = String.valueOf(data.getItemAt(0).getText());

        if (URLUtil.isNetworkUrl(dataUri)) {
            new Shortener(v, clipboard).execute(serverUrl.getText().toString(), dataUri);
        }

        editor.putString("Server", String.valueOf(serverUrl.getText()));
        editor.apply();
    }

    static class Shortener extends AsyncTask<String, Void, JSONObject> {


        private View view;
        private ClipboardManager clipman;

        private Shortener(View v, ClipboardManager clipman) {
            this.view = v;
            this.clipman = clipman;
        }

        protected JSONObject doInBackground(String... strings) {

            try {
                String urlParameters = String.format("format=json&long=%s", strings[1]);

                byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

                URL url = new URL(strings[0]);
                HttpURLConnection conn= (HttpURLConnection) url.openConnection();

                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(true);
                conn.setRequestMethod("POST");

                try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
                    wr.write(postData);
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;

                JSONObject responseJSON = new JSONObject();
                while ((inputLine = in.readLine()) != null) {
                    responseJSON = new JSONObject(inputLine);
                }
                in.close();
                return responseJSON;
            } catch (Exception e) {
                Snackbar.make(this.view, "Failed to generate URL. Error: " + e.toString(), 5000);
                return null;
            }
        }

        protected void onPostExecute(JSONObject responseJSON) {
            try {
                Snackbar.make(this.view, "Successfully shortened link! Now available at: " + responseJSON.getString("result"), 3000).show();
                ClipData replaceData = ClipData.newPlainText("liteshort shortened", responseJSON.getString("result"));
                this.clipman.setPrimaryClip(replaceData);
            } catch (JSONException e) {
                Snackbar.make(this.view, "Failed to generate URL. Error: " + e.toString(), 5000);
            }
        }

    }
}

