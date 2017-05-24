package com.derekxw.contactlist.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import com.derekxw.contactlist.R;
import com.derekxw.contactlistview.ContactRecyclerView;

/**
 * based off https://github.com/nemanja-kovacevic/recycler-view-swipe-to-delete
 * Created by Derek on 5/21/2017.
 */

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.SimpleCallback {

    private static final String TAG = "SimpleItemTouchHelper";
    // we want to cache these and not allocate anything repeatedly in the onChildDraw method
    private boolean initiated;
    private Drawable backgroundCall;
    private Drawable backgroundDelete;
    private Activity host;
    private RecyclerView recyclerView;
    private ContactListAdapter adapter;
    private SwipeEventListener eventListener;

    public interface SwipeEventListener {
        void onItemRemoved();
    }

    public void setEventListener(SwipeEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public SimpleItemTouchHelperCallback(Activity activity, RecyclerView recyclerView, int dragDirs, int swipeDirs) {
        super(dragDirs, swipeDirs);
        this.host = activity;
        this.recyclerView = recyclerView;
        this.adapter = (ContactListAdapter) recyclerView.getAdapter();
    }

    private void init() {
        backgroundCall = ContextCompat.getDrawable(host, R.drawable.bg_swipe_item_right);
        backgroundDelete = ContextCompat.getDrawable(host, R.drawable.bg_swipe_item_left);;
        initiated = true;
    }

    // not important, we don't want drag & drop
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // Headers are not swipeable
        if (viewHolder instanceof ContactListAdapter.HeaderViewHolder) return 0;
        return super.getSwipeDirs(recyclerView, viewHolder);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
        ContactListAdapter.ItemViewHolder ivh = (ContactListAdapter.ItemViewHolder) viewHolder;
        switch (swipeDir) {
            case ItemTouchHelper.LEFT:
                // delete
                char firstChar = ivh.personLastNameTextView.getText().toString().charAt(0);
                // make sure it is uppercase
                firstChar = Character.toUpperCase(firstChar);
                int sectionIndex = adapter.getSectionIndex(firstChar);
                int itemIndexInSection = adapter.getPositionInSection(sectionIndex, ivh.id);
                adapter.delete(ivh.id, firstChar);
                // ivh.getPositionInSection() may return wrong index
                recyclerView.getRecycledViewPool().clear();
                adapter.notifySectionItemRemoved(sectionIndex, itemIndexInSection);
                if (adapter.getNumberOfItemsInSection(sectionIndex) == 0) {
                    // section is empty, remove header
                    adapter.deleteHeader(sectionIndex);
                    adapter.notifySectionRemoved(sectionIndex);
                }
                eventListener.onItemRemoved();
                break;
            case ItemTouchHelper.RIGHT:
                // make a phone call
                String number = ivh.phone;
                Uri call = Uri.parse("tel:" + number);
                Intent surf = new Intent(Intent.ACTION_DIAL, call);
                host.startActivity(surf);
                adapter.notifyAllSectionsDataSetChanged();
                break;
            default:
        }
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;

        // not sure why, but this method get's called for viewholder that are already swiped away
        if (viewHolder.getAdapterPosition() == -1) {
            // not interested in those
            return;
        }

        if (!initiated) {
            init();
        }

        if (dX < 0) {
            // left
            backgroundDelete.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
            backgroundDelete.draw(c);
        } else {
            // right
            backgroundCall.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + (int) dX, itemView.getBottom());
            backgroundCall.draw(c);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
