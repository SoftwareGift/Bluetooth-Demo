package com.luckchoudog.devices.bluetoothwar;

import java.util.ArrayList;
import java.util.logging.Logger;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.example.bluetoothdemo.BaseActivity;

/**
 * 蓝牙操作实例(打开、关闭、处于被发现状态、搜索周围设备)，扫描到的设备通过接口回调，抛出给使用者，
 * 使用的时候需要setBlueToothInstanceLisenner进行设置，需要用到的权限
 * 
 * <pre>
 * <!-- 声明蓝牙权限 -->  
 * <uses-permission android:name="android.permission.BLUETOOTH" />  
 * <!-- 允许程序发现和配对蓝牙设备 -->  
 * <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 * </pre>
 * 
 * @author luckchoudog
 */
@SuppressLint("NewApi")
public class BluetoothInstance {
	private String TAG = BluetoothInstance.class.getSimpleName();
	/**
	 * Activity的onActivityResult回调码：打开蓝牙
	 */
	public static int BLE2OPEN_RESULTCODE = 1;
	/**
	 * Activity的onActivityResult回调码：将蓝牙处于可发现状态
	 */
	public static int BLE2FIND_RESULTCODE = 2;
	private static BluetoothInstance blueToothInstance;
	/**
	 * 本地的蓝牙适配器
	 */
	private BluetoothAdapter bluetooth;
	private BlueToothInstanceLisenner blueToothInstanceLisenner = null;

	private BluetoothInstance() {
		bluetooth = BluetoothAdapter.getDefaultAdapter();
	}

	public static BluetoothInstance getSingleInstance() {
		if (null == blueToothInstance) {
			blueToothInstance = new BluetoothInstance();
		}
		return blueToothInstance;
	}

	/**
	 * 请求将蓝牙打开
	 */
	public void openBlueTooth() {
		if (!bluetooth.isEnabled()) {
			// 蓝牙未启用，提示用户打开它
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			BaseActivity.getNowActivity().startActivityForResult(intent, BLE2OPEN_RESULTCODE);
		}
		Log.d(TAG, "openBlueTooth ~ end");
	}

	/**
	 * 将设备蓝牙关闭
	 */
	public void closeBlueTooth() {
		Log.d(TAG, "closeBlueTooth ~ start");
		if (bluetooth.isEnabled()) {
			// 蓝牙关闭
			bluetooth.disable();
		}
		Log.d(TAG, "closeBlueTooth ~ end");
	}

	/**
	 * 将设备蓝牙设备处于可发现状态，请先确定蓝牙已经开启，如果没有开启蓝牙将开启蓝牙
	 */
	public void findedBlueTooth() {
		findedBlueTooth(120);
	}

	/**
	 * 将设备蓝牙设备处于可发现状态，请先确定蓝牙已经开启，如果没有开启蓝牙将开启蓝牙
	 */
	public void findedBlueTooth(int time) {
		Log.d(TAG, "findedBlueTooth ~ start");
		if (time < 1 || time > 300) {
			time = 120;
		}
		if (!bluetooth.isEnabled()) {
			openBlueTooth();
		}
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, time); // 第二个参数是本机蓝牙被发现的时间，系统默认范围[1-300]秒，系统默认120秒
		BaseActivity.getNowActivity().startActivityForResult(intent, BLE2FIND_RESULTCODE);
		Log.d(TAG, "findedBlueTooth ~ end");
	}

	/**
	 * 扫描到周边的蓝牙设备list，已将每次扫描到的BluetoothDevice通过回调抛出，
	 * 使用的时候需要setBlueToothInstanceLisenner进行设置
	 */
	private ArrayList<BluetoothDevice> devicesList = new ArrayList<BluetoothDevice>();

	/**
	 * 开始搜索周边的蓝牙设备，请保证蓝牙已经开启，如果没有开启蓝牙将开启蓝牙
	 */
	public void discoveryDevice() {
		Log.d(TAG, "discoveryDevice ~ start");
		IntentFilter intent = new IntentFilter();
		intent.addAction(BluetoothDevice.ACTION_FOUND);
		intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		BaseActivity.getNowActivity().registerReceiver(searchDevices, intent);
		if (bluetooth.isEnabled() && !bluetooth.isDiscovering()) {
			devicesList.clear();
			bluetooth.startDiscovery();
		} else {
			openBlueTooth();
		}
		Log.d(TAG, "discoveryDevice ~ end");
	}

	/**
	 * 开始搜索周边的蓝牙设备，请保证蓝牙已经开启，如果没有开启蓝牙将开启蓝牙
	 */
	public void stopDiscoveryDevice() {
		if (bluetooth.isDiscovering())
			bluetooth.cancelDiscovery();
		BaseActivity.getNowActivity().unregisterReceiver(searchDevices);
	}

	/**
	 * 蓝牙配对时的广播接收
	 */
	private BroadcastReceiver searchDevices = new BroadcastReceiver() {
		@SuppressLint("UseSparseArrays")
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				devicesList.add(device);
				if (null != blueToothInstanceLisenner) {
					blueToothInstanceLisenner.onDiscoveryDevice(device);
				}
				Log.d(TAG, "devicesList ~ " + devicesList.toString());
			}
		}
	};

	public interface BlueToothInstanceLisenner {
		/**
		 * 每次扫描到的设备
		 * 
		 * @param device
		 *            新扫描到周围设备
		 */
		public void onDiscoveryDevice(BluetoothDevice device);
	}

	public void setBlueToothInstanceLisenner(BlueToothInstanceLisenner blueToothInstanceLisenner) {
		this.blueToothInstanceLisenner = blueToothInstanceLisenner;

	}
}
