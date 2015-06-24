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


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment implements MainActivity.OnFragmentDatasetChanged {

    private static ContactFragment instance = null;
    View view;
    Parcelable state;
    ContactAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    boolean loadContactDone = false, loadViewDone = false;
    private RecyclerView mRecyclerView;

    public ContactFragment() {
        // Required empty public constructor
    }

    public static Fragment getInstance() {
        if (instance == null) {
            instance = new ContactFragment();
        }
        return instance;
    }

    @Override
    public void onContactLoaded() {
        loadContactDone = true;
        if (loadViewDone) setContactIntoView();
    }

    @Override
    public void onPreLoad() {
        if (getView() != null) {
            getView().findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.contacts_list).setVisibility(View.GONE);
        }
    }

    @Override
    public void onFilter(String query) {
        if (!(loadContactDone && loadViewDone)) return;
        mAdapter.setContacts(MainActivity.contactFilter(GlobalObject.allContacts, query));
    }

    private void setContactIntoView() {
        if (!(loadContactDone && loadViewDone)) return;
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        mAdapter = new ContactAdapter(GlobalObject.allContacts, this.getActivity());
        //mRecyclerView.removeAllViews();
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.swapAdapter(mAdapter, false);
        //mRecyclerView.setAdapter(mAdapter);
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


}