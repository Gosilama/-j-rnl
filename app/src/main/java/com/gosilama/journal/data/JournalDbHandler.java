package com.gosilama.journal.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gosilama.journal.model.Journal;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.gosilama.journal.model.Constants.DATABASE_NAME;
import static com.gosilama.journal.model.Constants.DATABASE_VERSION;
import static com.gosilama.journal.model.Constants.KEY_ID;
import static com.gosilama.journal.model.Constants.KEY_JOURNAL_ENTRY;
import static com.gosilama.journal.model.Constants.KEY_JOURNAL_TITLE;
import static com.gosilama.journal.model.Constants.KEY_TIME_CREATED;
import static com.gosilama.journal.model.Constants.TABLE_NAME;

public class JournalDbHandler extends SQLiteOpenHelper{

    public JournalDbHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL Scripts
        // CREATE TABLE
        String CREATE_JOURNAL_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_JOURNAL_TITLE + " TEXT,"
                + KEY_JOURNAL_ENTRY + " TEXT,"
                + KEY_TIME_CREATED + " LONG"
                + ")";

        db.execSQL(CREATE_JOURNAL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // DROP TABLE
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // RECREATE TABLE
        onCreate(db);
    }

    public void createJournal(Journal journal) {
        SQLiteDatabase db = getWritableDatabase();

        // CREATE KEY-VALUE PAIR OF COLUMN DATA
        ContentValues values = new ContentValues();
        values.put(KEY_JOURNAL_TITLE, journal.getJournalTitle());
        values.put(KEY_JOURNAL_ENTRY, journal.getJournalEntry());
        values.put(KEY_TIME_CREATED, System.currentTimeMillis());

        db.insert(TABLE_NAME, null, values);

        db.close();
    }

    public Journal readJournalEntry(int id) {
        SQLiteDatabase db = getWritableDatabase();

        String columns[] = {KEY_ID, KEY_JOURNAL_TITLE, KEY_JOURNAL_ENTRY, KEY_TIME_CREATED};
        String selectionArg[] = {Integer.toString(id)};

        Cursor cursor = db.query(
                TABLE_NAME,
                columns,
                KEY_ID + "=?",
                selectionArg,
                null,
                null,
                null,
                null);

        if (cursor != null) {
            cursor.moveToFirst();

            Journal journal = new Journal();
            journal.setJournalTitle(cursor.getString(cursor.getColumnIndex(KEY_JOURNAL_TITLE)));
            journal.setJournalEntry(cursor.getString(cursor.getColumnIndex(KEY_JOURNAL_ENTRY)));
            journal.setTimeCreated(cursor.getLong(cursor.getColumnIndex(KEY_TIME_CREATED)));

            DateFormat dateFormat = DateFormat.getDateInstance();
            dateFormat.format(new Date(cursor.getLong(cursor.getColumnIndex(KEY_TIME_CREATED))).getTime());

            cursor.close();

            return journal;
        }
        return null;
    }

    public ArrayList<Journal> readAllJournalEntries() {
        SQLiteDatabase db = getReadableDatabase();

        ArrayList<Journal> journals = new ArrayList<>();

        String SELECT_ALL = "SELECT * FROM " + TABLE_NAME;

        Cursor cursor = db.rawQuery(SELECT_ALL, null);

        if (cursor.moveToFirst()) {
            do {
                Journal journal = new Journal();

                journal.setJournalTitle(cursor.getString(cursor.getColumnIndex(KEY_JOURNAL_TITLE)));
                journal.setJournalEntry(cursor.getString(cursor.getColumnIndex(KEY_JOURNAL_ENTRY)));
                journal.setTimeCreated(cursor.getLong(cursor.getColumnIndex(KEY_TIME_CREATED)));

                journals.add(journal);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return journals;
    }

    public int updateJournal(Journal journal) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_JOURNAL_TITLE, journal.getJournalTitle());
        values.put(KEY_JOURNAL_ENTRY, journal.getJournalEntry());
        values.put(KEY_TIME_CREATED, System.currentTimeMillis());

        String selectionArg[] = {Integer.toString(journal.getId())};

        return db.update(TABLE_NAME, values,KEY_ID + "=?", selectionArg);
    }

    public void deleteJournal(Journal journal) {
        SQLiteDatabase db = getWritableDatabase();

        String selectionArg[] = {Integer.toString(journal.getId())};

        db.delete(TABLE_NAME, KEY_ID + "=?", selectionArg);
        db.close();
    }

    public int getJournalEntryCount() {
        SQLiteDatabase db = getReadableDatabase();

        String countQuery = "SELECT * FROM " + TABLE_NAME;

        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();

        cursor.close();

        return count;
    }
}
