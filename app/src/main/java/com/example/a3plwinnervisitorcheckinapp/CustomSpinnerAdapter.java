package com.example.a3plwinnervisitorcheckinapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private String[] mObjects;
    private String mFirstElement;
    private boolean mIsFirstTime;

    public CustomSpinnerAdapter(Context _context, int _textViewResourceId, String[] _objects, String _defaultText) {
        super(_context, _textViewResourceId, _objects);
        this.mContext = _context;
        this.mObjects = _objects;
        this.mIsFirstTime = true;
        setDefaultText(_defaultText);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(mIsFirstTime) {
            mObjects[0] = mFirstElement;
            mIsFirstTime = false;
        }
        return getCustomView(position, convertView, parent);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        notifyDataSetChanged();
        return getCustomView(position, convertView, parent);
    }

    public void setDefaultText(String _defaultText) {
        this.mFirstElement = mObjects[0];
        mObjects[0] = _defaultText;
    }


    public View getCustomView(int _position, View _convertView, ViewGroup _parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.spinner_row, _parent, false);
        TextView label = (TextView) row.findViewById(R.id.spinner_text);
        label.setText(mObjects[_position]);

        return row;
    }
}
