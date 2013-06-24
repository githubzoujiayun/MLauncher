package com.android.mslauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SysShutDownReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (intent.getAction().equals("android.intent.action.ACTION_SHUTDOWN"))
		{
			if ((LauncherActivity) LauncherActivity.msContext != null)
			{
				((LauncherActivity) LauncherActivity.msContext)
						.setSysShutdown();
			}
		}
	}

}
