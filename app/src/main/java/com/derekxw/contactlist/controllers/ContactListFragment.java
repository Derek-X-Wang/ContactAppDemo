package com.derekxw.contactlist.controllers;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.derekxw.contactlist.MainActivity;
import com.derekxw.contactlist.R;
import com.derekxw.contactlist.models.Contact;
import com.derekxw.contactlist.models.ContactDao;
import com.derekxw.contactlist.utils.ContactListAdapter;
import com.derekxw.contactlist.utils.DatabaseHelper;
import com.derekxw.contactlist.utils.SimpleItemTouchHelperCallback;
import com.derekxw.contactlist.utils.SwipeableExampleAdapter;
import com.derekxw.contactlist.utils.StickyHeaderLayoutManager;
import com.derekxw.contactlistview.ContactRecyclerView;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.lapism.searchview.SearchAdapter;
import com.lapism.searchview.SearchHistoryTable;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

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

    private Toolbar mToolbar = null;
    private SearchHistoryTable mHistoryDatabase;

    private ContactDao dao;
    private ContactListAdapter adapter;

    public ContactListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//         DatabaseHelper helper = DatabaseHelper.getHelper(getActivity());
//         helper.clearTable(Contact.class);
        dao = new ContactDao(getActivity());
    }

    protected String getSaltString(int leng) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        SALTCHARS = SALTCHARS.toLowerCase();
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < leng) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contact_list, container, false);
        ButterKnife.bind(this, v);
//        setToolbar(v);
        setSearchView();
        setFab();
        setContactListView(v);
        setContactAlphabetView();
        return v;
    }

    private void setContactListView(final View v) {
        adapter = new ContactListAdapter(getActivity(), dao.getAll());
        mRecyclerView.setLayoutManager(new org.zakariya.stickyheaders.StickyHeaderLayoutManager());
        mRecyclerView.setAdapter(adapter);
        SimpleItemTouchHelperCallback simpleItemTouchCallback = new SimpleItemTouchHelperCallback(
                getActivity(), adapter, 0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
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
//            adapter.notifySectionItemInserted(adapter.undoSectionIndex, adapter.undoSectionItemIndex);
//            adapter.notifySectionInserted(adapter.undoSectionIndex);
            adapter.notifyAllSectionsDataSetChanged();
        } else {
            adapter.notifySectionItemInserted(adapter.undoSectionIndex, adapter.undoSectionItemIndex);
        }
    }

    private void setContactAlphabetView() {
        mContactRecyclerView.setRecyclerView(mRecyclerView);
        mContactRecyclerView.setUpAlphabet();
    }

    protected void setToolbar(View v) {
//        mToolbar = (Toolbar) v.findViewById(R.id.toolbar);
//        if (mToolbar != null) {
//            mToolbar.setNavigationContentDescription(getResources().getString(R.string.app_name));
//            ((MainActivity)getActivity()).getDelegate().setSupportActionBar(mToolbar);
//        }
    }

    private void setFab() {
        if (mFab != null) {
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Contact contact = new Contact("Derek", "Wang", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Ai", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Allenu", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Bananna", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Ban", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Clear", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Chnang", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Chen", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Dao", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Dang", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Eillen", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Ennn", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Finn", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Fang", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Gao", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Gggggg", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Hao", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Hello", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Iiie", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Izz", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Jay", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Joy", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Kotlin", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Leong", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Mao", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Manen", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Neeen", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Opppos", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Peey", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Qi", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Qin", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Rasd", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Rss", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Seed", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Teacher", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Unive", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Victor", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Wong", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Xiong", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Yang", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Yyang", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Zhao", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    contact = new Contact("Derek", "Zheng", "6263543629", new Date(), "93117");
                    dao.add(contact);
                    //mRecyclerView.getAdapter().notifyDataSetChanged();
                    Snackbar.make(v, "Search history deleted.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            });
        }
    }

    protected void setSearchView() {
        mHistoryDatabase = new SearchHistoryTable(getActivity());
        if (mSearchView != null) {
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    mSearchView.close(false);
                    Log.d(TAG, "onQueryTextSubmit: "+query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.searchName(newText);
                    adapter.notifyAllSectionsDataSetChanged();
                    Log.d(TAG, "onQueryTextChange: "+newText);
                    return true;
                }
            });
            mSearchView.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
                @Override
                public boolean onOpen() {
                    if (mFab != null) {
                        mFab.hide();
                    }
                    return true;
                }

                @Override
                public boolean onClose() {
                    if (mFab != null) {
                        mFab.show();
                    }
                    return true;
                }
            });

        }
    }
}
