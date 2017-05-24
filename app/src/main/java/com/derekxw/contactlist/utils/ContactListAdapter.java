package com.derekxw.contactlist.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.derekxw.contactlist.R;
import com.derekxw.contactlist.models.Contact;
import com.derekxw.contactlist.models.ContactDao;
import com.derekxw.contactlistview.ContactRecyclerView;
import com.derekxw.stickyheader.SectioningAdapter;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 *
 * Created by Derek on 5/20/2017.
 */

public class ContactListAdapter extends SectioningAdapter implements ContactRecyclerView.ContactListPositionGetter {

    private static final String TAG = "ContactListAdapter";
    private List<Section> mDataArray;
    private Activity host;
    private ContactListListener contactListListener;

    private Stack<Contact> pendingRemovalList; // only one undo

    public int undoSectionIndex = -1;
    public int undoSectionItemIndex = 0;
    public boolean newSection = false;

    public interface ContactListListener {
        void onItemClicked(int id);
    }

    public ContactListAdapter(Activity activity, List<Contact> dataArray) {
        host = activity;
        mDataArray = mapContactsToSections(dataArray);
        // init undo list
        pendingRemovalList = new Stack<>();
    }

    // find item index within section
    public int getPositionInSection(int index, int id) {
        Section section = mDataArray.get(index);
        return getItemInSectionIndex(id, section.people);
    }

    public void setData(List<Contact> dataArray) {
        mDataArray = mapContactsToSections(dataArray);
    }

    public void setContactListListener(ContactListListener contactListListener) {
        this.contactListListener = contactListListener;
    }

    // call by searchview, search name that match and reset mDataArray
    public void searchName(String name) {
        ContactDao dao = new ContactDao(host);
        List<Contact> contacts = dao.getAll();
        if (name.length()==0) {
            mDataArray = mapContactsToSections(contacts);
        }
        List<Contact> filteredContacts = new ArrayList<>();
        for (Contact contact : contacts) {
            if (contact.getFirstName().toLowerCase().contains(name.toLowerCase())
                    || contact.getLastName().toLowerCase().contains(name.toLowerCase()))
                filteredContacts.add(contact);
        }
        mDataArray = mapContactsToSections(filteredContacts);
    }

    // Transform the list(from database) to List<Section> and sorted in alphabetical order
    private List<Section> mapContactsToSections(List<Contact> dataArray) {
        List<Section> array = new ArrayList<>();
        // store sections in map
        Map<Character, Section> sections = new HashMap<>();
        for (Contact s : dataArray) {
            char c = s.getLastName().charAt(0);
            c = Character.toUpperCase(c);
            if (sections.containsKey(c)) {
                sections.get(c).add(s);
            } else {
                Section section = new Section(c);
                section.add(s);
                sections.put(c, section);
            }
        }
        // construct List<Section> from map
        for (int i = 0; i < 26; i++) {
            char c = (char) ('A' + i);
            if (sections.containsKey(c)) {
                Section section = sections.get(c);
                array.add(section);
            }
        }
        return array;
    }

    // delete contact in adapter and database, store in pendingRemovalList
    public void delete(int id, char c) {
        for (Section sec : mDataArray) {
            if (sec.alpha != c) continue;
            Contact removed = null;
            for (Contact contact : sec.people) {
                if (contact.getId() == id) {
                    removed = contact;
                    break;
                }
            }
            if (removed == null) return;
            pendingRemovalList.push(removed);
            sec.people.remove(removed);
            //Log.d(TAG, "delete: "+removed.getLastName());
            ContactDao dao = new ContactDao(host);
            dao.delete(id);
        }
    }

    // delete section
    public void deleteHeader(int index) {
        mDataArray.remove(index);
    }

    public void restoreLast() {
        if (pendingRemovalList.size() < 1) return;
        Contact contact = pendingRemovalList.pop();
        add(contact);
    }

    public void add(Contact c) {
        char first = c.getLastName().charAt(0);
        if (contain(first)) {
            // updating
            undoSectionIndex = getSectionIndex(first);
            Section section = mDataArray.get(undoSectionIndex);
            section.add(c);
            Collections.sort(section.people, new ContactComparator());
            undoSectionItemIndex = getItemInSectionIndex(c.getId(), section.people);
            newSection = false;
        } else {
            // new
            Section section = new Section(first);
            section.add(c);
            undoSectionItemIndex = 0;
            addSection(section);
            undoSectionIndex = getSectionIndex(first);
            newSection = true;
        }
        ContactDao dao = new ContactDao(host);
        dao.add(c);
    }

