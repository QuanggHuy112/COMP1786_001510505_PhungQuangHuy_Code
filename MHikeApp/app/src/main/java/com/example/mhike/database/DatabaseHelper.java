package com.example.mhike.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.mhike.models.Hike;
import com.example.mhike.models.Observation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Database Info
    private static final String DB_NAME = "mhike.db";
    // bumped to 5 so onUpgrade runs to add missing columns
    private static final int DB_VERSION = 5;

    // Hikes Table & expected columns
    private static final String HIKES_TABLE = "hikes";
    private static final String COL_ID = "id";
    private static final String COL_HIKE_NAME = "name";
    private static final String COL_HIKE_LOCATION = "location";
    private static final String COL_HIKE_DATE = "date";
    private static final String COL_HIKE_PARKING = "parking";
    private static final String COL_HIKE_DISTANCE = "length";
    private static final String COL_HIKE_LEVEL = "difficulty";
    private static final String COL_HIKE_INFO = "description";
    private static final String COL_HIKE_WEATHER = "weather";
    private static final String COL_HIKE_TEAM = "group_size";

    // Observations Table & columns
    private static final String OBS_TABLE = "observations";
    private static final String COL_OBS_ID = "id";
    private static final String COL_HIKE_REF = "hike_id";
    private static final String COL_OBS_CONTENT = "observation_text";
    private static final String COL_OBS_TIMESTAMP = "time"; // primary name used in code
    private static final String COL_OBS_NOTES = "comments";

    // Create statements
    // For new installs: create both time and timestamp columns to avoid future ambiguity
    private static final String CREATE_HIKES_SQL =
            "CREATE TABLE " + HIKES_TABLE + "(" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_HIKE_NAME + " TEXT NOT NULL," +
                    COL_HIKE_LOCATION + " TEXT NOT NULL," +
                    COL_HIKE_DATE + " TEXT NOT NULL," +
                    COL_HIKE_PARKING + " INTEGER NOT NULL," +
                    COL_HIKE_DISTANCE + " REAL NOT NULL," +
                    COL_HIKE_LEVEL + " TEXT NOT NULL," +
                    COL_HIKE_INFO + " TEXT," +
                    COL_HIKE_WEATHER + " TEXT," +
                    COL_HIKE_TEAM + " INTEGER" +
                    ")";

    private static final String CREATE_OBS_SQL =
            "CREATE TABLE " + OBS_TABLE + "(" +
                    COL_OBS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_HIKE_REF + " INTEGER NOT NULL," +
                    COL_OBS_CONTENT + " TEXT NOT NULL," +
                    "time TEXT NOT NULL," +                 // primary time column
                    "timestamp TEXT NOT NULL," +            // duplicate variant to support legacy names
                    COL_OBS_NOTES + " TEXT," +
                    "FOREIGN KEY(" + COL_HIKE_REF + ") REFERENCES " +
                    HIKES_TABLE + "(" + COL_ID + ") ON DELETE CASCADE" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_HIKES_SQL);
        database.execSQL(CREATE_OBS_SQL);
    }

    @Override
    public void onConfigure(SQLiteDatabase database) {
        super.onConfigure(database);
        database.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * onUpgrade tries to be safe: add missing columns instead of dropping tables.
     * Also performs migrations between time <-> timestamp when necessary.
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVer, int newVer) {
        Log.i(TAG, "onUpgrade from " + oldVer + " to " + newVer);

        // ensure hikes columns
        safeAddColumn(database, HIKES_TABLE, COL_HIKE_PARKING, "INTEGER", "0");
        safeAddColumn(database, HIKES_TABLE, COL_HIKE_LEVEL, "TEXT", "'Easy'");
        safeAddColumn(database, HIKES_TABLE, COL_HIKE_WEATHER, "TEXT", "NULL");
        safeAddColumn(database, HIKES_TABLE, COL_HIKE_TEAM, "INTEGER", "0");
        safeAddColumn(database, HIKES_TABLE, COL_HIKE_INFO, "TEXT", "NULL");
        safeAddColumn(database, HIKES_TABLE, COL_HIKE_DISTANCE, "REAL", "0");

        // ensure observations columns (adds both `time` and `timestamp` if missing)
        safeAddColumn(database, OBS_TABLE, COL_HIKE_REF, "INTEGER", "0");
        safeAddColumn(database, OBS_TABLE, COL_OBS_CONTENT, "TEXT", "''");
        safeAddColumn(database, OBS_TABLE, COL_OBS_TIMESTAMP, "TEXT", "''"); // adds 'time' if missing
        safeAddColumn(database, OBS_TABLE, "timestamp", "TEXT", "''");       // ensure the other variant exists
        safeAddColumn(database, OBS_TABLE, COL_OBS_NOTES, "TEXT", "NULL");

        // Migrate values: if timestamp is empty but time has data, copy time -> timestamp
        try {
            database.execSQL("UPDATE " + OBS_TABLE + " SET timestamp = time WHERE (timestamp IS NULL OR timestamp = '') AND (time IS NOT NULL AND time <> '')");
            database.execSQL("UPDATE " + OBS_TABLE + " SET time = timestamp WHERE (time IS NULL OR time = '') AND (timestamp IS NOT NULL AND timestamp <> '')");
            Log.d(TAG, "Observation time/timestamp migration executed");
        } catch (Exception e) {
            Log.e(TAG, "Migration copy timestamp<->time failed: " + e.getMessage(), e);
        }
    }

    private void safeAddColumn(SQLiteDatabase db, String tableName, String columnName, String columnType, String defaultValueExpr) {
        if (columnExists(db, tableName, columnName)) {
            Log.d(TAG, "Column exists: " + tableName + "." + columnName);
            return;
        }
        try {
            String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType;
            if (defaultValueExpr != null && !defaultValueExpr.isEmpty()) {
                sql += " DEFAULT " + defaultValueExpr;
            }
            db.execSQL(sql);
            Log.i(TAG, "Added column " + columnName + " to " + tableName);
        } catch (Exception e) {
            Log.e(TAG, "Failed adding column " + columnName + ": " + e.getMessage(), e);
        }
    }

    private boolean columnExists(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String col = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    if (columnName.equalsIgnoreCase(col)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "columnExists error", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return false;
    }

    private void logTableInfo(SQLiteDatabase db, String tableName) {
        Cursor c = null;
        try {
            c = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            StringBuilder sb = new StringBuilder("PRAGMA table_info(" + tableName + "): ");
            if (c != null && c.moveToFirst()) {
                do {
                    sb.append("[")
                            .append(c.getString(c.getColumnIndexOrThrow("name")))
                            .append(":")
                            .append(c.getString(c.getColumnIndexOrThrow("type")))
                            .append("] ");
                } while (c.moveToNext());
            }
            Log.d(TAG, sb.toString());
        } catch (Exception e) {
            Log.e(TAG, "logTableInfo error", e);
        } finally {
            if (c != null) c.close();
        }
    }

    private Set<String> getExistingColumns(SQLiteDatabase db, String tableName) {
        Set<String> cols = new HashSet<>();
        Cursor c = null;
        try {
            c = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            if (c != null && c.moveToFirst()) {
                do {
                    String name = c.getString(c.getColumnIndexOrThrow("name"));
                    cols.add(name);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "getExistingColumns error", e);
        } finally {
            if (c != null) c.close();
        }
        return cols;
    }

    private String safeString(String s) {
        return s == null ? "" : s;
    }

    // ------------------ CRUD for Hike ------------------

    public long addHike(Hike hike) {
        SQLiteDatabase database = this.getWritableDatabase();

        // debug schema
        logTableInfo(database, HIKES_TABLE);

        long insertId = -1;
        try {
            Set<String> existingCols = getExistingColumns(database, HIKES_TABLE);

            ContentValues vals = new ContentValues();

            if (existingCols.contains(COL_HIKE_NAME)) {
                vals.put(COL_HIKE_NAME, safeString(hike.getName()));
            }
            if (existingCols.contains(COL_HIKE_LOCATION)) {
                vals.put(COL_HIKE_LOCATION, safeString(hike.getLocation()));
            }
            if (existingCols.contains(COL_HIKE_DATE)) {
                vals.put(COL_HIKE_DATE, safeString(hike.getDate()));
            }

            int parkingVal = hike.isParking() ? 1 : 0;
            if (existingCols.contains(COL_HIKE_PARKING)) {
                vals.put(COL_HIKE_PARKING, parkingVal);
            }
            if (existingCols.contains("parking_available")) {
                vals.put("parking_available", parkingVal);
            }
            if (existingCols.contains("has_parking")) {
                vals.put("has_parking", parkingVal);
            }

            if (existingCols.contains(COL_HIKE_DISTANCE)) {
                vals.put(COL_HIKE_DISTANCE, hike.getLength());
            } else if (existingCols.contains("distance")) {
                vals.put("distance", hike.getLength());
            }

            if (existingCols.contains(COL_HIKE_LEVEL)) {
                vals.put(COL_HIKE_LEVEL, hike.getDifficulty() != null ? hike.getDifficulty() : "Easy");
            } else if (existingCols.contains("level")) {
                vals.put("level", hike.getDifficulty() != null ? hike.getDifficulty() : "Easy");
            }

            if (existingCols.contains(COL_HIKE_INFO)) {
                vals.put(COL_HIKE_INFO, safeString(hike.getDescription()));
            } else if (existingCols.contains("info")) {
                vals.put("info", safeString(hike.getDescription()));
            }

            if (existingCols.contains(COL_HIKE_WEATHER)) {
                vals.put(COL_HIKE_WEATHER, safeString(hike.getWeather()));
            } else if (existingCols.contains("weather_info")) {
                vals.put("weather_info", safeString(hike.getWeather()));
            }

            if (existingCols.contains(COL_HIKE_TEAM)) {
                vals.put(COL_HIKE_TEAM, hike.getGroupSize());
            } else if (existingCols.contains("group")) {
                vals.put("group", hike.getGroupSize());
            }

            // Defensive defaults for NOT NULL columns
            if (existingCols.contains(COL_HIKE_PARKING) && !vals.containsKey(COL_HIKE_PARKING)) {
                vals.put(COL_HIKE_PARKING, 0);
            }
            if (existingCols.contains("parking_available") && !vals.containsKey("parking_available")) {
                vals.put("parking_available", 0);
            }
            if (existingCols.contains(COL_HIKE_DISTANCE) && !vals.containsKey(COL_HIKE_DISTANCE)) {
                vals.put(COL_HIKE_DISTANCE, 0);
            }
            if (existingCols.contains(COL_HIKE_LEVEL) && !vals.containsKey(COL_HIKE_LEVEL)) {
                vals.put(COL_HIKE_LEVEL, "Easy");
            }
            if (existingCols.contains(COL_HIKE_NAME) && !vals.containsKey(COL_HIKE_NAME)) {
                vals.put(COL_HIKE_NAME, "");
            }
            if (existingCols.contains(COL_HIKE_LOCATION) && !vals.containsKey(COL_HIKE_LOCATION)) {
                vals.put(COL_HIKE_LOCATION, "");
            }
            if (existingCols.contains(COL_HIKE_DATE) && !vals.containsKey(COL_HIKE_DATE)) {
                vals.put(COL_HIKE_DATE, "");
            }

            insertId = database.insert(HIKES_TABLE, null, vals);
            Log.d(TAG, "addHike result id=" + insertId);
            if (insertId > 0) {
                try { hike.setId((int) insertId); } catch (Exception ignored) {}
            }
        } catch (SQLiteException sqle) {
            Log.e(TAG, "addHike SQLiteException", sqle);
        } catch (Exception e) {
            Log.e(TAG, "addHike error", e);
        } finally {
            database.close();
        }
        return insertId;
    }

    public Hike getHike(int hikeId) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor dataCursor = null;
        Hike hikeData = null;
        try {
            dataCursor = database.query(HIKES_TABLE, null, COL_ID + "=?",
                    new String[]{String.valueOf(hikeId)}, null, null, null);

            if (dataCursor != null && dataCursor.moveToFirst()) {
                hikeData = buildHikeFromCursor(dataCursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "getHike error", e);
        } finally {
            if (dataCursor != null) dataCursor.close();
            database.close();
        }
        return hikeData;
    }

    public List<Hike> getAllHikes() {
        List<Hike> allHikes = new ArrayList<>();
        String querySQL = "SELECT * FROM " + HIKES_TABLE + " ORDER BY " + COL_HIKE_DATE + " DESC";
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor dataCursor = null;
        try {
            dataCursor = database.rawQuery(querySQL, null);
            if (dataCursor != null && dataCursor.moveToFirst()) {
                do {
                    allHikes.add(buildHikeFromCursor(dataCursor));
                } while (dataCursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "getAllHikes error", e);
        } finally {
            if (dataCursor != null) dataCursor.close();
            database.close();
        }
        return allHikes;
    }

    public int updateHike(Hike hike) {
        SQLiteDatabase database = this.getWritableDatabase();
        int affectedRows = 0;
        try {
            Set<String> existingCols = getExistingColumns(database, HIKES_TABLE);
            ContentValues vals = new ContentValues();

            if (existingCols.contains(COL_HIKE_NAME)) vals.put(COL_HIKE_NAME, safeString(hike.getName()));
            if (existingCols.contains(COL_HIKE_LOCATION)) vals.put(COL_HIKE_LOCATION, safeString(hike.getLocation()));
            if (existingCols.contains(COL_HIKE_DATE)) vals.put(COL_HIKE_DATE, safeString(hike.getDate()));

            int parkingVal = hike.isParking() ? 1 : 0;
            if (existingCols.contains(COL_HIKE_PARKING)) vals.put(COL_HIKE_PARKING, parkingVal);
            if (existingCols.contains("parking_available")) vals.put("parking_available", parkingVal);
            if (existingCols.contains("has_parking")) vals.put("has_parking", parkingVal);

            if (existingCols.contains(COL_HIKE_DISTANCE)) vals.put(COL_HIKE_DISTANCE, hike.getLength());
            if (existingCols.contains(COL_HIKE_LEVEL)) vals.put(COL_HIKE_LEVEL, hike.getDifficulty() != null ? hike.getDifficulty() : "Easy");
            if (existingCols.contains(COL_HIKE_INFO)) vals.put(COL_HIKE_INFO, safeString(hike.getDescription()));
            if (existingCols.contains(COL_HIKE_WEATHER)) vals.put(COL_HIKE_WEATHER, safeString(hike.getWeather()));
            if (existingCols.contains(COL_HIKE_TEAM)) vals.put(COL_HIKE_TEAM, hike.getGroupSize());

            affectedRows = database.update(HIKES_TABLE, vals, COL_ID + "=?",
                    new String[]{String.valueOf(hike.getId())});
        } catch (Exception e) {
            Log.e(TAG, "updateHike error", e);
        } finally {
            database.close();
        }
        return affectedRows;
    }

    public void deleteHike(int hikeId) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            database.delete(HIKES_TABLE, COL_ID + "=?", new String[]{String.valueOf(hikeId)});
        } catch (Exception e) {
            Log.e(TAG, "deleteHike error", e);
        } finally {
            database.close();
        }
    }

    public void deleteAllHikes() {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            database.delete(OBS_TABLE, null, null);
            database.delete(HIKES_TABLE, null, null);
        } catch (Exception e) {
            Log.e(TAG, "deleteAllHikes error", e);
        } finally {
            database.close();
        }
    }

    public List<Hike> searchHikesByName(String searchName) {
        List<Hike> foundHikes = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor dataCursor = null;
        try {
            dataCursor = database.query(HIKES_TABLE, null,
                    COL_HIKE_NAME + " LIKE ?",
                    new String[]{searchName + "%"}, null, null,
                    COL_HIKE_DATE + " DESC");

            if (dataCursor != null && dataCursor.moveToFirst()) {
                do {
                    foundHikes.add(buildHikeFromCursor(dataCursor));
                } while (dataCursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "searchHikesByName error", e);
        } finally {
            if (dataCursor != null) dataCursor.close();
            database.close();
        }
        return foundHikes;
    }

    public List<Hike> advancedSearch(String name, String location, String distance, String date) {
        List<Hike> foundHikes = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor dataCursor = null;
        try {
            StringBuilder whereClause = new StringBuilder("1=1");
            List<String> whereArgs = new ArrayList<>();

            if (name != null && !name.isEmpty()) {
                whereClause.append(" AND ").append(COL_HIKE_NAME).append(" LIKE ?");
                whereArgs.add("%" + name + "%");
            }

            if (location != null && !location.isEmpty()) {
                whereClause.append(" AND ").append(COL_HIKE_LOCATION).append(" LIKE ?");
                whereArgs.add("%" + location + "%");
            }

            if (distance != null && !distance.isEmpty()) {
                try {
                    double distVal = Double.parseDouble(distance);
                    whereClause.append(" AND ").append(COL_HIKE_DISTANCE).append(" = ?");
                    whereArgs.add(String.valueOf(distVal));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }

            if (date != null && !date.isEmpty()) {
                whereClause.append(" AND ").append(COL_HIKE_DATE).append(" LIKE ?");
                whereArgs.add("%" + date + "%");
            }

            dataCursor = database.query(HIKES_TABLE, null, whereClause.toString(),
                    whereArgs.toArray(new String[0]), null, null, COL_HIKE_DATE + " DESC");

            if (dataCursor != null && dataCursor.moveToFirst()) {
                do {
                    foundHikes.add(buildHikeFromCursor(dataCursor));
                } while (dataCursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "advancedSearch error", e);
        } finally {
            if (dataCursor != null) dataCursor.close();
            database.close();
        }
        return foundHikes;
    }

    // ------------------ Observations CRUD (defensive) ------------------

    public long addObservation(Observation obs) {
        SQLiteDatabase database = this.getWritableDatabase();
        long insertId = -1;
        try {
            // debug schema
            logTableInfo(database, OBS_TABLE);

            Set<String> existingCols = getExistingColumns(database, OBS_TABLE);
            ContentValues vals = new ContentValues();

            // put only if column exists (support legacy/variant names)
            if (existingCols.contains(COL_HIKE_REF)) vals.put(COL_HIKE_REF, obs.getHikeId());
            else if (existingCols.contains("hike")) vals.put("hike", obs.getHikeId());

            if (existingCols.contains(COL_OBS_CONTENT)) vals.put(COL_OBS_CONTENT, safeString(obs.getObservationText()));
            else if (existingCols.contains("content")) vals.put("content", safeString(obs.getObservationText()));

            // time/timestamp variants - set all available to avoid NOT NULL fails
            if (existingCols.contains("time")) vals.put("time", safeString(obs.getTime()));
            if (existingCols.contains("timestamp")) vals.put("timestamp", safeString(obs.getTime()));
            if (existingCols.contains("obs_time")) vals.put("obs_time", safeString(obs.getTime()));
            if (existingCols.contains(COL_OBS_TIMESTAMP)) vals.put(COL_OBS_TIMESTAMP, safeString(obs.getTime())); // redundant but safe

            if (existingCols.contains(COL_OBS_NOTES)) vals.put(COL_OBS_NOTES, safeString(obs.getComments()));
            else if (existingCols.contains("comments")) vals.put("comments", safeString(obs.getComments()));
            else if (existingCols.contains("notes")) vals.put("notes", safeString(obs.getComments()));

            // Defensive defaults for NOT NULL
            if (existingCols.contains(COL_HIKE_REF) && !vals.containsKey(COL_HIKE_REF)) vals.put(COL_HIKE_REF, 0);
            if (existingCols.contains("time") && !vals.containsKey("time")) vals.put("time", "");
            if (existingCols.contains("timestamp") && !vals.containsKey("timestamp")) vals.put("timestamp", "");
            if (existingCols.contains(COL_OBS_CONTENT) && !vals.containsKey(COL_OBS_CONTENT)) vals.put(COL_OBS_CONTENT, "");

            insertId = database.insert(OBS_TABLE, null, vals);
            Log.d(TAG, "addObservation result id=" + insertId);
            if (insertId > 0) obs.setId((int) insertId);
        } catch (SQLiteException sqle) {
            Log.e(TAG, "addObservation SQLiteException", sqle);
        } catch (Exception e) {
            Log.e(TAG, "addObservation error", e);
        } finally {
            database.close();
        }
        return insertId;
    }

    public Observation getObservation(int obsId) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor dataCursor = null;
        Observation obsData = null;
        try {
            dataCursor = database.query(OBS_TABLE, null, COL_OBS_ID + "=?",
                    new String[]{String.valueOf(obsId)}, null, null, null);

            if (dataCursor != null && dataCursor.moveToFirst()) {
                obsData = buildObsFromCursor(dataCursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "getObservation error", e);
        } finally {
            if (dataCursor != null) dataCursor.close();
            database.close();
        }
        return obsData;
    }

    public List<Observation> getObservationsByHike(int hikeId) {
        List<Observation> allObs = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor dataCursor = null;
        try {
            // choose a safe sort column: prefer time variants, fallback to id
            Set<String> existingCols = getExistingColumns(database, OBS_TABLE);
            String orderBy = COL_OBS_ID + " DESC";
            if (existingCols.contains("time")) orderBy = "time DESC";
            else if (existingCols.contains("timestamp")) orderBy = "timestamp DESC";
            else if (existingCols.contains("obs_time")) orderBy = "obs_time DESC";

            // build where clause
            String where = COL_HIKE_REF + "=?";
            if (!existingCols.contains(COL_HIKE_REF) && existingCols.contains("hike")) where = "hike=?";

            dataCursor = database.query(OBS_TABLE, null,
                    where,
                    new String[]{String.valueOf(hikeId)}, null, null,
                    orderBy);

            if (dataCursor != null && dataCursor.moveToFirst()) {
                do {
                    allObs.add(buildObsFromCursor(dataCursor));
                } while (dataCursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "getObservationsByHike error", e);
        } finally {
            if (dataCursor != null) dataCursor.close();
            database.close();
        }
        return allObs;
    }

    public int updateObservation(Observation obs) {
        SQLiteDatabase database = this.getWritableDatabase();
        int affectedRows = 0;
        try {
            Set<String> existingCols = getExistingColumns(database, OBS_TABLE);
            ContentValues vals = new ContentValues();

            if (existingCols.contains(COL_HIKE_REF)) vals.put(COL_HIKE_REF, obs.getHikeId());
            else if (existingCols.contains("hike")) vals.put("hike", obs.getHikeId());

            if (existingCols.contains(COL_OBS_CONTENT)) vals.put(COL_OBS_CONTENT, safeString(obs.getObservationText()));
            if (existingCols.contains("time")) vals.put("time", safeString(obs.getTime()));
            if (existingCols.contains("timestamp")) vals.put("timestamp", safeString(obs.getTime()));
            if (existingCols.contains("obs_time")) vals.put("obs_time", safeString(obs.getTime()));
            if (existingCols.contains(COL_OBS_NOTES)) vals.put(COL_OBS_NOTES, safeString(obs.getComments()));

            affectedRows = database.update(OBS_TABLE, vals, COL_OBS_ID + "=?",
                    new String[]{String.valueOf(obs.getId())});
        } catch (Exception e) {
            Log.e(TAG, "updateObservation error", e);
        } finally {
            database.close();
        }
        return affectedRows;
    }

    public void deleteObservation(int obsId) {
        SQLiteDatabase database = this.getWritableDatabase();
        try {
            database.delete(OBS_TABLE, COL_OBS_ID + "=?", new String[]{String.valueOf(obsId)});
        } catch (Exception e) {
            Log.e(TAG, "deleteObservation error", e);
        } finally {
            database.close();
        }
    }

    // ------------------ Cursor -> Model helpers ------------------

    private Hike buildHikeFromCursor(Cursor dataCursor) {
        Hike hikeData = new Hike();
        try { hikeData.setId(dataCursor.getInt(dataCursor.getColumnIndexOrThrow(COL_ID))); } catch (Exception e) { hikeData.setId(0); }
        try { hikeData.setName(dataCursor.getString(dataCursor.getColumnIndexOrThrow(COL_HIKE_NAME))); } catch (Exception e) { hikeData.setName(""); }
        try { hikeData.setLocation(dataCursor.getString(dataCursor.getColumnIndexOrThrow(COL_HIKE_LOCATION))); } catch (Exception e) { hikeData.setLocation(""); }
        try { hikeData.setDate(dataCursor.getString(dataCursor.getColumnIndexOrThrow(COL_HIKE_DATE))); } catch (Exception e) { hikeData.setDate(""); }

        // Read parking: prefer parking_available (legacy), fallback to parking
        try {
            int val = -1;
            if (columnIndexExists(dataCursor, "parking_available")) {
                val = dataCursor.getInt(dataCursor.getColumnIndexOrThrow("parking_available"));
            } else if (columnIndexExists(dataCursor, COL_HIKE_PARKING)) {
                val = dataCursor.getInt(dataCursor.getColumnIndexOrThrow(COL_HIKE_PARKING));
            }
            hikeData.setParking(val == 1);
        } catch (Exception e) {
            hikeData.setParking(false);
        }

        try { hikeData.setLength(dataCursor.getDouble(dataCursor.getColumnIndexOrThrow(COL_HIKE_DISTANCE))); } catch (Exception e) { hikeData.setLength(0); }
        try { hikeData.setDifficulty(dataCursor.getString(dataCursor.getColumnIndexOrThrow(COL_HIKE_LEVEL))); } catch (Exception e) { hikeData.setDifficulty("Easy"); }
        try { hikeData.setDescription(dataCursor.getString(dataCursor.getColumnIndexOrThrow(COL_HIKE_INFO))); } catch (Exception e) { hikeData.setDescription(""); }
        try { hikeData.setWeather(dataCursor.getString(dataCursor.getColumnIndexOrThrow(COL_HIKE_WEATHER))); } catch (Exception e) { hikeData.setWeather(""); }
        try { hikeData.setGroupSize(dataCursor.getInt(dataCursor.getColumnIndexOrThrow(COL_HIKE_TEAM))); } catch (Exception e) { hikeData.setGroupSize(0); }
        return hikeData;
    }

    private boolean columnIndexExists(Cursor c, String colName) {
        try {
            c.getColumnIndexOrThrow(colName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Observation buildObsFromCursor(Cursor dataCursor) {
        Observation obsData = new Observation();
        try { obsData.setId(dataCursor.getInt(dataCursor.getColumnIndexOrThrow(COL_OBS_ID))); } catch (Exception e) { obsData.setId(0); }
        try { obsData.setHikeId(dataCursor.getInt(dataCursor.getColumnIndexOrThrow(COL_HIKE_REF))); } catch (Exception e) {
            // fallback variant
            try { obsData.setHikeId(dataCursor.getInt(dataCursor.getColumnIndexOrThrow("hike"))); } catch (Exception ex) { obsData.setHikeId(0); }
        }
        try { obsData.setObservationText(dataCursor.getString(dataCursor.getColumnIndexOrThrow(COL_OBS_CONTENT))); } catch (Exception e) { obsData.setObservationText(""); }

        // time/time variant
        String timeVal = "";
        try {
            if (columnIndexExists(dataCursor, "time")) timeVal = dataCursor.getString(dataCursor.getColumnIndexOrThrow("time"));
            else if (columnIndexExists(dataCursor, "timestamp")) timeVal = dataCursor.getString(dataCursor.getColumnIndexOrThrow("timestamp"));
            else if (columnIndexExists(dataCursor, "obs_time")) timeVal = dataCursor.getString(dataCursor.getColumnIndexOrThrow("obs_time"));
        } catch (Exception e) {
            timeVal = "";
        }
        obsData.setTime(timeVal);

        try { obsData.setComments(dataCursor.getString(dataCursor.getColumnIndexOrThrow(COL_OBS_NOTES))); } catch (Exception e) {
            try { obsData.setComments(dataCursor.getString(dataCursor.getColumnIndexOrThrow("comments"))); } catch (Exception ex) { obsData.setComments(""); }
        }
        return obsData;
    }
}
