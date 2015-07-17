package com.stocker.android;


import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stocker.stockerexercise.R;


public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new StockListFragment())
                    .commit();
        }

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class StockListFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

        StockPriceAdapter mStockPriceAdapter;


        public StockListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.main_fragment, container, false);
            rootView.findViewById(R.id.add_button).setOnClickListener(this);
            rootView.findViewById(R.id.refresh).setOnClickListener(this);

            ((TextView) rootView.findViewById(R.id.refresh)).setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fontawesome.ttf"));

            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
            RecyclerView.LayoutManager lm;
            if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                lm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            } else {
                lm = new LinearLayoutManager(getActivity());
            }
            recyclerView.setLayoutManager(lm);

            mStockPriceAdapter = new StockPriceAdapter(getActivity(), rootView.findViewById(R.id.empty_view), lm instanceof StaggeredGridLayoutManager);
            recyclerView.setAdapter(mStockPriceAdapter);
            mStockPriceAdapter.setOnLongClickListener(this);


            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();
            mStockPriceAdapter.setup();
        }

        @Override
        public void onStop() {
            super.onStop();
            mStockPriceAdapter.teardown();
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.add_button:
                    if (getFragmentManager().findFragmentByTag("dialog") == null) {
                        AddEditDialog.newInstance().show(getFragmentManager(), "dialog");
                    }
                    break;
                case R.id.refresh:
                    NetworkController nc = new NetworkController(getActivity());
                    nc.sync();
           }
        }


        @Override
        public boolean onLongClick(View v) {
            if (getFragmentManager().findFragmentByTag("dialog") == null) {
                Bundle b = new Bundle();
                b.putLong(AddEditDialog.EXTRA_STOCK_ID, (Long) v.getTag());
                AddEditDialog dialog = AddEditDialog.newInstance();
                dialog.setArguments(b);
                dialog.show(getFragmentManager(), "dialog");
            }
            return false;
        }
    }


}
