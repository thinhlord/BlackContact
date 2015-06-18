package vn.savvycom.blackcontact;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by ruler_000 on 14/05/2015.
 * Project: BlackContact
 */
public class Contact implements Parcelable {
    private String id;
    private String name;
    private Bitmap photo;
    private ArrayList<String> phone;
    private ArrayList<String> mail;
    String accountType;

    public Contact(String id, String name, Bitmap photo, String accountType, ArrayList<String> phone, ArrayList<String> mail) {
        this.id = id;
        this.name = name;
        this.photo = photo;
        this.accountType = accountType;
        this.phone = phone;
        this.mail = mail;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public ArrayList<String> getPhone() {
        return phone;
    }

    public ArrayList<String> getMail() {
        return mail;
    }

    public String getAccountType() {
        return accountType;
    }

    public void addPhones(ArrayList<String> newPhones) {
        phone.addAll(newPhones);
    }

    public void addMails(ArrayList<String> mails) {
        mail.addAll(mails);
    }

    protected Contact(Parcel in) {
        id = in.readString();
        name = in.readString();
        photo = (Bitmap) in.readValue(Bitmap.class.getClassLoader());
        if (in.readByte() == 0x01) {
            phone = new ArrayList<String>();
            in.readList(phone, String.class.getClassLoader());
        } else {
            phone = null;
        }
        if (in.readByte() == 0x01) {
            mail = new ArrayList<String>();
            in.readList(mail, String.class.getClassLoader());
        } else {
            mail = null;
        }
        accountType = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeValue(photo);
        if (phone == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(phone);
        }
        if (mail == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mail);
        }
        dest.writeString(accountType);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
}