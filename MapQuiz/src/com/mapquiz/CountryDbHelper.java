package com.mapquiz;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class CountryDbHelper extends SQLiteOpenHelper implements BaseColumns {

	public static final String TABLE_NAME = "countries";
	public static final String COUNTRY = "country";

	public CountryDbHelper(Context context) {
		super(context, CountriesProvider.DB_COUNTRIES, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE " + TABLE_NAME
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + COUNTRY
				+ " TEXT);");

		ContentValues values = new ContentValues();

		values.put(COUNTRY, "Россия");
		db.insert(TABLE_NAME, COUNTRY, values);

		values.put(COUNTRY, "Австралия");
		db.insert(TABLE_NAME, COUNTRY, values);

		values.put(COUNTRY, "Китай");
		db.insert(TABLE_NAME, COUNTRY, values);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}
}