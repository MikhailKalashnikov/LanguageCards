package mikhail.kalashnikov.languagecards;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DataModel implements LangCardsDBHelper.LangCardsDBHelperListener, SharedPreferences.OnSharedPreferenceChangeListener {

    interface ModelCallbacks {
        void onDataUploaded();
    }
    private static final String TAG = "MK";
    private static final String ALL_LESSON = "#ALL#";
    private static DataModel singleton =null;
    private LangCardsDBHelper mDBHelper;
    private boolean mIsDataUploaded = false;
    private Map<String, List<LanguageCard>> mLangCardsMap;
    private List<LanguageCard> mLangCardsLst;
    private boolean mIsRandom = false;
    private int mCurrentIdx;
    private boolean mDirection12;
    private Random mRandom;
    private ModelCallbacks mListener;

    public synchronized static DataModel getInstance(Context context){
        Log.d(TAG, "getInstance");
        if(singleton==null){
            singleton = new DataModel(context);
        }
        return singleton;

    }

    private DataModel(Context context){
        mDBHelper = LangCardsDBHelper.getInstance(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(this);
        mIsRandom = prefs.getBoolean(SettingsActivity.KEY_PREF_IS_RANDOM, true);
        mRandom = new Random(System.currentTimeMillis());
        mCurrentIdx = prefs.getInt(SettingsActivity.KEY_PREF_LAST_POS, 0);
        mDirection12 = prefs.getBoolean(SettingsActivity.KEY_PREF_DIRECTION, true);

    }

    public boolean isDataUploaded(){
        return mIsDataUploaded;
    }

    public void uploadData(ModelCallbacks listener){
        Log.d(TAG, "uploadData = " + mIsDataUploaded);
        mListener = listener;
        if (!mIsDataUploaded){
            mDBHelper.getDataAsync(this);
        }
    }

    String getNextWord(String lesson) {
//        Log.d(TAG,"getNextWord mIsRandom=" + mIsRandom);
        //getNextPos
        if (mLangCardsLst.size() == 0){
            return null;
        }

        if (mIsRandom) {
            mCurrentIdx = mRandom.nextInt(mLangCardsList.size());
        } else {
            mCurrentIdx = (++mCurrentIdx) % mLangCardsList.size();
        }

        if (mLangCardsList.get(mCurrentIdx).getLearned()) { // skipped learned
            int startIdx = mCurrentIdx;
            do {
                mCurrentIdx = (++mCurrentIdx) % mLangCardsList.size();
            } while (mLangCardsList.get(mCurrentIdx).getLearned() && startIdx != mCurrentIdx);
        }

        if (mDirection12) {
            return mLangCardsList.get(mCurrentIdx).getWord_lang1();
        } else {
            return mLangCardsList.get(mCurrentIdx).getWord_lang2();
        }
    }

    String getCurrentWord() {
        if (mLangCardsList.size() == 0){
            return null;
        }
        if (mDirection12) {
            return mLangCardsList.get(mCurrentIdx).getWord_lang1();
        } else {
            return mLangCardsList.get(mCurrentIdx).getWord_lang2();
        }
    }


    String getCurrentAnswer() {
        if (mDirection12) {
            return mLangCardsList.get(mCurrentIdx).getWord_lang2();
        } else {
            return mLangCardsList.get(mCurrentIdx).getWord_lang1();
        }
    }

    void switchDirection() {
        mDirection12 = !mDirection12;
    }

    public int getCurrentPosition() {
        return mCurrentIdx;
    }

    public boolean getDirection() {
        return mDirection12;
    }

    public void insertLanguageCardAsync(String word_lang1, String word_lang2, String lesson) {
        Log.d(TAG, "insertLanguageCardAsync: word_lang1="+word_lang1 + ", word_lang2="+word_lang2
                + " lesson =" + lesson);
        long groupId = 1;
        // check if already exist
        if (mLangCardsMap.containsKey(lesson)) {
            for (LanguageCard lc : mLangCardsMap.get(lesson)) {
                if (lc.getWord_lang1().equalsIgnoreCase(word_lang1) && lc.getWord_lang2().equalsIgnoreCase(word_lang2)) {
                    return;
                }
            }
        }

        LanguageCard lc = new LanguageCard(word_lang1, word_lang2, groupId, 0, lesson);
        if (!mLangCardsMap.containsKey(lesson)) {
            mLangCardsMap.put(lesson, new ArrayList<LanguageCard>());
        }
        mLangCardsMap.get(lesson).add(lc);
        mLangCardsLst.add(lc);
        ModelFragment.executeAsyncTask(new InsertLanguageCardTask(lc));
    }

    public void updateLanguageCardAsync(long id, String word_lang1, String word_lang2, boolean learned,
                                        String lesson, String oldlesson) {
        Log.d(TAG, "UpdateLanguageCardTask: id= " + id + ", word_lang1=" + word_lang1 + ", word_lang2=" + word_lang2);
        if (mLangCardsMap.containsKey(lesson)) {
            for (LanguageCard lc : mLangCardsMap.get(lesson)) {
                if (lc.getId() == id) {
                    lc.setWord_lang1(word_lang1);
                    lc.setWord_lang2(word_lang2);
                    lc.setLearned(learned ? 1 : 0);
                    lc.setLesson(lesson);
                    ModelFragment.executeAsyncTask(new UpdateLanguageCardTask(lc));
                    break;
                }
            }
        } else {
            for (LanguageCard lc : mLangCardsMap.get(oldlesson)) {
                if (lc.getId() == id) {
                    mLangCardsMap.get(oldlesson).remove(lc);
                    if (mLangCardsMap.get(oldlesson).size() == 0) {
                        mLangCardsMap.remove(oldlesson);
                    }
                    mLangCardsMap.put(lesson, new ArrayList<LanguageCard>());
                    lc.setWord_lang1(word_lang1);
                    lc.setWord_lang2(word_lang2);
                    lc.setLearned(learned ? 1 : 0);
                    lc.setLesson(lesson);
                    mLangCardsMap.get(lesson).add(lc);
                    ModelFragment.executeAsyncTask(new UpdateLanguageCardTask(lc));
                    break;
                }
            }
        }
    }

    public void deleteLanguageCardAsync(long id, String lesson) {
        Log.d(TAG, "deleteLanguageCardAsync: id= " + id);
        for (LanguageCard lc : mLangCardsMap.get(lesson)) {
            if (lc.getId() == id) {
                mLangCardsMap.get(lesson).remove(lc);
                if (mLangCardsMap.get(lesson).size() == 0) {
                    mLangCardsMap.remove(lesson);
                }
                mLangCardsLst.remove(lc);
                ModelFragment.executeAsyncTask(new DeleteLanguageCardTask(id));
                break;
            }
        }

    }

    @Override
    public void onDataUploaded(List<LanguageCard> languageCards) {
        // Log.d(TAG, "onDataUploaded + " + languageCards.toString());
        mIsDataUploaded = true;
        mLangCardsMap = new HashMap<>();
        for (LanguageCard lc : languageCards) {
            if (!mLangCardsMap.containsKey(lc.getLesson())) {
                mLangCardsMap.put(lc.getLesson(), new ArrayList<LanguageCard>());
            }
            mLangCardsMap.get(lc.getLesson()).add(lc);
        }
        mLangCardsLst = languageCards;

        mListener.onDataUploaded();
    }

    public List<LanguageCard> getLangCardsList() {
        return mLangCardsLst;
    }


    private class InsertLanguageCardTask extends AsyncTask<Void, Void, Void> {
        private LanguageCard languageCard;
        private long newRowId;

        InsertLanguageCardTask(LanguageCard languageCard){
            this.languageCard = languageCard;
        }

        @Override
        protected Void doInBackground(Void... params) {
            newRowId = mDBHelper.insertLanguageCard(languageCard);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            languageCard.setId(newRowId);
        }

    }

    private class UpdateLanguageCardTask extends AsyncTask<Void, Void, Void>{
        private LanguageCard languageCard;

        UpdateLanguageCardTask(LanguageCard languageCard){
            this.languageCard = languageCard;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mDBHelper.updateLanguageCard(languageCard);
            return null;
        }

    }

    private class DeleteLanguageCardTask extends AsyncTask<Void, Void, Void>{
        private long id;

        DeleteLanguageCardTask(long id){
            this.id = id;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mDBHelper.deleteLanguageCard(id);
            return null;
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        Log.d(TAG, "onSharedPreferenceChanged key=" + key);
        if (key.equals(SettingsActivity.KEY_PREF_IS_RANDOM)) {
            mIsRandom = sharedPreferences.getBoolean(SettingsActivity.KEY_PREF_IS_RANDOM, true);
        }
    }

}
