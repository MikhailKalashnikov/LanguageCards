package mikhail.kalashnikov.languagecards;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity  extends PreferenceActivity {
    final static String KEY_PREF_IS_RANDOM = "pref_random";
    final static String KEY_PREF_SHOW_LEARNED = "pref_show_learned";
    final static String KEY_PREF_LAST_POS = "pref_last_pos";
    final static String KEY_PREF_DIRECTION = "pref_direction";
    final static String KEY_PREF_LESSON = "pref_lesson";
    final static String KEY_PREF_EXPAND_GROUP = "pref_expand_group";
    final static String KEY_PREF_SORT_MODE = "pref_sort_mode";

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
