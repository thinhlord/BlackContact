package vn.savvycom.blackcontact;

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


public class ContactDetailActivity extends BaseActivity implements View.OnClickListener{
    public static final String EXTRA_CONTACT = "CONTACT";
    private Contact contact;
    private boolean favorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableHomeButton();
        contact = getIntent().getParcelableExtra(EXTRA_CONTACT);
        if (contact == null) {
            this.finish();
        } else {
            setTitle(contact.getName());
            ImageView imageView = (ImageView) findViewById(R.id.photo);
            if (contact.getPhoto() != null) {
                imageView.setImageBitmap(contact.getPhoto());
            } else {
                imageView.setImageResource(R.mipmap.ic_launcher);
            }
            ((TextView) findViewById(R.id.name)).setText(contact.getName());

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
            if (contact.getAccountType().contains("sim")) typeImage.setImageResource(R.drawable.ic_sim_card);
            else if (contact.getAccountType().contains("google")) typeImage.setImageResource(R.drawable.icon_google);
            else typeImage.setImageResource(R.drawable.ic_phone);
            favorite = DatabaseController.getInstance(this).isFavorite(contact.getId());
            ImageButton call = (ImageButton) findViewById(R.id.button_call);
            ImageButton text = (ImageButton) findViewById(R.id.button_text);
            call.setOnClickListener(this);
            text.setOnClickListener(this);
        }
    }

    private void addPhoneView(String phone, String type, LinearLayout parent) {
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
        }
        parent.addView(newPhoneView);
    }

    private void addMailView(String mail, String type, LinearLayout parent) {
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
                return true;
            case R.id.action_edit:
                Intent intent = new Intent(this, ContactEditorActivity.class);
                intent.putExtra(ContactEditorActivity.EXTRA_CONTACT, contact);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_call:
                String uri = "tel:" + contact.getPhone().get(0).trim() ;
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
                break;
            case R.id.button_text:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", contact.getPhone().get(0), null)));
                break;
        }
    }
}
