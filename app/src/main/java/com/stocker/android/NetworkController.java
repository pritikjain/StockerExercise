package com.stocker.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.stocker.android.model.Stock;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import yahoofinance.YahooFinance;

/**
 * Created by pritijain on 03/05/15.
 */
public class NetworkController implements Stock.OnSymbolAdded {
    String action = "timerAction";
    Context mContext;
    PendingIntent pi;
    RefreshTimerReceiver alarmReceiver = new RefreshTimerReceiver();

    public NetworkController(Context context) {
        mContext = context;
    }

    public void start() {
        Stock.addOnSymbolAddedListener(this);


        IntentFilter ifilter = new IntentFilter();
        ifilter.addAction(action);

        mContext.registerReceiver(alarmReceiver, ifilter);
        startTimer();
        sync();
    }

    public void stop() {
        Stock.removeOnSymbolAddedListener(this);

        try {
            pi.cancel();
            mContext.unregisterReceiver(alarmReceiver);
        } catch (Exception e) {

        }

    }

    public void startTimer() {
        //Start a pending intent based timer
        Intent intent = new Intent(action);
        pi = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, 2);


        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
    }

    @Override
    public void onNewSymbolAdded(Stock stock) {
        sync();
    }



    public void sync() {
        Scheduler.runInBackground(new Runnable() {
            @Override
            public void run() {
                String[] symbols = Stock.getCommaSeparatedSymbols();
                if (symbols != null && symbols.length > 0) {
                    Map<String, yahoofinance.Stock> stocks = YahooFinance.get(symbols);
                    for (Map.Entry<String, yahoofinance.Stock> entry : stocks.entrySet()) {
                        List<Stock> dbStocks = Stock.find(Stock.class, "M_SYMBOL = ?", new String[]{entry.getKey()});
                        if (dbStocks != null && dbStocks.size() > 0) {
                            Stock dbStock = dbStocks.get(0);
                            dbStock.setName(entry.getValue().getName());
                            dbStock.setPrice(entry.getValue().getQuote().getPrice().floatValue());
                            dbStock.save();
                        }
                    }
                }

                startTimer();
            }
        });

    }


    class RefreshTimerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            sync();
        }
    }
}
