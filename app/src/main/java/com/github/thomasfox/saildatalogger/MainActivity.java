package com.github.thomasfox.saildatalogger;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ToggleButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);

        ToggleButton enableLoggingButton = findViewById(R.id.enableLoggingButton);
        enableLoggingButton.setOnClickListener(new EnableLoggingClickListener(statusText,this));

        setSupportActionBar((Toolbar) findViewById(R.id.mainToolbar));
        getSupportActionBar().setTitle(
                getResources().getString(R.string.app_name) + " "
                        + getResources().getString(R.string.app_version));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.close:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
