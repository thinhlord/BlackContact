package vn.savvycom.blackcontact;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;


public class MainActivity extends BaseActivity {

    private ArrayList<Contact> contacts = new ArrayList<>();
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        if (contacts.size() == 0) {
            reload();
        } else {
            ((OnFragmentDatasetChanged) mSectionsPagerAdapter.getItem(0)).onContactLoaded(contacts);
            ((OnFragmentDatasetChanged) mSectionsPagerAdapter.getItem(1)).onContactLoaded(contacts);
        }
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK){
                reload();
            }
        }
    }

    private void loadContacts() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
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
                    ArrayList<String> phone = new ArrayList<>();
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phone.add(phoneNo);
                    }
                    Bitmap photo = null;
                    InputStream inputStream = openDisplayPhoto(Long.parseLong(id));
                    if (inputStream != null) {
                        photo = BitmapFactory.decodeStream(inputStream);
                    }
                    contacts.add(new Contact(id, name, photo, accountType, phone));
                    pCur.close();
                }
            }
        }
        cur.close();
        Collections.sort(contacts, new ContactComparator());
    }

    public InputStream openDisplayPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
        try {
            AssetFileDescriptor fd =
                    getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
            return fd.createInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    public interface OnFragmentDatasetChanged {
        void onContactLoaded(ArrayList<Contact> contacts);
    }

    private class LoadContactTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... voids) {
            loadContacts();
            return null;
        }

        protected void onPostExecute(Void voided) {
            ((OnFragmentDatasetChanged) mSectionsPagerAdapter.getItem(0)).onContactLoaded(contacts);
            ((OnFragmentDatasetChanged) mSectionsPagerAdapter.getItem(1)).onContactLoaded(contacts);
        }
    }

    private class ContactComparator implements Comparator<Contact> {
        public int compare(Contact left, Contact right) {
            return left.getName().compareTo(right.getName());
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
