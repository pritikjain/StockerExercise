package com.stocker.android.model;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by pritijain on 03/05/15.
 */
public class StocksQuoteProvider extends ContentProvider {


    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return Stock.getCursor(Stock.class, null, null, null, null, null);
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("This operation is not supported");

    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        throw new UnsupportedOperationException("This operation is not supported");
    }
}
