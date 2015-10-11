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
import java.util.List;


public class MainActivity extends AppCompatActivity implements DataModel.ModelCallbacks,
        LangCardDialog.LangCardDialogListener{

    private static final String MODEL="model";
    private static final int FILE_SELECT_CODE = 1;
    private static final String TAG = "MK";
    private DataModel mDataModel;
    private TextView mWordLang1;
    private TextView mWordLang2;
    private Button mNextBtn;

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
                    showNextWord();
                }
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.recipe_toolbar);
        Spinner lesson_spinner = (Spinner) findViewById(R.id.lessons_list);
        List<String> items = new ArrayList<String>();
        items.add("tets");
        items.add("tets2");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                items);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lesson_spinner.setAdapter(adapter);
        lesson_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Selecetd = " + parent.getAdapter().getItem(position));
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
            DialogFragment dialog = LangCardDialog.newInstance(LangCardDialog.NEW_CARD_ID, null, null, false);
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
            showNextWord();
        }

        return super.onOptionsItemSelected(item);
    }


    private void showNextWord() {
        mWordLang1.setText(mDataModel.getNextWord());
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
        showCurrentWord();
    }

    @Override
    public void onLangCardEdited(long id, String word1, String word2, boolean learned) {
        if (id == LangCardDialog.NEW_CARD_ID) {
            mDataModel.insertLanguageCardAsync(word1, word2);
        } else {
            mDataModel.updateLanguageCardAsync(id, word1, word2, learned);
        }

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

                        mDataModel.insertLanguageCardAsync(
                                line.substring(0, line.indexOf("|")),
                                line.substring(line.indexOf("|") + 1));
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
        }
    }


    @Override
    protected void onPause() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(SettingsActivity.KEY_PREF_LAST_POS, mDataModel.getCurrentPosition());
        editor.putBoolean(SettingsActivity.KEY_PREF_DIRECTION, mDataModel.getDirection());
        editor.apply();
        super.onPause();
    }

}
