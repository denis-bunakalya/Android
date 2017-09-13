package com.netchat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

public class NetChatTask extends AsyncTask<Void, String, Void> {

	private Socket socket;
	private byte[] buffer;

	private InputStream serverInput;
	private OutputStream serverOutput;

	private String host;
	private int port;
	private String myName;

	private NetChatActivity netChatActivity;
	private Map<String, Drawable> imageByName;

	public NetChatTask(String host, int port, String myName, NetChatActivity netChatActivity,
			Map<String, Drawable> imageByName) {

		buffer = new byte[16384];

		this.host = host;
		this.port = port;
		this.myName = myName;

		this.netChatActivity = netChatActivity;
		this.imageByName = imageByName;
	}

	@Override
	protected Void doInBackground(Void... params) {

		try {
			socket = new Socket(host, port);
			publishProgress(myName + ":Connected to " + host + ":" + port);

			serverInput = socket.getInputStream();
			serverOutput = socket.getOutputStream();

			imageByName.put("SERVER", Drawable.createFromStream(serverInput, "src"));
			sendMessage(myName);

			((BitmapDrawable) imageByName.get(myName)).getBitmap().compress(Bitmap.CompressFormat.PNG, 0, serverOutput);

			String message = receiveMessage();
			while (message != null) {

				publishProgress(message);

				if (message.startsWith("SERVER: Other connected users (")) {

					int end = message.indexOf(")");
					int numberOfUsers = Integer.valueOf(message.substring(31, end));

					for (int i = 0; i < numberOfUsers; i++) {

						message = receiveMessage();
						publishProgress(message);

						imageByName.put(message.substring(8), Drawable.createFromStream(serverInput, "src"));
					}
				}

				if (message.startsWith("SERVER: ") && message.endsWith(" connected")) {

					int end = message.indexOf(' ', 8);

					imageByName.put(message.substring(8, end), Drawable.createFromStream(serverInput, "src"));
				}

				message = receiveMessage();
			}

		} catch (Throwable t) {
			NetChatActivity.error = "Exception in doInBackground:" + t.toString();
			publishProgress(myName + ":Exception in doInBackground:" + t.toString() + "\n");
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(String... values) {

		super.onProgressUpdate(values);
		try {
			int index = values[0].indexOf(':');
			String name = values[0].substring(0, index);
			String message = values[0].substring(index + 1);

			netChatActivity.UpdateListView(name, message);

		} catch (Throwable t) {
			NetChatActivity.error = "Exception in onProgressUpdate:" + t.toString();
		}
	}

	public void sendMessage(String message) throws IOException {

		byte[] messageBytes = message.getBytes("UTF-8");
		String size = String.valueOf(messageBytes.length);
		int sizeOfSize = size.getBytes("UTF-8").length;

		serverOutput.write(sizeOfSize);
		serverOutput.write(size.getBytes("UTF-8"));

		serverOutput.write(messageBytes);
		serverOutput.flush();
	}

	private String receiveMessage() throws IOException {

		int sizeOfSize = serverInput.read();
		if (sizeOfSize == -1) {
			return null;
		}

		serverInput.read(buffer, 0, sizeOfSize);
		int size = Integer.valueOf(new String(buffer, 0, sizeOfSize, "UTF-8"));

		serverInput.read(buffer, 0, size);
		return new String(buffer, 0, size, "UTF-8");
	}

	public OutputStream getServerOutput() {

		return serverOutput;
	}

	public Socket getSocket() {

		return socket;
	}
}
