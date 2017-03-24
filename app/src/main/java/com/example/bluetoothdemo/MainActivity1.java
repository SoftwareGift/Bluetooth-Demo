package com.example.bluetoothdemo;

import java.util.ArrayList;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.luckchoudog.devices.bluetoothwar.BluetoothClientService;
import com.luckchoudog.devices.bluetoothwar.BluetoothInstance;
import com.luckchoudog.devices.bluetoothwar.BluetoothInstance.BlueToothInstanceLisenner;

import de.greenrobot.event.EventBus;

public class MainActivity1 extends BaseActivity {
	Button button1, button2, button3, button4;
	ListView listView1;
	BluetoothInstance blueTooth;
	ArrayList<BluetoothDevice> devicesList = new ArrayList<BluetoothDevice>();
	AdapterDemo adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main1);
		initData();
		findView();
	}
	@Override
	protected void onStart() {
		// 开启后台service
		Log.e("qwerq", "aefasgsdgag");
		Intent startService = new Intent(MainActivity1.this, BluetoothClientService.class);
		startService(startService);
		super.onStart();
	}
	private void initData() {
		blueTooth = BluetoothInstance.getSingleInstance();
		blueTooth.setBlueToothInstanceLisenner(new BlueToothInstanceLisenner() {
			
			@Override
			public void onDiscoveryDevice(BluetoothDevice device) {
				if (!devicesList.contains(device)) {
					devicesList.add(device);
					adapter.setDeviceList(devicesList);
				}
			}
		});
		adapter = new AdapterDemo(getApplicationContext(), devicesList);
	}

	private void findView() {
		// TODO Auto-generated method stub
		button1 = (Button) findViewById(R.id.button1);
		button2 = (Button) findViewById(R.id.button2);
		button3 = (Button) findViewById(R.id.button3);
		button4 = (Button) findViewById(R.id.button4);
		listView1 = (ListView) findViewById(R.id.listView1);
		button1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				blueTooth.openBlueTooth();
			}
		});
		button2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				blueTooth.closeBlueTooth();
			}
		});
		button3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				blueTooth.findedBlueTooth();
			}
		});
		button4.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				blueTooth.discoveryDevice();
			}
		});
		listView1.setAdapter(adapter);
		listView1.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				blueTooth.stopDiscoveryDevice();
//				blueTooth.connectDevice(devicesList.get(position));
				Intent clientIntent = new Intent(MainActivity1.this,
						ClientActivity.class);
				clientIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(clientIntent);
				Log.e("qwerq", "aefasgsdgag  address = "+ devicesList.get(position));
				EventBus.getDefault().post( devicesList.get(position));
			}
		});
	}

}
