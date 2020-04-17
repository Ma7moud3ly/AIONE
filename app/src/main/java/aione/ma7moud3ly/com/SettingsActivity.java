package aione.ma7moud3ly.com;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        File bootstrap_dir = new File(MainActivity.data_dir + "/" + "bootstrap");
        ((TextView) findViewById(R.id.arch)).setText("Architecture : " + CopyBinary.GetArch());
        ((Switch) findViewById(R.id.bootstrap_switch)).setChecked(bootstrap_dir.exists());
    }

    public void clear_history(View v) {
        try {
            openOrCreateDatabase("settings", MODE_PRIVATE, null).execSQL("DROP TABLE history");
            Toast.makeText(this, "Commands History Was Cleared..", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "No History To Clear..", Toast.LENGTH_SHORT).show();
        }
    }

    public void about(View v) {
        startActivity(new Intent(this, AboutActivity.class));
    }

    public static class CopyBootstrap extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;
        private Activity activity;
        private Switch s = null;
        private boolean enable;
        final private String bootstrap = "bootstrap-" + CopyBinary.arch2() + ".zip";
        final private File bootstrap_dir = new File(MainActivity.data_dir + "/" + "bootstrap");

        public CopyBootstrap(Activity activity, Switch s) {
            this.activity = activity;
            dialog = new ProgressDialog(activity);
            this.s = s;
        }

        public CopyBootstrap(Activity activity, boolean enable) {
            this.activity = activity;
            this.enable = enable;
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Loading Files..");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... args) {
            try {
                if (s != null) enable = s.isChecked();
                if (enable) {
                    boolean b = CopyBinary.unzipBootstrap(bootstrap, bootstrap_dir, activity);
                    if (b) {
                        new ShellCommands(new String[]{"chmod", "+x", "-R", bootstrap_dir.getAbsolutePath()}, null).eval(false);
                        Toast.makeText(activity, "done..", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(activity, "failed..", Toast.LENGTH_SHORT).show();
                } else {
                    new ShellCommands(new String[]{"rm", "-R", bootstrap_dir.getAbsolutePath()}, null).eval(false);
                    Toast.makeText(activity, "done..", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (s != null) s.setChecked(bootstrap_dir.exists());
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

}
