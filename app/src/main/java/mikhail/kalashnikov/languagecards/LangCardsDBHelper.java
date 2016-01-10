package mikhail.kalashnikov.languagecards;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class LangCardsDBHelper extends SQLiteOpenHelper {
    private final String TAG = "MK";
    private static final String DATABASE_NAME="LanguageCards.db";
    private static final int SCHEMA_VERSION = 2;
    private static LangCardsDBHelper singleton=null;
    private List<LanguageCard> mLangCardsList;

    interface LangCardsDBHelperListener {
        void onDataUploaded(List<LanguageCard> languageCards);
    }
    synchronized static LangCardsDBHelper getInstance(Context ctxt){
        if(singleton==null){
            singleton = new LangCardsDBHelper(ctxt.getApplicationContext());
        }
        return singleton;
    }

    private LangCardsDBHelper(Context ctxt){
        super(ctxt, DATABASE_NAME, null, SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            db.beginTransaction();
            db.execSQL("CREATE TABLE " + LanguageCard.TABLE_NAME + "("
                    + LanguageCard._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + LanguageCard.COLUMN_WORD_LANG1 + " TEXT,"
                    + LanguageCard.COLUMN_WORD_LANG2 + " TEXT,"
                    + LanguageCard.COLUMN_GROUP_ID + " INTEGER,"
                    + LanguageCard.COLUMN_LESSON + " TEXT,"
                    + LanguageCard.COLUMN_LEARNED + " INTEGER"
                    + ");");

//            ContentValues cv = new ContentValues();
//            cv.put(LanguageCard.COLUMN_WORD_LANG1, "Test Lang1");
//            cv.put(LanguageCard.COLUMN_WORD_LANG2, "Test Lang2");
//            cv.put(LanguageCard.COLUMN_GROUP_ID, 1);
//            db.insert(LanguageCard.TABLE_NAME, null, cv);
//
//            cv = new ContentValues();
//            cv.put(LanguageCard.COLUMN_WORD_LANG1, "Test2 Lang1");
//            cv.put(LanguageCard.COLUMN_WORD_LANG2, "Test2 Lang2");
//            cv.put(LanguageCard.COLUMN_GROUP_ID, 1);
//            db.insert(LanguageCard.TABLE_NAME, null, cv);

            db.setTransactionSuccessful();
        }finally{
            db.endTransaction();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion >= 2) {
            db.execSQL("ALTER TABLE " + LanguageCard.TABLE_NAME +
                    " ADD COLUMN " + LanguageCard.COLUMN_LESSON+ " TEXT"
                    + ";");

            db.execSQL("UPDATE " + LanguageCard.TABLE_NAME +
                    " SET " + LanguageCard.COLUMN_LESSON + " = 'first'"
                    + ";");
        } else {
            Log.e(TAG, "Unknown DB update");
        }

    }

    void getDataAsync(LangCardsDBHelperListener listener) {
        ModelFragment.executeAsyncTask(new GetDataTask(listener));
    }

    private class GetDataTask extends AsyncTask<Void, Void, Void> {
        private LangCardsDBHelperListener listener;

        GetDataTask(LangCardsDBHelperListener listener){
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                Cursor c = getReadableDatabase().rawQuery(
                        "SELECT " +
                                LanguageCard._ID + "," +
                                LanguageCard.COLUMN_WORD_LANG1 + "," +
                                LanguageCard.COLUMN_WORD_LANG2 + "," +
                                LanguageCard.COLUMN_GROUP_ID + "," +
                                LanguageCard.COLUMN_LEARNED + "," +
                                LanguageCard.COLUMN_LESSON +
                                " FROM " + LanguageCard.TABLE_NAME,
                        null);

                mLangCardsList = new ArrayList<>();
                while (c.moveToNext()) {
                    long id=c.getLong(0);
                    String word1=c.getString(1);
                    String word2=c.getString(2);
                    long groupId=c.getLong(3);
                    int learned=c.getInt(4);
                    String lesson = c.getString(5);
                    mLangCardsList.add(new LanguageCard(id, word1, word2, groupId, learned, lesson));
                }

                c.close();
//                for(LanguageCard s: mLangCardsList){
//                    Log.d(TAG, s.toString());
//                }

            }catch (Exception e) {
                Log.e(TAG, "GetDataTask", e);
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void v) {
            Log.d(TAG, "GetDataTask.onPostExecute" + (listener==null));
            listener.onDataUploaded(mLangCardsList);
        }

    }

    long insertLanguageCard(LanguageCard lc){
        Log.d(TAG, "insertLanguageCard:" + lc);
        ContentValues values = new ContentValues();
        values.put(LanguageCard.COLUMN_WORD_LANG1, lc.getWord_lang1());
        values.put(LanguageCard.COLUMN_WORD_LANG2, lc.getWord_lang2());
        values.put(LanguageCard.COLUMN_GROUP_ID, lc.getGroup_id());
        values.put(LanguageCard.COLUMN_LEARNED, lc.getLearned());
        values.put(LanguageCard.COLUMN_LESSON, lc.getLesson());
        return getWritableDatabase().insert(LanguageCard.TABLE_NAME, null, values);
    }

    void deleteLanguageCard(long id){
        Log.d(TAG, "deleteLanguageCard: " + id);
        String whereClause = LanguageCard._ID + " = ?";
        String[] args = {String.valueOf(id)};
        getWritableDatabase().delete(LanguageCard.TABLE_NAME, whereClause, args);
    }

    void updateLanguageCard(LanguageCard lc){
        Log.d(TAG, "updateLanguageCard: " + lc);
        String whereClause = LanguageCard._ID + " = ?";
        String[] args = {String.valueOf(lc.getId())};
        ContentValues values = new ContentValues();
        values.put(LanguageCard.COLUMN_WORD_LANG1, lc.getWord_lang1());
        values.put(LanguageCard.COLUMN_WORD_LANG2, lc.getWord_lang2());
        values.put(LanguageCard.COLUMN_GROUP_ID, lc.getGroup_id());
        values.put(LanguageCard.COLUMN_LEARNED, lc.getLearned());
        values.put(LanguageCard.COLUMN_LESSON, lc.getLesson());
        getWritableDatabase().update(LanguageCard.TABLE_NAME, values, whereClause, args);
    }

}
