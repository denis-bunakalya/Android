package com.netchat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NetChatActivity extends Activity {

	private String myName;
	private ListView listView;
	private EditText editText;

	private List<String> names;
	private List<Drawable> images;
	private List<String> messages;

	private Map<String, Drawable> imageByName;
	private NetChatTask netChatTask;
	public static String error = "null";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);

			if (!isTaskRoot()) {
				final Intent intent = getIntent();

				if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(intent.getAction())) {
					finish();
					return;
				}
			}
			setContentView(R.layout.net_chat_activity);

			editText = (EditText) findViewById(R.id.edit_text);
			listView = (ListView) findViewById(R.id.list_view);

			names = new ArrayList<String>();
			images = new ArrayList<Drawable>();
			messages = new ArrayList<String>();

			imageByName = new HashMap<String, Drawable>();

			Intent intent = new Intent();
			intent.setClass(this, AddressNameActivity.class);
			startActivityForResult(intent, 0);

			editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					try {
						if ((netChatTask.getServerOutput() != null) && (actionId == EditorInfo.IME_ACTION_DONE)) {

							UpdateListView(myName, editText.getText().toString());
							netChatTask.sendMessage(editText.getText().toString());

							editText.setText("");
							return true;
						}
					} catch (Throwable t) {
						error = "Exception in onEditorAction: " + t.toString();
						UpdateListView(myName, "Exception in onEditorAction: " + t.toString());
					}
					return false;
				}
			});
		} catch (Throwable t) {
			Toast.makeText(this, "Exception in onCreate: " + t.toString(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {
			super.onActivityResult(requestCode, resultCode, data);

			if (resultCode != RESULT_OK) {
				finish();
				return;
			}

			String address = data.getStringExtra("address");
			int index = address.indexOf(':');

			String host = address.substring(0, index);
			int port = Integer.parseInt(address.substring(index + 1));

			myName = data.getStringExtra("name");
			Bitmap image = (Bitmap) data.getParcelableExtra("image");

			if (image != null) {
				imageByName.put(myName, new BitmapDrawable(getResources(), image));
			} else {
				imageByName.put(myName, getResources().getDrawable(R.drawable.ic_launcher));
			}

			UpdateListView(myName, "Connecting " + address + "...");
			netChatTask = new NetChatTask(host, port, myName, this, imageByName);
			netChatTask.execute();

		} catch (Throwable t) {
			error = "Exception in onActivityResult: " + t.toString();
			UpdateListView(myName, "Exception in onActivityResult: " + t.toString());
		}
	}

	@Override
	protected void onDestroy() {
		try {
			super.onDestroy();

			if (!error.equals("null") && !error.contains("Socket closed")) {
				Toast.makeText(this, error, Toast.LENGTH_LONG).show();
			}
			netChatTask.sendMessage("exit");

		} catch (Throwable t) {
		} finally {
			try {
				netChatTask.getSocket().close();
			} catch (Throwable t) {
			}
		}
	}

	public ListView getListView() {

		return listView;
	}

	public void UpdateListView(String name, String message) {

		names.add(name);
		if (name.startsWith("PRIVATE FROM ")) {
			name = name.substring(name.lastIndexOf(' ') + 1);
		}
		images.add(imageByName.get(name));
		messages.add(message);

		listView.setAdapter(new ListAdapter(this, names, images, messages));
		listView.setSelection(listView.getCount() - 1);
	}
}
