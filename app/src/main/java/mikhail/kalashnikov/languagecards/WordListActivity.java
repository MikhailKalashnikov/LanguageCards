package mikhail.kalashnikov.languagecards;

import android.app.DialogFragment;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordListActivity extends AppCompatActivity implements LangCardDialog.LangCardDialogListener {

    private SimpleExpandableListAdapter mExpandableListAdapter;
    private DataModel mDataModel;
    private String mOldLesson;
    private static final String ATTR_GROUP_NAME = "groupName";
    private static final String ATTR_ITEM_NAME = "itemName";
    private List<List<Map<String, LanguageCard>>> mChildData;
    private int[] mChildTo;
    private String[] mChildFrom;
    private ActionMode mActiveMode = null;
    private ExpandableListView mListView;
    private int mEditedGroupPosition;
    private int mEditedChildPosition;
    private List<Map<String, String>> mGroupData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_word_list);

        mDataModel = DataModel.getInstance(getApplicationContext());
        mListView = (ExpandableListView) findViewById(R.id.word_list);

        Map<String, List<LanguageCard>> langCardsMap = mDataModel.getLangCardsMap();
        mGroupData = new ArrayList<>();
        for(String lesson: langCardsMap.keySet()){
            Map<String, String> m = new HashMap<>();
            m.put(ATTR_GROUP_NAME, lesson == null? "-": lesson);
            mGroupData.add(m);
        }

        String[] groupFrom = new String[]{ATTR_GROUP_NAME};
        int[] groupTo = new int[]{android.R.id.text1};

        mChildData = new ArrayList<>();
        for(String lesson: langCardsMap.keySet()){
            List<Map<String,LanguageCard>> childDataItem = new ArrayList<>();
            for(LanguageCard i: langCardsMap.get(lesson)){
                Map<String, LanguageCard> m = new HashMap<>();
                m.put(ATTR_ITEM_NAME, i);
                childDataItem.add(m);
            }

            mChildData.add(childDataItem);
        }

        mChildFrom  = new String[]{ATTR_ITEM_NAME};
        mChildTo = new int[]{R.id.row_word1, R.id.row_word2};

        mExpandableListAdapter = new SimpleExpandableListAdapter(
                this,
                mGroupData,
                android.R.layout.simple_expandable_list_item_1,
                groupFrom,
                groupTo,
                mChildData,
                R.layout.list_row,
                mChildFrom,
                mChildTo){
            @Override
            public View getChildView(int groupPosition, int childPosition,
                                     boolean isLastChild, View convertView, ViewGroup parent) {

                View v;
                if (convertView == null) {
                    v = newChildView(isLastChild, parent);
                } else {
                    v = convertView;
                }
                bindView(v, mChildData.get(groupPosition).get(childPosition), mChildFrom, mChildTo);
                return v;
            }

            private void bindView(View view, Map<String, LanguageCard> data, String[] from, int[] to) {
                ViewHolder holder = (ViewHolder) view.getTag();

                if(holder==null){
                    holder = new ViewHolder(view);
                    view.setTag(holder);
                }
                LanguageCard item = data.get(from[0]);
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

            }
        };

        mListView.setAdapter(mExpandableListAdapter);
        mListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                LanguageCard lc = mChildData.get(groupPosition).get(childPosition).get(ATTR_ITEM_NAME);
                mOldLesson = lc.getLesson();
                mEditedGroupPosition = groupPosition;
                mEditedChildPosition = childPosition;

                DialogFragment dialog = LangCardDialog.newInstance(lc.getId(),
                        lc.getWord_lang1(), lc.getWord_lang2(),
                        lc.getLesson(),
                        lc.getLearned());
                dialog.show(getFragmentManager(), "LangCardDialog");
                return false;
            }
        });

        for(int i=0;i<mGroupData.size();i++){
            mListView.expandGroup(i);
        }

        //TODO List with category. save expand option for each group

        mListView.setLongClickable(true);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mListView.clearChoices();
                mListView.setItemChecked(position, true);

                if (mActiveMode == null) {
                    mActiveMode= startSupportActionMode(actionModeCallback);
                }
                return(true);
            }
        });
    }

    @Override
    public void onLangCardEdited(long id, String word1, String word2, String lesson, boolean learned) {
        boolean newLessonAdded = false;
        if (id == LangCardDialog.NEW_CARD_ID) {
            //Should not be called
        } else {
            newLessonAdded = mDataModel.updateLanguageCardAsync(id, word1, word2, learned, lesson, mOldLesson);
        }

        if (!lesson.equals(mOldLesson)) {
            LanguageCard lc = mChildData.get(mEditedGroupPosition).get(mEditedChildPosition).get(ATTR_ITEM_NAME);
            mChildData.get(mEditedGroupPosition).remove(mEditedChildPosition);

            if (newLessonAdded) {
                Map<String, String> m = new HashMap<>();
                m.put(ATTR_GROUP_NAME, lesson == null? "-": lesson);
                mGroupData.add(m);

                List<Map<String,LanguageCard>> childDataItem = new ArrayList<>();
                Map<String, LanguageCard> mc = new HashMap<>();
                mc.put(ATTR_ITEM_NAME, lc);
                childDataItem.add(mc);
                mChildData.add(childDataItem);
            } else {
                for (int i = 0; i < mGroupData.size(); i++) {
                    if (mGroupData.get(i).get(ATTR_GROUP_NAME).equals(lesson)) {
                        Map<String, LanguageCard> mc = new HashMap<>();
                        mc.put(ATTR_ITEM_NAME, lc);
                        mChildData.get(i).add(mc);
                        break;
                    }
                }
            }

        }
        mExpandableListAdapter.notifyDataSetChanged();


    }

    class ViewHolder{
        TextView word1=null;
        TextView word2=null;
        ViewHolder(View row){
            word1=(TextView) row.findViewById(R.id.row_word1);
            word2=(TextView) row.findViewById(R.id.row_word2);
        }
    }

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.word_list_action_mode, menu);

            int pos = mListView.getCheckedItemPosition();
            long elpos = mListView.getExpandableListPosition(pos);
            Log.d("MK", "typ" + ExpandableListView.getPackedPositionType(elpos));
            return ExpandableListView.getPackedPositionType(elpos) == ExpandableListView.PACKED_POSITION_TYPE_CHILD;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_del:
                    int pos = mListView.getCheckedItemPosition();
                    long elpos = mListView.getExpandableListPosition(pos);
                    int group_pos = ExpandableListView.getPackedPositionGroup(elpos);
                    int child_pos = ExpandableListView.getPackedPositionChild(elpos);

                    if(ExpandableListView.getPackedPositionType(elpos) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                        LanguageCard lc = mChildData.get(group_pos).get(child_pos).get(ATTR_ITEM_NAME);
                        mDataModel.deleteLanguageCardAsync(lc.getId(), lc.getLesson());
                        mChildData.get(group_pos).remove(child_pos);
                        mExpandableListAdapter.notifyDataSetChanged();
                    }

                    mode.finish();
                    return true;

                default:
                    return false;
            }

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActiveMode = null;
            mListView.clearChoices();
            mListView.requestLayout();

        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

    };


}
