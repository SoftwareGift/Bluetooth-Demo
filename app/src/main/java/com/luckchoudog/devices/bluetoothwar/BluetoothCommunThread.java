package com.luckchoudog.devices.bluetoothwar;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.bluetooth.BluetoothSocket;
import de.greenrobot.event.EventBus;

/**
 * 蓝牙通讯线程，单例模式
 */
public class BluetoothCommunThread extends Thread {

	private static BluetoothCommunThread myThread;
	private BluetoothSocket socket;
	private ObjectInputStream inStream; // 对象输入流
	private ObjectOutputStream outStream; // 对象输出流;
	private BluetoothConnectMessages bluetoothConnectMessages;

	/**
	 * 构造函数
	 * 
	 * @param handler
	 *            用于接收消息
	 * @param socket
	 */
	private BluetoothCommunThread(BluetoothSocket socket) {
		this.socket = socket;
		bluetoothConnectMessages = new BluetoothConnectMessages();
		try {
			this.outStream = new ObjectOutputStream(socket.getOutputStream());
			this.inStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
		} catch (Exception e) {
			try {
				if (null != socket) {
					socket.close();
				}
			} catch (IOException e1) {
			}
			// 发送连接失败消息
			bluetoothConnectMessages.BLUETOOTHWAR_MESSAGE_CONNECT = false;
			EventBus.getDefault().post(bluetoothConnectMessages);
		}
	}

	public static BluetoothCommunThread getInstance(BluetoothSocket socket) {
		if (null == myThread) {
			myThread = new BluetoothCommunThread(socket);
		}
		return myThread;
	}

	/**
	 * 写入一个可序列化的对象
	 * 
	 * @param obj
	 */
	public void writeObject(Object obj) {
		if (null == obj) {
			return;
		}
		try {
			outStream.flush();
			outStream.writeObject(obj);
			outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void stopMyThread() {
		if (myThread == null) {
			return;
		}
		BluetoothCommunThread tmpThread = myThread;
		myThread = null;
		if (tmpThread != null) {
			tmpThread.interrupt();
		}
	}

	public void run() {
		try { // 延时启动线程 all the run() method's code goes here
			while (true) {
				if (myThread == null) {
					break; // stopped before started.
				}
				try {
					Object obj = inStream.readObject();
					// 发送成功读取到对象的消息，消息的obj参数为读取到的对象
					bluetoothConnectMessages.BLUETOOTHWAR_MESSAGE_CONNECT = true;
					bluetoothConnectMessages.BLUETOOTHWAR_MESSAGE_CONNECT_READ_OBJECT = (Serializable) obj;
					EventBus.getDefault().post(bluetoothConnectMessages);
				} catch (Exception ex) {
					// 发送连接失败消息
					bluetoothConnectMessages.BLUETOOTHWAR_MESSAGE_CONNECT = false;
					EventBus.getDefault().post(bluetoothConnectMessages);
					return;
				}
			}
			// 关闭流
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outStream != null) {
				try {
					outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Thread.yield(); // let another thread have some time perhaps to
							// stop this one.
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException("Stopped by ifInterruptedStop()");
			}
			// do some more work
		} catch (Throwable t) {
			// log/handle all errors here
			System.out.println("-----------线程干掉------------" + t);
		}
	}
}
