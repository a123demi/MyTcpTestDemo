package com.example.mytcptestdemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class ClientThread implements Runnable {

	private static final String TAG = "com.example.mytcptestdemo.ClientThread ";
	public static final int MSG_RECEIVE_SOCKET = 1;
	public static final int MSG_SEND_SOCKET = 2;
	public static final int MSG_TIME_OUT = 3;
	public static final int MSG_NETWORK = 4;
	public static final int MSG_CONNECT = 5;
	public static final int MSG_OUTPUTSTREAM = 6;
	public static final int MSG_INPUTSTREAM = 7;

	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	private Handler sendHandler;
	private Handler receiveHandler;
	private String ip;
	private int port;

	public ClientThread() {
	}

	public ClientThread(Handler receiveHandler, String ip, int port) {
		this.receiveHandler = receiveHandler;
		this.ip = ip;
		this.port = port;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		socket = new Socket();
		InetSocketAddress isa = new InetSocketAddress(ip, port);

		try {
			socket.connect(isa, 5000);

			Log.i(TAG, "run():socketConnect->" + socket.isConnected());
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
		} catch (SocketTimeoutException e) {
			Log.e(TAG, "run():socketTimeoutException->" + e.getMessage());
			receiveHandler.sendEmptyMessage(MSG_TIME_OUT);
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "run():IOException->" + e.getMessage());
			if (e.getMessage().contains("ENETUNREACH")) {
				receiveHandler.sendEmptyMessage(MSG_NETWORK);
			}
			return;
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (socket != null) {
					if (inputStream == null) {
						try {
							inputStream = socket.getInputStream();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							Log.e(TAG,
									"new Thread:run():IOException->"
											+ e.getMessage());
							return;
						}
					}

					byte[] buff;

					while (true) {
						try {
							buff = new byte[2048];
							inputStream.read(buff);
							String str = new String(buff, "GBK").trim();

							receiveHandler.sendMessage(receiveHandler
									.obtainMessage(MSG_RECEIVE_SOCKET, str));
						} catch (IOException e) {
							Log.e(TAG,
									"new Thread:run():inputStream receive:IOException->"
											+ e.getMessage());
							if(e.getMessage().contains("ETIMEDOUT")){
								receiveHandler.sendEmptyMessage(MSG_INPUTSTREAM);
							}
							break;
						}
					}

				} else {
					Log.e(TAG, "new Thread:run():socket-> is null");
				}

			}

		}).start();

		Looper.prepare();

		sendHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_SEND_SOCKET:
					if (socket != null) {
						try {
							if (outputStream == null) {
								outputStream = socket.getOutputStream();
							}
							outputStream.write(msg.obj.toString().getBytes());
							outputStream.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							Log.e(TAG,
									"sendHandler:IOException->"
											+ e.getMessage());

							if (e.getMessage().contains("EPIPE")
									|| e.getMessage().contains("ETIMEDOUT")) {
								receiveHandler
										.sendEmptyMessage(MSG_OUTPUTSTREAM);
							}
							break;
						}
					} else {
						Log.e(TAG, "sendHandler:socket-> is null");
					}
					break;
				}
			}

		};

		Looper.loop();

	}

	public void close() {

		try {
			if (inputStream != null) {
				inputStream.close();
				inputStream = null;
			}

			if (outputStream != null) {
				outputStream.close();
				outputStream = null;
			}

			if (socket != null) {
				socket.close();
				socket = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "close():IOException->" + e.getMessage());
		}

	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public Handler getSendHandler() {
		return sendHandler;
	}

	public void setSendHandler(Handler sendHandler) {
		this.sendHandler = sendHandler;
	}
}
