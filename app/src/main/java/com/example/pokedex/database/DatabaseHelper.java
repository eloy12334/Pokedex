package com.example.pokedex.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pokedex.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_FAVORITOS = "favoritos";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NOMBRE = "nombre";
    private static final String COLUMN_IMAGEN = "imagen";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String crearTabla = "CREATE TABLE " + TABLE_FAVORITOS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NOMBRE + " TEXT NOT NULL, " +
                COLUMN_IMAGEN + " TEXT)";
        db.execSQL(crearTabla);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // En esta versión no se eliminan los datos existentes.
    }

    public boolean agregarFavorito(int id, String nombre, String imagen) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, id);
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_IMAGEN, imagen);

        long resultado = db.insertWithOnConflict(
                TABLE_FAVORITOS,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );

        return resultado != -1;
    }

    public boolean eliminarFavorito(int id) {
        SQLiteDatabase db = getWritableDatabase();

        int resultado = db.delete(
                TABLE_FAVORITOS,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        return resultado > 0;
    }

    public boolean esFavorito(int id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_FAVORITOS,
                new String[]{COLUMN_ID},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        boolean existe = cursor.moveToFirst();
        cursor.close();

        return existe;
    }

    public List<Integer> obtenerIdsFavoritos() {
        List<Integer> favoritos = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_FAVORITOS,
                new String[]{COLUMN_ID},
                null,
                null,
                null,
                null,
                COLUMN_ID + " ASC"
        );

        while (cursor.moveToNext()) {
            favoritos.add(cursor.getInt(0));
        }

        cursor.close();
        return favoritos;
    }

    public int contarFavoritos() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_FAVORITOS,
                null
        );

        int total = 0;

        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }

        cursor.close();
        return total;
    }
}
