package com.example.bluetoothdemo;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdapterDemo extends BaseAdapter {
	// 此处的Object最好是Map<String, Object>,可以方便记清楚数据
	private ArrayList<BluetoothDevice> obgList;
	// private Context context;
	private LayoutInflater mInflater;

	public AdapterDemo(Context context, ArrayList<BluetoothDevice> list) {
		super();
		// this.context = context;
		this.mInflater = LayoutInflater.from(context);
		this.obgList = list;
	}

	/**
	 * 数据发生改变由adapter自己去处理，外部调用时注意在主线程中去调用： a.调用Activity.runOnUIThread()方法；
	 * b.使用Handler（其实这并不非常准确，因为Handler也可以运行在非UI线程）； 　　c.使用AsyncTask。
	 * 
	 * @param list
	 */
	@SuppressWarnings("unchecked")
	public void setDeviceList(ArrayList<BluetoothDevice> list) {
		if (list != null) {
			obgList = (ArrayList<BluetoothDevice>) list.clone();
			notifyDataSetChanged();
		}
	}

	public void clearDeviceList() {
		if (obgList != null) {
			obgList.clear();
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return null == obgList ? 0 : obgList.size();
	}

	@Override
	public Object getItem(int position) {
		return null == obgList ? null : obgList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HoldView hold;
		if (convertView == null) {
			// convertView =
			// mInflater.inflate(R.layout.ui_sortlist_adapter_item, null);
			convertView = mInflater.inflate(R.layout.item, null);
			hold = new HoldView();
			hold.initView(convertView);
			convertView.setTag(hold);
		} else {
			hold = (HoldView) convertView.getTag();
		}
		// 对UI内的组件进行操作控制
		hold.mac.setText(obgList.get(position).getAddress());
		hold.name.setText(obgList.get(position).getName());
		return convertView;
	}

	// 内部类，操作要填充的view部件内容
	static class HoldView {
		TextView name;
		TextView mac;

		public void initView(View convertView) {
			name = (TextView) convertView.findViewById(R.id.textView1);
			mac = (TextView) convertView.findViewById(R.id.textView2);
		}
	}
}
