package vn.savvycom.blackcontact;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class ContactEditorActivity extends BaseActivity {
    public static final String EXTRA_CONTACT = "CONTACT";
    private Contact contact;
    private boolean add = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableHomeButton();
        contact = getIntent().getParcelableExtra(EXTRA_CONTACT);
        if (contact == null) {
            setTitle("Add new contact");
            add = true;
        } else {
            add = false;
            setTitle("Edit");
            ImageView imageView = (ImageView) findViewById(R.id.photo);
            if (contact.getPhoto() != null) {
                imageView.setImageBitmap(contact.getPhoto());
            } else {
                imageView.setImageResource(R.mipmap.ic_launcher);
            }
            ((TextView) findViewById(R.id.name)).setText(contact.getName());
            ((TextView) findViewById(R.id.phone)).setText(contact.getPhone().get(0));
//            ListView listView = (ListView) findViewById(R.id.phone_list);
//            listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contact.getPhone()));
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_contact_editor;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveContact();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveContact() {
        TextView name = (TextView) findViewById(R.id.name);
        TextView phone = (TextView) findViewById(R.id.phone);
        if (name.getText().toString().equals("")) {
            Toast.makeText(this, "Name is null", Toast.LENGTH_LONG).show();
            return;
        }
        if (phone.getText().toString().equals("")) {
            Toast.makeText(this, "Name is null", Toast.LENGTH_LONG).show();
            return;
        }
        if (add) {
            ArrayList<ContentProviderOperation> operationList = new ArrayList<>();
            operationList.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            // names
            operationList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name.getText().toString())
                    .build());

            operationList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone.getText().toString())
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
                    .build());
//            operationList.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//
//                    .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
//                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, "abc@xyz.com")
//                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
//                    .build());

            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
//            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
//
//            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
//                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, contact.getId())
//                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
//                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name.getText().toString())
//                    .build());
//
//            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
//                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, contact.getId())
//                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
//                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone.getText().toString())
//                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
//                    .build());
//
//            try {
//                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
//                Intent returnIntent = new Intent();
//                setResult(RESULT_OK, returnIntent);
//                finish();
//            }
//            catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }
}
