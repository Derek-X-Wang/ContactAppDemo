package com.derekxw.contactlist.controllers;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.derekxw.contactlist.MainActivity;
import com.derekxw.contactlist.R;
import com.derekxw.contactlist.models.Contact;
import com.derekxw.contactlist.models.ContactDao;
import com.derekxw.contactlist.utils.ContactListAdapter;
import com.derekxw.contactlist.utils.FakeDataGenerator;
import com.derekxw.contactlist.utils.SimpleItemTouchHelperCallback;
import com.derekxw.contactlistview.ContactRecyclerView;
import com.derekxw.stickyheader.StickyHeaderLayoutManager;
import com.lapism.searchview.SearchView;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.derekxw.contactlist.MainActivity.HAS_INIT;
import static com.derekxw.contactlist.MainActivity.HAS_INIT_ROUTINE;

/**
 * Contact List
 * Created by Derek on 5/16/2017.
 */

public class ContactListFragment extends Fragment {

    private static final String TAG = "ContactListFragment";
    @BindView(R.id.searchView)
    SearchView mSearchView = null;
    @BindView(R.id.fab_contact_list)
    FloatingActionButton mFab = null;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.contact_alphabet_view)
    ContactRecyclerView mContactRecyclerView;
    @BindView(R.id.app_bar)
    AppBarLayout appBarLayout;

    private ContactDao dao;
    private ContactListAdapter adapter;

    public ContactListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dao = new ContactDao(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contact_list, container, false);
        ButterKnife.bind(this, v);
        setSearchView();
        setFab();
        setContactListView(v);
        setContactAlphabetView();
        // Only run once
        if (!Prefs.getBoolean(HAS_INIT, false)) {
            Prefs.putBoolean(HAS_INIT, true);
            long size = dao.count();
            // if there is no contact, add mine
            if (size < 1) {
                dao.add(new Contact("Derek", "Wang", "6263543629", new Date(), "91731"));
                adapter.setData(dao.getAll());
                adapter.notifyAllSectionsDataSetChanged();
            }
        }
        // Only run once, every time when app opens
        if (!Prefs.getBoolean(HAS_INIT_ROUTINE, false)) {
            Prefs.putBoolean(HAS_INIT_ROUTINE, true);
            setup();
        }
        // fix for api 21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "onCreateView: api");
            appBarLayout.setStateListAnimator(null);
        }
        return v;
    }

    protected void setToolbar(View v) {
        Toolbar mToolbar = (Toolbar) v.findViewById(R.id.toolbar);
        if (mToolbar != null) {
            mToolbar.setNavigationContentDescription(getResources().getString(R.string.app_name));
        }
    }

    private void setup() {
        long size = dao.count();
        // if need fake contacts
        if (size < 5) {
            new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("No friend :(")
                    .setContentText("Do you want to generate some fake friends?")
                    .setConfirmText("Sure!")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            FakeDataGenerator.generateFakeData(dao);
                            adapter.setData(dao.getAll());
                            adapter.notifyAllSectionsDataSetChanged();
                            showHint(sDialog);
                        }
                    })
                    .setCancelText("Leave me alone")
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            showHint(sweetAlertDialog);
                        }
                    })
                    .show();
        }
    }

    private void showHint(SweetAlertDialog dialog) {
        dialog.setTitleText("By the way~")
                .setContentText("You can swipe to call or delete.")
                .setConfirmText("Cool")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    sDialog.dismiss();
                }
            })
                .setCancelText("Cancel")
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                    }
                })
                .show();
    }

    private void setContactListView(final View v) {
        adapter = new ContactListAdapter(getActivity(), dao.getAll());
        // go to detail page
        adapter.setContactListListener(new ContactListAdapter.ContactListListener() {
            @Override
            public void onItemClicked(int id) {
                ((MainActivity)getActivity()).startFragment(R.id.fragment_detail, id);
            }
        });
        mRecyclerView.setLayoutManager(new StickyHeaderLayoutManager());
        mRecyclerView.setAdapter(adapter);
        SimpleItemTouchHelperCallback simpleItemTouchCallback = new SimpleItemTouchHelperCallback(
                getActivity(), mRecyclerView, 0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        // undo callback
        simpleItemTouchCallback.setEventListener(new SimpleItemTouchHelperCallback.SwipeEventListener() {
            @Override
            public void onItemRemoved() {
                Snackbar.make(v, "1 item removed", Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                undoDelete();
                            }
                        }).show();
            }
        });
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void undoDelete() {
        adapter.restoreLast();
        if (adapter.newSection) {
            adapter.notifyAllSectionsDataSetChanged();
        } else {
            adapter.notifySectionItemInserted(adapter.undoSectionIndex, adapter.undoSectionItemIndex);
        }
    }

    private void setContactAlphabetView() {
        mContactRecyclerView.setRecyclerView(mRecyclerView);
        mContactRecyclerView.setUpAlphabet();
    }

    private void setFab() {
        if (mFab != null) {
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity)getActivity()).startFragment(R.id.fragment_editable, -1);
                }
            });
        }
    }

    protected void setSearchView() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.close(false);
                // Log.d(TAG, "onQueryTextSubmit: "+query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.searchName(newText);
                adapter.notifyAllSectionsDataSetChanged();
                // Log.d(TAG, "onQueryTextChange: "+newText);
                return true;
            }
        });
        mSearchView.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
            @Override
            public boolean onOpen() {
                mFab.hide();
                return true;
            }

            @Override
            public boolean onClose() {
                mFab.show();
                return true;
            }
        });
    }
}
