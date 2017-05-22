package com.derekxw.contactlist.utils;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.derekxw.contactlist.R;
import com.derekxw.contactlistview.ContactRecyclerView;

import java.util.List;


/**
 * https://github.com/viethoa/recyclerview-alphabet-fast-scroller-android
 * Created by Derek on 5/17/2017.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>
        implements ContactRecyclerView.BubbleTextGetter, ContactRecyclerView.ContactListPositionGetter {

    private List<String> mDataArray;

    public RecyclerViewAdapter(List<String> dataset) {
        mDataArray = dataset;
    }

    @Override
    public int getPositionWithFirstChar(char c) {
        for (int i = 0; i < getItemCount(); i++) {
            String cur = mDataArray.get(i);
            if (cur != null && cur.length() > 0)
                if (cur.charAt(0) == c)
                    return i;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        if (mDataArray == null)
            return 0;
        return mDataArray.size();
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_list, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(mDataArray.get(position));
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        if (pos < 0 || pos >= mDataArray.size())
            return null;

        String name = mDataArray.get(pos);
        if (name == null || name.length() < 1)
            return null;

        return mDataArray.get(pos).substring(0, 1);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.item_contact_list_last);
        }
    }

    public static class HeaderHolder extends RecyclerView.ViewHolder {
        TextView header;

        public HeaderHolder(View itemView) {
            super(itemView);
            header = (TextView) itemView.findViewById(R.id.header_contact_list);
        }
    }

}
