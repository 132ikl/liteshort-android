package sh.ikl.liteshort;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

public class OpenListener implements View.OnClickListener{

    private String link;

    @Override
    public void onClick(View v) {
        if (this.link != null) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(this.link));
            v.getContext().startActivity(browserIntent);
        }
    }

    void setLink(String link) {
        this.link = link;
    }
}

