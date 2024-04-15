package com.example.martinn_todoapp.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.martinn_todoapp.Model.ToDoModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String NAME = "toDoListDatabase";
    private static final String TODO_TABLE = "todo";
    private static final String ID = "id";
    private static final String TASK = "task";
    private static final String STATUS = "status";

    private static final String PRIORITY = "priority";
    private static final String DEADLINE = "deadline";
    private static final String CREATE_TODO_TABLE = "CREATE TABLE " + TODO_TABLE + "("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TASK + " TEXT, "
            + STATUS + " INTEGER, "
            + PRIORITY + " TEXT, "
            + DEADLINE + " TEXT)";

    private SQLiteDatabase db;

    public DatabaseHandler(Context context) {
        super(context, NAME, null, VERSION);
    }

    public void deleteDatabase(Context context) {
        context.deleteDatabase(NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TODO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TODO_TABLE);
        // Create tables again
        onCreate(db);
    }

    public void openDatabase() {
        db = this.getWritableDatabase();
    }

    public void addToDo(ToDoModel task){
        ContentValues cv = new ContentValues();
        cv.put(TASK, task.getTask());
        cv.put(STATUS, 0);
        cv.put(PRIORITY, task.getPriority().toString());
        cv.put(DEADLINE, task.getDeadline().toString()); // Convert LocalDate to String
        db.insert(TODO_TABLE, null, cv);
    }

    public List<ToDoModel> getToDos(int status){
        List<ToDoModel> taskList = new ArrayList<>();
        Cursor cur = null;
        db.beginTransaction();
        try{
            String[] selectionArgs = {String.valueOf(status)}; // Status value for Pending
            cur = db.query(
                    TODO_TABLE,
                    null,
                    STATUS + " = ?",
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if(cur != null){
                if(cur.moveToFirst()){
                    do{
                        ToDoModel task = new ToDoModel();
                        task.setId(cur.getInt(cur.getColumnIndexOrThrow(ID)));
                        task.setTask(cur.getString(cur.getColumnIndexOrThrow(TASK)));
                        task.setStatus(cur.getInt(cur.getColumnIndexOrThrow(STATUS)));
                        task.setPriority(ToDoModel.Priority.valueOf(cur.getString(cur.getColumnIndexOrThrow(PRIORITY))));

                        // Convert String to LocalDateTime
                        String deadlineString = cur.getString(cur.getColumnIndexOrThrow(DEADLINE));
                        task.setDeadline(LocalDate.parse(deadlineString));

                        taskList.add(task);
                    }
                    while(cur.moveToNext());
                }
            }
        }
        finally {
            db.endTransaction();
            assert cur != null;
            cur.close();
        }
        return taskList;
    }

    public void updateStatus(int id, int status){
        ContentValues cv = new ContentValues();
        cv.put(STATUS, status);
        db.update(TODO_TABLE, cv, ID + "= ?", new String[] {String.valueOf(id)});
    }

    public void updateToDo(int id, String task) {
        ContentValues cv = new ContentValues();
        cv.put(TASK, task);
        db.update(TODO_TABLE, cv, ID + "= ?", new String[] {String.valueOf(id)});
    }

    public void deleteToDo(int id){
        db.delete(TODO_TABLE, ID + "= ?", new String[] {String.valueOf(id)});
    }

    public List<ToDoModel> getToDosSortedByPriority(int status) {
        List<ToDoModel> taskList = new ArrayList<>();
        Cursor cur = null;
        db.beginTransaction();
        try {
            String[] selectionArgs = {String.valueOf(status)}; // Status value for Pending
            cur = db.query(
                    TODO_TABLE,
                    null,
                    STATUS + " = ?",
                    selectionArgs,
                    null,
                    null,
                    PRIORITY + " DESC"
            );

            if (cur != null) {
                if (cur.moveToFirst()) {
                    do {
                        ToDoModel task = new ToDoModel();
                        task.setId(cur.getInt(cur.getColumnIndexOrThrow(ID)));
                        task.setTask(cur.getString(cur.getColumnIndexOrThrow(TASK)));
                        task.setStatus(cur.getInt(cur.getColumnIndexOrThrow(STATUS)));
                        task.setPriority(ToDoModel.Priority.valueOf(cur.getString(cur.getColumnIndexOrThrow(PRIORITY))));
                        // Convert deadline string to LocalDate
                        if (cur.getString(cur.getColumnIndexOrThrow(DEADLINE)) != null) {
                            task.setDeadline(LocalDate.parse(cur.getString(cur.getColumnIndexOrThrow(DEADLINE))));
                        }
                        taskList.add(task);
                    } while (cur.moveToNext());
                }
            }
        } finally {
            db.endTransaction();
            if (cur != null) {
                cur.close();
            }
        }

        // Sort taskList based on priority value
        Collections.sort(taskList, new Comparator<ToDoModel>() {
            @Override
            public int compare(ToDoModel o1, ToDoModel o2) {
                return Integer.compare(o1.getPriority().getValue(), o2.getPriority().getValue());
            }
        });

        return taskList;
    }

    public List<ToDoModel> getToDosSortedByDeadline(int status) {
        List<ToDoModel> taskList = new ArrayList<>();
        Cursor cur = null;
        db.beginTransaction();
        try {
            String[] selectionArgs = {String.valueOf(status)}; // Status value for Pending
            cur = db.query(
                    TODO_TABLE,
                    null,
                    STATUS + " = ?",
                    selectionArgs,
                    null,
                    null,
                    DEADLINE + " DESC"
            );

            if (cur != null) {
                if (cur.moveToFirst()) {
                    do {
                        ToDoModel task = new ToDoModel();
                        task.setId(cur.getInt(cur.getColumnIndexOrThrow(ID)));
                        task.setTask(cur.getString(cur.getColumnIndexOrThrow(TASK)));
                        task.setStatus(cur.getInt(cur.getColumnIndexOrThrow(STATUS)));
                        task.setPriority(ToDoModel.Priority.valueOf(cur.getString(cur.getColumnIndexOrThrow(PRIORITY))));
                        // Convert deadline string to LocalDate
                        if (cur.getString(cur.getColumnIndexOrThrow(DEADLINE)) != null) {
                            task.setDeadline(LocalDate.parse(cur.getString(cur.getColumnIndexOrThrow(DEADLINE))));
                        }
                        taskList.add(task);
                    } while (cur.moveToNext());
                }
            }
        } finally {
            db.endTransaction();
            if (cur != null) {
                cur.close();
            }
        }
        return taskList;
    }
}
