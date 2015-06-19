package vn.savvycom.blackcontact;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;


public class MusicActivity extends BaseActivity implements View.OnClickListener {
    Intent playIntent;
    MaterialDialog dialog;
    boolean started = false;

    private BroadcastReceiver gpsBRec = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(MusicService.START, false)) {
                started = true;
                dialog.dismiss();
            } else {
                dialog.dismiss();
                dialog = new MaterialDialog.Builder(MusicActivity.this)
                        .title("Error")
                        .content("Cannot connect to Music Service")
                        .show();
            }

        }
    };

    @Override
    public void onStart() {
        super.onStart();
        playIntent = new Intent(this, MusicService.class);
        findViewById(R.id.toggleButton).setOnClickListener(this);
        findViewById(R.id.share).setOnClickListener(this);
        dialog = new MaterialDialog.Builder(this)
                .title("Connecting to Music Service")
                .content("Please wait...")
                .progress(true, 0)
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (!started) {
                            stopService(playIntent);
                            ((ToggleButton) findViewById(R.id.toggleButton)).setChecked(false);
                        }
                    }
                })
                .build();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_music;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_music, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toggleButton:
                if (((ToggleButton) view).isChecked()) {
                    dialog.show();
                    startService(playIntent);
                } else {
                    started = false;
                    stopService(playIntent);
                }
                break;
            case R.id.share:
                Toast.makeText(getApplicationContext(), "Call", Toast.LENGTH_LONG).show();
                shareMusic();
        }
    }

    private void shareMusic() {
        Intent emailIntent = new Intent();
        emailIntent.setAction(Intent.ACTION_SEND);
        // Native email client doesn't currently support HTML, but it doesn't hurt to try in case they fix it
        emailIntent.putExtra(Intent.EXTRA_TEXT, MusicService.url);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Share test");
        emailIntent.setType("message/rfc822");

        PackageManager pm = getPackageManager();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");


        Intent openInChooser = Intent.createChooser(emailIntent, "Share...");

        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
        List<LabeledIntent> intentList = new ArrayList<>();
        for (int i = 0; i < resInfo.size(); i++) {
            // Extract the label, append it, and repackage it in a LabeledIntent
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;
            if (packageName.contains("android.email")) {
                emailIntent.setPackage(packageName);
            } else if (packageName.contains("twitter") || packageName.contains("facebook") || packageName.contains("mms") || packageName.contains("android.gm")) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                if (packageName.contains("twitter")) {
                    intent.putExtra(Intent.EXTRA_TEXT, "Share over twitter");
                } else if (packageName.contains("facebook")) {
                    // Warning: Facebook IGNORES our text. They say "These fields are intended for users to express themselves. Pre-filling these fields erodes the authenticity of the user voice."
                    // One workaround is to use the Facebook SDK to post, but that doesn't allow the user to choose how they want to share. We can also make a custom landing page, and the link
                    // will show the <meta content ="..."> text from that page with our link in Facebook.
                    intent.putExtra(Intent.EXTRA_TEXT, "Share over facebook");
                } else if (packageName.contains("mms")) {
                    intent.putExtra(Intent.EXTRA_TEXT, "Share over mail");
                } else if (packageName.contains("android.gm")) { // If Gmail shows up twice, try removing this else-if clause and the reference to "android.gm" above
                    intent.putExtra(Intent.EXTRA_TEXT, MusicService.url);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Share test");
                    intent.setType("message/rfc822");
                }

                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }

        // convert intentList to array
        LabeledIntent[] extraIntents = intentList.toArray(new LabeledIntent[intentList.size()]);

        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        startActivity(openInChooser);

    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(gpsBRec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(gpsBRec, new IntentFilter(MusicService.BROADCAST));
    }
}
