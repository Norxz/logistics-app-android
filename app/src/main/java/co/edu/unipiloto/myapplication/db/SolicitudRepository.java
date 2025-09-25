package co.edu.unipiloto.myapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import co.edu.unipiloto.myapplication.model.Solicitud;

public class SolicitudRepository {
    private final DBHelper helper;
    public static final String EST_PENDIENTE = "PENDIENTE";
    public static final String EST_ASIGNADA  = "ASIGNADA";
    public static final String EST_EN_CAMINO = "EN_CAMINO";
    public static final String EST_ENTREGADA = "ENTREGADA";
    public static final String EST_CONFIRMADA= "CONFIRMADA";
    public static final String EST_CANCELADA = "CANCELADA";
    public SolicitudRepository(Context ctx){ this.helper = new DBHelper(ctx); }

    /**
     * Crear solicitud: siempre queda PENDIENTE y sin recolector asignado.
     */
    public long crear(long userId, String dir, String fecha, String franja, String notas, String zona){
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("direccion", dir);
        cv.put("fecha", fecha);
        cv.put("franja", franja);
        cv.put("notas", notas);
        cv.put("zona", zona);
        cv.put("estado", "PENDIENTE");
        cv.put("created_at", System.currentTimeMillis());
        return db.insert("solicitudes", null, cv);
    }

    /**
     * Listar solicitudes del cliente (para mostrar en su panel).
     */
    public List<SolicitudItem> listarPorUsuario(long userId){
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT id, direccion, fecha, franja, estado, created_at, zona FROM solicitudes " +
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
                it.zona = c.getString(6);
                list.add(it);
            }
        } finally { c.close(); }
        return list;
    }

    /**
     * Obtener solicitudes (modelo completo) por zona: solo PENDIENTES para que el recolector las vea.
     */
    public List<Solicitud> pendientesPorZona(String zona) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<Solicitud> lista = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT id, user_id, direccion, fecha, franja, notas, estado, zona, created_at " +
                        "FROM solicitudes WHERE zona=? AND (estado IS NULL OR estado='PENDIENTE') ORDER BY created_at DESC",
                new String[]{zona}
        );
        try {
            while (c.moveToNext()) {
                Solicitud s = new Solicitud(
                        c.getLong(0),
                        c.getLong(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4),
                        c.getString(5),
                        c.getString(6),
                        c.getString(7),
                        c.getLong(8)
                );
                lista.add(s);
            }
        } finally { c.close(); }
        return lista;
    }

    /**
     * Asignar/aceptar una solicitud por parte de un recolector.
     * Solo actualiza si la solicitud todavía está PENDIENTE (evita race conditions).
     */
    public int asignarARecolector(long solicitudId, long recolectorId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("recolector_id", recolectorId);
        cv.put("estado", "ASIGNADA");
        // solo actualizar si estaba pendiente
        return db.update("solicitudes", cv, "id=? AND (estado IS NULL OR estado='PENDIENTE')",
                new String[]{String.valueOf(solicitudId)});
    }

    /**
     * Cancelar solicitud por parte del cliente. Solo si pertenece al usuario y está pendiente.
     */
    public int cancelarSolicitud(long solicitudId, long userId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("estado", "CANCELADA");
        return db.update("solicitudes", cv, "id=? AND user_id=? AND (estado IS NULL OR estado='PENDIENTE')",
                new String[]{String.valueOf(solicitudId), String.valueOf(userId)});
    }

    /**
     * listar solicitudes asignadas a un recolector
     */
    public List<Solicitud> asignadasA(long recolectorId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        List<Solicitud> out = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT id, user_id, direccion, fecha, franja, notas, estado, zona, created_at " +
                        "FROM solicitudes WHERE recolector_id=? ORDER BY created_at DESC",
                new String[]{String.valueOf(recolectorId)}
        );
        try {
            while (c.moveToNext()) {
                out.add(new Solicitud(
                        c.getLong(0), c.getLong(1), c.getString(2), c.getString(3),
                        c.getString(4), c.getString(5), c.getString(6), c.getString(7), c.getLong(8)
                ));
            }
        } finally { c.close(); }
        return out;
    }

    public static class SolicitudItem {
        public long id; public String direccion; public String fecha; public String franja;
        public String estado; public long createdAt; public String zona;
    }

    public int actualizarEstado(long solicitudId, String nuevoEstado, String timestampField) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("estado", nuevoEstado);
        if (timestampField != null) {
            cv.put(timestampField, System.currentTimeMillis());
        }
        return db.update("solicitudes", cv, "id=?", new String[]{ String.valueOf(solicitudId) });
    }

    /** Métodos concretos para conveniencia */
    public int marcarEnCamino(long solicitudId, long recolectorId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("estado", EST_EN_CAMINO);
        cv.put("recolector_id", recolectorId);
        cv.put("en_camino_at", System.currentTimeMillis());
        return db.update("solicitudes", cv, "id=? AND (estado IS NULL OR estado='PENDIENTE' OR estado='ASIGNADA')",
                new String[]{ String.valueOf(solicitudId) });
    }

    public int marcarEntregada(long solicitudId, long recolectorId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("estado", EST_ENTREGADA);
        cv.put("entregado_at", System.currentTimeMillis());
        cv.put("recolector_id", recolectorId);
        return db.update("solicitudes", cv,
                "id=? AND (estado='ASIGNADA' OR estado='EN_CAMINO')",
                new String[]{ String.valueOf(solicitudId) });
    }


}
