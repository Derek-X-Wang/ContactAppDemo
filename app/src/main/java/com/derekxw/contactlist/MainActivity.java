package com.derekxw.contactlist;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.derekxw.contactlist.controllers.ContactDetailFragment;
import com.derekxw.contactlist.controllers.ContactEditableFragment;
import com.derekxw.contactlist.controllers.ContactListFragment;
import com.derekxw.contactlist.models.Contact;
import com.derekxw.contactlist.utils.DatabaseHelper;
import com.pixplicity.easyprefs.library.Prefs;

public class MainActivity extends Activity {

    public static final String CONTACT_REFERENCE = "ContactId";
    public static final String HAS_INIT = "HasInit";
    public static final String HAS_INIT_ROUTINE = "HasInitRoutine";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        DatabaseHelper helper = DatabaseHelper.getHelper(this);
//        helper.clearTable(Contact.class);

        ContactListFragment contactListFragment = new ContactListFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_content, contactListFragment)
                .commit();
    }

    public void startFragment(int fragId, int contactId) {
        FragmentTransaction transition = getFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putInt(CONTACT_REFERENCE, contactId);
        switch (fragId) {
            case R.id.fragment_list:
                ContactListFragment contactListFragment = new ContactListFragment();
                contactListFragment.setArguments(bundle);
                // When the change on contact is finished, user cannot go back to edit page
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                transition.replace(R.id.fragment_content, contactListFragment)
                        .commit();
                break;
            case R.id.fragment_detail:
                ContactDetailFragment detailFragment = new ContactDetailFragment();
                detailFragment.setArguments(bundle);
                transition.replace(R.id.fragment_content, detailFragment)
                        .addToBackStack("tag")
                        .commit();
                break;
            case R.id.fragment_editable:
                ContactEditableFragment editableFragment = new ContactEditableFragment();
                editableFragment.setArguments(bundle);
                transition.replace(R.id.fragment_content, editableFragment)
                        .addToBackStack("tag")
                        .commit();
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Prefs.putBoolean(HAS_INIT_ROUTINE, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
}
