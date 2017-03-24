package com.example.bluetoothdemo;

import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.luckchoudog.devices.bluetoothwar.BluetoothConnectMessages;
import com.luckchoudog.devices.bluetoothwar.BluetoothExchangeMessages;
import com.luckchoudog.devices.bluetoothwar.BluetoothServerService;

import de.greenrobot.event.EventBus;

public class ServerActivity extends BaseActivity {
	private TextView serverStateTextView;
	private EditText msgEditText;
	private EditText sendMsgEditText;
	private Button sendBtn;
	private BluetoothExchangeMessages message;

	@Override
	protected void onStart() {
		// 开启后台service
		Intent startService = new Intent(ServerActivity.this, BluetoothServerService.class);
		startService(startService);
		super.onStart();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server);
		EventBus.getDefault().register(this);
		message = new BluetoothExchangeMessages();
		serverStateTextView = (TextView) findViewById(R.id.serverStateTxt);
		serverStateTextView.setText("等待连接...");

		msgEditText = (EditText) findViewById(R.id.serverEditText);

		sendMsgEditText = (EditText) findViewById(R.id.serverSendEditText);

		sendBtn = (Button) findViewById(R.id.serverSendMsgBtn);
		sendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ("".equals(sendMsgEditText.getText().toString().trim())) {
					Toast.makeText(ServerActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
				} else {
					// 发送消息
					message.BLUETOOTHSERVICESERVICE_STOP_SERVICE = false;
					message.BLUETOOTHSERVICESERVICE_DATA_WRITE_TO_SERVICE = sendMsgEditText.getText().toString();
					EventBus.getDefault().post(message);
				}
			}
		});

		sendBtn.setEnabled(false);
	}

	@Override
	protected void onStop() {
		// 关闭后台Service
		message.BLUETOOTHSERVICESERVICE_STOP_SERVICE = true;
		EventBus.getDefault().post(message);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	public void onEventMainThread(BluetoothConnectMessages message) {
		if (!message.BLUETOOTHWAR_MESSAGE_CONNECT) {
			serverStateTextView.setText("连接失败");
		} else {
			serverStateTextView.setText("连接成功");
			sendBtn.setEnabled(true);

			Object data = message.BLUETOOTHWAR_MESSAGE_CONNECT_READ_OBJECT;
			if (null == data) {
				return;
			}
			String msg = "from remote " + new Date().toLocaleString() + " :\r\n" + data.toString() + "\r\n";
			msgEditText.append(msg);
		}
	}
}
