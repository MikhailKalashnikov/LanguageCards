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
import java.util.Set;

public class DataModel implements LangCardsDBHelper.LangCardsDBHelperListener, SharedPreferences.OnSharedPreferenceChangeListener {

    interface ModelCallbacks {
        void onDataUploaded();
    }
    private static final String TAG = "MK";
    public static final String ALL_LESSON = "ALL";
    private static DataModel singleton =null;
    private LangCardsDBHelper mDBHelper;
    private boolean mIsDataUploaded = false;
    private Map<String, List<LanguageCard>> mLangCardsMap;
    private List<LanguageCard> mLangCardsLst;
    private boolean mIsRandom = false;
    private int mCurrentIdx;
    private LanguageCard mCurrentLangCard = null;
    private boolean mDirection12;
    private Random mRandom;
    private boolean mShowLearned;
    private ModelCallbacks mListener;
    private String mCurrentLesson  = ALL_LESSON;

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
        mShowLearned = prefs.getBoolean(SettingsActivity.KEY_PREF_SHOW_LEARNED, false);
        mRandom = new Random(System.currentTimeMillis());
        mCurrentIdx = prefs.getInt(SettingsActivity.KEY_PREF_LAST_POS, 0);
        mDirection12 = prefs.getBoolean(SettingsActivity.KEY_PREF_DIRECTION, true);
        mCurrentLesson = prefs.getString(SettingsActivity.KEY_PREF_LESSON, ALL_LESSON);

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

    int getTotalCount () {
        return  mLangCardsLst.size();
    }
    String getNextWord(String lesson) {
        //Log.d(TAG,"getNextWord lesson=" + lesson);
        //getNextPos
        mCurrentLesson = lesson;
        if (mLangCardsLst.size() == 0){
            return null;
        }

        if (lesson.equals(ALL_LESSON)) {
            if (mIsRandom) {
                mCurrentIdx = mRandom.nextInt(mLangCardsLst.size());
            } else {
                mCurrentIdx = (++mCurrentIdx) % mLangCardsLst.size();
            }

            if (!mShowLearned && mLangCardsLst.get(mCurrentIdx).getLearned()) { // skipped learned
                int startIdx = mCurrentIdx;
                do {
                    mCurrentIdx = (++mCurrentIdx) % mLangCardsLst.size();
                } while (mLangCardsLst.get(mCurrentIdx).getLearned() && startIdx != mCurrentIdx);
            }
            mCurrentLangCard = mLangCardsLst.get(mCurrentIdx);
            if (mDirection12) {
                return mLangCardsLst.get(mCurrentIdx).getWord_lang1();
            } else {
                return mLangCardsLst.get(mCurrentIdx).getWord_lang2();
            }
        } else {
            if (!mLangCardsMap.containsKey(lesson) || mLangCardsMap.get(lesson).size() == 0) {
                mCurrentLangCard = null;
                return null;
            }

            List<LanguageCard> LessonsList = mLangCardsMap.get(lesson);

            if (mIsRandom) {
                mCurrentIdx = mRandom.nextInt(LessonsList.size());
            } else {
                mCurrentIdx = (++mCurrentIdx) % LessonsList.size();
            }

            if (!mShowLearned && LessonsList.get(mCurrentIdx).getLearned()) { // skipped learned
                int startIdx = mCurrentIdx;
                do {
                    mCurrentIdx = (++mCurrentIdx) % LessonsList.size();
                } while (LessonsList.get(mCurrentIdx).getLearned() && startIdx != mCurrentIdx);
            }
            mCurrentLangCard = LessonsList.get(mCurrentIdx);
            if (mDirection12) {
                return LessonsList.get(mCurrentIdx).getWord_lang1();
            } else {
                return LessonsList.get(mCurrentIdx).getWord_lang2();
            }

        }
    }


    String getCurrentWordLesson() {
        if (mLangCardsLst.size() == 0){
            return ALL_LESSON;
        }
        if (mCurrentLesson.equals(ALL_LESSON)) {
            return mLangCardsLst.get(mCurrentIdx).getLesson();
        } else {
            return mCurrentLesson;
        }

    }

    String getCurrentWord() {
        if (mLangCardsLst.size() == 0){
            mCurrentLangCard = null;
            return null;
        }
        if (mCurrentLesson.equals(ALL_LESSON)) {
            if (mCurrentIdx >= mLangCardsLst.size()) {
                mCurrentIdx = 0;
            }
            mCurrentLangCard = mLangCardsLst.get(mCurrentIdx);
            if (mDirection12) {
                return mLangCardsLst.get(mCurrentIdx).getWord_lang1();
            } else {
                return mLangCardsLst.get(mCurrentIdx).getWord_lang2();
            }
        } else {
            if (mCurrentIdx >= mLangCardsMap.get(mCurrentLesson).size()) {
                mCurrentIdx = 0;
            }
            mCurrentLangCard = mLangCardsMap.get(mCurrentLesson).get(mCurrentIdx);
            if (mDirection12) {
                return mLangCardsMap.get(mCurrentLesson).get(mCurrentIdx).getWord_lang1();
            } else {
                return mLangCardsMap.get(mCurrentLesson).get(mCurrentIdx).getWord_lang2();
            }
        }
    }

