package vn.savvycom.blackcontact;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by ruler_000 on 15/05/2015.
 * Project: BlackContact
 */
public class DatabaseController {

    private static DatabaseController global;
    private SQLiteDatabase db;
    private String[] allColumn = {};
    private static ArrayList<Long> favorites = null;

    public static DatabaseController getInstance(Context context) {
        if (global == null) {
            global = new DatabaseController(context);
        }
        return global;
    }

    public DatabaseController(Context context) {
        DatabaseHelper mDbHelper = DatabaseHelper.getInstance(context);
        db = mDbHelper.getWritableDatabase();

    }

    public ArrayList<Long> loadFavorites() {
        if (favorites != null) return favorites;
        favorites = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_NAME, allColumn, null, null, null, null, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                favorites.add(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_CONTACT_ID)));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return favorites;
    }

    public void addFavorite(long contactId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CONTACT_ID, contactId);
        db.insert(DatabaseHelper.TABLE_NAME, null, values);
        favorites.add(contactId);
    }

    public void addFavorite(String contactId) {
        addFavorite(Long.parseLong(contactId));
    }

    public boolean isFavorite(String contactId) {
        Long id = Long.parseLong(contactId);
        if (favorites == null) loadFavorites();
        return favorites.contains(id);
    }

    public void delFavorite(String contactId) {
        String selection = DatabaseHelper.COLUMN_CONTACT_ID + " LIKE ?";
        String[] selectionArgs = { contactId };
        db.delete(DatabaseHelper.TABLE_NAME, selection, selectionArgs);
        favorites.remove(Long.parseLong(contactId));
    }

    public boolean toggleFavorite(String contactId) {
        if (isFavorite(contactId)) {
            delFavorite(contactId);
            return false;
        } else {
            addFavorite(contactId);
            return true;
        }
    }
}
