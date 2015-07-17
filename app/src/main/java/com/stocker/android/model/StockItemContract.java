package com.stocker.android.model;

import android.net.Uri;

/**
 * Created by pritijain on 03/05/15.
 */
public final class StockItemContract {
    /**
     * The authority of the StockItems provider.
     */
    public static final String AUTHORITY = "com.stocker";

    /**
     * The content URI for the top-level
     * stock table
     */
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY);


}
