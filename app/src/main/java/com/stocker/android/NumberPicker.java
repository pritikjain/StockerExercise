package com.stocker.android;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stocker.stockerexercise.R;

/**
 * Created by pritijain on 04/05/15.
 */
public class NumberPicker extends RelativeLayout {

    final EditText mValueEdit;

    public NumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.number_picker, this);
        mValueEdit = (EditText) findViewById(R.id.value);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberPicker);
        int initialValue = 10;
        if (a.getIndexCount() > 0) {
            initialValue = a.getInt(0, initialValue);
        }
        a.recycle();
        mValueEdit.setText(String.valueOf(initialValue));
        TextView chevron = (TextView) findViewById(R.id.number_dec);
        chevron.setTypeface(Typeface.createFromAsset(context.getAssets(), "fontawesome.ttf"));
        chevron.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                float intval = Float.parseFloat(mValueEdit.getText().toString());
                mValueEdit.setText(String.valueOf(intval - 1.0));
            }
        });
        chevron = (TextView) findViewById(R.id.number_inc);
        chevron.setTypeface(Typeface.createFromAsset(context.getAssets(), "fontawesome.ttf"));
        chevron.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                int intval = Integer.parseInt(mValueEdit.getText().toString());

                mValueEdit.setText(String.valueOf(intval + 1));
            }
        });
    }

    public float getValue() {
        return Float.parseFloat(mValueEdit.getText().toString());
    }

    public void setValue(float value){
        mValueEdit.setText(String.valueOf(value));
    }
}
