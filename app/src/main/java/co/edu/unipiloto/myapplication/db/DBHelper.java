package co.edu.unipiloto.myapplication.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "logistica.db";
    // bump version to add RECOLECTOR into allowed roles and provide migration
    public static final int DB_VERSION = 3;

    public DBHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "email TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                // agregar RECOLECTOR al CHECK
                "role TEXT NOT NULL CHECK(role IN ('CLIENTE','CONDUCTOR', 'FUNCIONARIO', 'RECOLECTOR', 'ANALISTA'))," +
                "zona TEXT" +
                ")");

        // direcciones: estructura para direcciones (se puede rellenar parcialmente)
        db.execSQL("CREATE TABLE direcciones (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "ciudad TEXT," +
                "tipo_calle TEXT," +
                "numero TEXT," +
                "letra TEXT," +
                "complemento TEXT," +
                "numero_secundario TEXT," +
                "letra_secundaria TEXT," +
                "complemento_secundario TEXT," +
                "numero_terciario TEXT," +
                "full_address TEXT NOT NULL," + // texto completo de la dirección (fallback)
                "created_at INTEGER NOT NULL" +
                ")");

        // guia: información asociada a una guía/guía física o tracking
        db.execSQL("CREATE TABLE guia (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "solicitud_id INTEGER," +
                "tracking_number TEXT," +
                "transportista TEXT," +
                "detalle TEXT," +
                "estado TEXT," +
                "created_at INTEGER NOT NULL," +
                "FOREIGN KEY(solicitud_id) REFERENCES solicitudes(id)" +
                ")");

        db.execSQL("CREATE TABLE solicitudes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "direccion TEXT NOT NULL," +
                "direccion_id INTEGER," + // nueva referencia a tabla direcciones (nullable)
                "fecha TEXT NOT NULL," +
                "franja TEXT NOT NULL," +
                "notas TEXT," +
                "zona TEXT NOT NULL," +
                "recolector_id INTEGER," +
                "guia_id INTEGER," + // referencia a tabla guia (nullable)
                "estado TEXT NOT NULL DEFAULT 'PENDIENTE'," +
                "created_at INTEGER NOT NULL," +
                "en_camino_at INTEGER," +
                "entregado_at INTEGER," +
                "confirmado_at INTEGER," +
                "FOREIGN KEY(user_id) REFERENCES users(id)," +
                "FOREIGN KEY(recolector_id) REFERENCES users(id)," +
                "FOREIGN KEY(direccion_id) REFERENCES direcciones(id)," +
                "FOREIGN KEY(guia_id) REFERENCES guia(id))");

        // índices útiles
        db.execSQL("CREATE INDEX idx_users_role_zona ON users(role, zona)");
        db.execSQL("CREATE INDEX idx_sol_zona ON solicitudes(zona)");
        db.execSQL("CREATE INDEX idx_sol_recolector ON solicitudes(recolector_id)");
        db.execSQL("CREATE INDEX idx_direcciones_full ON direcciones(full_address)");
        db.execSQL("CREATE INDEX idx_guia_solicitud ON guia(solicitud_id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        // si venimos de <2, añadimos las columnas nuevas de solicitudes (v2)
        if (oldV < 2) {
            db.execSQL("ALTER TABLE solicitudes ADD COLUMN en_camino_at INTEGER");
            db.execSQL("ALTER TABLE solicitudes ADD COLUMN entregado_at INTEGER");
            db.execSQL("ALTER TABLE solicitudes ADD COLUMN confirmacion_at INTEGER");
        }

        // v3: añadimos tablas direcciones y guia y columnas direccion_id, guia_id
        if (oldV < 3) {
            // crear tabla direcciones si no existe
            db.execSQL("CREATE TABLE IF NOT EXISTS direcciones (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "ciudad TEXT," +
                    "tipo_calle TEXT," +
                    "numero TEXT," +
                    "letra TEXT," +
                    "complemento TEXT," +
                    "numero_secundario TEXT," +
                    "letra_secundaria TEXT," +
                    "complemento_secundario TEXT," +
                    "numero_terciario TEXT," +
                    "full_address TEXT NOT NULL," +
                    "created_at INTEGER NOT NULL" +
                    ")");

            // crear tabla guia si no existe
            db.execSQL("CREATE TABLE IF NOT EXISTS guia (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "solicitud_id INTEGER," +
                    "tracking_number TEXT," +
                    "transportista TEXT," +
                    "detalle TEXT," +
                    "estado TEXT," +
                    "created_at INTEGER NOT NULL" +
                    ")");

            // añadir columnas a solicitudes existentes (ALTER TABLE ADD COLUMN es seguro)
            try { db.execSQL("ALTER TABLE solicitudes ADD COLUMN direccion_id INTEGER"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE solicitudes ADD COLUMN guia_id INTEGER"); } catch (Exception ignored) {}
        }
    }
}