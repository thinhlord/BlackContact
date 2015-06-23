package vn.savvycom.blackcontact;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Iterator;

import vn.savvycom.blackcontact.Item.Contact;


public class ContactDetailActivity extends BaseActivity implements View.OnClickListener{
    private Contact contact;
    private boolean favorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableHomeButton();
        contact = getIntent().getParcelableExtra(GlobalObject.EXTRA_CONTACT);
        if (contact == null) {
            this.finish();
        } else {
            setTitle(contact.getName());
            ImageView imageView = (ImageView) findViewById(R.id.photo);
            if (contact.getPhoto() != null) {
                imageView.setImageURI(contact.getPhoto());
            } else {
                imageView.setImageResource(R.mipmap.ic_launcher);
            }
            ((TextView) findViewById(R.id.name)).setText(contact.getName());
            favorite = DatabaseController.getInstance(this).isFavorite(contact.getId());
            ImageButton call = (ImageButton) findViewById(R.id.button_call);
            ImageButton text = (ImageButton) findViewById(R.id.button_text);
            call.setOnClickListener(this);
            text.setOnClickListener(this);

            LinearLayout phoneList = (LinearLayout) findViewById(R.id.phone_list);
            if (contact.getPhone().size() != 0) {
                for (int i = 0; i < contact.getPhone().size(); i++) {
                    addPhoneView(contact.getPhone().get(i), contact.getPhoneType().get(i), phoneList);
                }
            } else {
                addPhoneView("None", null, phoneList);
            }

            LinearLayout mailList = (LinearLayout) findViewById(R.id.mail_list);
            if (contact.getMail().size() != 0) {
                for (int i = 0; i < contact.getMail().size(); i++) {
                    addMailView(contact.getMail().get(i), contact.getMailType().get(i), mailList);
                }
            } else {
                addMailView("None", null, mailList);
            }

            ImageView typeImage = (ImageView) findViewById(R.id.place);
            if (contact.getAccountType() != null) {
                if (contact.getAccountType().contains("sim"))
                    typeImage.setImageResource(R.drawable.ic_sim_card);
                else if (contact.getAccountType().contains("google"))
                    typeImage.setImageResource(R.drawable.icon_google);
                else typeImage.setImageResource(R.drawable.ic_phone);
            }
            else typeImage.setImageResource(R.drawable.ic_phone);
        }
    }

    private void addPhoneView(final String phone, String type, LinearLayout parent) {
        View newPhoneView = LayoutInflater.from(this).inflate(R.layout.detail_phone_number_layout, null);
        if (phone.equals("None")) {
            ((TextView) newPhoneView.findViewById(R.id.phone_type)).setText(phone);
        } else {
            int typeInt = Integer.parseInt(type);
            String typeStr;
            switch (typeInt) {
                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                    typeStr = "Home";
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                    typeStr = "Mobile";
                    break;
                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                    typeStr = "Work";
                    break;
                default:
                    typeStr = "Other";
                    break;
            }
            ((TextView) newPhoneView.findViewById(R.id.phone_type)).setText(typeStr);
            ((TextView) newPhoneView.findViewById(R.id.phone_number)).setText(phone);
            ((ImageView) newPhoneView.findViewById(R.id.icon)).setImageResource(R.drawable.ic_call_grey);
            newPhoneView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    call(phone);
                }
            });

        }
        parent.addView(newPhoneView);
    }

    private void addMailView(final String mail, String type, LinearLayout parent) {
        View newPhoneView = LayoutInflater.from(this).inflate(R.layout.detail_phone_number_layout, null);
        if (mail.equals("None")) {
            ((TextView) newPhoneView.findViewById(R.id.phone_type)).setText(mail);
        } else {
            int typeInt = Integer.parseInt(type);
            String typeStr;
            switch (typeInt) {
                case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
                    typeStr = "Home";
                    break;
                case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE:
                    typeStr = "Mobile";
                    break;
                case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
                    typeStr = "Work";
                    break;
                default:
                    typeStr = "Other";
                    break;
            }
            ((TextView) newPhoneView.findViewById(R.id.phone_type)).setText(typeStr);
            ((TextView) newPhoneView.findViewById(R.id.phone_number)).setText(mail);
            ((ImageView) newPhoneView.findViewById(R.id.icon)).setImageResource(R.drawable.ic_mail_grey);
            newPhoneView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mailTo(mail);
                }
            });
        }
        parent.addView(newPhoneView);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_contact_detail;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_favorite);
        if (favorite) item.setIcon(R.drawable.ic_star_yellow);
        else item.setIcon(R.drawable.ic_star_white);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_favorite:
                favorite = DatabaseController.getInstance(this).toggleFavorite(contact.getId());
                if (favorite) FavoriteFragment.getInstance().onFavoriteAdded(contact);
                else FavoriteFragment.getInstance().onFavoriteRemoved(contact);
                invalidateOptionsMenu();
                Toast.makeText(this, "Added " + contact.getName() + " to favorite", Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_edit:
                Intent intent = new Intent(this, ContactEditorActivity.class);
                intent.putExtra(GlobalObject.EXTRA_CONTACT, contact);
                startActivityForResult(intent, 2);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_call:
                call(null);
                break;
            case R.id.button_text:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", contact.getPhone().get(0), null)));
                break;
        }
    }

    private void call(String phone) {
        String uri;
        if (phone == null) uri = "tel:" + contact.getPhone().get(0).trim();
        else uri = "tel:" + phone.trim();
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(uri));
        startActivity(intent);
    }

    private void mailTo(String mail) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", mail, null));
        ;
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                if (data.getExtras().getBoolean(GlobalObject.EXTRA_MERGE)) {
                    new MaterialDialog.Builder(this)
                            .content("There is a contact with the same name as your edited one and they were merged.")
                            .positiveText("OK")
                            .dismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    ContactDetailActivity.this.finish();
                                }
                            })
                            .show();
                } else if (data.getExtras().getBoolean(GlobalObject.EXTRA_DELETE)) {
                    ContactDetailActivity.this.finish();
                } else {
                    Contact newContact = data.getExtras().getParcelable(GlobalObject.EXTRA_CONTACT);
                    Intent intent = new Intent(this, ContactDetailActivity.class);
                    intent.putExtra(GlobalObject.EXTRA_CONTACT, newContact);
                    startActivity(intent);
                    this.finish();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
