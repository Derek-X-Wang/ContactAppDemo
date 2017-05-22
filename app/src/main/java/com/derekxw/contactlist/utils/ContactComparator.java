package com.derekxw.contactlist.utils;

import com.derekxw.contactlist.models.Contact;

import java.util.Comparator;

/**
 *
 * Created by Derek on 5/22/2017.
 */

public class ContactComparator implements Comparator<Contact> {
    @Override
    public int compare(Contact contact, Contact t1) {
        String s1 = contact.getLastName() + contact.getFirstName();
        String s2 = t1.getLastName() + t1.getFirstName();
        return s1.compareToIgnoreCase(s2);
    }
}
