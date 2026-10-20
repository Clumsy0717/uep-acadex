package com.example.acadex;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/** Local SQLite storage for tasks, reminders, and class schedules. */
public class AcadexDb extends SQLiteOpenHelper {

    private static final String NAME = "acadex.db";
    private static final int VERSION = 2;

    public AcadexDb(Context context) {
        super(context.getApplicationContext(), NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, subject TEXT NOT NULL, "
                + "code TEXT, description TEXT, deadline INTEGER NOT NULL, done INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE reminders (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, "
                + "note TEXT, date INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE classes (id INTEGER PRIMARY KEY AUTOINCREMENT, time TEXT NOT NULL, "
                + "code TEXT NOT NULL, room TEXT NOT NULL, name TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tasks");
        db.execSQL("DROP TABLE IF EXISTS reminders");
        db.execSQL("DROP TABLE IF EXISTS classes");
        onCreate(db);
    }

    // ---- Tasks ----

    public long addTask(String subject, String code, String description, long deadline) {
        ContentValues v = new ContentValues();
        v.put("subject", subject);
        v.put("code", code);
        v.put("description", description);
        v.put("deadline", deadline);
        return getWritableDatabase().insert("tasks", null, v);
    }

    public List<Task> getTasks(boolean pendingOnly) {
        List<Task> out = new ArrayList<>();
        String where = pendingOnly ? "done = 0" : null;
        try (Cursor c = getReadableDatabase().query("tasks", null, where, null, null, null, "deadline ASC")) {
            while (c.moveToNext()) {
                out.add(new Task(
                        c.getLong(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("subject")),
                        c.getString(c.getColumnIndexOrThrow("code")),
                        c.getString(c.getColumnIndexOrThrow("description")),
                        c.getLong(c.getColumnIndexOrThrow("deadline")),
                        c.getInt(c.getColumnIndexOrThrow("done")) == 1));
            }
        }
        return out;
    }

    public void markDone(long id) {
        setTaskDone(id, true);
    }

    public void setTaskDone(long id, boolean done) {
        ContentValues v = new ContentValues();
        v.put("done", done ? 1 : 0);
        getWritableDatabase().update("tasks", v, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteTask(long id) {
        getWritableDatabase().delete("tasks", "id = ?", new String[]{String.valueOf(id)});
    }

    // ---- Reminders ----

    public long addReminder(String title, String note, long date) {
        ContentValues v = new ContentValues();
        v.put("title", title);
        v.put("note", note);
        v.put("date", date);
        return getWritableDatabase().insert("reminders", null, v);
    }

    public List<Reminder> getReminders() {
        List<Reminder> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("reminders", null, null, null, null, null, "date ASC")) {
            while (c.moveToNext()) {
                out.add(new Reminder(
                        c.getLong(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("title")),
                        c.getString(c.getColumnIndexOrThrow("note")),
                        c.getLong(c.getColumnIndexOrThrow("date"))));
            }
        }
        return out;
    }

    public void deleteReminder(long id) {
        getWritableDatabase().delete("reminders", "id = ?", new String[]{String.valueOf(id)});
    }

    // ---- Classes ----

    public long addClass(String time, String code, String room, String name) {
        ContentValues v = new ContentValues();
        v.put("time", time);
        v.put("code", code);
        v.put("room", room);
        v.put("name", name);
        return getWritableDatabase().insert("classes", null, v);
    }

    public List<ClassItem> getClasses() {
        List<ClassItem> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("classes", null, null, null, null, null, "id ASC")) {
            while (c.moveToNext()) {
                out.add(new ClassItem(
                        c.getLong(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("time")),
                        c.getString(c.getColumnIndexOrThrow("code")),
                        c.getString(c.getColumnIndexOrThrow("room")),
                        c.getString(c.getColumnIndexOrThrow("name"))));
            }
        }
        return out;
    }

    public void deleteClass(long id) {
        getWritableDatabase().delete("classes", "id = ?", new String[]{String.valueOf(id)});
    }
}
