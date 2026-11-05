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
    private static final int DATABASE_VERSION = 2;

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

        String crearTablaEquipo = "CREATE TABLE equipos (" +
                "slot INTEGER PRIMARY KEY, " +
                "pokemon_id INTEGER NOT NULL, " +
                "nombre TEXT NOT NULL)";
        db.execSQL(crearTablaEquipo);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS equipos (" +
                    "slot INTEGER PRIMARY KEY, " +
                    "pokemon_id INTEGER NOT NULL, " +
                    "nombre TEXT NOT NULL)");
        }
    }

    public boolean guardarPokemonEquipo(int slot, int pokemonId, String nombre) {
        if (slot < 1 || slot > 6 || pokemonId < 1 || pokemonId > 151) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("slot", slot);
        values.put("pokemon_id", pokemonId);
        values.put("nombre", nombre);

        return db.insertWithOnConflict(
                "equipos", null, values, SQLiteDatabase.CONFLICT_REPLACE
        ) != -1;
    }

    public boolean eliminarPokemonEquipo(int slot) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("equipos", "slot=?",
                new String[]{String.valueOf(slot)}) > 0;
    }

    public Cursor obtenerEquipo() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query("equipos",
                new String[]{"slot", "pokemon_id", "nombre"},
                null, null, null, null, "slot ASC");
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
