package vn.savvycom.blackcontact.Item;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ruler_000 on 22/06/2015.
 * Project: BlackContact
 */
public class Mail implements Parcelable {
    String address;
    String type;
    String rawId;


    protected Mail(Parcel in) {
        address = in.readString();
        type = in.readString();
        rawId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(type);
        dest.writeString(rawId);
    }

    public static final Creator<Mail> CREATOR = new Creator<Mail>() {
        @Override
        public Mail createFromParcel(Parcel in) {
            return new Mail(in);
        }

        @Override
        public Mail[] newArray(int size) {
            return new Mail[size];
        }
    };
}
