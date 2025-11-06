package co.edu.unipiloto.myapplication.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * Clase auxiliar para la gestión de la base de datos SQLite de la aplicación.
 * Se encarga de la creación, actualización y gestión de versiones de la base de datos.
 */
class DBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        /** Nombre del archivo de la base de datos. */
        const val DB_NAME = "logistica.db"

        /**
         * Versión de la base de datos. Incrementada a 9 para incluir la tabla de administradores
         * y las correcciones de esquema.
         */
        const val DB_VERSION = 9

        // Nombres de Tablas
        const val TABLE_USERS = "users"
        const val TABLE_RECOLECTORES = "recolectores"
        const val TABLE_ADMINS = "administrators"
    }

    /**
     * Crea las tablas iniciales de la base de datos.
     */
    private fun createTables(db: SQLiteDatabase) {

        // ==========================================================
        // 1. Tabla de Usuarios (Clientes) - (CORREGIDA: Añadido 'name')
        // ==========================================================
        db.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                email TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                phone_number TEXT
            )
        """
        )

        // ==========================================================
        // 1.5. Tabla de Administradores (NUEVA)
        // Almacena información sobre los administradores del sistema.
        // ==========================================================
        db.execSQL(
            """
            CREATE TABLE $TABLE_ADMINS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                name TEXT,
                is_super_admin INTEGER DEFAULT 0
            )
        """
        )

        // ==========================================================
        // 2. Tabla de Recolectores (Personal de Logística) - (CORREGIDA: email UNIQUE y 'sucursal')
        // ==========================================================
        db.execSQL(
            """
            CREATE TABLE $TABLE_RECOLECTORES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL, 
                password_hash TEXT NOT NULL,
                role TEXT NOT NULL CHECK(role IN ('CONDUCTOR', 'GESTOR', 'FUNCIONARIO', 'ANALISTA')),
                sucursal TEXT,
                is_active INTEGER DEFAULT 1
            )
        """
        )

        // ==========================================================
        // 3. Tabla de Direcciones
        // ==========================================================
        db.execSQL(
            """
            CREATE TABLE direcciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                ciudad TEXT NOT NULL,
                full_address TEXT NOT NULL,
                latitude REAL,
                longitude REAL,
                created_at INTEGER NOT NULL,
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
                peso REAL DEFAULT 0.0
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
                FOREIGN KEY(recolector_id) REFERENCES $TABLE_RECOLECTORES(id),
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
     * Inserta un usuario administrador de prueba en la tabla principal de usuarios (Users).
     * Esto asegura que el login funcione al buscar en la misma tabla que Clientes/Gestores.
     * * @param db Instancia de la base de datos abierta.
     */
    private fun insertDefaultAdmin(db: SQLiteDatabase) {
        // 🏆 ¡Importante! Aquí asumimos que tu tabla principal se llama "Users"
        // y que tiene las columnas necesarias para el rol y la zona.
        val TABLE_USERS = "Users"

        val cv = ContentValues().apply {
            put("name", "Administrador Principal")
            put("email", "admin@logistica.com")

            // El hash de "admin123" que obtuviste de tu función hashPassword()
            // Si tu función de hashing es SHA-256 (como la de LoginActivity),
            // este es el valor que estás utilizando:
            put("password_hash", "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918")

            // Columnas requeridas por la lógica de roles de tu aplicación:
            put("role", "ADMIN")
            put("sucursal", "CENTRAL")
            put("status", "ACTIVO")
            put("is_super_admin", 1)

            // Asegúrate de incluir aquí cualquier otra columna requerida por tu tabla Users (ej: phone_number).
        }
        try {
            // 🏆 CORRECCIÓN CRÍTICA: Cambiamos TABLE_ADMINS por TABLE_USERS
            db.insert(TABLE_USERS, null, cv)
            Log.d("DBHelper", "Admin insertado correctamente en la tabla $TABLE_USERS.")
        } catch (e: Exception) {
            Log.e("DBHelper", "Error al insertar admin en la tabla $TABLE_USERS: ${e.message}")
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Llama al método para crear todas las tablas
        createTables(db)

        // Inserta un administrador por defecto
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
            db.execSQL("DROP TABLE IF EXISTS $TABLE_ADMINS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")

            onCreate(db)
        }
    }
}