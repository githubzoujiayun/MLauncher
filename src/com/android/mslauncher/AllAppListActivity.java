package com.android.mslauncher;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class AllAppListActivity extends Activity
{
	private static final String TAG = "AllAppListActivity";
	private GridView appGridView;
	private List<ResolveInfo> listAllApps;
	private AllAppAdapter mAllAppAdapter;
	private static final int TOTALICON = 15;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.allapplication);
		appGridView = (GridView) findViewById(R.id.appgridview);

		initGridView();

		appGridView.setSelection(0);
	}

	private void initGridView()
	{
		final PackageManager packageManager = getPackageManager();
		final Intent mIntent = new Intent(Intent.ACTION_MAIN, null);
		mIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		listAllApps = packageManager.queryIntentActivities(mIntent, 0);
		mAllAppAdapter = new AllAppAdapter(this, listAllApps);
		appGridView.setAdapter(mAllAppAdapter);
		appGridView.setOnItemClickListener(new AllAppItemcListener());
	}

	private void addItemResult(int page)
	{
		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		ad.setTitle("shortcut");
		if (page >= 5)
		{
			if (page == TOTALICON)
			{
				ad.setMessage("All the page is full");
			}
			else
			{
				int i = page - TOTALICON;
				ad.setMessage("This App have exist at page:" + i);
			}
		}
		else
		{
			ad.setMessage("This Application have added to the " + page + " page");
		}

		ad.setPositiveButton("ok", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int arg1)
			{

			}
		});
		ad.show();
	}

	/*private void changeInputSource(String packName)
	{
		if (packName != null)
		{
			if (packName.contentEquals("mstar.tvsetting.ui")
					|| packName.contentEquals("mstar.factorymenu.ui")
					|| packName.contentEquals("com.tvos.pip")
					|| packName.contentEquals("com.mstar.tvsetting.hotkey")
					|| packName.contentEquals("com.android.settings"))
			{
				Log.i(TAG, "------------TV AP");
			}
			else
			{
				ITvServiceServer tvService = ITvServiceServer.Stub.asInterface(ServiceManager
						.checkService(Context.TV_SERVICE));

				if (tvService == null)
				{
					Log.w(TAG, "Unable to find ITvService interface.");
				}
				try
				{
					ITvServiceServerCommon commonService = tvService.getCommonManager();
					try
					{
						commonService.SetInputSource(EN_INPUT_SOURCE_TYPE.E_INPUT_SOURCE_STORAGE);
					}
					catch (RemoteException e)
					{
						e.printStackTrace();
					}
				}
				catch (RemoteException e)
				{
					e.printStackTrace();
				}
			}
		}
	}*/

	private class AllAppItemcListener implements OnItemClickListener
	{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			ResolveInfo appInfo = (ResolveInfo) parent.getItemAtPosition(position);
//			changeInputSource(appInfo.activityInfo.packageName);
			Intent mIntent = AllAppListActivity.this.getPackageManager().getLaunchIntentForPackage(
					appInfo.activityInfo.packageName);
			mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			try
			{
				AllAppListActivity.this.startActivity(mIntent);
			}
			catch (ActivityNotFoundException anf)
			{
				Toast.makeText(AllAppListActivity.this, "package not find", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

}
