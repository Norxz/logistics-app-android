package co.edu.unipiloto.myapplication.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "logistica.db";
    public static final int DB_VERSION = 2;

    public DBHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "email TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "role TEXT NOT NULL CHECK(role IN ('CLIENTE','RECOLECTOR'))," +
                "zona TEXT" +
                ")");

        db.execSQL("CREATE TABLE solicitudes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "direccion TEXT NOT NULL," +
                "fecha TEXT NOT NULL," +
                "franja TEXT NOT NULL," +
                "notas TEXT," +
                "zona TEXT NOT NULL," +
                "recolector_id INTEGER," +
                "estado TEXT NOT NULL DEFAULT 'PENDIENTE'," +
                "created_at INTEGER NOT NULL," +
                "en_camino_at INTEGER," +
                "entregado_at INTEGER," +
                "confirmado_at INTEGER," +
                "FOREIGN KEY(user_id) REFERENCES users(id)," +
                "FOREIGN KEY(recolector_id) REFERENCES users(id))");

        // índices útiles
        db.execSQL("CREATE INDEX idx_users_role_zona ON users(role, zona)");
        db.execSQL("CREATE INDEX idx_sol_zona ON solicitudes(zona)");
        db.execSQL("CREATE INDEX idx_sol_recolector ON solicitudes(recolector_id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        if (oldV < 2) {
            db.execSQL("ALTER TABLE solicitudes ADD COLUMN en_camino_at INTEGER");
            db.execSQL("ALTER TABLE solicitudes ADD COLUMN entregado_at INTEGER");
            db.execSQL("ALTER TABLE solicitudes ADD COLUMN confirmacion_at INTEGER");
        }
    }
}
