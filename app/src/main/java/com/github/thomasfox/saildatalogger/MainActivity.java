package com.github.thomasfox.saildatalogger;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ToggleButton enableLoggingButton;

    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);

        enableLoggingButton = findViewById(R.id.enableLoggingButton);
        enableLoggingButton.setOnClickListener(new EnableLoggingClickListener(statusText,this));
    }

    public void enableLog(View view)
    {
        System.out.print("view called");
    }
}
