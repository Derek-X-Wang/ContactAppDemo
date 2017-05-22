package com.derekxw.contactlist;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.derekxw.contactlist.models.Contact;
import com.derekxw.contactlist.models.ContactDao;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class OrmLiteDBTest {
    Context context = InstrumentationRegistry.getTargetContext();
    @Test
    public void testAddContacts() throws Exception {
        Contact contact = new Contact("Derek", "Wang", "6263543629", new Date(), "93117");
        new ContactDao(context).add(contact);
    }
}
