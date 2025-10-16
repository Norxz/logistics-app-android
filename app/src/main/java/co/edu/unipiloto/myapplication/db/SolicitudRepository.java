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
     * Crear solicitud: ahora guarda también una fila mínima en "direcciones" y referencia su id.
     * Esto mantiene la columna textual "direccion" para compatibilidad.
     */
    public long crear(long userId, String dir, String fecha, String franja, String notas, String zona){
        SQLiteDatabase db = helper.getWritableDatabase();

        // 1) crear entrada en direcciones (guardamos full_address para fallback)
        long direccionId = -1L;
        try {
            ContentValues dirCv = new ContentValues();
            dirCv.put("full_address", dir != null ? dir : "");
            dirCv.put("created_at", System.currentTimeMillis());
            direccionId = db.insert("direcciones", null, dirCv);
        } catch (Exception ex) {
            // si falla la inserción en direcciones, continuamos sin dirección_id (no bloqueante)
            direccionId = -1L;
        }

        // 2) crear solicitud usando la columna textual y la referencia direccion_id (si disponible)
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("direccion", dir != null ? dir : "");
        if (direccionId != -1L) cv.put("direccion_id", direccionId);
        cv.put("fecha", fecha);
        cv.put("franja", franja);
        cv.put("notas", notas);
        cv.put("zona", zona);
        cv.put("estado", EST_PENDIENTE);
        cv.put("created_at", System.currentTimeMillis());
        // guia_id queda null al crear
        long newRowId = db.insert("solicitudes", null, cv);
        db.close();
        return newRowId;
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
        } finally {
            c.close();
            db.close();
        }
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
        } finally {
            c.close();
            db.close();
        }
        return lista;
    }

    /**
     * Obtener solicitudes asignadas a un recolector (útil para DriverDashboardActivity.loadAssignedRoutes)
     */
    public List<Solicitud> getAssignedRoutesByDriver(long recolectorId) {
        return asignadasA(recolectorId);
    }

    /**
     * Asignar/aceptar una solicitud por parte de un recolector.
     * Solo actualiza si la solicitud todavía está PENDIENTE (evita race conditions).
     */
    public int asignarARecolector(long solicitudId, long recolectorId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("recolector_id", recolectorId);
        cv.put("estado", EST_ASIGNADA);
        // solo actualizar si estaba pendiente
        int rows = db.update("solicitudes", cv, "id=? AND (estado IS NULL OR estado=?)",
                new String[]{String.valueOf(solicitudId), EST_PENDIENTE});
        db.close();
        return rows;
    }

    /**
     * Cancelar solicitud por parte del cliente. Solo si pertenece al usuario y está pendiente.
     */
    public int cancelarSolicitud(long solicitudId, long userId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("estado", EST_CANCELADA);
        int rows = db.update("solicitudes", cv, "id=? AND user_id=? AND (estado IS NULL OR estado=?)",
                new String[]{String.valueOf(solicitudId), String.valueOf(userId), EST_PENDIENTE});
        db.close();
        return rows;
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
        } finally {
            c.close();
            db.close();
        }
        return out;
    }

    // --------------------------------------------------------------------------------------
    // 🆕 FUNCIÓN DE RASTREO: Obtener Solicitud por ID 🆕
    // --------------------------------------------------------------------------------------

    /**
     * Obtiene una Solicitud completa por su ID (usado como código de guía en la búsqueda del cliente).
     * @param solicitudId El ID único de la solicitud.
     * @return El objeto Solicitud o null si no se encuentra.
     */
    public Solicitud getSolicitudById(long solicitudId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Solicitud s = null;
        Cursor c = db.rawQuery(
                "SELECT id, user_id, direccion, fecha, franja, notas, estado, zona, created_at " +
                        "FROM solicitudes WHERE id=?",
                new String[]{String.valueOf(solicitudId)}
        );

        try {
            if (c.moveToFirst()) {
                s = new Solicitud(
                        c.getLong(0),   // id
                        c.getLong(1),   // user_id
                        c.getString(2), // direccion
                        c.getString(3), // fecha
                        c.getString(4), // franja
                        c.getString(5), // notas
                        c.getString(6), // estado
                        c.getString(7), // zona
                        c.getLong(8)    // created_at
                );
            }
        } finally {
            c.close();
            db.close();
        }
        return s;
    }

    // --------------------------------------------------------------------------------------
    // 🆕 NUEVA FUNCIONALIDAD: Actualizar Estado y Guardar Código de Confirmación 🆕
    // --------------------------------------------------------------------------------------

    /**
     * Actualiza el estado a EN_CAMINO y guarda el código de confirmación de 4 dígitos.
     * @param solicitudId ID de la solicitud.
     * @param status El nuevo estado (debería ser EST_EN_CAMINO).
     * @param code El código de 4 dígitos generado.
     * @return Número de filas afectadas (1 si fue exitoso).
     */
    public int updateStatusAndCode(long solicitudId, String status, String code) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        // 1. Datos a actualizar
        cv.put("estado", status); // EST_EN_CAMINO
        cv.put("en_camino_at", System.currentTimeMillis());
        cv.put("confirmation_code", code); // Guarda el código de 4 dígitos

        // Condición: La solicitud debe estar pendiente o asignada para iniciar el trayecto
        int rows = db.update("solicitudes", cv,
                "id=? AND (estado=? OR estado=?)",
                new String[]{
                        String.valueOf(solicitudId),
                        EST_ASIGNADA,
                        EST_PENDIENTE
                });

        db.close();
        return rows;
    }


    public static class SolicitudItem {
        public long id; public String direccion; public String fecha; public String franja;
        public String estado; public long createdAt; public String zona;
    }

    // Método genérico para actualizar estado (mantenido por compatibilidad)
    public int actualizarEstado(long solicitudId, String nuevoEstado, String timestampField) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("estado", nuevoEstado);
        if (timestampField != null) {
            cv.put(timestampField, System.currentTimeMillis());
        }
        int rows = db.update("solicitudes", cv, "id=?", new String[]{ String.valueOf(solicitudId) });
        db.close();
        return rows;
    }

    /** Métodos concretos para conveniencia (mantenidos, pero updateStatusAndCode es preferido para SMS) */
    public int marcarEnCamino(long solicitudId, long recolectorId) {
        // NOTA: Este método NO guarda el confirmation_code. Usa updateStatusAndCode para SMS.
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("estado", EST_EN_CAMINO);
        cv.put("recolector_id", recolectorId);
        cv.put("en_camino_at", System.currentTimeMillis());
        int rows = db.update("solicitudes", cv, "id=? AND (estado IS NULL OR estado=? OR estado=?)",
                new String[]{ String.valueOf(solicitudId), EST_PENDIENTE, EST_ASIGNADA });
        db.close();
        return rows;
    }

    public int marcarEntregada(long solicitudId, long recolectorId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("estado", EST_ENTREGADA);
        cv.put("entregado_at", System.currentTimeMillis());
        cv.put("recolector_id", recolectorId);
        int rows = db.update("solicitudes", cv,
                "id=? AND (estado=? OR estado=?)",
                new String[]{ String.valueOf(solicitudId), EST_ASIGNADA, EST_EN_CAMINO });
        db.close();
        return rows;
    }
}