package co.edu.unipiloto.myapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private final DBHelper helper;
    public UserRepository(Context ctx){ this.helper = new DBHelper(ctx); }


    public static class ConductorInfo {
        public long id;
        public String email; // Usaremos el email como nombre
    }

    /** Devuelve una lista de usuarios con el rol GESTOR o CONDUCTOR. */
    public List<ConductorInfo> getConductores() {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<ConductorInfo> conductores = new ArrayList<>();

        // Filtrar por roles GESTOR (asumido como rol de conductor en DB) y CONDUCTOR
        String selection = "UPPER(role)=UPPER(?) OR UPPER(role)=UPPER(?)";
        String[] selectionArgs = new String[]{"GESTOR", "CONDUCTOR"};

        Cursor c = db.query(
                "users",
                new String[]{"id", "email"},
                selection,
                selectionArgs,
                null,
                null,
                "email ASC"
        );

        try {
            while (c.moveToNext()) {
                ConductorInfo info = new ConductorInfo();
                info.id = c.getLong(0);
                info.email = c.getString(1);
                conductores.add(info);
            }
            return conductores;
        } finally {
            c.close();
        }
    }

    public long register(String email, String password, String role, String zona) throws Exception {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("email", email);
        cv.put("password", password);
        cv.put("role", role);
        if ("Gestor".equals(role)) cv.put("zona", zona);
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
        // use case-insensitive comparison to be tolerant con mayúsculas/minúsculas
        Cursor c = db.rawQuery("SELECT id FROM users WHERE UPPER(role)=UPPER(?) LIMIT 1", new String[]{ role });
        try {
            if (c.moveToFirst()) {
                return c.getLong(0);
            }
            return -1L;
        } finally { c.close(); }
    }

    /** Devuelve el rol de un usuario por su id, o null si no se encuentra. */
    public String getRoleById(long userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT role FROM users WHERE id=?", new String[]{ String.valueOf(userId) });
        try {
            if (c.moveToFirst()) {
                return c.getString(0);
            }
            return null;
        } finally {
            c.close();
        }
    }
}
