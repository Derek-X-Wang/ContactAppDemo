package com.derekxw.contactlist.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Comparator;
import java.util.Date;

/**
 * Using ORMLite
 * Created by Derek on 5/16/2017.
 */

@DatabaseTable(tableName = "tb_contact")
public class Contact {

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(columnName = "first_name")
    private String firstName;
    @DatabaseField(columnName = "last_name")
    private String lastName;
    @DatabaseField(columnName = "phone")
    private String phone;
    @DatabaseField(columnName = "date_of_birth")
    private Date dob;
    @DatabaseField(columnName = "zip_code")
    private String zipCode;

    public Contact() {
    }

    public Contact(String firstName, String lastName, String phone, Date dob, String zipCode) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.dob = dob;
        this.zipCode = zipCode;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

}
