package com.stocker.android;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.stocker.android.model.Stock;
import com.stocker.stockerexercise.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Created by pritijain on 01/05/15.
 */
public class StockPriceAdapter extends RecyclerView.Adapter implements Stock.OnValueChanged, Stock.OnSymbolAdded, Stock.OnSymbolRemoved {
    private static final int VIEW_ITEM_ENTRY = 0;
    private static final int VIEW_ITEM_HEADER = 1;
    private static final String SP_LAST_UPDATE_TIME = "last_sync_time";
    private final boolean mInLandscape;

    Long stockId;

    long mLastUpdateTime = 0;

    private Context mContext;

    private List<Stock> mStocks;
    private Map<Long, Integer> mStockPositionMap;

    private View.OnLongClickListener mOnLongClickListener;


    private View mEmptyView; // When there is no stock to display
    private Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {
            notifyItemChanged(0);
            startTimer();
        }
    };
    // 1. first time when user opens the app
    // 2. user deleted all the added symbols

    public StockPriceAdapter(Context context, View emptyView, boolean inLandscape) {
        this.mContext = context;
        mInLandscape = inLandscape;
        mEmptyView = emptyView;
        mLastUpdateTime = mContext.getSharedPreferences("stocker", Context.MODE_PRIVATE).getLong(SP_LAST_UPDATE_TIME, -1);
        loadStocksFromDb();
    }

    public void teardown() {
        Stock.removeOnSymbolAddedListener(this);
        Stock.removeOnSymbolRemovedListener(this);
        Stock.removeOnValueChangedListener(this);
        mEmptyView.removeCallbacks(mTimerRunnable);
    }

    public void setup() {
        Stock.addOnSymbolAddedListener(this);
        Stock.addOnSymbolRemovedListener(this);
        Stock.addOnValueChangeListener(this);
        startTimer();

    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        mOnLongClickListener = onLongClickListener;
    }

    private void startTimer() {
        mEmptyView.postDelayed(mTimerRunnable, 10000);
    }


    @Override
    public int getItemCount() {
        if (mStocks.size() == 0) return 0;
        return 1 + mStocks.size();  // added 1 : 0th item is : last updated 2 minutes ago
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_ITEM_HEADER;
        }
        return VIEW_ITEM_ENTRY;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (VIEW_ITEM_HEADER == viewType) {
            return new HeaderViewHolder(LayoutInflater.from(mContext).inflate(R.layout.header_item, parent, false), mInLandscape);
        }
        final View itemView = LayoutInflater.from(mContext).inflate(R.layout.entry_item, parent, false);

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnLongClickListener != null) {

                    return mOnLongClickListener.onLongClick(v);
                }
                return false;
            }
        });

        ImageButton deleteButton = (ImageButton) itemView.findViewById(R.id.delete_button);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                stockId = (Long) itemView.getTag();
                Stock stock = Stock.findById(Stock.class, stockId);
                String symbol = stock.getSymbol();
                Stock.deleteFromDB(symbol);
            }
        });
        return new EntryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object data;
        if (position == 0) {

            if (mLastUpdateTime == -1) {
                data = mContext.getString(R.string.never_refreshed);
            } else {
                data = mContext.getString(R.string.last_updated, TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - mLastUpdateTime), "minutes ago");
            }
        } else {
            data = mStocks.get(position - 1);
        }

        ((AbstractViewHolder) holder).bind(data);

    }

    private void loadStocksFromDb() {
        mStocks = Stock.listAll(Stock.class);
        mStockPositionMap = new HashMap();
        int position = 0;
        for (Stock stock : mStocks) {
            mStockPositionMap.put(stock.getId(), position++);
        }

        mEmptyView.setVisibility(mStocks.size() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onNewSymbolAdded(Stock stock) {
        mStocks.add(stock);
        int newPosition = mStocks.size() - 1;
        mStockPositionMap.put(stock.getId(), newPosition);
        notifyItemInserted(newPosition + 1);
        mEmptyView.setVisibility(View.GONE);
    }

    @Override
    public void onValueChanged(Stock stock) {
        mStocks.set(mStockPositionMap.get(stock.getId()), stock);
        if (stock.getPrice() > 0) {
            setLastUpdateTime(System.currentTimeMillis());
        }
        notifyItemChanged(mStockPositionMap.get(stock.getId()) + 1);

    }

    @Override
    public void onSymbolRemoved(Stock stock) {
        int position = mStockPositionMap.get(stock.getId());
        mStocks.remove(position);
        mStockPositionMap.remove(stock.getId());


        int newPosition = 0;
        for (Stock s : mStocks) {
            mStockPositionMap.put(s.getId(), newPosition++);
        }

        mEmptyView.setVisibility(mStocks.size() == 0 ? View.VISIBLE : View.GONE);

        if (mStocks.size() > 0) {
            notifyItemRemoved(position + 1);
        } else {
            notifyDataSetChanged();
        }
    }

    private void setLastUpdateTime(long time) {
        mLastUpdateTime = time;
        mContext.getSharedPreferences("stocker", Context.MODE_PRIVATE).edit().putLong(SP_LAST_UPDATE_TIME, mLastUpdateTime).commit();
        notifyItemChanged(0);
    }

    abstract static class AbstractViewHolder<T> extends RecyclerView.ViewHolder {
        public AbstractViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bind(T dataItem);
    }

    static class HeaderViewHolder extends AbstractViewHolder<String> {
        TextView mHeader;
        boolean mInLandscape;

        public HeaderViewHolder(View itemView, boolean inLandscape) {
            super(itemView);
            mInLandscape = inLandscape;
            mHeader = (TextView) itemView.findViewById(R.id.last_update_header);


        }

        @Override
        public void bind(String dataItem) {
            mHeader.setText(dataItem);
            if (mInLandscape) {
                StaggeredGridLayoutManager.LayoutParams spl = (StaggeredGridLayoutManager.LayoutParams) itemView.getLayoutParams();
                spl.setFullSpan(true);
            }
        }
    }

    static class EntryViewHolder extends AbstractViewHolder<Stock> {

        TextView stockNameView;
        TextView stockValueView;

        public EntryViewHolder(View itemView) {
            super(itemView);
            stockNameView = (TextView) itemView.findViewById(R.id.stock_name);
            stockValueView = (TextView) itemView.findViewById(R.id.stock_value);
        }

        @Override
        public void bind(Stock dataItem) {
            itemView.setTag(dataItem.getId());
           String name = dataItem.getName();
            String price = dataItem.getPrice()== 0?"-":"" + dataItem.getPrice();
            if(name == null)
                name = " ";
            stockNameView.setText(name + " " + "[" + dataItem.getSymbol().toUpperCase() + "]");
            SpannableStringBuilder cs = new SpannableStringBuilder("$" + price);
            cs.setSpan(new ForegroundColorSpan(stockNameView.getContext().getResources().getColor(R.color.text_blue)), 0, cs.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            stockValueView.setText(cs);
        }
    }
}
