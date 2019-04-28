package sh.ikl.liteshort;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.ClipboardManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.CheckBox;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    boolean shouldCopy = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load stored server from prefs and put it into the text box
        EditText serverUrl = findViewById(R.id.serverUrl);
        SharedPreferences settings = getApplicationContext().getSharedPreferences("liteshort", 0);
        serverUrl.setText(settings.getString("Server", "https://ls.ikl.sh"));
    }

    public void onClick(View v) {

        // Hide keyboard
        hideKeyboard(this);

        // Create preferences to store server URL
        EditText TextServerUrl = findViewById(R.id.serverUrl);
        SharedPreferences settings = getApplicationContext().getSharedPreferences("liteshort", 0);
        SharedPreferences.Editor editor = settings.edit();

        shortenLink(v, shouldCopy);

        // Put server into config
        editor.putString("Server", TextServerUrl.getText().toString());
        editor.apply();
    }

    public static void shortenLink(View v, boolean shouldCopy) {
        // Put UI selections into variables
        EditText TextServerUrl = v.findViewById(R.id.serverUrl);
        EditText TextLongUrl = v.findViewById(R.id.longurl);
        EditText TextShortUrl = v.findViewById(R.id.shorturl);

        // Create storage for long url
        String longUrl = "";

        // Create clipboard manager for later
        ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);

        // Get the first item off of the primary clipboard
        ClipData data = clipboard.getPrimaryClip();
        String dataUri = data.getItemAt(0).getText().toString();

        // Check to see if something is inputted in the long URL box
        if (!TextLongUrl.getText().toString().isEmpty()) {
            longUrl = TextLongUrl.getText().toString();
        }
        else if (URLUtil.isNetworkUrl(dataUri)) {
            // If no value in box, check to see if the clipboard has an URL
            longUrl = dataUri;
        } else {
            Snackbar.make(v, "Copied item is not a URL and no long URL supplied.", 3000).show();
        }

        Shortener shortener = new Shortener(v, clipboard);
        shortener.setShouldCopy(shouldCopy);
        if (!longUrl.isEmpty()) {
            if (!TextShortUrl.getText().toString().isEmpty()) {
                shortener.execute(TextServerUrl.getText().toString(), longUrl, TextShortUrl.getText().toString());
            } else {
                shortener.execute(TextServerUrl.getText().toString(), longUrl);
            }
        }
    }


    public void onCheckboxClicked(View view) {
        // Set value to whether box is checked or not
        shouldCopy = ((CheckBox) view).isChecked();
    }


    public static void hideKeyboard(Activity activity) { // https://stackoverflow.com/a/17789187
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}

