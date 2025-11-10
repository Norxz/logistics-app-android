package co.edu.unipiloto.myapplication.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import co.edu.unipiloto.myapplication.security.PasswordHasher // 🏆 Importación de la nueva clase

/**
 * Clase auxiliar para la gestión de la base de datos SQLite de la aplicación.
 * Se encarga de la creación, actualización y gestión de versiones de la base de datos.
 */
class DBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        /** Nombre del archivo de la base de datos. */
        const val DB_NAME = "logistica.db"

        /**
         * Versión de la base de datos. Incrementada a 10 para forzar la actualización del esquema.
         */
        const val DB_VERSION = 10

        // Nombres de Tablas
        const val TABLE_USERS = "users"
        const val TABLE_RECOLECTORES = "recolectores"
        const val TABLE_DIRECCIONES = "direcciones" // 👈 Asegura que exista
        const val TABLE_GUIA = "guia" // 👈 Asegura que exista
        const val TABLE_SOLICITUDES = "solicitudes" // 👈 Asegura que exista
    }

    // ... (El método createTables sigue siendo exactamente el mismo que la versión anterior)
    private fun createTables(db: SQLiteDatabase) {

        // ==========================================================
        // 1. Tabla de Usuarios (Autenticación Global) - CORREGIDA
        // ==========================================================
        db.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                email TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                phone_number TEXT,
                role TEXT NOT NULL CHECK(role IN ('CLIENTE', 'ADMIN', 'CONDUCTOR', 'GESTOR', 'FUNCIONARIO', 'ANALISTA')),
                sucursal TEXT DEFAULT 'N/A' 
            )
        """
        )

        // ==========================================================
        // 2. Tabla de Recolectores (Información detallada)
        // ==========================================================
        db.execSQL(
            """
            CREATE TABLE $TABLE_RECOLECTORES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER UNIQUE NOT NULL, 
                is_active INTEGER DEFAULT 1,
                FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id)
            )
        """
        )

        // ==========================================================
        // 3. Tabla de Direcciones
        // ==========================================================
        db.execSQL(
            """
            CREATE TABLE ${DBHelper.TABLE_DIRECCIONES} (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                ciudad TEXT NOT NULL,
                full_address TEXT NOT NULL,
                latitude REAL,
                longitude REAL,
                created_at TEXT NOT NULL, -- Usar TEXT para guardar fecha/hora en formato ISO8601
                FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id)
            )
        """
        )

        // ==========================================================
        // 4. Tabla de Guía / Tracking
        // ==========================================================
        db.execSQL(
            """
            CREATE TABLE guia (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tracking_number TEXT UNIQUE NOT NULL,
                descripcion TEXT,
                valor REAL DEFAULT 0.0,
                peso REAL DEFAULT 0.0,
                created_at INTEGER NOT NULL
            )
        """
        )

        // ==========================================================
        // 5. Tabla de Solicitudes (Tabla central)
        // ==========================================================
        db.execSQL(
            """
            CREATE TABLE solicitudes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                recolector_id INTEGER, 
                direccion_id INTEGER NOT NULL,
                fecha TEXT NOT NULL,
                franja TEXT NOT NULL,
                notas TEXT,
                zona TEXT NOT NULL,
                guia_id INTEGER,
                estado TEXT NOT NULL DEFAULT 'PENDIENTE',
                confirmation_code TEXT,
                created_at INTEGER NOT NULL,
                FOREIGN KEY(user_id) REFERENCES $TABLE_USERS(id),
                FOREIGN KEY(recolector_id) REFERENCES $TABLE_RECOLECTORES(id), -- ¡CORRECCIÓN AQUÍ!
                FOREIGN KEY(direccion_id) REFERENCES direcciones(id),
                FOREIGN KEY(guia_id) REFERENCES guia(id)
            )
        """
        )

        // ÍNDICES para optimizar las búsquedas
        db.execSQL("CREATE INDEX idx_sol_recolector ON solicitudes(recolector_id)")
        db.execSQL("CREATE INDEX idx_guia_tracking ON guia(tracking_number)")
    }


    /**
     * Inserta un usuario administrador de prueba en la tabla principal de usuarios ($TABLE_USERS).
     * @param db Instancia de la base de datos abierta.
     */
    private fun insertDefaultAdmin(db: SQLiteDatabase) {
        val adminPassword = "Admin123"

        // 🏆 USO DE LA NUEVA CLASE PasswordHasher
        val hashedPassword = PasswordHasher.hashPassword(adminPassword)

        if (hashedPassword.isEmpty()) {
            Log.e("DBHelper", "FATAL: No se pudo generar el hash del password de administrador.")
            return
        }

        val cv = ContentValues().apply {
            put("name", "Administrador Principal")
            put("email", "admin@logistica.com")
            put("password_hash", hashedPassword)
            put("phone_number", "3223691238")
            put("role", "ADMIN")
            put("sucursal", "CENTRAL")
        }
        try {
            db.insert(TABLE_USERS, null, cv)
            Log.d("DBHelper", "Admin insertado correctamente en la tabla $TABLE_USERS.")
        } catch (e: Exception) {
            Log.e("DBHelper", "Error al insertar admin: ${e.message}")
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        createTables(db)
        insertDefaultAdmin(db)
    }

    /**
     * Se llama cuando la base de datos necesita ser actualizada.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        if (oldV < newV) {
            Log.w(
                "DBHelper",
                "Actualizando base de datos de V$oldV a V$newV. ¡Se borrarán todos los datos!"
            )
            // Eliminar todas las tablas (migración destructiva)
            db.execSQL("DROP TABLE IF EXISTS solicitudes")
            db.execSQL("DROP TABLE IF EXISTS direcciones")
            db.execSQL("DROP TABLE IF EXISTS guia")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_RECOLECTORES")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")

            onCreate(db)
        }
    }
}