package com.mapquiz;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class CountriesProvider extends ContentProvider {

	public static final String DB_COUNTRIES = "countries.db";

	public static final Uri CONTENT_URI = Uri
			.parse("content://com.mapquiz.countriesprovider/country");
	public static final int URI_CODE = 1;
	public static final int URI_CODE_ID = 2;

	private static final UriMatcher mUriMatcher;
	private static HashMap<String, String> mCountryMap;

	private SQLiteDatabase db;

	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI("com.mapquiz.countriesprovider",
				CountryDbHelper.TABLE_NAME, URI_CODE);
		mUriMatcher.addURI("com.mapquiz.countriesprovider",
				CountryDbHelper.TABLE_NAME + "/#", URI_CODE_ID);

		mCountryMap = new HashMap<String, String>();
		mCountryMap.put(CountryDbHelper._ID, CountryDbHelper._ID);
		mCountryMap.put(CountryDbHelper.COUNTRY, CountryDbHelper.COUNTRY);
	}

	public String getDbName() {
		return (DB_COUNTRIES);
	}

	@Override
	public boolean onCreate() {

		db = (new CountryDbHelper(getContext())).getWritableDatabase();
		return (db == null) ? false : true;
	}

	@Override
	public Cursor query(Uri url, String[] projection, String selection,
			String[] selectionArgs, String sort) {

		Cursor c = db.query(CountryDbHelper.TABLE_NAME, projection, selection,
				selectionArgs, null, null, sort);
		c.setNotificationUri(getContext().getContentResolver(), url);
		return c;
	}

	@Override
	public Uri insert(Uri url, ContentValues inValues) {

		ContentValues values = new ContentValues(inValues);

		long rowId = db.insert(CountryDbHelper.TABLE_NAME,
				CountryDbHelper.COUNTRY, values);
		if (rowId > 0) {
			Uri uri = ContentUris.withAppendedId(CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		} else {
			throw new SQLException("Failed to insert row into " + url);
		}
	}

	@Override
	public int delete(Uri url, String where, String[] whereArgs) {
		int retVal = db.delete(CountryDbHelper.TABLE_NAME, where, whereArgs);

		getContext().getContentResolver().notifyChange(url, null);
		return retVal;
	}

	@Override
	public int update(Uri url, ContentValues values, String where,
			String[] whereArgs) {
		int retVal = db.update(CountryDbHelper.TABLE_NAME, values, where,
				whereArgs);

		getContext().getContentResolver().notifyChange(url, null);
		return retVal;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}
}
