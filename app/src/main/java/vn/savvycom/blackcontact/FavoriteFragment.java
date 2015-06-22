package vn.savvycom.blackcontact;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;

import vn.savvycom.blackcontact.Item.Contact;


/**
 * A simple {@link Fragment} subclass.
 */
public class FavoriteFragment extends Fragment implements MainActivity.OnFragmentDatasetChanged {

    private static FavoriteFragment instance = null;
    ArrayList<Contact> contacts = new ArrayList<>();
    ArrayList<Contact> allContacts = new ArrayList<>();
    ArrayList<Long> favorites = new ArrayList<>();
    View view;
    RecyclerView.LayoutManager mLayoutManager;
    Parcelable state;
    boolean loadContactDone = false;
    boolean loadViewDone = false;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    public FavoriteFragment() {
    }

    public static FavoriteFragment getInstance() {
        if (instance == null) {
            instance = new FavoriteFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        state = mLayoutManager.onSaveInstanceState();
    }

    @Override
    public void onResume() {
        if (state != null) {
            mLayoutManager.onRestoreInstanceState(state);
        }
        super.onResume();
    }

    @Override
    public void onContactLoaded(final ArrayList<Contact> allContacts) {
        this.allContacts = allContacts;
        loadContactDone = true;
        if (loadViewDone) setContactIntoView();
    }

    @Override
    public void onPreLoad() {

    }

    private void setContactIntoView() {
        if (!(loadContactDone && loadViewDone)) return;
        contacts = new ArrayList<>();
        favorites = DatabaseController.getInstance(getActivity()).loadFavorites();
        for (Contact c : allContacts) {
            if (favorites.contains(Long.parseLong(c.getId()))) {
                contacts.add(c);
            }
        }
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        mAdapter = new ContactAdapter(contacts);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_contact, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.contacts_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(getActivity(), ContactDetailActivity.class);
                        intent.putExtra(ContactDetailActivity.EXTRA_CONTACT, contacts.get(position));
                        startActivity(intent);
                    }
                })
        );
        loadViewDone = true;
        if (loadContactDone) setContactIntoView();
        return view;
    }

    public void onFavoriteAdded(Contact contact) {
        contacts.add(contact);
        mAdapter.notifyDataSetChanged();
    }

    public void onFavoriteRemoved(Contact contact) {
        Contact removedContact = null;
        for (Contact c : contacts) {
            if (contact.getId().equals(c.getId())) {
                removedContact = c;
                break;
            }
        }
        if (removedContact != null) {
            contacts.remove(removedContact);
        }
        mAdapter.notifyDataSetChanged();
    }
}
