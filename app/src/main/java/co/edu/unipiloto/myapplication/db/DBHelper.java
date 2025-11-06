package co.edu.unipiloto.myapplication.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Clase auxiliar para la gestión de la base de datos SQLite de la aplicación.
 * Se encarga de la creación, actualización y gestión de versiones de la base de datos.
 */
public class DBHelper extends SQLiteOpenHelper {
    /**
     * Nombre del archivo de la base de datos.
     */
    public static final String DB_NAME = "logistica.db";
    /**
     * Versión de la base de datos. Si se cambia este número, se activará el método onUpgrade.
     * Versión 8: Fuerza la recreación total con un esquema consistente. Requiere desinstalar la app en el dispositivo de desarrollo.
     */
    public static final int DB_VERSION = 8;

    /**
     * Create a DBHelper configured to manage the application's SQLite database.
     *
     * @param context the application context used to access or create the database
     */
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Creates the initial database schema (users, recolectores, direcciones, guia, solicitudes) and their indexes in the provided database.
     *
     * This method is intended to be invoked from onCreate when the database is created for the first time.
     *
     * @param db the writable SQLiteDatabase where tables and indexes will be created
     */
    private void createTables(SQLiteDatabase db) {

        // ==========================================================
        // 1. Tabla de Usuarios (Clientes)
        // Almacena la información de los clientes que realizan envíos.
        // ==========================================================
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "email TEXT UNIQUE NOT NULL," +
                "password_hash TEXT NOT NULL," +
                "full_name TEXT," +
                "phone_number TEXT" +
                ")");

        // ==========================================================
        // 2. Tabla de Recolectores (Personal de Logística)
        // Almacena información sobre el personal interno como conductores y gestores.
        // ==========================================================
        db.execSQL("CREATE TABLE recolectores (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password_hash TEXT NOT NULL," +
                "role TEXT NOT NULL CHECK(role IN ('CONDUCTOR', 'GESTOR', 'FUNCIONARIO', 'ANALISTA'))," +
                "zona TEXT," +
                "is_active INTEGER DEFAULT 1" +
                ")");

        // ==========================================================
        // 3. Tabla de Direcciones
        // Almacena las direcciones de recogida y destino de los envíos.
        // ==========================================================
        db.execSQL("CREATE TABLE direcciones (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "ciudad TEXT NOT NULL," +
                "full_address TEXT NOT NULL," +
                "latitude REAL," +
                "longitude REAL," +
                "created_at INTEGER NOT NULL," +
                "FOREIGN KEY(user_id) REFERENCES users(id)" +
                ")");

        // ==========================================================
        // 4. Tabla de Guía / Tracking
        // Contiene la información de seguimiento y detalles del paquete.
        // ==========================================================
        db.execSQL("CREATE TABLE guia (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tracking_number TEXT UNIQUE NOT NULL," +
                "descripcion TEXT," +
                "valor REAL DEFAULT 0.0," +
                "peso REAL DEFAULT 0.0" +
                ")");

        // ==========================================================
        // 5. Tabla de Solicitudes (Tabla central)
        // Es la tabla principal que une toda la información de un envío.
        // ==========================================================
        db.execSQL("CREATE TABLE solicitudes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "recolector_id INTEGER," +
                "direccion_id INTEGER NOT NULL," +
                "fecha TEXT NOT NULL," +
                "franja TEXT NOT NULL," +
                "notas TEXT," +
                "zona TEXT NOT NULL," +
                "guia_id INTEGER," +
                "estado TEXT NOT NULL DEFAULT 'PENDIENTE'," +
                "confirmation_code TEXT," +
                "created_at INTEGER NOT NULL," +
                "FOREIGN KEY(user_id) REFERENCES users(id)," +
                "FOREIGN KEY(recolector_id) REFERENCES recolectores(id)," +
                "FOREIGN KEY(direccion_id) REFERENCES direcciones(id)," +
                "FOREIGN KEY(guia_id) REFERENCES guia(id))");

        // ÍNDICES para optimizar las búsquedas
        db.execSQL("CREATE INDEX idx_sol_recolector ON solicitudes(recolector_id)");
        db.execSQL("CREATE INDEX idx_guia_tracking ON guia(tracking_number)");
    }

    /**
     * Create the initial database schema when the database is created for the first time.
     *
     * @param db the writable SQLiteDatabase used to execute schema creation statements
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Llama al método para crear todas las tablas
        createTables(db);
    }

    /**
     * Performs a destructive schema migration when the database version increases.
     *
     * <p>If oldV is less than newV, all application tables are dropped and the schema is recreated by
     * calling onCreate(db). This operation permanently deletes existing data.
     *
     * @param db the SQLiteDatabase instance being upgraded
     * @param oldV the current (old) database version
     * @param newV the target (new) database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        if (oldV < newV) {
            db.execSQL("DROP TABLE IF EXISTS solicitudes");
            db.execSQL("DROP TABLE IF EXISTS direcciones");
            db.execSQL("DROP TABLE IF EXISTS guia");
            db.execSQL("DROP TABLE IF EXISTS recolectores");
            db.execSQL("DROP TABLE IF EXISTS users");

            onCreate(db);
        }
    }
}