    private void addSection(Section s) {
        mDataArray.add(s);
        Collections.sort(mDataArray, new Comparator<Section>() {
            @Override
            public int compare(Section section, Section t1) {
                return section.alpha - t1.alpha;
            }
        });
    }

    // if section c existed
    private boolean contain(char c) {
        for (Section sec : mDataArray) {
            if (sec.alpha == c) {
                return true;
            }
        }
        return false;
    }

    public int getSectionIndex(char c) {
        for (int i = 0; i < mDataArray.size(); i++) {
            if (mDataArray.get(i).alpha == c) return i;
        }
        return 0;
    }

    public int getItemInSectionIndex(int id, List<Contact> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == id) return i;
        }
        return 0;
    }

    @Override
    public int getPositionWithFirstChar(char c) {
        int res = getAvailablePosition(c);
        if (res >= 0) return res;
        // next available downward
        for (int i = c - 'A' + 1; i < 26; i++) {
            res = getAvailablePosition((char)(i+'A'));
            if (res >= 0) return res;
        }
        // next available upward
        for (int i = c - 'A' - 1; i > 0; i--) {
            res = getAvailablePosition((char)(i+'A'));
            if (res >= 0) return res;
        }
        return 0;
    }

    // if section c existed, return position
    private int getAvailablePosition(char c) {
        int acc = 0;
        for (int i = 0; i < getNumberOfSections(); i++) {
            Section cur = mDataArray.get(i);
            if (cur.alpha == c)
                return i*2 + acc;
            else
                acc += cur.size();
        }
        return -1;
    }

    @Override
    public int getNumberOfSections() {
        return mDataArray.size();
    }

    @Override
    public int getNumberOfItemsInSection(int sectionIndex) {
        return mDataArray.get(sectionIndex).size();
    }

    @Override
    public boolean doesSectionHaveHeader(int sectionIndex) {
        return true;
    }

    @Override
    public boolean doesSectionHaveFooter(int sectionIndex) {
        return false;
    }

    @Override
    public ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int itemType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_contact_list, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent, int headerType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.header_contact_list, parent, false);
        return new HeaderViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindItemViewHolder(SectioningAdapter.ItemViewHolder viewHolder, int sectionIndex, int itemIndex, int itemType) {
        Section s = mDataArray.get(sectionIndex);
        Contact c = s.people.get(itemIndex);
        ItemViewHolder ivh = (ItemViewHolder) viewHolder;
        ivh.personFirstNameTextView.setText(c.getFirstName());
        ivh.personLastNameTextView.setText(c.getLastName());
        ivh.phone = c.getPhone();
        ivh.id = c.getId();
        final int id = ivh.id;
        ivh.mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contactListListener != null) contactListListener.onItemClicked(id);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindHeaderViewHolder(SectioningAdapter.HeaderViewHolder viewHolder, int sectionIndex, int headerType) {
        Section s = mDataArray.get(sectionIndex);
        HeaderViewHolder hvh = (HeaderViewHolder) viewHolder;
        // Log.d(TAG, "onBindHeaderViewHolder: "+s.alpha);
        hvh.titleTextView.setText(String.valueOf(s.alpha));
    }

    public class Section {
        char alpha;
        List<Contact> people;

        public Section(char alpha) {
            this.alpha = alpha;
            this.people = new ArrayList<>();
        }

        int size() {return people.size();}
        public void add(Contact s) {people.add(s);}
    }

    public class ItemViewHolder extends SectioningAdapter.ItemViewHolder {
        public FrameLayout mContainer;
        TextView personFirstNameTextView;
        TextView personLastNameTextView;
        public String phone;
        public int id;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mContainer = (FrameLayout) itemView.findViewById(R.id.item_container);
            personFirstNameTextView = (TextView) itemView.findViewById(R.id.item_contact_list_first);
            personLastNameTextView = (TextView) itemView.findViewById(R.id.item_contact_list_last);
        }
    }

    public class HeaderViewHolder extends SectioningAdapter.HeaderViewHolder {
        TextView titleTextView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.header_contact_list);
        }
    }
}
