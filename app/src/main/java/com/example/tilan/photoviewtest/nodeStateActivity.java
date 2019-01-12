package com.example.tilan.photoviewtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class nodeStateActivity extends AppCompatActivity {

    String[] mobileArray = {"Android","IPhone","WindowsMobile","Blackberry",
            "WebOS","Ubuntu","Windows7","Max OS X"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_state);
        ArrayAdapter adapter = new ArrayAdapter<>(this,
                R.layout.list_item, mobileArray);

        ListView listView = findViewById(R.id.nodes);
        listView.setAdapter(adapter);
    }
}
