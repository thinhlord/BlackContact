package vn.savvycom.blackcontact;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
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

            ListView phoneList = (ListView) findViewById(R.id.phone_list);
            setListViewHeightBasedOnChildren(phoneList);
            phoneList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contact.getPhone()));

            ListView mailList = (ListView) findViewById(R.id.mail_list);
            setListViewHeightBasedOnChildren(mailList);
            mailList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contact.getMail()));

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

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem instanceof ViewGroup) {
                listItem.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
            }
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
