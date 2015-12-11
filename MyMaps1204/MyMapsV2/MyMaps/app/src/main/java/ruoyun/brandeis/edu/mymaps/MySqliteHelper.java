package ruoyun.brandeis.edu.mymaps;

/**
 * Created by reallifejasmine on 11/16/15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MySqliteHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "ParkLocation";

    public MySqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create  table
        //modifide by, add indoor level information
        String CREATE_LOCATION_TABLE = "CREATE TABLE IF NOT EXISTS newlocations ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "lat TEXT , "+
                "long TEXT , "+
                "level TEXT )";

        // create table
        db.execSQL(CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS newlocations");

        // create fresh locations table
        this.onCreate(db);
    }


    // Locations table name
    // modified by, change the table name
    private static final String TABLE_LOCATIONS = "newlocations";

    // Locations Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LONG = "long";
    // added by
    private static final String KEY_LEVEL = "level";

    private static final String[] COLUMNS = {KEY_ID,KEY_LAT,KEY_LONG,KEY_LEVEL};


    //modified by, add level
    public void addLocation(LatLng latLng, String level){
        //for logging
        Log.d("addLocation", latLng.toString());

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_LAT, latLng.latitude); // get lat
        values.put(KEY_LONG, latLng.longitude); // get long
        values.put(KEY_LEVEL, level); // get level

        // 3. insert
        db.insert(TABLE_LOCATIONS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }


    //added by
    public void deleteLocations(){
        //for logging
        Log.d("deleteLocations", "delete all locations in table");

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(TABLE_LOCATIONS, // table
                null, //whereClause
                null);

        // 3. close
        db.close();
    }

    //added by
    public List<String> queryLocation() {
        //for logging
        Log.d("queryLocation", "query Location in table");
        ArrayList<String> arr = new ArrayList<String>();

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. get the first row in table
        Cursor cursor = db.query(
                TABLE_LOCATIONS,
                null,
                null,
                null,
                null,
                null,
                "id",
                "1"
        );

        if(cursor.moveToFirst()) {
            arr.add(cursor.getString(cursor.getColumnIndex(KEY_LAT)));
            arr.add(cursor.getString(cursor.getColumnIndex(KEY_LONG)));
            arr.add(cursor.getString(cursor.getColumnIndex(KEY_LEVEL)));
        }
        cursor.close();

        // 3. close
        db.close();
        return arr;
    }

    //added by
    public int countLocations() {
        //for logging
        Log.d("countLocations", "query count of Locations in table");

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. get the first row in table
        Cursor cursor = db.query(
                TABLE_LOCATIONS,
                null,
                null,
                null,
                null,
                null,
                null
        );

        int count = 0;

        if(cursor.moveToFirst()) {
            do{
                count++;
            }while(cursor.moveToNext());
        }
        cursor.close();

        // 3. close
        db.close();
        return count;
    }
}


