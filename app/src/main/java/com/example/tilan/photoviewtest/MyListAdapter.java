package com.example.tilan.photoviewtest;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

public class MyListAdapter extends ArrayAdapter<String> {
    private final Activity context;
    String[] nodeNames;
    Boolean[] nodeStates;

    public MyListAdapter(@NonNull Activity context, String[] nodeName,Boolean[] nodeState) {
        super(context, R.layout.list_item);
        this.context = context;
        this.nodeNames=nodeName;
        this.nodeStates=nodeState;
    }
    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_item, null, true);

        TextView nodeName=rowView.findViewById(R.id.nodeName);
        RadioButton nodeState=rowView.findViewById(R.id.nodeState);

        nodeName.setText(nodeNames[position]);
        nodeState.setChecked(nodeStates[position]);

        return rowView;
    }
}
