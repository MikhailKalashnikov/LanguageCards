package mikhail.kalashnikov.languagecards;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import java.util.Collections;
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
    private String[] mChildFrom;
    private ActionMode mActiveMode = null;
    private ExpandableListView mListView;
    private int mEditedGroupPosition;
    private int mEditedChildPosition;
    private List<Map<String, String>> mGroupData;
    private SharedPreferences mPrefs;
    private int mSortMode = 0;
    private static final int SORT_MODE_ID = 0;
    private static final int SORT_MODE_WORD1 = 1;
    private static final int SORT_MODE_WORD2 = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String expandGroupSettings = mPrefs.getString(SettingsActivity.KEY_PREF_EXPAND_GROUP, "0");
        mSortMode = mPrefs.getInt(SettingsActivity.KEY_PREF_SORT_MODE, 0);

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
        int[] childTo = new int[]{R.id.row_word1, R.id.row_word2};

        mExpandableListAdapter = new SimpleExpandableListAdapter(
                this,
                mGroupData,
                android.R.layout.simple_expandable_list_item_1,
                groupFrom,
                groupTo,
                mChildData,
                R.layout.list_row,
                mChildFrom,
                childTo){
            @Override
            public View getChildView(int groupPosition, int childPosition,
                                     boolean isLastChild, View convertView, ViewGroup parent) {

                View v;
                if (convertView == null) {
                    v = newChildView(isLastChild, parent);
                } else {
                    v = convertView;
                }
                bindView(v, mChildData.get(groupPosition).get(childPosition), mChildFrom);
                return v;
            }

            private void bindView(View view, Map<String, LanguageCard> data, String[] from) {
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

        for(int i = 0; i < mGroupData.size(); i++){
            if (i < expandGroupSettings.length()
                    && expandGroupSettings.charAt(i) == '1') {
                mListView.expandGroup(i);
            }
        }

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

        if (mSortMode != SORT_MODE_ID) {
            sortList();
        }
    }

    private void sortList() {
        Map<String, List<LanguageCard>> langCardsMap = mDataModel.getLangCardsMap();
        mChildData = new ArrayList<>();
        for(String lesson: langCardsMap.keySet()){
            List<Map<String,LanguageCard>> childDataItem = new ArrayList<>();
            List<LanguageCard> lcList = langCardsMap.get(lesson);

            if(mSortMode == SORT_MODE_WORD1) {
                Collections.sort(lcList, LanguageCard.getWord1Comparator());
            } else if(mSortMode == SORT_MODE_WORD2) {
                Collections.sort(lcList, LanguageCard.getWord2Comparator());
            }

            for(LanguageCard i: lcList){
                Map<String, LanguageCard> m = new HashMap<>();
                m.put(ATTR_ITEM_NAME, i);
                childDataItem.add(m);
            }

            mChildData.add(childDataItem);
        }

        mExpandableListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLangCardEdited(long id, String word1, String word2, String lesson, boolean learned) {
        boolean newLessonAdded;
        if (id == LangCardDialog.NEW_CARD_ID) {
            newLessonAdded = mDataModel.insertLanguageCardAsync(word1, word2, lesson);
        } else {
            newLessonAdded = mDataModel.updateLanguageCardAsync(id, word1, word2, learned, lesson, mOldLesson);
        }

        //TODO update list when add new
        if (!lesson.equals(mOldLesson)) {
            LanguageCard lc = mChildData.get(mEditedGroupPosition).get(mEditedChildPosition).get(ATTR_ITEM_NAME);
            mChildData.get(mEditedGroupPosition).remove(mEditedChildPosition);

            if (newLessonAdded) {
                Map<String, String> m = new HashMap<>();
                m.put(ATTR_GROUP_NAME, lesson);
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


    @Override
    protected void onPause() {

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mPrefs.edit();
        StringBuilder groupState = new StringBuilder(mGroupData.size()) ;
        for (int i = 0; i < mGroupData.size(); i++){
            groupState.append(mListView.isGroupExpanded(i)?'1':'0');
        }
        editor.putString(SettingsActivity.KEY_PREF_EXPAND_GROUP, groupState.toString());
        editor.putInt(SettingsActivity.KEY_PREF_SORT_MODE, mSortMode);
        editor.apply();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_word_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add) {
            DialogFragment dialog = LangCardDialog.newInstance(LangCardDialog.NEW_CARD_ID, null, null, null, false);
            dialog.show(getFragmentManager(), "LangCardDialog");

        } else if (id == R.id.action_sort) {
            mSortMode = (++mSortMode) % 3;
            sortList();
        }

        return super.onOptionsItemSelected(item);
    }
}
