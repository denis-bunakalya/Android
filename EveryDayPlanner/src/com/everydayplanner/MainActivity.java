package com.everydayplanner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Set;
import java.util.TreeSet;

import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class MainActivity extends ListActivity implements OnClickListener {

	private EditText editText;

	private Button deleteButton;
	private Button changeButton;
	
	private Button deleteAllButton;
	private Button timeButton;

	private int hour;
	private int minute;

	private Animation animation;

	private Set<String> tasks = new TreeSet<String>();
	private String selected;

	private InputMethodManager inputMethodManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
			
			loadData();
			setListAdapter(new ArrayAdapter<Object>(this,
					android.R.layout.simple_list_item_1, tasks.toArray()));

			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);	
			
			timeButton = (Button) findViewById(R.id.time_button);

			timeButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					showTimePickerDialog();
				}
			});
			
			editText = (EditText) findViewById(R.id.edit_text);

			editText.addTextChangedListener(new TextWatcher() {
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					enableOrDisableDeleteAndChangeButtons();
				}

				public void afterTextChanged(Editable s) {

				}
			});
			
			Button clearButton = (Button) findViewById(R.id.clear_button);

			clearButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					editText.setText("");
				}
			});

			deleteAllButton = (Button) findViewById(R.id.delete_all_button);
			deleteAllButton.setOnClickListener(this);
			
			if (tasks.isEmpty()) {
				deleteAllButton.setEnabled(false);
			}

			Button addButton = (Button) findViewById(R.id.add_button);
			addButton.setOnClickListener(this);

			deleteButton = (Button) findViewById(R.id.delete_button);
			deleteButton.setOnClickListener(this);

			changeButton = (Button) findViewById(R.id.change_button);
			changeButton.setOnClickListener(this);	
			
			Calendar c = Calendar.getInstance();
			hour = getPreferences(0).getInt("hour", c.get(Calendar.HOUR_OF_DAY));
			minute = getPreferences(0).getInt("minute", c.get(Calendar.MINUTE));
			
			timeButton.setText(pad(hour) + ":" + pad(minute));
			
			editText.setText(getPreferences(0).getString("edit_text", ""));
			
			enableOrDisableDeleteAndChangeButtons();

			inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);			

			animation = AnimationUtils.loadAnimation(this, R.anim.total);
			addButton.startAnimation(animation);
			
			deleteButton.startAnimation(animation);
			changeButton.startAnimation(animation);
			deleteAllButton.startAnimation(animation);	
			
	}

	@Override
	public void onClick(View v) {

		try {

			switch (v.getId()) {

			case R.id.add_button:
				if (editText.getText().toString().equals("")) {

					Toast.makeText(getApplicationContext(),
							"Please, enter your task...", Toast.LENGTH_LONG)
							.show();

				} else {
					tasks.add(timeButton.getText() + " "
							+ editText.getText().toString());

					deleteAllButton.setEnabled(true);
				}
				break;

			case R.id.delete_button:
				tasks.remove(timeButton.getText() + " "
						+ editText.getText().toString());

				if (tasks.isEmpty()) {
					deleteAllButton.setEnabled(false);
				}
				break;

			case R.id.change_button:

				if (editText.getText().toString().equals("")) {

					Toast.makeText(getApplicationContext(),
							"Please, enter your task...", Toast.LENGTH_LONG)
							.show();

				} else {
					tasks.remove(selected);
					tasks.add(timeButton.getText() + " "
							+ editText.getText().toString());

				}
				break;

			case R.id.delete_all_button:

				tasks.clear();
				deleteAllButton.setEnabled(false);
				break;
			}

			selected = null;
			editText.setText("");

			enableOrDisableDeleteAndChangeButtons();

			inputMethodManager.hideSoftInputFromWindow(getCurrentFocus()
					.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

			setListAdapter(new ArrayAdapter<Object>(this,
					android.R.layout.simple_list_item_1, tasks.toArray()));

		} catch (Throwable t) {

			Toast.makeText(getApplicationContext(),
					"Exception: " + t.toString(), Toast.LENGTH_LONG).show();
		}
	}

	public void onListItemClick(ListView parent, View v, int position, long id) {

		selected = ((TextView) v).getText().toString();
		deleteButton.setEnabled(true);

		hour = Integer.parseInt(selected.substring(0, 2));
		minute = Integer.parseInt(selected.substring(3, 5));

		timeButton.setText(selected.substring(0, 5));
		editText.setText(selected.substring(6));
	}

	private OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int newHour, int newMinute) {
			
			hour = newHour;
			minute = newMinute;
			
			timeButton.setText(pad(hour) + ":" + pad(minute));
			enableOrDisableDeleteAndChangeButtons();
		}
	};

	private static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}

	private void showTimePickerDialog() {
		(new TimePickerDialog(this, mTimeSetListener, hour, minute, true))
				.show();
	}

	private void enableOrDisableDeleteAndChangeButtons() {

		if (!(timeButton.getText() + " " + editText.getText().toString())
				.equals(selected)) {
			deleteButton.setEnabled(false);

			if (selected != null) {
				changeButton.setEnabled(true);
			} else {
				changeButton.setEnabled(false);
			}
		} else {
			changeButton.setEnabled(false);
		}
	}

	private void loadData() {
		try {			

			selected = getPreferences(0).getString("selected", null);

			InputStream inputStream = openFileInput("data");
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream);
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);

			String task = bufferedReader.readLine();
			
			while (task != null) {
				tasks.add(task);
				task = bufferedReader.readLine();
			}

			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();

		} catch (FileNotFoundException e) {
		} catch (Throwable t) {

			Toast.makeText(getApplicationContext(),
					"Exception: " + t.toString(), Toast.LENGTH_LONG).show();
		}
	}

	private void saveData() {
		try {
			
			Editor editor = getPreferences(0).edit();
			editor.putString("selected", selected);
			
			editor.putInt("hour", hour);
			editor.putInt("minute", minute);
			
			editor.putString("edit_text", editText.getText().toString());
			editor.commit();

			OutputStream outputStream = openFileOutput("data", 0);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
					outputStream);

			for (String task : tasks) {
				outputStreamWriter.write(task + "\n");
			}

			outputStreamWriter.close();
			outputStream.close();

		} catch (Throwable t) {
			Toast.makeText(getApplicationContext(),
					"Exception: " + t.toString(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, 0, 0, "Exit");
		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		saveData();
		finish();
		return true;
	}

}
