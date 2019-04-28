package sh.ikl.liteshort;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;
import android.webkit.URLUtil;
import android.widget.Toast;

@RequiresApi(api = Build.VERSION_CODES.N)
public class LiteshortTileService extends TileService {

    // Set the tile to be have inactive background (just aesthetics)
    public void onStartListening() {
        Tile t = getQsTile();
        t.setState(Tile.STATE_INACTIVE);
        t.updateTile();
    }

    public void onClick() {
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
