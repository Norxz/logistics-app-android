package co.edu.unipiloto.myapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserRepository {
    private final DBHelper helper;
    public UserRepository(Context ctx){ this.helper = new DBHelper(ctx); }

    public long register(String email, String password) throws Exception {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("email", email);
        cv.put("password", password); // (Opcional: hashear)
        long id = db.insert("users", null, cv);
        if (id == -1) throw new Exception("Email ya registrado");
        return id;
    }

    public Long login(String email, String password){
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM users WHERE email=? AND password=?",
                new String[]{email, password});
        try {
            if (c.moveToFirst()) return c.getLong(0);
            return null;
        } finally { c.close(); }
    }
}
