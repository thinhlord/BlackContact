package vn.savvycom.blackcontact;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;


public abstract class BaseActivity extends AppCompatActivity {

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        //showAds();
    }

    protected abstract int getLayoutResource();

    protected void enableHomeButton() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void setTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

//    public int getActionBarSize() {
//        TypedValue typedValue = new TypedValue();
//        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
//        int indexOfAttrTextSize = 0;
//        TypedArray a = obtainStyledAttributes(typedValue.data, textSizeAttr);
//        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
//        a.recycle();
//        return actionBarSize;
//    }
//
//    public int getScreenHeight() {
//        DisplayMetrics displaymetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//        return displaymetrics.heightPixels;
//    }
//
//    public int getScreenWidth() {
//        DisplayMetrics displaymetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//        return displaymetrics.widthPixels;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                //startSettingActivity();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    protected void startSettingActivity() {
//        Intent intent = new Intent(this, SettingsActivity.class);
//        startActivity(intent);
//    }

    protected void setActionBarIcon(int iconRes) {
        toolbar.setNavigationIcon(iconRes);
    }

//    public void showAds() {
//        AdView mAdView = (AdView) findViewById(R.id.adView);
//        if (mAdView != null) {
//            AdRequest adRequest = new AdRequest.Builder()
//                    .addTestDevice("C53B6CAB5F7A8A42A08B80A084477DB4")
//                    .build();
//            mAdView.loadAd(adRequest);
//        }
//    }
}
