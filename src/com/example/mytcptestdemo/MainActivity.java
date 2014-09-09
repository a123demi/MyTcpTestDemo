package com.example.mytcptestdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private static final String TAG = "com.example.mytcptestdemo.MainActivity ";
	private EditText ipEt;
	private EditText portEt;
	private Button connBtn;
	private EditText sendEt;
	private Button sendBtn;
	private TextView receiveTv;
	private TextView displayConnectTv;

	private ClientThread clientThread;
	private Thread clientStartThread;
	private int connCount = 1;

	private Handler receiveHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ClientThread.MSG_RECEIVE_SOCKET:
				String receiveMsg = msg.obj.toString();
				receiveTv.setText(receiveMsg);
				break;
			case ClientThread.MSG_TIME_OUT:
				if(connCount < 5){
					connCount++;
					socketConnect();
				}else{
					connCount = 0;
					Toast.makeText(MainActivity.this, "socekt connect timeout", Toast.LENGTH_SHORT).show();
				}
				
				
				break;
			case ClientThread.MSG_NETWORK:
				if(connCount < 5){
					connCount++;
					socketConnect();
				}else{
					connCount = 0;
					Toast.makeText(MainActivity.this, "network is unreachable", Toast.LENGTH_SHORT).show();
				}
				break;
			case ClientThread.MSG_OUTPUTSTREAM:
				Toast.makeText(MainActivity.this, "outputstream:network is unreachable,please login agian", Toast.LENGTH_SHORT).show();
				break;
			case ClientThread.MSG_INPUTSTREAM:
				Toast.makeText(MainActivity.this, "inputStream:network is unreachable,please login agian", Toast.LENGTH_SHORT).show();
			}
			
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ipEt = (EditText) this.findViewById(R.id.ip_address_et);
		portEt = (EditText) this.findViewById(R.id.port_et);
		connBtn = (Button) this.findViewById(R.id.connect_btn);
		sendEt = (EditText) this.findViewById(R.id.send_et);
		sendBtn = (Button) this.findViewById(R.id.send_btn);
		receiveTv = (TextView) this.findViewById(R.id.receive_tv);
		displayConnectTv = (TextView) this.findViewById(R.id.display_connect_tv);
		
		connBtn.setOnClickListener(this);
		sendBtn.setOnClickListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.send_btn:
			if (clientThread.getSocket() != null) {
				if(clientThread.getSendHandler() != null)
				clientThread.getSendHandler().sendMessage(
						clientThread.getSendHandler().obtainMessage(
								ClientThread.MSG_SEND_SOCKET, sendEt.getText().toString()));
				else{
					Log.e(TAG, "onClick():clientThread.getSendHandler()-> is null");
				}
			}

			break;
		case R.id.connect_btn:
			socketConnect();
			break;
		}
	}
	
	private void socketConnect(){
		clientThread = new ClientThread(receiveHandler, ipEt.getText()
				.toString(), Integer.valueOf(portEt.getText().toString()));
		clientStartThread = new Thread(clientThread);
		clientStartThread.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "onClicke():InterruptedException->" + e.getMessage());
		}
	}
}