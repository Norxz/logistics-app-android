package co.edu.unipiloto.myapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserRepository {
    private final DBHelper helper;
    public UserRepository(Context ctx){ this.helper = new DBHelper(ctx); }

    public long register(String email, String password, String role, String zona) throws Exception {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("email", email);
        cv.put("password", password);
        cv.put("role", role);
        if ("RECOLECTOR".equals(role)) cv.put("zona", zona);
        try {
            long id = db.insertOrThrow("users", null, cv);
            return id;
        } catch (android.database.sqlite.SQLiteConstraintException ex) {
            String msg = ex.getMessage();
            if (msg != null) {
                String lower = msg.toLowerCase();
                if (lower.contains("unique") && lower.contains("email")) {
                    throw new Exception("Email ya registrado");
                }
                if (lower.contains("check") || lower.contains("constraint")) {
                    throw new Exception("Valor inválido para rol o zona");
                }
            }
            throw new Exception("Error al registrar: " + ex.getMessage());
        }
    }


    public static class UserInfo { public long id; public String role; public String zona; }

    public UserInfo login(String email, String password){
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, role, zona FROM users WHERE email=? AND password=?",
                new String[]{email, password});
        try {
            if (c.moveToFirst()) {
                UserInfo u = new UserInfo();
                u.id = c.getLong(0); u.role = c.getString(1); u.zona = c.getString(2);
                return u;
            }
            return null;
        } finally { c.close(); }
    }

    /** Devuelve el id del primer usuario que tenga el rol indicado, o -1 si no existe. */
    public long getFirstIdByRole(String role) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM users WHERE role=? LIMIT 1", new String[]{ role });
        try {
            if (c.moveToFirst()) {
                return c.getLong(0);
            }
            return -1L;
        } finally { c.close(); }
    }
}
