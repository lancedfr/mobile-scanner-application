package za.ac.cput;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class AsyncTimer extends AsyncTask<Void, Integer, Boolean> {
    public ProgressBar spinner;
    private boolean isRunning;
    private boolean stop;
    private int seconds;
    private Context context;
    private View rootView;
    private onprogressUpdateListener listener;

    public AsyncTimer(Context c, View rootView) {
        context = c;
        this.rootView = rootView;
    }

    public void setonprogressUpdateListener(onprogressUpdateListener l) {
        listener = l;
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {

        stop = false;
        isRunning = true;
        seconds = 0;
        this.publishProgress(seconds);
        while (seconds < 5 && stop != true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e("", e.getMessage());
            }
            seconds++;
            this.publishProgress(seconds);

        }

        if (stop == false) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onCancelled() {
        stop = true;
        isRunning = false;
        Toast.makeText(context, "Search stopped ", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        isRunning = false;
        if (result == true) {
            spinner = (ProgressBar) rootView.findViewById(R.id.progressBar1);
            spinner.setVisibility(View.INVISIBLE);
        }
    }

    protected void onprogressUpdate(Integer... values) {
        listener.progressUpdate(values[0]);
    }

    public boolean getIsRunning() {
        return isRunning;
    }

    public interface onprogressUpdateListener {
        public void progressUpdate(int i);
    }

}


