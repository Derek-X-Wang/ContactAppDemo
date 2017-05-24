package com.derekxw.contactlist.controllers;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.derekxw.contactlist.MainActivity;
import com.derekxw.contactlist.R;
import com.derekxw.contactlist.models.Contact;
import com.derekxw.contactlist.models.ContactDao;
import com.derekxw.contactlist.utils.Utilities;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 *
 * Created by Derek on 5/16/2017.
 */

public class ContactEditableFragment extends Fragment {

    @BindView(R.id.editable_first_name)
    MaterialEditText editTextFirstName;
    @BindView(R.id.editable_last_name)
    MaterialEditText editTextLastName;
    @BindView(R.id.editable_phone)
    MaterialEditText editTextPhone;
    @BindView(R.id.editable_dob)
    MaterialEditText editTextDOB;
    @BindView(R.id.editable_zip_code)
    MaterialEditText editTextZipCode;
    @BindView(R.id.fab_contact_editable)
    FloatingActionButton fab;
    @BindView(R.id.editable_delete_button)
    Button button;

    private ContactDao dao = null;
    private Contact contact = null;
    private Date dob = null;

    public ContactEditableFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int id = getArguments().getInt(MainActivity.CONTACT_REFERENCE);
            dao = new ContactDao(getActivity());
            if (id == -1) {
                // new contact
                contact = null;
            } else {
                contact = dao.get(id);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contact_editable, container, false);
        ButterKnife.bind(this, v);
        setViews();

        return v;
    }

    private void setViews() {
        // set text if there is contact data
        if (contact != null) {
            editTextFirstName.setText(contact.getFirstName());
            editTextLastName.setText(contact.getLastName());
            editTextPhone.setText(contact.getPhone());
            editTextDOB.setText(Utilities.dateToString(contact.getDob()));
            editTextZipCode.setText(contact.getZipCode());
            dob = contact.getDob();
        }

        // fab, save data
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInputs()) {
                    saveChanges();
                    ((MainActivity)getActivity()).startFragment(R.id.fragment_list, -1);
                }
            }
        });

        editTextDOB.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) return;
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                Calendar cal = Calendar.getInstance();
                                cal.set(year, monthOfYear, dayOfMonth);
                                dob = cal.getTime();
                                editTextDOB.setText(Utilities.dateToString(dob));
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });

        if (contact != null) {
            // show delete button when updating
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteContact(contact.getId());
                    ((MainActivity)getActivity()).startFragment(R.id.fragment_list, -1);
                }
            });
        } else {
            button.setVisibility(View.GONE);
        }
    }

    private void deleteContact(int id) {
        dao.delete(id);
    }

    private void saveChanges() {
        if (contact != null) {
            saveEditViewData(contact);
            dao.update(contact);
        } else {
            contact = new Contact();
            saveEditViewData(contact);
            dao.add(contact);
        }
    }

    private void saveEditViewData(Contact c) {
        c.setFirstName(editTextFirstName.getText().toString());
        c.setLastName(editTextLastName.getText().toString());
        c.setPhone(editTextPhone.getText().toString());
        c.setDob(dob);
        c.setZipCode(editTextZipCode.getText().toString());
    }

    private boolean validateInputs() {
        try {
            noEmptyValidation(editTextFirstName, "First Name");
            alphabetValidation(editTextFirstName);
            noEmptyValidation(editTextLastName, "Last Name");
            alphabetValidation(editTextLastName);
            noEmptyValidation(editTextPhone, "Phone");
            dateValidation(dob, editTextDOB, "Birthday");
            noEmptyValidation(editTextZipCode, "Zip Code");
        } catch (Exception e) {
            new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Wait....")
                    .setContentText(e.getMessage())
                    .setConfirmText("Okay, I'll fix it!")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismiss();
                        }
                    })
                    .show();
            return false;
        }
        return true;
    }

    private void dateValidation(Date date, EditText editText, String name) throws Exception {
        if (date == null) {
            ((MaterialEditText)editText).setError("required!");
            throw new Exception(name+" is not set!");
        }
    }

    private void noEmptyValidation(EditText editText, String name) throws Exception {
        String s = editText.getText().toString();
        if (s.length() == 0) {
            ((MaterialEditText)editText).setError("cannot be empty!");
            throw new Exception(name+" cannot be empty!");
        }
    }

    private void alphabetValidation(EditText editText) throws Exception {
        String s = editText.getText().toString();
        boolean hasNonAlpha = s.matches("^.*[^a-zA-Z].*$");
        if (hasNonAlpha) {
            ((MaterialEditText)editText).setError("have to be alphabetic!");
            throw new Exception("only allow a-z, A-Z");
        }
    }

    private int getDimen(int id) {
        return (int)getResources().getDimension(id);
    }

}
