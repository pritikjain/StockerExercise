package com.stocker.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.widget.RemoteViews;

import com.stocker.android.model.Stock;
import com.stocker.stockerexercise.R;

import java.text.DecimalFormat;

/**
 * Created by pritijain on 03/05/15.
 */
public class NotificationController implements Stock.OnValueChanged {
    Context mContext;

    public NotificationController(Context context) {
        mContext = context;
    }

    @Override
    public void onValueChanged(Stock stock) {
        if (stock.getLastDelta() == null || stock.getLastDelta() == 0) return;
        //Check for threshold and if it holds update the notification
        int change = (int) ((stock.getLastDelta() * 100) / (stock.getPrice() - stock.getLastDelta()));


        if( Math.abs(change) < Math.abs(stock.getThreshold()) )
            return;
        createOrUpdateNotification(stock);
    }

    public void createOrUpdateNotification(Stock stock) {
        //This should be cancellable notification using NotificationBuilder

        // Prepare intent which ius triggered if the notification is selected
        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        // Build notification
        // Actions are just fake

        int color = stock.getLastDelta() > 0 ? mContext.getResources().getColor(android.R.color.holo_green_light) : mContext.getResources().getColor(android.R.color.holo_red_light);

        int change = (int) ((stock.getLastDelta() * 100) / (stock.getPrice() - stock.getLastDelta()));
        String indicator_text = stock.getLastDelta() > 0 ? mContext.getString(R.string.risen) : mContext.getString(R.string.dropped);

        DecimalFormat myFormatter = new DecimalFormat("##0.00");


        String contentText = mContext.getString(R.string.notification_content, stock
                .getSymbol().toUpperCase(), color, indicator_text, myFormatter.format(stock.getPrice()), "(" + change +"%)");


        SpannableStringBuilder cs = new SpannableStringBuilder(contentText);
        int start = contentText.indexOf(indicator_text);
        int end = start + indicator_text.length();
        cs.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);



        Notification noti = new NotificationCompat.Builder(mContext)
                .setContentTitle(mContext.getString(R.string.title))
                .setContentText(cs)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // Hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(stock.getId().intValue(), noti);


    }

    public void start() {
        Stock.addOnValueChangeListener(this);
    }

    public void stop() {
        Stock.removeOnValueChangedListener(this);
    }
}
