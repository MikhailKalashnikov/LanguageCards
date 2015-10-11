package mikhail.kalashnikov.languagecards;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class LangCardDialog extends DialogFragment {
    public static final int NEW_CARD_ID = -1;
    private LangCardDialogListener mListener;

    public interface LangCardDialogListener {
        void onLangCardEdited(long id, String word1, String word2, boolean learned);
    }

    static LangCardDialog newInstance (long id, String word1, String word2, boolean learned) {
        Log.d("MK", "LangCardDialog newInstance id="+id+ ", word1="+word1+ ", word2="+word2);
        LangCardDialog d = new LangCardDialog();
        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putString("word1", word1);
        args.putString("word2", word2);
        args.putBoolean("learned", learned);
        d.setArguments(args);
        return d;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.lang_card_dialog, null);
        final TextView tvWord1 = (TextView) view.findViewById(R.id.word_to_add1);
        final TextView tvWord2 = (TextView) view.findViewById(R.id.word_to_add2);
        final CheckBox cbLearned = (CheckBox) view.findViewById(R.id.cb_learned);
        final long id = getArguments().getLong("id");
        boolean newCardMode = (id == NEW_CARD_ID);
        if(!newCardMode){
            tvWord1.setText(getArguments().getString("word1"));
            tvWord2.setText(getArguments().getString("word2"));
            cbLearned.setChecked(getArguments().getBoolean("learned"));
        } else {
            cbLearned.setVisibility(View.GONE);
            cbLearned.setChecked(false);
        }

        builder
                .setTitle(newCardMode?R.string.action_add:R.string.action_edit)
                .setInverseBackgroundForced(true)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onLangCardEdited(id, tvWord1.getText().toString(), tvWord2.getText().toString(), cbLearned.isChecked());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);

        return builder.create();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (LangCardDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement LangCardDialogListener");
        }
    }
}
