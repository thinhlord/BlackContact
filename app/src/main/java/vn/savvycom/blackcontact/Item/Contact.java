package vn.savvycom.blackcontact.Item;

import android.net.Uri;
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
    private Uri photo;
    private ArrayList<String> phone;
    private ArrayList<String> phoneType;
    private ArrayList<String> mail;
    private ArrayList<String> mailType;
    String accountType;

    public Contact(String id, String name, Uri photo, String accountType, ArrayList<String> phone, ArrayList<String> phoneType,
                   ArrayList<String> mail, ArrayList<String> mailType) {
        this.id = id;
        this.name = name;
        this.photo = photo;
        this.accountType = accountType;
        this.phone = phone;
        this.mail = mail;
        this.phoneType = phoneType;
        this.mailType = mailType;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Uri getPhoto() {
        return photo;
    }

    public ArrayList<String> getPhone() {
        return phone;
    }

    public ArrayList<String> getMail() {
        return mail;
    }

    public ArrayList<String> getPhoneType() {
        return phoneType;
    }

    public ArrayList<String> getMailType() {
        return mailType;
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

    public int haveThisPhone(String checkingPhone, String type) {
        for (int i = 0; i < this.phone.size(); i++) {
            if (phone.get(i).equals(checkingPhone) && phoneType.get(i).equals(type)) return i;
        }
        return -1;
    }

    public int haveThisMail(String checkingMail, String type) {
        for (int i = 0; i < mail.size(); i++) {
            if (mail.get(i).equals(checkingMail) && mailType.get(i).equals(type)) return i;
        }
        return -1;
    }

    protected Contact(Parcel in) {
        id = in.readString();
        name = in.readString();
        photo = (Uri) in.readValue(Uri.class.getClassLoader());
        if (in.readByte() == 0x01) {
            phone = new ArrayList<String>();
            in.readList(phone, String.class.getClassLoader());
        } else {
            phone = null;
        }
        if (in.readByte() == 0x01) {
            phoneType = new ArrayList<String>();
            in.readList(phoneType, String.class.getClassLoader());
        } else {
            phoneType = null;
        }
        if (in.readByte() == 0x01) {
            mail = new ArrayList<String>();
            in.readList(mail, String.class.getClassLoader());
        } else {
            mail = null;
        }
        if (in.readByte() == 0x01) {
            mailType = new ArrayList<String>();
            in.readList(mailType, String.class.getClassLoader());
        } else {
            mailType = null;
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
        if (phoneType == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(phoneType);
        }
        if (mail == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mail);
        }
        if (mailType == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mailType);
        }
        dest.writeString(accountType);
    }

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