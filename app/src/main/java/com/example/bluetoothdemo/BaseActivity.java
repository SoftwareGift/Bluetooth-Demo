package com.example.bluetoothdemo;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Activity基类，所有的activity继承此类
 */
public class BaseActivity extends FragmentActivity {
	public static List<Activity> activityList = new ArrayList<Activity>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		activityList.add(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		activityList.remove(this);
		super.onDestroy();
	}

	/**
	 * 获取已经启动的Activity实例
	 * 
	 * @param otherActivitySimpleName
	 *            已经启动过的Activity实例
	 * @return 如果没有启动过将返回null，如果启动后销毁了也返回null
	 */
	protected Activity getOtherActivity(String otherActivitySimpleName) {
		for (Activity activity : activityList) {
			if (otherActivitySimpleName.equals(activity.getClass().getSimpleName())) {
				return activity;
			}
		}
		return null;
	}

	/**
	 * 获取当前的Activity实例
	 * 
	 * @return 当前的Activity实例
	 */
	public static Activity getNowActivity() {
		if (0 != activityList.size()) {
			return activityList.get(activityList.size() - 1);
		}
		return null;
	}
}
