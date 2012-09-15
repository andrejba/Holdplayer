package de.abmw.holdplayer;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class Holdplayer extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_holdplayer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_holdplayer, menu);
        return true;
    }
}
