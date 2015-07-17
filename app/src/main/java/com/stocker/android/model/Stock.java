package com.stocker.android.model;

import com.orm.SugarRecord;
import com.stocker.android.Scheduler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by pritijain on 03/05/15.
 * <p/>
 * The model class for stocks
 */
public class Stock extends SugarRecord {

    /**
     * Event interface
     */
    public interface OnValueChanged {
        /**
         * Sent for each stock whose value has changed
         *
         * @param stock
         */
        void onValueChanged(Stock stock);
    }

    public interface OnSymbolAdded {
        /**
         * Sent for each stock item when added to the model
         *
         * @param stock
         */
        void onNewSymbolAdded(Stock stock);
    }

    public interface OnSymbolRemoved {
        /**
         * Sent for each stock being removed
         * @param stock
         */
        void onSymbolRemoved(Stock stock);
    }

    /**
     * List of event listeners
     */
    static Set<OnValueChanged> mOnValueChangedListeners = new HashSet<>();
    static Set<OnSymbolAdded> mOnSymbolAddedListeners = new HashSet<>();
    static Set<OnSymbolRemoved> mOnSymbolRemovedListeners = new HashSet<>();

    /**
     * Public name for this listed stock
     */
    String mName;
    /**
     * The stock symbol
     */
    String mSymbol;
    /**
     * Last price
     */
    float mPrice;
    /**
     * Tracking threshold, can be in another db table logically but for now simplifying
     */
    float mThreshold;

    /**
     * The delta between last tracked values, used to generat notification triggers
     */
    Float mLastDelta = null;

    public Stock() {

    }

    public Stock(String symbol){
        mSymbol = symbol;
    }

    public Stock(String symbol, final float threshold) {
        mSymbol = symbol;
        mThreshold = threshold;
    }

    public Stock(String name, String symbol, float price, float threshold) {
        this.mName = name;
        this.mSymbol = symbol;
        this.mPrice = price;
        this.mThreshold = threshold;
    }

    // Getter and setter for every memeber variable
    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getSymbol() {
        return mSymbol;
    }

    public void setSymbol(String symbol) {
        mSymbol = symbol;
    }

    public float getPrice() {
        return mPrice;
    }

    public void setPrice(float price) {

        if (mPrice != 0) {
            mLastDelta = price - mPrice;
        }
        mPrice = price;
    }

    public Float getLastDelta() {
        return mLastDelta;
    }


    public float getThreshold() {
        return mThreshold;
    }

    public void setThreshold(float threshold) {
        mThreshold = threshold;
    }

    @Override
    public boolean delete() {
        boolean result = super.delete();
        if (result) {
            // send callback to symbols deleted.
            sendSymbolDeleted(this);
        }
        return result;
    }

    public long save() {
        boolean isAdded = getId() == null;

        super.save();

        if (isAdded) {
            //send callback to symbols added
            sendSymbolAdded(this);
        } else {
            //send callback to symbol value modified
            sendValueChanged(this);
        }

        return getId();
    }


    private static void sendSymbolAdded(final Stock stock) {
        for (final OnSymbolAdded listener : mOnSymbolAddedListeners) {
            Scheduler.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onNewSymbolAdded(stock);
                }
            });
        }
    }

    private static void sendValueChanged(final Stock stock) {
        for (final OnValueChanged listener : mOnValueChangedListeners) {
            Scheduler.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onValueChanged(stock);
                }
            });
        }
    }

    private static void sendSymbolDeleted(final Stock stock) {
        for (final OnSymbolRemoved listener : mOnSymbolRemovedListeners) {
            Scheduler.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onSymbolRemoved(stock);
                }
            });
        }
    }

    // Adding Listeners in the set.
    public static void addOnValueChangeListener(OnValueChanged listener) {
        mOnValueChangedListeners.add(listener);
    }

    public static void removeOnValueChangedListener(OnValueChanged listener) {
        mOnValueChangedListeners.remove(listener);
    }

    public static void addOnSymbolAddedListener(OnSymbolAdded listener) {
        mOnSymbolAddedListeners.add(listener);
    }

    public static void removeOnSymbolAddedListener(OnSymbolAdded listener) {
        mOnSymbolAddedListeners.remove(listener);
    }

    public static void addOnSymbolRemovedListener(OnSymbolRemoved listener) {
        mOnSymbolRemovedListeners.add(listener);
    }

    public static void removeOnSymbolRemovedListener(OnSymbolRemoved listener) {
        mOnSymbolRemovedListeners.remove(listener);
    }

    public static void addToDb(final String symbol, final float threshold) {
        Scheduler.runInBackground(new Runnable() {
            public void run() {
                new Stock(symbol, threshold).save();
            }
        });
    }

    public static void deleteFromDB(final String symbol){
        Scheduler.runInBackground(new Runnable() {
            public void run() {
                List<Stock> result = Stock.find(Stock.class, "M_SYMBOL=?", symbol);
                if (result != null && result.size() > 0){
                    for(Stock s : result){
                        s.delete();
                    }
                }
            }
        });
    }

    public static String[] getCommaSeparatedSymbols() {
        List<Stock> stocks = Stock.listAll(Stock.class);
        String[] symbols = new String[stocks.size()];
        int size = stocks.size();
        for (int i = 0; i < size; i++) {
            symbols[i] = stocks.get(i).getSymbol();
        }
        return symbols;
    }


}
