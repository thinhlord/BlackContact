package vn.savvycom.blackcontact.Item;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ruler_000 on 22/06/2015.
 * Project: BlackContact
 */
public class Phone implements Parcelable {
    String number;
    String type;
    String rawId;


    protected Phone(Parcel in) {
        number = in.readString();
        type = in.readString();
        rawId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(number);
        dest.writeString(type);
        dest.writeString(rawId);
    }

    public static final Parcelable.Creator<Phone> CREATOR = new Parcelable.Creator<Phone>() {
        @Override
        public Phone createFromParcel(Parcel in) {
            return new Phone(in);
        }

        @Override
        public Phone[] newArray(int size) {
            return new Phone[size];
        }
    };
}
