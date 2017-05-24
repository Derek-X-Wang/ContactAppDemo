package com.derekxw.contactlist.models;

import android.content.Context;

import com.derekxw.contactlist.utils.DatabaseHelper;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by Derek on 5/16/2017.
 */

public class ContactDao {

    private Context context;
    private Dao<Contact, Integer> contactDao;
    private DatabaseHelper helper;

    public ContactDao(Context context) {
        this.context = context;
        try {
            helper = DatabaseHelper.getHelper(context);
            contactDao = helper.getDao(Contact.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add new contact
     * @param contact contact object
     */
    public void add(Contact contact) {
        try {
            contactDao.create(contact);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * get Contact by id
     * @param id contact id
     * @return Contact obj
     */
    public Contact get(int id)
    {
        Contact contact = null;
        try {
            contact = contactDao.queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contact;
    }

    /**
     * get call contacts
     * @return all contacts
     */
    public List<Contact> getAll()
    {
        try {
            return contactDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void delete(int id) {
        try {
            contactDao.deleteById(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Contact contact) {
        try {
            contactDao.update(contact);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long count() {
        try {
            return contactDao.countOf();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
