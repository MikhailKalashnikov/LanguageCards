package mikhail.kalashnikov.languagecards.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.Button;

import mikhail.kalashnikov.languagecards.MainActivity;
import mikhail.kalashnikov.languagecards.R;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity mMainActivity;
    private Button mNextBtn;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(true);
        mMainActivity = getActivity();
        mNextBtn = (Button) mMainActivity.findViewById(R.id.btn_check);
    }


    public void testPreconditions() {
        assertNotNull("mMainActivity is null", mMainActivity);
        assertNotNull("mNextBtn is null", mNextBtn);
    }

    @MediumTest
    public void testNextButton_textLabel() {
        final String expected = mMainActivity.getString(R.string.btn_next);
        final String actual = mNextBtn.getText().toString();
        assertEquals(expected, actual);

    }

    @MediumTest
    public void testNextButton_clickButtonTextLabel() {
        final String expected = mMainActivity.getString(R.string.btn_check);
        TouchUtils.clickView(this, mNextBtn);
        final String actual = mNextBtn.getText().toString();
        assertEquals(expected, actual);

    }

}
