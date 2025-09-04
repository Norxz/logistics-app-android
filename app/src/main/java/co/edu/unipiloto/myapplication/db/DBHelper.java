package co.edu.unipiloto.myapplication.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "logistica.db";
    public static final int DB_VERSION = 1;

    public DBHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "email TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL)");

        db.execSQL("CREATE TABLE solicitudes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "direccion TEXT NOT NULL," +
                "fecha TEXT NOT NULL," +
                "franja TEXT NOT NULL," +
                "notas TEXT," +
                "estado TEXT NOT NULL DEFAULT 'PENDIENTE'," +
                "created_at INTEGER NOT NULL," +
                "FOREIGN KEY(user_id) REFERENCES users(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS solicitudes");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}
