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
        // Get long URL from clipboard
        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = clipboard.getPrimaryClip();
        String longUrl = data.getItemAt(0).getText().toString();

        // Get server URL from preferences
        SharedPreferences settings = getApplicationContext().getSharedPreferences("liteshort", 0);
        String serverUrl = settings.getString("Server", "https://ls.ikl.sh");

        // Check to see if long URL is an actual URL
        if (URLUtil.isNetworkUrl(longUrl)) {
            new Shortener(this, clipboard).execute(serverUrl, longUrl);
        } else {
            Toast.makeText(this, "Copied item is not a URL.", Toast.LENGTH_LONG).show();
        }

    }

}
