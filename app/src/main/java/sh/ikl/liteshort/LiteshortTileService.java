package sh.ikl.liteshort;

import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.N)
public class LiteshortTileService extends TileService {

    // Set the tile to be have inactive background (just aesthetics)
    public void onStartListening() {
        Tile t = getQsTile();
        t.setState(Tile.STATE_INACTIVE);
        t.updateTile();
    }

    // Launch TileActivity (cannot read clipboard without being in focus)
    public void onClick() {
        Intent tileIntent = new Intent(getApplicationContext(), TileActivity.class);
        tileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(tileIntent);
    }

}
