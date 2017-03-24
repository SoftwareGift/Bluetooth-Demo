package com.luckchoudog.devices.bluetoothwar;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import de.greenrobot.event.EventBus;

public class BluetoothServerService extends Service {
	/**
	 * 蓝牙适配器
	 */
	private BluetoothInstance bluetoothInstance = BluetoothInstance.getSingleInstance();
	/**
	 * 蓝牙通讯线程
	 */
	private BluetoothCommunThread bluetoothCommunThread;
	/**
	 * 服务器连接线程
	 */
	private volatile BluetoothServerConnThread bluetoothServerConnThread;

	/**
	 * 蓝牙操作类实例对象
	 */
	private BluetoothConnectMessages bluetoothConnectMessages;

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

	@Override
	public void onCreate() {
		super.onCreate();
		EventBus.getDefault().register(this);
		bluetoothConnectMessages = new BluetoothConnectMessages();
		bluetoothInstance.openBlueTooth(); // 打开蓝牙
		// 开启蓝牙发现功能（300秒）
		bluetoothInstance.findedBlueTooth(300);
		// 开启后台连接线程
		bluetoothServerConnThread = new BluetoothServerConnThread();
		bluetoothServerConnThread.start();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (bluetoothCommunThread != null) {
			bluetoothCommunThread.stopMyThread();
		}
		if (bluetoothServerConnThread != null) {
			bluetoothServerConnThread.stopMyThread();
		}
		EventBus.getDefault().unregister(this);
	}

	class BluetoothServerConnThread extends Thread {
		private BluetoothAdapter adapter;
		private BluetoothSocket socket; // 用于通信的Socket
		private BluetoothServerSocket serverSocket;

		/**
		 * 构造函数
		 */
		public BluetoothServerConnThread() {
			adapter = BluetoothAdapter.getDefaultAdapter();
		}

		public void stopMyThread() {
			if (bluetoothServerConnThread == null) {
				return; // stopped before started.
			}
			BluetoothServerConnThread tmpThread = bluetoothServerConnThread;
			bluetoothServerConnThread = null;
			if (tmpThread != null) {
				tmpThread.interrupt();
			}
		}

		@Override
		public void run() {
			if (bluetoothServerConnThread == null) {
				return;
			}
			try {
				Thread.sleep(1000);// 休眠1秒，防止出错
				try {
					serverSocket = adapter.listenUsingRfcommWithServiceRecord("Server",
							BluetoothMessages.BLUETOOTHWAR_PRIVATE_UUID);
					socket = serverSocket.accept();
				} catch (Exception e) {
					// 发送连接失败消息
					bluetoothConnectMessages.BLUETOOTHWAR_MESSAGE_CONNECT = false;
					EventBus.getDefault().post(bluetoothConnectMessages);
					return;
				} finally {
					try {
						serverSocket.close();
					} catch (Exception e) {
					}
				}
				if (socket != null) {
					// 发送连接成功消息，消息的obj字段为连接的socket
					bluetoothCommunThread = BluetoothCommunThread.getInstance(socket);
					bluetoothCommunThread.start();
					// 发送连接成功消息
					bluetoothConnectMessages.BLUETOOTHWAR_MESSAGE_CONNECT = true;
					EventBus.getDefault().post(bluetoothConnectMessages);
				} else {
					// 发送连接失败消息
					bluetoothConnectMessages.BLUETOOTHWAR_MESSAGE_CONNECT = false;
					EventBus.getDefault().post(bluetoothConnectMessages);
					return;
				}
				Thread.yield(); // let another thread have some time perhaps to
								// stop this one.
				if (Thread.currentThread().isInterrupted()) {
					throw new InterruptedException("Stopped by ifInterruptedStop()");
				}
			} catch (Throwable t) {
			}
		}
	}
}
