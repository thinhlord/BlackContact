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
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment implements MainActivity.OnFragmentDatasetChanged {

    ArrayList<Contact> contacts = new ArrayList<>();
    View view;
    private RecyclerView mRecyclerView;
    Parcelable state;
    RecyclerView.LayoutManager mLayoutManager;

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onContactLoaded(ArrayList<Contact> newContacts) {
        contacts = newContacts;
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        RecyclerView.Adapter mAdapter = new ContactAdapter(contacts);
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
                        Toast.makeText(getActivity(), contacts.get(position).getAccountType(), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getActivity(), ContactDetailActivity.class);
                        intent.putExtra(ContactDetailActivity.EXTRA_CONTACT, contacts.get(position));
                        startActivity(intent);
                    }
                })
        );
        return view;
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

    private static ContactFragment instance = null;

    public static Fragment getInstance() {
        if (instance == null) {
            instance = new ContactFragment();
        }
        return instance;
    }


}
