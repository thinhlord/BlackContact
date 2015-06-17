package vn.savvycom.blackcontact;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
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

import java.util.ArrayList;


public class ContactEditorActivity extends BaseActivity implements View.OnClickListener {
    public static final String EXTRA_CONTACT = "CONTACT";
    ArrayAdapter<CharSequence> phoneTypeAdapter;
    private Contact contact;
    private boolean add = true;
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

        contact = getIntent().getParcelableExtra(EXTRA_CONTACT);
        if (contact == null) {
            setTitle("Add new contact");
            addPhoneView(null);
            add = true;
        } else {
            add = false;
            Toast.makeText(this, "" + contact.getId(), Toast.LENGTH_LONG).show();
            setTitle("Edit");
            ImageView imageView = (ImageView) findViewById(R.id.photo);
            if (contact.getPhoto() != null) {
                imageView.setImageBitmap(contact.getPhoto());
            } else {
                imageView.setImageResource(R.mipmap.ic_launcher);
            }
            ((TextView) findViewById(R.id.name)).setText(contact.getName());
            for (String phoneNumber : contact.getPhone()) {
                addPhoneView(phoneNumber);
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
        if (name.getText().toString().equals("")) {
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

            // phones
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
                Log.d("PHONE", phoneNumber + " " + phoneType);
            }

            // mails
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
                Log.d("MAIL", email + " " + mailType);
            }

            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_phone:
                addPhoneView(null);
                break;
            case R.id.add_mail:
                addEmailView(null);
                break;
        }
    }

    private void addPhoneView(String phone) {
        if (phoneGroupLayout.getChildCount() >= 6) return;
        final View newPhoneView = LayoutInflater.from(this).inflate(R.layout.phone_number_child_layout, null);
        Spinner spinner = (Spinner) newPhoneView.findViewById(R.id.phone_type);
        spinner.setAdapter(phoneTypeAdapter);
        if (phone != null) {
            EditText phoneView = (EditText) newPhoneView.findViewById(R.id.phone_number);
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

    private void addEmailView(String mailAddress) {
        if (mailGroupLayout.getChildCount() >= 6) return;
        final View newMailView = LayoutInflater.from(this).inflate(R.layout.email_child_layout, null);
        Spinner spinner = (Spinner) newMailView.findViewById(R.id.mail_type);
        spinner.setAdapter(phoneTypeAdapter);
        if (mailAddress != null) {
            EditText email = (EditText) newMailView.findViewById(R.id.email);
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
}
