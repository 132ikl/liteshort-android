package sh.ikl.liteshort;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.URLUtil;
import android.widget.Toast;

public class TileActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load blank activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            return;
        }
        shorten();
        finishAffinity();
    }

    private void shorten() {
        // Get clipboard service
        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);

        // Check to make sure there actually is something in clipboard
        String longUrl = "";
        if (clipboard.getPrimaryClip() != null) {
            ClipData data = clipboard.getPrimaryClip();
            longUrl = data.getItemAt(0).getText().toString();
        }

        // Get server URL from preferences
        SharedPreferences settings = getApplicationContext().getSharedPreferences("liteshort", 0);
        String serverUrl = settings.getString("Server", "https://ls.ikl.sh");

        // Check to see if long URL exists and is an actual URL
        if (!longUrl.isEmpty()) {
            if (URLUtil.isNetworkUrl(longUrl)) {
                new Shortener(this, clipboard).execute(serverUrl, longUrl);
            } else {
                Toast.makeText(this, "Copied item is not a URL.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "No clipboard data found.", Toast.LENGTH_LONG).show();
        }
    }

}
