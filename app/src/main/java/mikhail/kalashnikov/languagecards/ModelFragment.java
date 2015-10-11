package mikhail.kalashnikov.languagecards;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;


public class ModelFragment extends Fragment{
    private final String TAG = "MK";
    private DataModel mModel;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");

        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        mModel = DataModel.getInstance(getActivity().getApplicationContext());
        uploadData();

    }

    synchronized private void uploadData(){
        Log.d(TAG, "uploadData ");
        if (mModel.isDataUploaded()){
            ((DataModel.ModelCallbacks)getActivity()).onDataUploaded();
        }else {
            mModel.uploadData((DataModel.ModelCallbacks)getActivity());
        }
    }

    @TargetApi(11)
    static public <T> void executeAsyncTask(AsyncTask<T, ?, ?> task, T... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        }
        else {
            task.execute(params);
        }
    }

}
