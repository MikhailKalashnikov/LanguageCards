package mikhail.kalashnikov.languagecards;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DataModel.ModelCallbacks,
        LangCardDialog.LangCardDialogListener{

    private static final String MODEL="model";
    private static final int FILE_SELECT_CODE = 1;
    private static final String TAG = "MK";
    private DataModel mDataModel;
    private TextView mWordLang1;
    private TextView mWordLang2;
    private Button mNextBtn;
    private String mCurrentLesson;
    private ArrayAdapter<String> mAdapterLessons;
    private Spinner mLesson_spinner;

    enum ButtonMode {CHECK, NEXT}
    private ButtonMode mbtnMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        ModelFragment model;
        if (getFragmentManager().findFragmentByTag(MODEL)==null) {
            model = new ModelFragment();
            getFragmentManager().beginTransaction()
                    .add(model, MODEL)
                    .commit();
        }else{
            model = (ModelFragment)getFragmentManager().findFragmentByTag(MODEL);
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mCurrentLesson = prefs.getString(SettingsActivity.KEY_PREF_LESSON, DataModel.ALL_LESSON);

        setContentView(R.layout.activity_main);
        mbtnMode = ButtonMode.NEXT;
        mWordLang1 = (TextView) findViewById(R.id.word_lang1);
        mWordLang2 = (TextView) findViewById(R.id.word_lang2);
        mNextBtn = (Button) findViewById(R.id.btn_check);
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mbtnMode == ButtonMode.CHECK) {
                    showAnswerWord();
                } else if (mbtnMode == ButtonMode.NEXT) {
                    showNextWord(mCurrentLesson);
                }
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.recipe_toolbar);
        mLesson_spinner = (Spinner) findViewById(R.id.lessons_list);

        mAdapterLessons = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new ArrayList<String>());

        mAdapterLessons.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLesson_spinner.setAdapter(mAdapterLessons);
        mLesson_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Selected = " + parent.getAdapter().getItem(position));
                mCurrentLesson = (String)parent.getAdapter().getItem(position);
                showNextWord(mCurrentLesson);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_add) {
            DialogFragment dialog = LangCardDialog.newInstance(LangCardDialog.NEW_CARD_ID, null, null, null, false);
            dialog.show(getFragmentManager(), "LangCardDialog");
        } else if (id == R.id.action_word_list) {
                Intent i = new Intent(this, WordListActivity.class);
                startActivity(i);
        } else if (id == R.id.load_list) {
            boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
            if (isKitKat) {
                Intent intent = new Intent();
                intent.setType("text/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(intent,FILE_SELECT_CODE);

            } else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/*");
                startActivityForResult(intent,1);
            }
        } else if (id == R.id.action_switch_direction) {
            mDataModel.switchDirection();
            mbtnMode = ButtonMode.CHECK;
            mNextBtn.setText(R.string.btn_check);
            mNextBtn.setBackgroundResource(R.drawable.check);
            showNextWord(mCurrentLesson);
        }

        return super.onOptionsItemSelected(item);
    }


    private void showNextWord(String lesson) {
        mWordLang1.setText(mDataModel.getNextWord(lesson));
        mWordLang2.setText("");
        mbtnMode = ButtonMode.CHECK;
        mNextBtn.setText(R.string.btn_check);
        mNextBtn.setBackgroundResource(R.drawable.check);
    }

    private void showCurrentWord() {
        mWordLang1.setText(mDataModel.getCurrentWord());
        mWordLang2.setText("");
        mbtnMode = ButtonMode.CHECK;
        mNextBtn.setText(R.string.btn_check);
        mNextBtn.setBackgroundResource(R.drawable.check);
    }

    private void showAnswerWord() {
        mbtnMode = ButtonMode.NEXT;
        mNextBtn.setText(R.string.btn_next);
        mNextBtn.setBackgroundResource(R.drawable.next);
        mWordLang2.setText(mDataModel.getCurrentAnswer());
    }


    @Override
    public void onDataUploaded() {
        mDataModel = DataModel.getInstance(getApplicationContext());
        refreshLessonsList();
        showCurrentWord();
    }

    private void refreshLessonsList() {
        mAdapterLessons.clear();
        mAdapterLessons.add(DataModel.ALL_LESSON);
        mAdapterLessons.addAll(mDataModel.getLessonsList());
        mLesson_spinner.setSelection(mAdapterLessons.getPosition(mCurrentLesson));
    }

    @Override
    public void onLangCardEdited(long id, String word1, String word2, String lesson, boolean learned) {
        if (id == LangCardDialog.NEW_CARD_ID) {
            mDataModel.insertLanguageCardAsync(word1, word2, lesson);
        } else {
            mDataModel.updateLanguageCardAsync(id, word1, word2, learned, lesson, lesson);// should not be called here
        }
        refreshLessonsList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == RESULT_OK) {

                Uri currFileURI = data.getData();
                String filePath = currFileURI.getPath();
                String selectedMediaPath = getRealPathFromURI(this, currFileURI);
                if (!selectedMediaPath.equals("")) {
                    filePath = selectedMediaPath;
                }
                LoadListTask task = new LoadListTask();
                task.execute(filePath);
            } else {
                Toast.makeText(this, R.string.load_list_err, Toast.LENGTH_LONG).show();
            }

        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {

            String[] proj = { MediaStore.MediaColumns.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } else {
                return "";
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    class LoadListTask extends AsyncTask<String, Void, Void> {
        private boolean isError = false;
        @Override
        protected Void doInBackground(String... file) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File(file[0])));
                String line;

                while ((line = br.readLine()) != null) {
                    if (!line.contains("|")) {
                        Log.d(TAG, line);
                    } else {
                        int delimiterpos = line.indexOf("|");
                        String word1 = line.substring(0, delimiterpos);
                        String word2 = line.substring(delimiterpos + 1, (delimiterpos = line.indexOf("|", delimiterpos + 1)));
                        String lesson = line.substring(delimiterpos + 1);
                        mDataModel.insertLanguageCardAsync(word1, word2, lesson);
                    }
                }
                br.close();

            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                isError = true;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (isError) {
                Toast.makeText(MainActivity.this, R.string.load_list_err, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, R.string.load_list_loaded, Toast.LENGTH_LONG).show();
            }
            refreshLessonsList();
        }
    }


    @Override
    protected void onPause() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(SettingsActivity.KEY_PREF_LAST_POS, mDataModel.getCurrentPosition());
        editor.putBoolean(SettingsActivity.KEY_PREF_DIRECTION, mDataModel.getDirection());
        editor.putString(SettingsActivity.KEY_PREF_LESSON, mCurrentLesson);
        editor.apply();
        super.onPause();
    }

}
