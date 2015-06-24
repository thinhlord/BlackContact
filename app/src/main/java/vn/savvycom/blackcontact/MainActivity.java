package vn.savvycom.blackcontact;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
import com.astuetz.PagerSlidingTabStrip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import vn.savvycom.blackcontact.Item.Contact;

//import com.twitter.sdk.android.Twitter;
//import com.twitter.sdk.android.core.TwitterAuthConfig;


public class MainActivity extends BaseActivity implements SearchView.OnQueryTextListener {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
//    private static final String TWITTER_KEY = "XLPlZaIWZL80pJi27Ln5jnVF8";
//    private static final String TWITTER_SECRET = "zxRNnSpFyXPlg7YXCvdkbRgwKY3kHS9tllv2z9hSPfnUGyXfqH";


    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
//        Fabric.with(this, new Twitter(authConfig));
        setActionBarIcon(R.mipmap.ic_launcher);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabs.setViewPager(mViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (GlobalObject.allContacts.size() == 0) {
            reload();
        } else {
            ((OnFragmentDatasetChanged) mSectionsPagerAdapter.getItem(0)).onContactLoaded();
            ((OnFragmentDatasetChanged) mSectionsPagerAdapter.getItem(1)).onContactLoaded();
        }
    }

    public static ArrayList<Contact> contactFilter(ArrayList<Contact> input, String query) {
        query = query.toLowerCase();

        final ArrayList<Contact> filteredModelList = new ArrayList<>();
        for (Contact model : input) {
            if (model.getName().toLowerCase().contains(query)) {
                filteredModelList.add(model);
            } else {
                for (String phone : model.getPhone()) {
                    if (phone.contains(query)) {
                        filteredModelList.add(model);
                        break;
                    }
                }
            }
        }
        return filteredModelList;
    }

    public void reload() {
        new LoadContactTask().execute();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (searchView != null) {
            searchView.setOnQueryTextListener(this);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_add:
                Intent intentAdd = new Intent(this, ContactEditorActivity.class);
                startActivityForResult(intentAdd, 1);
                return true;
            case R.id.action_music:
                Intent intentMusic = new Intent(this, MusicActivity.class);
                startActivity(intentMusic);
                return true;
            case R.id.action_share:
                Intent intentShare = new Intent(this, ShareActivity.class);
                startActivity(intentShare);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (searchView != null && !searchView.isIconified()) searchView.setIconified(true);
        else super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (data.getExtras().getBoolean(GlobalObject.EXTRA_MERGE)) {
                    new MaterialDialog.Builder(this)
                            .content("There is a contact with the same name as your added one and they were merged.")
                            .positiveText("OK")
                            .show();
                }
                reload();
//                Contact newContact = loadContacts((Uri) data.getExtras().get("newID"));
//                ((OnFragmentDatasetChanged) mSectionsPagerAdapter.getItem(0)).addItem(newContact);
//                Contact newContact = data.getExtras().getParcelable("editContact");

            }
        }
    }

    private void loadContacts() {
        GlobalObject.allContacts.clear();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                // Get account type
                String accountType = null;
                Cursor aCur = cr.query(
                        ContactsContract.RawContacts.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id}, null);
                if (aCur != null && aCur.getCount() > 0) {
                    aCur.moveToFirst();
                    accountType = aCur.getString(aCur.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
                    aCur.close();
                }

                // Get all phone numbers
                ArrayList<String> phones = new ArrayList<>();
                ArrayList<String> phoneTypes = new ArrayList<>();
                Cursor pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id}, null);
                while (pCur.moveToNext()) {
                    String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String phoneType = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                    phones.add(phoneNo);
                    phoneTypes.add(phoneType);
                }
                pCur.close();

                // Get all email
                ArrayList<String> mails = new ArrayList<>();
                ArrayList<String> mailTypes = new ArrayList<>();
                Cursor pCurs = cr.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{id}, null);
                while (pCurs.moveToNext()) {
                    String nextMail = pCurs.getString(pCurs.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                    String mailType = pCurs.getString(pCurs.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
                    mails.add(nextMail);
                    mailTypes.add(mailType);
                }
                pCurs.close();

                Uri photo = getDisplayPhotoUri(Long.parseLong(id));
                GlobalObject.allContacts.add(new Contact(id, name, photo, accountType, phones, phoneTypes, mails, mailTypes));
            }

        }
        cur.close();
        Collections.sort(GlobalObject.allContacts, new GlobalObject.ContactComparator());
    }

    public Uri getDisplayPhotoUri(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
        try {
            AssetFileDescriptor fd =
                    getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
            fd.createInputStream();
            return displayPhotoUri;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        ((OnFragmentDatasetChanged) mSectionsPagerAdapter.getItem(0)).onFilter(newText);
        ((OnFragmentDatasetChanged) mSectionsPagerAdapter.getItem(1)).onFilter(newText);
        return false;
    }

    public interface OnFragmentDatasetChanged {
        void onContactLoaded();
        void onPreLoad();

        void onFilter(String query);
    }

    private class LoadContactTask extends AsyncTask<Void, Void, Void> {
        protected void onPreExecute() {
            ((OnFragmentDatasetChanged) mSectionsPagerAdapter.getItem(0)).onPreLoad();
            ((OnFragmentDatasetChanged) mSectionsPagerAdapter.getItem(1)).onPreLoad();
        }

        protected Void doInBackground(Void... voids) {
            loadContacts();
            return null;
        }

        protected void onPostExecute(Void voided) {
            ((OnFragmentDatasetChanged) mSectionsPagerAdapter.getItem(0)).onContactLoaded();
            ((OnFragmentDatasetChanged) mSectionsPagerAdapter.getItem(1)).onContactLoaded();
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) return ContactFragment.getInstance();
            return FavoriteFragment.getInstance();
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
            }
            return null;
        }
    }

}
