package co.edu.unipiloto.myapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class SolicitudRepository {
    private final DBHelper helper;
    public SolicitudRepository(Context ctx){ this.helper = new DBHelper(ctx); }

    public long crear(long userId, String dir, String fecha, String franja, String notas, String zona){
        SQLiteDatabase db = helper.getWritableDatabase();


        Long recolectorId = null;
        Cursor r = db.rawQuery(
                "SELECT id FROM users WHERE role='RECOLECTOR' AND zona=? LIMIT 1",
                new String[]{zona});
        try { if (r.moveToFirst()) recolectorId = r.getLong(0); } finally { r.close(); }


        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("direccion", dir);
        cv.put("fecha", fecha);
        cv.put("franja", franja);
        cv.put("notas", notas);
        cv.put("zona", zona);
        if (recolectorId != null) {
            cv.put("recolector_id", recolectorId);
            cv.put("estado", "ASIGNADA");
        } else {
            cv.put("estado", "PENDIENTE");
        }
        cv.put("created_at", System.currentTimeMillis());
        return db.insert("solicitudes", null, cv);
    }

    public List<SolicitudItem> listarPorUsuario(long userId){
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT id, direccion, fecha, franja, estado, created_at FROM solicitudes " +
                        "WHERE user_id=? ORDER BY created_at DESC", new String[]{String.valueOf(userId)});
        List<SolicitudItem> list = new ArrayList<>();
        try {
            while(c.moveToNext()){
                SolicitudItem it = new SolicitudItem();
                it.id = c.getLong(0);
                it.direccion = c.getString(1);
                it.fecha = c.getString(2);
                it.franja = c.getString(3);
                it.estado = c.getString(4);
                it.createdAt = c.getLong(5);
                list.add(it);
            }
        } finally { c.close(); }
        return list;
    }

    public List<SolicitudItem> listarAsignadasARecolector(long recolectorId){
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT id, direccion, fecha, franja, estado, created_at, zona " +
                        "FROM solicitudes WHERE recolector_id=? ORDER BY created_at DESC",
                new String[]{String.valueOf(recolectorId)});
        List<SolicitudItem> list = new ArrayList<>();
        try {
            while(c.moveToNext()){
                SolicitudItem it = new SolicitudItem();
                it.id = c.getLong(0);
                it.direccion = c.getString(1);
                it.fecha = c.getString(2);
                it.franja = c.getString(3);
                it.estado = c.getString(4);
                it.createdAt = c.getLong(5);
                it.zona = c.getString(6);
                list.add(it);
            }
        } finally { c.close(); }
        return list;
    }


    public static class SolicitudItem {
        public long id; public String direccion; public String fecha; public String franja;
        public String estado; public long createdAt; public String zona;
    }
}
