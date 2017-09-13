package com.mapquiz;

import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.MapView;
import ru.yandex.yandexmapkit.OverlayManager;
import ru.yandex.yandexmapkit.map.GeoCode;
import ru.yandex.yandexmapkit.map.GeoCodeListener;
import ru.yandex.yandexmapkit.map.MapLayer;
import ru.yandex.yandexmapkit.net.Downloader;
import ru.yandex.yandexmapkit.overlay.Overlay;
import ru.yandex.yandexmapkit.overlay.balloon.BalloonItem;
import ru.yandex.yandexmapkit.overlay.balloon.OnBalloonListener;
import ru.yandex.yandexmapkit.utils.GeoPoint;
import ru.yandex.yandexmapkit.utils.ScreenPoint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MapQuizActivity extends Activity {

	private OverlayGeoCode overlayGeoCode;
	private MapController mapController;

	private Cursor cursor;
	private static final String[] content = new String[] { CountryDbHelper._ID,
			CountryDbHelper.COUNTRY };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.map_quiz);

			cursor = getContentResolver().query(CountriesProvider.CONTENT_URI,
					content, null, null, null);
			cursor.moveToFirst();

			final MapView mapView = (MapView) findViewById(R.id.map);

			mapController = mapView.getMapController();
			mapController.setZoomCurrent(1);

			OverlayManager overlayManager = mapController.getOverlayManager();
			overlayManager.getMyLocation().setEnabled(false);

			TextView textView = (TextView) findViewById(R.id.text_view);

			overlayGeoCode = new OverlayGeoCode(mapController, textView,
					savedInstanceState, cursor);

			overlayManager.addOverlay(overlayGeoCode);

		} catch (Throwable t) {
			Toast.makeText(this, t.toString(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedlnstanceState) {

		savedlnstanceState.putAll(overlayGeoCode.getState());
		super.onSaveInstanceState(savedlnstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, 0, 0, "Настройки");
		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent intent = new Intent();
		intent.setClass(this, PreferencesActivity.class);
		startActivity(intent);

		return true;
	}

	@Override
	public void onResume() {
		super.onResume();

		try {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);

			String layer = prefs.getString(getString(R.string.map_layer), null);

			if (layer != null) {
				int layerNumber = 0;

				switch (layer) {
				case "Схема":
					layerNumber = 0;
					break;
				case "Спутник":
					layerNumber = 1;
					break;
				case "Народная":
					layerNumber = 2;
					break;
				}
				mapController.setCurrentMapLayer((MapLayer) mapController
						.getListMapLayer().get(layerNumber));
			}
		} catch (Throwable t) {
			Toast.makeText(this, t.toString(), Toast.LENGTH_LONG).show();
		}
	}

}

