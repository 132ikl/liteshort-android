package sh.ikl.liteshort;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Shortener extends AsyncTask<String, Void, JSONObject> {

    private View view;
    private Context context;
    private ClipboardManager clipman;
    private boolean shouldCopy;

    // Used for MainActivity
    Shortener(View v, ClipboardManager clipman) {
        this.view = v;
        this.clipman = clipman;
    }

    // Used for QS Tile
    Shortener(Context context, ClipboardManager clipman) {
        this.context = context;
        this.clipman = clipman;
    }

    protected JSONObject doInBackground(String... strings) {
        try {

            // Use options string so can easily add short if exists
            String options = "&long=" + strings[1];

            // Check if short URL provided and add to options
            if (strings.length >= 3) {
                options = options + "&short=" + strings[2];
            }

            // Grab long url from parameters, and put it in POST string
            String urlParameters = "format=json" + options;

            // HttpURLConnection handles POSTs really weird, so encode it into byte array
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            // Create new connection from the first string passed (liteshort server)
            URL url = new URL(strings[0]);
            HttpURLConnection conn= (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("POST");

            // Write post data
            try( DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.write(postData);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            JSONObject resultJSON = new JSONObject();

            // Read output of request and turn it into a JSON object
            while ((inputLine = in.readLine()) != null) {
                resultJSON = new JSONObject(inputLine);
            }

            in.close();
            return resultJSON;
            
        } catch (IOException e) {
            // Usually only happens when can't connect to server
            handleReturn(false, "Invalid server?");
            return null;
        } catch (JSONException e) {
            handleReturn(false, e.toString());
            return null;
        }
    }

    protected void onPostExecute(JSONObject resultJSON) {
        try {
            // Return true with link
            handleReturn(true, resultJSON.getString("result"));
        } catch (JSONException e) {
            // Return false with error
            handleReturn(false, e.toString());
        } catch (NullPointerException e) {
            // Don't show error here, because it's probably already been dealt with
        }
    }

    private void handleReturn(boolean success, String result) {
        // If MainActivity
        if (this.view != null) {
            if (success) {
                // Create popup with link to new short URL
                Snackbar sb = Snackbar.make(this.view, "Successfully shortened link! Now available at: " + result, 3500);

                // Add "Open" selection on popup
                OpenListener sbListener = new OpenListener();
                sbListener.setLink(result);
                sb.setAction(R.string.open_string, sbListener);

                sb.show();
                sb.setActionTextColor(Color.parseColor("#0087ff"));

                // If copy url checkbox is selected then put new url on clipboard
                if (this.shouldCopy) {
                    ClipData replaceData = ClipData.newPlainText("liteshort url", result);
                    this.clipman.setPrimaryClip(replaceData);
                }
            } else {
                Snackbar.make(this.view, "Failed to generate short URL. Error: " + result, 5000).show();
            }
        // If QS Tile
        } else if (this.context != null) {
            if (success) {
                // Put new url on clipboard and send toast
                ClipData replaceData = ClipData.newPlainText("liteshort url", result);
                this.clipman.setPrimaryClip(replaceData);
                Toast.makeText(this.context, "Successfully shortened link! Now available at: " + result, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this.context, "Failed to generate short URL. Error: " + result, Toast.LENGTH_LONG).show();
            }
        }
    }

    // shouldCopy setter
    void setShouldCopy(boolean shouldCopy) {
        this.shouldCopy = shouldCopy;
    }
}