    public void setLearned(boolean learned) {
        mCurrentLangCard.setLearned(learned? 1: 0);
        updateLanguageCardAsync(mCurrentLangCard.getId(),
                mCurrentLangCard.getWord_lang1(),
                mCurrentLangCard.getWord_lang2(),
                mCurrentLangCard.getLearned(),
                mCurrentLangCard.getLesson(),
                mCurrentLangCard.getLesson());
    }

    String getCurrentAnswer() {
        if (mCurrentLesson.equals(ALL_LESSON)) {
            if (mDirection12) {
                return mLangCardsLst.get(mCurrentIdx).getWord_lang2();
            } else {
                return mLangCardsLst.get(mCurrentIdx).getWord_lang1();
            }
        } else {
            if (mDirection12) {
                return mLangCardsMap.get(mCurrentLesson).get(mCurrentIdx).getWord_lang2();
            } else {
                return mLangCardsMap.get(mCurrentLesson).get(mCurrentIdx).getWord_lang1();
            }
        }
    }

    boolean getCurrentIsLearned() {
        if (mLangCardsLst.size() == 0){
            return false;
        }
        if (mCurrentLesson.equals(ALL_LESSON)) {
            return mLangCardsLst.get(mCurrentIdx).getLearned();
        } else {
            return mLangCardsMap.get(mCurrentLesson).get(mCurrentIdx).getLearned();
        }
    }

    void switchDirection() {
        mDirection12 = !mDirection12;
    }

    public void setLesson(String lesson) {
        mCurrentLesson = lesson;
    }

    public int getCurrentPosition() {
        return mCurrentIdx;
    }

    public boolean getDirection() {
        return mDirection12;
    }

    /**
     *
     * @param word_lang1
     * @param word_lang2
     * @param lesson
     * @return true - new lesson added.
     */
    public boolean insertLanguageCard(String word_lang1, String word_lang2, String lesson, boolean isAsync) {
        Log.d(TAG, "insertLanguageCardAsync: word_lang1="+word_lang1 + ", word_lang2="+word_lang2
                + " lesson =" + lesson);
        long groupId = 1;
        boolean newLessonAdded = false;
        // check if already exist
        if (mLangCardsMap.containsKey(lesson)) {
            for (LanguageCard lc : mLangCardsMap.get(lesson)) {
                if (lc.getWord_lang1().equalsIgnoreCase(word_lang1) && lc.getWord_lang2().equalsIgnoreCase(word_lang2)) {
                    return newLessonAdded;
                }
            }
        }

        LanguageCard lc = new LanguageCard(word_lang1, word_lang2, groupId, 0, lesson);
        if (!mLangCardsMap.containsKey(lesson)) {
            mLangCardsMap.put(lesson, new ArrayList<LanguageCard>());
            newLessonAdded = true;
        }
        mLangCardsMap.get(lesson).add(lc);
        mLangCardsLst.add(lc);
        if (isAsync) {
            ModelFragment.executeAsyncTask(new InsertLanguageCardTask(lc));
        } else {
            lc.setId(mDBHelper.insertLanguageCard(lc));
        }
        return newLessonAdded;
    }

    /**
     *
     * @param id
     * @param word_lang1
     * @param word_lang2
     * @param learned
     * @param lesson
     * @param oldlesson
     * @return true - new lesson added.
     */
    public boolean updateLanguageCardAsync(long id, String word_lang1, String word_lang2, boolean learned,
                                        String lesson, String oldlesson) {
        Log.d(TAG, "UpdateLanguageCardTask: id= " + id + ", word_lang1=" + word_lang1 + ", word_lang2=" + word_lang2
            + ", lesson=" + lesson + ", oldlesson=" + oldlesson);
        boolean newLessonAdded = false;
        if (!mLangCardsMap.containsKey(lesson)) {
            mLangCardsMap.put(lesson, new ArrayList<LanguageCard>());
            newLessonAdded = true;
        }
        for (LanguageCard lc : mLangCardsMap.get(oldlesson)) {
            if (lc.getId() == id) {
                mLangCardsMap.get(oldlesson).remove(lc);
                if (mLangCardsMap.get(oldlesson).size() == 0) {
                    mLangCardsMap.remove(oldlesson);
                }
                lc.setWord_lang1(word_lang1);
                lc.setWord_lang2(word_lang2);
                lc.setLearned(learned ? 1 : 0);
                lc.setLesson(lesson);
                mLangCardsMap.get(lesson).add(lc);
                ModelFragment.executeAsyncTask(new UpdateLanguageCardTask(lc));
                break;
            }
        }
        return newLessonAdded;
    }

    public void deleteLanguageCardAsync(long id, String lesson) {
        Log.d(TAG, "deleteLanguageCardAsync: id= " + id);
        for (LanguageCard lc : mLangCardsMap.get(lesson)) {
            if (lc.getId() == id) {
                mLangCardsMap.get(lesson).remove(lc);
                if (mLangCardsMap.get(lesson).size() == 0) {
                    mLangCardsMap.remove(lesson);
                }
                Log.d(TAG, "deleteLanguageCardAsync: id= " + lc.toString());
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

        if (!mLangCardsMap.containsKey(mCurrentLesson)) {
            mCurrentLesson = ALL_LESSON;
        }

        mListener.onDataUploaded();
    }

    public Map<String, List<LanguageCard>> getLangCardsMap() {
        return mLangCardsMap;
    }

    public Set<String> getLessonsList() {
        return mLangCardsMap.keySet();
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