class OverlayGeoCode extends Overlay implements GeoCodeListener,
		OnBalloonListener {

	private MapController mapController;
	private Context context;

	private BalloonItem balloonItem;
	private Downloader downloader;

	private String country;
	private TextView textView;

	private boolean correct;
	private boolean balloonIsOn;

	private Cursor cursor;

	public OverlayGeoCode(MapController mapController, TextView textView,
			Bundle savedInstanceState, Cursor cursor) {
		super(mapController);

		this.mapController = mapController;
		context = mapController.getContext();

		GeoPoint balloonGeoPoint = new GeoPoint();

		balloonItem = new BalloonItem(context, balloonGeoPoint);
		balloonItem.setOnBalloonListener(this);
		downloader = mapController.getDownloader();

		this.textView = textView;
		balloonIsOn = false;

		this.cursor = cursor;

		Double centerLat = Double.NaN;
		Double centerLon = Double.NaN;

		if (savedInstanceState != null) {

			cursor.moveToPosition(savedInstanceState.getInt("position"));

			balloonIsOn = savedInstanceState.getBoolean("balloonIsOn");
			mapController
					.setZoomCurrent(savedInstanceState.getFloat("zoom", 1));

			centerLat = savedInstanceState.getDouble("centerLat", Double.NaN);
			centerLon = savedInstanceState.getDouble("centerLon", Double.NaN);
		}

		textView.setText("Где находится " + cursor.getString(1) + "?");

		if (balloonIsOn) {

			country = savedInstanceState.getString("country");
			balloonItem.setText(country);

			balloonGeoPoint.setLat(savedInstanceState.getDouble("balloonLat"));
			balloonGeoPoint.setLon(savedInstanceState.getDouble("balloonLon"));

			balloonItem.setGeoPoint(balloonGeoPoint);

			mapController.setPositionNoAnimationTo(balloonGeoPoint);
			mapController.showBalloon(balloonItem);

		} else if ((centerLat != Double.NaN) && (centerLon != Double.NaN)) {

			GeoPoint centerGeoPoint = new GeoPoint();
			centerGeoPoint.setLat(centerLat);
			centerGeoPoint.setLon(centerLon);

			mapController.setPositionNoAnimationTo(centerGeoPoint);
		}

	}

	@Override
	public boolean onSingleTapUp(float x, float y) {
		try {
			GeoPoint geoPoint = getMapController().getGeoPoint(
					new ScreenPoint(x, y));

			mapController.hideBalloon();
			balloonIsOn = false;
			balloonItem.setGeoPoint(geoPoint);

			downloader.getGeoCode(this, geoPoint);

		} catch (Throwable t) {
			Toast.makeText(context, "Exception: " + t, Toast.LENGTH_LONG)
					.show();
		}
		return true;
	}

	@Override
	public boolean onFinishGeoCode(final GeoCode geoCode) {
		if (geoCode == null) {
			return true;
		}
		try {
			if (geoCode.getKind().equals("country")
					|| geoCode.getKind().equals("hydro")
					|| geoCode.getKind().equals("other")) {

				country = geoCode.getDisplayName();
			} else {
				country = geoCode.getSubtitle();
			}

			if (country != null) {
				int i = country.lastIndexOf(',');
				if (i != -1) {
					country = country.substring(i + 1);
				}
			}

			if ((country == null) || (country.equals(""))) {
				country = "NULL";
			}

			country = country.trim();
			correct = country.equals(cursor.getString(1));

			if (correct) {
				country = "Правильно! Нажмите сюда для следующей загадки";
			} else {
				country = "Неправильно, это " + country;
			}

			balloonItem.setText(country);
			mapController.showBalloon(balloonItem);
			balloonIsOn = true;

		} catch (Throwable t) {
			Toast.makeText(context, "Exception: " + t, Toast.LENGTH_LONG)
					.show();
		}
		return true;
	}

	@Override
	public void onBalloonViewClick(BalloonItem arg0, View arg1) {

		mapController.hideBalloon();
		balloonIsOn = false;

		if (correct) {
			if (!cursor.moveToNext()) {
				cursor.moveToFirst();
			}
			textView.setText("Где находится " + cursor.getString(1) + "?");
		}
	}

	public Bundle getState() {

		Bundle savedInstanceState = new Bundle();

		savedInstanceState.putInt("position", cursor.getPosition());

		savedInstanceState.putBoolean("balloonIsOn", balloonIsOn);
		savedInstanceState.putString("country", country);

		savedInstanceState.putDouble("balloonLat", balloonItem.getGeoPoint()
				.getLat());
		savedInstanceState.putDouble("balloonLon", balloonItem.getGeoPoint()
				.getLon());

		savedInstanceState.putFloat("zoom", mapController.getZoomCurrent());

		savedInstanceState.putDouble("centerLat", mapController.getMapCenter()
				.getLat());
		savedInstanceState.putDouble("centerLon", mapController.getMapCenter()
				.getLon());

		return savedInstanceState;
	}

	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onBalloonAnimationEnd(BalloonItem arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBalloonAnimationStart(BalloonItem arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBalloonHide(BalloonItem arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBalloonShow(BalloonItem arg0) {
		// TODO Auto-generated method stub

	}

}