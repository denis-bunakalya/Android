package com.netchat;

import java.util.List;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ListAdapter extends ArrayAdapter<String> {

	private Activity context;
	private List<String> names;
	private List<Drawable> images;
	private List<String> messages;

	public ListAdapter(Activity context, List<String> names,
			List<Drawable> images, List<String> messages) {

		super(context, R.layout.list_row, names);
		try {
			this.context = context;
			this.names = names;
			this.images = images;
			this.messages = messages;

		} catch (Throwable t) {
			Toast.makeText(context,
					"Exception in CustomListAdapter: " + t.toString(),
					Toast.LENGTH_LONG).show();
		}
	}

	public View getView(int position, View view, ViewGroup parent) {

		View rowView = null;
		try {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.list_row, null, true);

			TextView nameView = (TextView) rowView.findViewById(R.id.nameView);
			ImageView imageView = (ImageView) rowView
					.findViewById(R.id.imageView);			
			
			TextView messageView = (TextView) rowView
					.findViewById(R.id.messageView);

			imageView.setImageDrawable(images.get(position));
			nameView.setText(names.get(position));
			messageView.setText(messages.get(position));

		} catch (Throwable t) {
			Toast.makeText(context, "Exception in getView: " + t.toString(),
					Toast.LENGTH_LONG).show();
		}
		return rowView;
	};
}