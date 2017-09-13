package com.netchat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class AddressNameActivity extends Activity implements OnClickListener {

	private EditText addressEditText;
	private EditText nameEditText;

	private ImageView imageView;
	private String imagePath;
	private Bitmap image;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.address_name_activity);
			setResult(RESULT_CANCELED);

			addressEditText = (EditText) findViewById(R.id.address_edit_text);
			nameEditText = (EditText) findViewById(R.id.name_edit_text);

			addressEditText.setText(getPreferences(0).getString("address", "192.168.0.100:7890"));
			nameEditText.setText(getPreferences(0).getString("name", "mike"));

			imageView = (ImageView) findViewById(R.id.imageView);
			imagePath = getPreferences(0).getString("imagePath", null);
			setImage();

			Button buttonChange = (Button) findViewById(R.id.button_change);
			buttonChange.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					Intent intent = new Intent();
					intent.setType("image/*");

					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(Intent.createChooser(intent, "Select Picture"), 0);
				}
			});

			Button buttonOk = (Button) findViewById(R.id.button_ok);
			buttonOk.setOnClickListener(this);

		} catch (Throwable t) {
			Toast.makeText(this, "Exception in AddressNameActivity.onCreate: " + t.toString(), Toast.LENGTH_LONG)
					.show();
		}
	}

	private void setImage() {

		if (imagePath != null) {
			Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

			if (bitmap != null) {
				image = Bitmap.createScaledBitmap(bitmap, 48, 48, true);
				imageView.setImageBitmap(image);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {
			super.onActivityResult(requestCode, resultCode, data);

			if (resultCode != RESULT_OK) {
				return;
			}
			Uri imageUri = data.getData();

			String wholeID = DocumentsContract.getDocumentId(imageUri);
			String id = wholeID.split(":")[1];

			String[] column = { MediaStore.Images.Media.DATA };
			String sel = MediaStore.Images.Media._ID + "=?";

			Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel,
					new String[] { id }, null);
			int columnIndex = cursor.getColumnIndex(column[0]);

			cursor.moveToFirst();
			imagePath = cursor.getString(columnIndex);
			cursor.close();

			setImage();

		} catch (Throwable t) {
			Toast.makeText(this, "Exception in onActivityResult: " + t.toString(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onClick(View v) {
		try {
			Editor editor = getPreferences(0).edit();

			editor.putString("address", addressEditText.getText().toString());
			editor.putString("name", nameEditText.getText().toString());

			editor.putString("imagePath", imagePath);
			editor.commit();

			Intent intent = new Intent();

			intent.putExtra("address", addressEditText.getText().toString());
			intent.putExtra("name", nameEditText.getText().toString());
			intent.putExtra("image", image);

			setResult(RESULT_OK, intent);
			finish();

		} catch (Throwable t) {
			Toast.makeText(this, "Exception in onClick: " + t.toString(), Toast.LENGTH_LONG).show();
		}
	}
}