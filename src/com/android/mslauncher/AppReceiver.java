package com.android.mslauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED"))
		{
			String packageName = intent.getDataString();
		}
		else if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED"))
		{
			String packageName = intent.getDataString();
			if ((LauncherActivity) LauncherActivity.msContext != null)
			{
			}
		}
	}

}
