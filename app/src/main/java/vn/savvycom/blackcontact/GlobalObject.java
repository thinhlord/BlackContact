package vn.savvycom.blackcontact;

import java.util.ArrayList;
import java.util.Comparator;

import vn.savvycom.blackcontact.Item.Contact;

/**
 * Created by ruler_000 on 22/06/2015.
 * Project: BlackContact
 */
public class GlobalObject {
    public static final String EXTRA_MERGE = "merge";
    public static final String EXTRA_DELETE = "delete";
    public static final String EXTRA_CONTACT = "contact";

    public static ArrayList<Contact> allContacts = new ArrayList<>();

    public static class ContactComparator implements Comparator<Contact> {
        public int compare(Contact left, Contact right) {
            return left.getName().compareTo(right.getName());
        }
    }

}
