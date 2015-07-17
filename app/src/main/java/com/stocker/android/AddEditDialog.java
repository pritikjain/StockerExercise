package com.stocker.android;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.stocker.android.model.Stock;
import com.stocker.stockerexercise.R;

import static com.stocker.stockerexercise.R.id.stock_name;


/**
 * Created by pritijain on 03/05/15.
 */
public class AddEditDialog extends DialogFragment {

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static AddEditDialog newInstance() {
        return new AddEditDialog();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.add_dialog_fragment, null);
        final EditText symbolEdit = (EditText) contentView.findViewById(stock_name);
        final long stockId = getArguments() != null? getArguments().getLong(EXTRA_STOCK_ID, -1): -1;
        final boolean inEditMode = stockId != -1;
        final NumberPicker numberPicker = (NumberPicker) contentView.findViewById(R.id.stock_tracking_threshold);

        if (inEditMode){
            Stock stock = Stock.findById(Stock.class, stockId);
            symbolEdit.setText(stock.getSymbol());
            symbolEdit.setEnabled(false);
            ((TextView)contentView.findViewById(R.id.add_stock_title)).setText(getActivity().getString(R.string.edit_stock));
            numberPicker.setValue(stock.getThreshold());

        }

        return new AlertDialog.Builder(getActivity()).setView(contentView)
                .setPositiveButton(inEditMode?R.string.update :R.string.add,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                float threshold = numberPicker.getValue();
                                if (!inEditMode) {  // this is add case
                                    String symbolName = symbolEdit.getText().toString().trim();
                                    if (TextUtils.isEmpty(symbolName)) {
                                        Toast.makeText(getActivity(), R.string.no_symbol_was_added, Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    Stock.addToDb(symbolName, threshold);
                                } else {  // this is update case
                                    Stock stock = Stock.findById(Stock.class, stockId);
                                    stock.setThreshold(threshold);
                                    stock.save();
                                }
                            }
                        }
                )
                .setNegativeButton(R.string.cancel_buton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismiss();
                            }
                        }
                )
                .create();
    }


    public static final String EXTRA_STOCK_ID  = "stock_id";
}
