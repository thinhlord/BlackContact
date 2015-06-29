package vn.savvycom.blackcontact;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import vn.savvycom.blackcontact.Item.Contact;


public class ContactEditorActivity extends BaseActivity implements View.OnClickListener {
    ArrayAdapter<CharSequence> phoneTypeAdapter;
    private Contact contact;
    private boolean add = true;
    private ImageView photo;
    private Uri photoUri = null;
    private Bitmap mBitmap;
    private LinearLayout phoneGroupLayout, mailGroupLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableHomeButton();

        phoneGroupLayout = (LinearLayout) findViewById(R.id.phone_group);
        mailGroupLayout = (LinearLayout) findViewById(R.id.mail_group);

        phoneTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.phone_type, android.R.layout.simple_spinner_item);
        phoneTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        contact = getIntent().getParcelableExtra(GlobalObject.EXTRA_CONTACT);
        if (contact == null) {
            setTitle("Add new contact");
            addPhoneView(null, null);
            add = true;
        } else {
            add = false;
            setTitle("Edit");
            photo = (ImageView) findViewById(R.id.photo);
            if (contact.getPhoto() != null) {
                photo.setImageURI(contact.getPhoto());
            } else {
                photo.setImageResource(R.mipmap.ic_launcher);
            }
            photo.setOnClickListener(this);
            ((TextView) findViewById(R.id.name)).setText(contact.getName());
            if (contact.getPhone().size() == 0) {
                addPhoneView(null, null);
            } else for (int i = 0; i < contact.getPhone().size(); i++) {
                addPhoneView(contact.getPhone().get(i), contact.getPhoneType().get(i));
            }
            for (int i = 0; i < contact.getMail().size(); i++) {
                addEmailView(contact.getMail().get(i), contact.getMailType().get(i));
            }
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_contact_editor;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!add) getMenuInflater().inflate(R.menu.menu_contact_editor, menu);
        else getMenuInflater().inflate(R.menu.menu_contact_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveContact();
                return true;
            case R.id.action_del:
                new MaterialDialog.Builder(this)
                        .title("Confirm delete")
                        .content("Do you want to delete this contact?")
                        .positiveText("Yes")
                        .negativeText("No")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                deleteContact();
                                super.onPositive(dialog);
                            }
                        })
                        .show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveContact() {
        TextView name = (TextView) findViewById(R.id.name);
        if (name.getText().toString().equals("")) {
            Toast.makeText(this, "Name is null", Toast.LENGTH_LONG).show();
            return;
        }
        boolean sendNewContactBack = true;
        for (Contact c : GlobalObject.allContacts) {
            if (c.getName().equals(name.getText().toString())) {
                sendNewContactBack = false;
                break;
            }
        }
        if (add) {
            // Add new
            ArrayList<ContentProviderOperation> operationList = new ArrayList<>();
            operationList.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            // name
            operationList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name.getText().toString())
                    .build());

            // phones
            ArrayList<String> phones = new ArrayList<>();
            ArrayList<String> phoneTypes = new ArrayList<>();
            for (int i = 0; i < phoneGroupLayout.getChildCount(); i++) {
                String phoneNumber = ((EditText) phoneGroupLayout.getChildAt(i).findViewById(R.id.phone_number)).getText().toString();
                int phoneType;
                int selected = ((Spinner) phoneGroupLayout.getChildAt(i).findViewById(R.id.phone_type)).getSelectedItemPosition();
                switch (selected) {
                    case 0:
                        phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
                        break;
                    case 1:
                        phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
                        break;
                    case 2:
                        phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
                        break;
                    default:
                        phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_OTHER;
                        break;
                }
                operationList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phoneType)
                        .build());
                phones.add(phoneNumber);
                phoneTypes.add("" + phoneType);
            }

            // mails
            ArrayList<String> mails = new ArrayList<>();
            ArrayList<String> mailTypes = new ArrayList<>();

            for (int i = 0; i < mailGroupLayout.getChildCount(); i++) {
                String email = ((EditText) mailGroupLayout.getChildAt(i).findViewById(R.id.email)).getText().toString();
                int mailType;
                int selected = ((Spinner) mailGroupLayout.getChildAt(i).findViewById(R.id.mail_type)).getSelectedItemPosition();
                switch (selected) {
                    case 0:
                        mailType = ContactsContract.CommonDataKinds.Email.TYPE_HOME;
                        break;
                    case 1:
                        mailType = ContactsContract.CommonDataKinds.Email.TYPE_MOBILE;
                        break;
                    case 2:
                        mailType = ContactsContract.CommonDataKinds.Email.TYPE_WORK;
                        break;
                    default:
                        mailType = ContactsContract.CommonDataKinds.Email.TYPE_OTHER;
                        break;
                }
                operationList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, mailType)
                        .build());
                mails.add(email);
                mailTypes.add("" + mailType);
            }

            // photo
            if (photoUri != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                if (mBitmap != null) {    // If an image is selected successfully
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 75, stream);

                    // Adding insert operation to operations list
                    // to insert Photo in the table ContactsContract.Data
                    operationList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, stream.toByteArray())
                            .build());

                    try {
                        stream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                ContentProviderResult[] results = getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
                String id = results[0].uri.getLastPathSegment();
                String accountType = null;
                Cursor aCur = getContentResolver().query(
                        ContactsContract.RawContacts.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id}, null);
                if (aCur != null && aCur.getCount() > 0) {
                    aCur.moveToFirst();
                    accountType = aCur.getString(aCur.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
                    aCur.close();
                }
                contact = new Contact(id, name.getText().toString(), photoUri, accountType, phones, phoneTypes, mails, mailTypes);
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                if (sendNewContactBack) {
                    returnIntent.putExtra(GlobalObject.EXTRA_CONTACT, contact);
                    GlobalObject.allContacts.add(contact);
                    Collections.sort(GlobalObject.allContacts, new GlobalObject.ContactComparator());
                } else returnIntent.putExtra(GlobalObject.EXTRA_MERGE, true);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Update
            // name
            ArrayList<ContentProviderOperation> operationList = new ArrayList<>();

            if (!name.getText().toString().equals(contact.getName())) {
                operationList.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "=?",
                                new String[]{contact.getId(),
                                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE})
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name.getText().toString())
                        .build());
                contact.setName(name.getText().toString());
            }

            contact.clearPhoneMail();

            // phones
            // Bad method: remove all numbers then add all current numbers in the UI
            operationList.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.Data.CONTACT_ID + "=? and " + ContactsContract.Data.MIMETYPE + "=?",
                            new String[]{contact.getId(), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE})
                    .build());

            ArrayList<String> phones = new ArrayList<>();
            ArrayList<String> phoneTypes = new ArrayList<>();
            for (int i = 0; i < phoneGroupLayout.getChildCount(); i++) {
                String phoneNumber = ((EditText) phoneGroupLayout.getChildAt(i).findViewById(R.id.phone_number)).getText().toString();
                int phoneType;
                int selected = ((Spinner) phoneGroupLayout.getChildAt(i).findViewById(R.id.phone_type)).getSelectedItemPosition();
                switch (selected) {
                    case 0:
                        phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
                        break;
                    case 1:
                        phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
                        break;
                    case 2:
                        phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
                        break;
                    default:
                        phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_OTHER;
                        break;
                }
                operationList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, contact.getId())
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phoneType)
                        .build());
                phones.add(phoneNumber);
                phoneTypes.add("" + phoneType);
            }
            contact.addPhones(phones, phoneTypes);

            // mails
            ArrayList<String> mails = new ArrayList<>();
            ArrayList<String> mailTypes = new ArrayList<>();
            operationList.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? and " + ContactsContract.Data.MIMETYPE + "=?",
                            new String[]{contact.getId(), ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE})
                    .build());

            for (int i = 0; i < mailGroupLayout.getChildCount(); i++) {
                String email = ((EditText) mailGroupLayout.getChildAt(i).findViewById(R.id.email)).getText().toString();
                int mailType;
                int selected = ((Spinner) mailGroupLayout.getChildAt(i).findViewById(R.id.mail_type)).getSelectedItemPosition();
                switch (selected) {
                    case 0:
                        mailType = ContactsContract.CommonDataKinds.Email.TYPE_HOME;
                        break;
                    case 1:
                        mailType = ContactsContract.CommonDataKinds.Email.TYPE_MOBILE;
                        break;
                    case 2:
                        mailType = ContactsContract.CommonDataKinds.Email.TYPE_WORK;
                        break;
                    default:
                        mailType = ContactsContract.CommonDataKinds.Email.TYPE_OTHER;
                        break;
                }
                operationList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, contact.getId())
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, mailType)
                        .build());
                mails.add(email);
                mailTypes.add("" + mailType);
            }
            contact.addMails(mails, mailTypes);

            if (photoUri != null) {
                contact.setPhoto(photoUri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                if (mBitmap != null) {    // If an image is selected successfully
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 75, stream);

                    // Adding insert operation to operations list
                    // to insert Photo in the table ContactsContract.Data
                    operationList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValue(ContactsContract.Data.RAW_CONTACT_ID, contact.getId())
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, stream.toByteArray())
                            .build());

                    try {
                        stream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                returnIntent.putExtra(GlobalObject.EXTRA_CONTACT, contact);
                Iterator<Contact> iterator = GlobalObject.allContacts.iterator();
                while (true) {
                    if (!(iterator.hasNext())) break;
                    Contact c = iterator.next();
                    if (c.getId().equals(contact.getId())) {
                        GlobalObject.allContacts.remove(c);
                        break;
                    }
                }
                GlobalObject.allContacts.add(contact);
                Collections.sort(GlobalObject.allContacts, new GlobalObject.ContactComparator());
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_phone:
                addPhoneView(null, null);
                break;
            case R.id.add_mail:
                addEmailView(null, null);
                break;
            case R.id.photo:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 1);
        }
    }

    Target target = new Target() {
        MaterialDialog md;

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mBitmap = bitmap;
            photo.setImageBitmap(bitmap);
            md.dismiss();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            md = new MaterialDialog.Builder(ContactEditorActivity.this)
                    .title("Loading")
                    .content("Wait a bit...")
                    .autoDismiss(false)
                    .progress(true, 0)
                    .show();
        }

    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                photoUri = data.getData();
                Picasso.with(this)
                        .load(photoUri)
                        .resize(500, 500)
                        .centerInside()
                        .into(target);

            }
        }
    }

    private void addPhoneView(String phone, String type) {
        //if (phoneGroupLayout.getChildCount() >= 6) return;
        final View newPhoneView = LayoutInflater.from(this).inflate(R.layout.edit_phone_number_layout, null);
        Spinner spinner = (Spinner) newPhoneView.findViewById(R.id.phone_type);
        spinner.setAdapter(phoneTypeAdapter);
        if (type != null) {
            int typeInt = Integer.parseInt(type);
            switch (typeInt) {
                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                    typeInt = 0;
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                    typeInt = 1;
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                    typeInt = 2;
                    break;
                default:
                    typeInt = 3;
                    break;
            }
            spinner.setSelection(typeInt);
        }
        EditText phoneView = (EditText) newPhoneView.findViewById(R.id.phone_number);
        phoneView.requestFocus();
        if (phone != null) {
            phoneView.setText(phone);
        }
        newPhoneView.findViewById(R.id.delete_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneGroupLayout.removeView(newPhoneView);
            }
        });
        phoneGroupLayout.addView(newPhoneView);
    }

    private void addEmailView(String mailAddress, String type) {
        //if (mailGroupLayout.getChildCount() >= 6) return;
        final View newMailView = LayoutInflater.from(this).inflate(R.layout.edit_email_layout, null);
        Spinner spinner = (Spinner) newMailView.findViewById(R.id.mail_type);
        spinner.setAdapter(phoneTypeAdapter);
        if (type != null) {
            int typeInt = Integer.parseInt(type);
            switch (typeInt) {
                case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
                    typeInt = 0;
                    break;
                case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE:
                    typeInt = 1;
                    break;
                case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
                    typeInt = 2;
                    break;
                default:
                    typeInt = 3;
                    break;
            }
            spinner.setSelection(typeInt);
        }
        EditText email = (EditText) newMailView.findViewById(R.id.email);
        email.requestFocus();
        if (mailAddress != null) {
            email.setText(mailAddress);
        }
        newMailView.findViewById(R.id.delete_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mailGroupLayout.removeView(newMailView);
            }
        });
        mailGroupLayout.addView(newMailView);
    }

    public void deleteContact() {
        final ContentResolver cr = getContentResolver();
        final ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation
                .newDelete(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                + " = ?",
                        new String[]{contact.getId()})
                .build());
        try {
            cr.applyBatch(ContactsContract.AUTHORITY, ops);
            Intent returnIntent = new Intent();
            setResult(RESULT_OK, returnIntent);
            returnIntent.putExtra(GlobalObject.EXTRA_DELETE, true);
            Iterator<Contact> iterator = GlobalObject.allContacts.iterator();
            while (true) {
                if (!(iterator.hasNext())) break;
                Contact c = iterator.next();
                if (c.getId().equals(contact.getId())) {
                    GlobalObject.allContacts.remove(c);
                    break;
                }
            }
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
