package vn.savvycom.blackcontact;


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
    ArrayList<Contact> favoriteContacts = new ArrayList<>();
    ArrayList<Long> favoriteIds = new ArrayList<>();
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
    public void onContactLoaded() {
        loadContactDone = true;
        if (loadViewDone) setContactIntoView();
    }

    @Override
    public void onPreLoad() {

    }

    private void setContactIntoView() {
        if (!(loadContactDone && loadViewDone)) return;
        favoriteContacts = new ArrayList<>();
        favoriteIds = DatabaseController.getInstance(getActivity()).loadFavorites();
        for (Contact c : GlobalObject.allContacts) {
            if (favoriteIds.contains(Long.parseLong(c.getId()))) {
                favoriteContacts.add(c);
            }
        }
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        mAdapter = new ContactAdapter(favoriteContacts, this.getActivity());
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
        loadViewDone = true;
        if (loadContactDone) setContactIntoView();
        return view;
    }

    public void onFavoriteAdded(Contact contact) {
        favoriteContacts.add(contact);
        mAdapter.notifyDataSetChanged();
    }

    public void onFavoriteRemoved(Contact contact) {
        Contact removedContact = null;
        for (Contact c : favoriteContacts) {
            if (contact.getId().equals(c.getId())) {
                removedContact = c;
                break;
            }
        }
        if (removedContact != null) {
            favoriteContacts.remove(removedContact);
        }
        mAdapter.notifyDataSetChanged();
    }
}
