package com.derekxw.contactlist.controllers;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.derekxw.contactlist.MainActivity;
import com.derekxw.contactlist.R;
import com.derekxw.contactlist.models.Contact;
import com.derekxw.contactlist.models.ContactDao;
import com.derekxw.contactlist.utils.Utilities;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Contact Detail
 * Created by Derek on 5/16/2017.
 */

public class ContactDetailFragment extends Fragment {

    @BindView(R.id.text_cap_image)
    ImageView imageCapText;
    @BindView(R.id.detail_first_name)
    MaterialEditText editTextFirstName;
    @BindView(R.id.detail_last_name)
    MaterialEditText editTextLastName;
    @BindView(R.id.detail_phone)
    MaterialEditText editTextPhone;
    @BindView(R.id.detail_dob)
    MaterialEditText editTextDOB;
    @BindView(R.id.detail_zip_code)
    MaterialEditText editTextZipCode;
    @BindView(R.id.fab_contact_detail)
    FloatingActionButton fab;

    private ContactDao dao = null;
    private Contact contact = null;

    public ContactDetailFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int id = getArguments().getInt(MainActivity.CONTACT_REFERENCE);
            dao = new ContactDao(getActivity());
            contact = dao.get(id);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contact_detail, container, false);
        ButterKnife.bind(this, v);
        if(dao != null) {
            setViews();
        }
        return v;
    }

    private void setViews() {
        editTextFirstName.setText(contact.getFirstName());
        editTextLastName.setText(contact.getLastName());
        editTextPhone.setText(contact.getPhone());
        editTextDOB.setText(Utilities.dateToString(contact.getDob()));
        editTextZipCode.setText(contact.getZipCode());

        editTextFirstName.setEnabled(false);
        editTextLastName.setEnabled(false);
        editTextPhone.setEnabled(false);
        editTextDOB.setEnabled(false);
        editTextZipCode.setEnabled(false);

        String cap = contact.getLastName().substring(0,1) + contact.getFirstName().substring(0,1);
        cap = cap.toUpperCase();
        TextDrawable drawable = TextDrawable.builder()
                .buildRound(cap, Utilities.getColor(R.color.text_image, getActivity()));
        imageCapText.setImageDrawable(drawable);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).startFragment(R.id.fragment_editable, contact.getId());
            }
        });
    }
}
