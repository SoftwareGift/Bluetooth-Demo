package com.example.bluetoothdemo;

import java.util.Date;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.luckchoudog.devices.bluetoothwar.BluetoothConnectMessages;
import com.luckchoudog.devices.bluetoothwar.BluetoothExchangeMessages;

import de.greenrobot.event.EventBus;

public class ClientActivity extends BaseActivity {
	private TextView serversText;
	private EditText chatEditText;
	private EditText sendEditText;
	private Button sendBtn;
	private BluetoothExchangeMessages message;

	public void onEventMainThread(BluetoothConnectMessages message) {
		Log.e("test", "ClientActivity  onEventMainThread message "+message.BLUETOOTHWAR_MESSAGE_CONNECT);
		if (!message.BLUETOOTHWAR_MESSAGE_CONNECT) {
			chatEditText.setText("连接失败");
		} else {
			serversText.setText("连接成功");
			sendBtn.setEnabled(true);

			Object data = message.BLUETOOTHWAR_MESSAGE_CONNECT_READ_OBJECT;
			if (null == data) {
				return;
			}
			String msg = "from remote " + new Date().toLocaleString() + " :\r\n" + data.toString() + "\r\n";
			chatEditText.append(msg);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);
		EventBus.getDefault().register(this);
		message = new BluetoothExchangeMessages();
		serversText = (TextView) findViewById(R.id.clientServersText);
		chatEditText = (EditText) findViewById(R.id.clientChatEditText);
		sendEditText = (EditText) findViewById(R.id.clientSendEditText);
		sendBtn = (Button) findViewById(R.id.clientSendMsgBtn);
		sendBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 发送消息
				if ("".equals(sendEditText.getText().toString().trim())) {
					Toast.makeText(ClientActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
				} else {
					// 发送消息
					message.BLUETOOTHSERVICESERVICE_STOP_SERVICE = false;
					message.BLUETOOTHSERVICESERVICE_DATA_WRITE_TO_SERVICE = sendEditText.getText().toString();
					EventBus.getDefault().post(message);
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

}
