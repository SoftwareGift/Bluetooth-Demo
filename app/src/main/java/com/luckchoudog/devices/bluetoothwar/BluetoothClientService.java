package com.luckchoudog.devices.bluetoothwar;

import java.io.IOException;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import de.greenrobot.event.EventBus;

public class BluetoothClientService extends Service {
	private String TAG = BluetoothClientService.class.getSimpleName();
	/**
	 * 蓝牙通讯线程
	 */
	private BluetoothCommunThread bluetoothCommunThread;
	/**
	 * 连接的蓝牙
	 */
	private BluetoothDevice serverDevice;
	/**
	 * 蓝牙连接线程
	 */
	private BluetoothClientConnThread bluetoothClientConnThread;
	/**
	 * 蓝牙适配器
	 */
	private BluetoothInstance bluetoothInstance = BluetoothInstance.getSingleInstance();
	/**
	 * 蓝牙操作类实例对象
	 */
	private BluetoothConnectMessages bluetoothConnectMessages;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	/**
	 * 获取通讯线程
	 * 
	 * @return
	 */
	public BluetoothCommunThread getBluetoothCommunThread() {
		return bluetoothCommunThread;
	}

	public void onEventMainThread(BluetoothExchangeMessages message) {
		if (message.BLUETOOTHSERVICESERVICE_STOP_SERVICE) {
			if (bluetoothCommunThread != null) {
				bluetoothCommunThread.stopMyThread();
			}
		} else {
			Object data = message.BLUETOOTHSERVICESERVICE_DATA_WRITE_TO_SERVICE;
			if (null != bluetoothCommunThread && null != data) {
				bluetoothCommunThread.writeObject(data);
			}
		}
	}
	public void onEvent(BluetoothDevice device) {
		Log.e("qwerq", "aefasgsdgag  address = "+device);
		if (null == device || "".equals(device.getAddress())) {
			return;
		}
		serverDevice = device;
		bluetoothInstance.openBlueTooth();
		try {
			Thread.sleep(1000);// 休眠1秒
		} catch (Exception e) {
			Log.e(TAG, "onEventMainThread Exception",e);
		}
		bluetoothClientConnThread = new BluetoothClientConnThread(serverDevice);
		bluetoothClientConnThread.start();
	}

	/**
	 * Service创建时的回调函数
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		EventBus.getDefault().register(this);
		bluetoothConnectMessages = new BluetoothConnectMessages();
		Log.e("qwerq", "onCreate");
	}

	/**
	 * Service销毁时的回调函数
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (bluetoothCommunThread != null) {
			bluetoothCommunThread.stopMyThread();
		}
		if (null != bluetoothClientConnThread) {
			bluetoothClientConnThread.stopMyThread();
		}
		EventBus.getDefault().unregister(this);
	}

	class BluetoothClientConnThread extends Thread {
		private BluetoothDevice serverDevice; // 服务器设备
		private BluetoothSocket socket; // 通信Socket

		/**
		 * 构造函数
		 */
		public BluetoothClientConnThread(BluetoothDevice serverDevice) {
			this.serverDevice = serverDevice;
		}

		public void stopMyThread() {
			if (bluetoothClientConnThread == null) {
				return;
			}
			BluetoothClientConnThread tmpThread = bluetoothClientConnThread;
			bluetoothClientConnThread = null;
			if (tmpThread != null) {
				tmpThread.interrupt();
			}
		}

		@Override
		public void run() {
			if (bluetoothClientConnThread == null) {
				return; // stopped before started.
			}
			try {
				try {
					// 建立连接
					socket = serverDevice.createRfcommSocketToServiceRecord(BluetoothMessages.BLUETOOTHWAR_PRIVATE_UUID);
					socket.connect();
				} catch (Exception ex) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					// 发送连接失败消息
					bluetoothConnectMessages.BLUETOOTHWAR_MESSAGE_CONNECT = false;
					EventBus.getDefault().post(bluetoothConnectMessages);
					Log.e(TAG, "BluetoothClientConnThread run Exception",ex);
					return;
				}
				// 发送连接成功消息，消息的obj参数为连接的socket
				Log.e("test", "run here");
				bluetoothCommunThread = BluetoothCommunThread.getInstance(socket);
				bluetoothCommunThread.start();
				bluetoothConnectMessages.BLUETOOTHWAR_MESSAGE_CONNECT = true;
				EventBus.getDefault().post(bluetoothConnectMessages);
				Thread.yield();
				if (Thread.currentThread().isInterrupted()) {
					throw new InterruptedException("Stopped by ifInterruptedStop()");
				}
			} catch (Throwable t) {
			}
		}
	}
}
