package mikhail.kalashnikov.languagecards;

import android.app.DialogFragment;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class WordListActivity extends AppCompatActivity implements LangCardDialog.LangCardDialogListener {

    private LanguageCardListAdapter mAdapter;
    private DataModel mDataModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_word_list);
        View coordinatorLayout = findViewById(R.id.coordinatorLayout);

        mDataModel = DataModel.getInstance(getApplicationContext());
        List<LanguageCard> languageCards = mDataModel.getLangCardsList();
        mAdapter = new LanguageCardListAdapter(languageCards);
        final ListView listView = (ListView) findViewById(R.id.word_list);
        if (languageCards==null) Log.d("MK", "languageCards null");

        String strDeleted = getString(R.string.msg_deleted);
        String strUndo = getString(R.string.msg_btn_undo);

        listView.setAdapter(mAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LanguageCard lc = mAdapter.getItem(position);
                DialogFragment dialog = LangCardDialog.newInstance(lc.getId(), lc.getWord_lang1(), lc.getWord_lang2(), lc.getLearned());
                dialog.show(getFragmentManager(), "LangCardDialog");
            }
        });

        final Snackbar mMessageBar = Snackbar.make(coordinatorLayout, strDeleted, Snackbar.LENGTH_SHORT)
                .setAction(strUndo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAdapter.undoRemove();
                        //mMessageBar.dismiss();
                    }
                });

        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        listView,
                        new SwipeDismissListViewTouchListener.OnDismissCallback() {
                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    mAdapter.remove(mAdapter.getItem(position));
                                }
                                mMessageBar.show();
                            }
                        });
        listView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        listView.setOnScrollListener(touchListener.makeScrollListener());
    }

    @Override
    public void onLangCardEdited(long id, String word1, String word2, boolean learned) {
        if (id == LangCardDialog.NEW_CARD_ID) {
            //TODO
        } else {
            mDataModel.updateLanguageCardAsync(id, word1, word2, learned);
        }
        mAdapter.notifyDataSetChanged();
    }


    class LanguageCardListAdapter extends ArrayAdapter<LanguageCard> {
        private LanguageCard removedItem = null;

        LanguageCardListAdapter(List<LanguageCard> cards) {
            super(WordListActivity.this, R.layout.list_row, R.id.row_word1, cards);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = super.getView(position, convertView, parent);
            ViewHolder holder = (ViewHolder) row.getTag();

            if(holder==null){
                holder = new ViewHolder(row);
                row.setTag(holder);
            }
            LanguageCard item = getItem(position);
            holder.word1.setText(item.getWord_lang1());
            holder.word2.setText(item.getWord_lang2());

            if(item.getLearned()){
                holder.word1.setPaintFlags(holder.word1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.word1.setBackgroundResource(R.color.doneTextViewColor);
                holder.word1.setTypeface(null, Typeface.ITALIC);
                holder.word2.setPaintFlags(holder.word2.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.word2.setBackgroundResource(R.color.doneTextViewColor);
                holder.word2.setTypeface(null, Typeface.ITALIC);
            }else{
                holder.word1.setPaintFlags(holder.word1.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                holder.word1.setTypeface(null, Typeface.NORMAL);
                holder.word1.setBackgroundResource(android.R.color.transparent);
                holder.word2.setPaintFlags(holder.word2.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                holder.word2.setTypeface(null, Typeface.BOLD);
                holder.word2.setBackgroundResource(android.R.color.transparent);
            }


            return row;
        }

        @Override
        public void remove(LanguageCard langCard) {
            removedItem = langCard;
            mDataModel.deleteLanguageCardAsync(langCard.getId());
            notifyDataSetChanged();
        }

        public void undoRemove() {
            if (removedItem != null){
                mDataModel.insertLanguageCardAsync(removedItem.getWord_lang1(), removedItem.getWord_lang2());
                notifyDataSetChanged();
            }
        }
    }


    class ViewHolder{
        TextView word1=null;
        TextView word2=null;
        ViewHolder(View row){
            word1=(TextView) row.findViewById(R.id.row_word1);
            word2=(TextView) row.findViewById(R.id.row_word2);
        }
    }
}
