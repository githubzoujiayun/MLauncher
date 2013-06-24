package com.android.mslauncher;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.provider.Settings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemProperties;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

////////////////for tv////////////////////
import com.tvos.common.AudioManager.EnumMuteType;
import com.tvos.common.TvManager;
import com.tvos.common.TvManager.EnumScreenMuteType;
import com.tvos.common.exception.TvCommonException;
import com.tvos.common.vo.TvOsType.EnumInputSource;
import com.tvos.common.vo.TvOsType.EnumScalerWindow;
import com.tvos.common.vo.VideoWindowType;
import com.mstar.tv.service.TvServiceServer;
import com.mstar.tv.service.MiniDataBase;
import com.mstar.tv.service.interfaces.ITvServiceServer;
import com.mstar.tv.service.interfaces.ITvServiceServerCommon;
import com.mstar.tv.service.interfaces.ITvServiceServerChannel;
import com.mstar.tv.service.aidl.EN_INPUT_SOURCE_TYPE;
import com.mstar.tv.service.aidl.EN_MEMBER_SERVICE_TYPE;
import com.mstar.tv.service.aidl.EN_FIRST_SERVICE_INPUT_TYPE;
import com.mstar.tv.service.aidl.EN_FIRST_SERVICE_TYPE;
import android.view.IWindowManager;

import com.mstar.tv.service.aidl.ProgramCount;

public class LauncherActivity extends Activity
{
	private static final String TAG = "LauncherActivity";
	private static final String STR_STATUS_NONE = "0";
	private static final String STR_STATUS_SUSPENDING = "1";
	private static final String STR_STATUS_WAKEUP = "2";

	// --------------Launcher整体布局---------------------
	RelativeLayout mainLayout;

	// --------------五个图片按钮---------------------
	private Button btn_tv;
	private Button btn_movie;
	private Button btn_chrome;
	private Button btn_apps;
	private Button btn_system;
	public static Context msContext;
	private Boolean bSystemShutdown = false;

	// -----------------------字体----------------------------------
	Typeface face;

	// -----------------------节目数量-------------------------------
	private TextView tvNumber_view;

	// --------------apps的数量-------------------------------------
	private TextView appsNumber_view;
	private int number_apps;

	// ---the clock----------------------
	private Draw_clock clock;
	private int hour;
	private int minute;
	protected static final int MSG_CLOCK = 0x1234;
	private LinearLayout clock_pannel;
	public Handler mHandler;
	private Thread mClockThread;

	// ----------------digital time-----------------------
	protected static final int MSG_TIME = 0x1010;
	private int mHour;
	private int mMinutes;
	public Handler mHandler2;
	private Thread mClockThread2;
	private ImageView mHourImage1, mHourImage2, mMinutesImage1, mMinutesImage2;

	private TextView digitalTime_view;

	// ---------------按钮动画-----------------------------
	// private Animation animation;

	// -------------------aidl------------------------------------
	private ProgramCount programCount;
	int count;

	// --------------------消息机制---------------------------------
	public final static int COUNT = 0;
	int program_count;
	TV_Handler tv_hHandler;

	// ----------------------天气---------------------------------
	public MyHandler WeatherHandler;
	private Thread mWeatherThread;
	private TextView temperatrue_view;
	private TextView minus_view; // 负号
	private String weatherstr;
	private static final int MSG_Weather1 = 0x0010, MSG_Weather2 = 0x0011;

	// ------------------for tv------------------------------------
	SurfaceView surfaceView = null;
	android.view.SurfaceHolder.Callback callback;
	private WindowManager wm;
	LayoutParams surfaceParams;
	private boolean PowerOn = true;
	private boolean createsurface = false;
	// ------------------for tv end--------------------------------

	// ------------------for wallpaper-----------------------------
	private static boolean mWallpaperChecked;
	private final BroadcastReceiver mWallpaperReceiver = new WallpaperIntentReceiver();

	// ------------------for wallpaper end-------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		setContentView(R.layout.main);
		msContext = this;

		// 字体初始化
		face = Typeface.createFromAsset(this.getAssets(), "MSYI.TTF");

		/*
		 * 这些如果放在image_num_init()中初始化，在focusChangeListener中
		 * 就会报空指针错误，应该是逻辑顺序错误，所以干脆最先去定义这些图片。
		 */

		// tv节目数量view
		tvNumber_view = (TextView) this.findViewById(R.id.tv_numbers);

		// apps数量view
		appsNumber_view = (TextView) this.findViewById(R.id.appsNumber_view);
		tv_hHandler = new TV_Handler();

		// this.clock_init();
		this.digital_time_init();

		// 气温
		// temperatrue_view = (TextView)
		// this.findViewById(R.id.temperatrue_view);
		// minus_view = (TextView) this.findViewById(R.id.minus);
		// temperatrue_view.setTypeface(face);
		// WeatherHandler = new MyHandler();
		// WeatherHandler.sendEmptyMessage(MSG_Weather1);

		// 隐藏整体界面
		mainLayout = (RelativeLayout) this.findViewById(R.id.mainLayout);
		mainLayout.setVisibility(View.INVISIBLE);

	}

	Handler handlertv = new Handler();
	Runnable handlerRuntv = new Runnable()
	{

		@Override
		public void run()
		{
			try
			{
				surfaceView = new SurfaceView(getApplicationContext());
				if (surfaceView.getVisibility() == View.VISIBLE)
				{
					surfaceView.setVisibility(View.INVISIBLE);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			handlertv.removeCallbacks(handlerRuntv);
		}
	};

	@Override
	protected void onPause()
	{
		super.onPause();
		this.unbindService(connection);

		if (STR_STATUS_SUSPENDING.equals(SystemProperties.get("mstar.str.suspending", "0")))
		{
		}
		else if (STR_STATUS_WAKEUP.equals(SystemProperties.get("mstar.str.suspending", "0")))
		{
			if (surfaceView != null)
			{
				surfaceView.setVisibility(View.INVISIBLE);
			}
			mainLayout.setVisibility(View.INVISIBLE);
			updateWallpaperVisibility(false);
			// Settings.System.putInt(getContentResolver(),
			// "home_hot_key_disable", 1);
		}
		else
		{
			if ((surfaceView != null) && (createsurface))
			{
				surfaceView.setVisibility(View.INVISIBLE);
				if (bSystemShutdown == false)
				{
					setFullscale();
				}
				else
				{
					bSystemShutdown = false;
				}

			}
			handlertv.postDelayed(enable_homekey, 800);
		}
	}

	// delay enableHomekey
	Runnable enable_homekey = new Runnable()
	{

		@Override
		public void run()
		{
			Settings.System.putInt(getContentResolver(), "home_hot_key_disable", 0);
		}
	};

	@Override
	protected void onResume()
	{
		super.onResume();
		this.bindService(new Intent("com.mstar.tv.servcie.aidl"), connection, BIND_AUTO_CREATE);
		// this.button_init();

		if (PowerOn == true)
		{
			setFullscale();
			// modify by dehoo huangdong 2012.12.26
			PowerOn = false;
			ComponentName componentName = new ComponentName("mstar.tvsetting.ui",
					"mstar.tvsetting.ui.RootActivity");
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setComponent(componentName);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			LauncherActivity.this.startActivity(intent);
			// modify by dehoo huangdong 2012.12.26 end

			// handlertv.postDelayed(enable_homekey,2000); //delay 3s enable
			// home key
		}
		else
		{
			if (STR_STATUS_NONE.equals(SystemProperties.get("mstar.str.suspending", "0")))
			{
				Intent it = new Intent("com.biaoqi.stb.launcher.onresume");
				sendBroadcast(it);
				handlertv.removeCallbacks(enable_homekey);
				Settings.System.putInt(getContentResolver(), "home_hot_key_disable", 1);
				if (surfaceView == null)
				{
					mainLayout.setVisibility(View.VISIBLE);
					updateWallpaperVisibility(true);
					handlertv.postDelayed(handlerRuntv, 300);
				}
				else
				{
					// handlertv.postDelayed(pip_thread, 300);
				}
			}
			else if (STR_STATUS_WAKEUP.equals(SystemProperties.get("mstar.str.suspending", "0")))
			{
				ClearTVManager_RestorSTR();
				SetPropertyForSTR("0");
				if (surfaceView != null)
				{
					surfaceView.getHolder().removeCallback(
							(android.view.SurfaceHolder.Callback) callback);
					surfaceView = null;
				}
				// start TV apk here when STR resume

				// modify by dehoo huangdong 2012.12.26
				ComponentName componentName = new ComponentName("mstar.tvsetting.ui",
						"mstar.tvsetting.ui.RootActivity");
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.setComponent(componentName);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				LauncherActivity.this.startActivity(intent);
				// modify by dehoo huangdong 2012.12.26 end

				// handlertv.postDelayed(enable_homekey,3000);//delay 3s enable
				// home key
			}
		}

		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		if (surfaceView != null && surfaceView.getVisibility() == View.VISIBLE)
			surfaceView.setVisibility(View.INVISIBLE);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		this.unbindService(connection);

		if (surfaceView != null)
		{
			surfaceView.getHolder().removeCallback((android.view.SurfaceHolder.Callback) callback);
			surfaceView = null;
		}
	}

	/*
	 * 五个按钮的初始化
	 */
	private void button_init()
	{
		this.btn_tv = (Button) this.findViewById(R.id.btn_tv);
		this.btn_movie = (Button) this.findViewById(R.id.btn_movies);
		this.btn_chrome = (Button) this.findViewById(R.id.btn_chrome);
		this.btn_apps = (Button) this.findViewById(R.id.btn_apps);
		this.btn_system = (Button) this.findViewById(R.id.btn_system);

		// animation = AnimationUtils.loadAnimation(this, R.anim.launcher_anim);

		// ----------Focus-------------------------
		btn_tv.setOnFocusChangeListener(focusChangeListener);
		btn_apps.setOnFocusChangeListener(focusChangeListener);

		// ----------Click-------------------------
		this.btn_tv.setOnClickListener(clickListener);
		this.btn_movie.setOnClickListener(clickListener);
		this.btn_chrome.setOnClickListener(clickListener);
		this.btn_apps.setOnClickListener(clickListener);
		this.btn_system.setOnClickListener(clickListener);
	}

	/*
	 * clock的初始化
	 */
	private void clock_init()
	{
		Calendar time = Calendar.getInstance();

		hour = time.get(Calendar.HOUR);
		minute = time.get(Calendar.MINUTE);

		clock_pannel = (LinearLayout) findViewById(R.id.clock1);

		clock = new Draw_clock(this);
		clock_pannel.addView(clock);

		mHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
				case LauncherActivity.MSG_CLOCK:
				{
					clock_pannel.removeView(clock);
					clock = new Draw_clock(LauncherActivity.this);
					clock_pannel.addView(clock);
				}
					break;
				}
				super.handleMessage(msg);
			}
		};

		mClockThread = new LooperThread();
		mClockThread.start();

	}

	/*
	 * 数字时钟的初始化
	 */
	private void digital_time_init()
	{
		digitalTime_view = (TextView) this.findViewById(R.id.digitalTime_view);
		this.displayTime();
		mHandler2 = new Handler()
		{
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
				case LauncherActivity.MSG_TIME:
					LauncherActivity.this.displayTime();
					break;
				}
				super.handleMessage(msg);
			}
		};

		mClockThread2 = new LooperThread2();
		mClockThread2.start();

	}

	/*
	 * 获取焦点事件处理,如tv获取焦点时，显示当前电视节目数量
	 */
	OnFocusChangeListener focusChangeListener = new OnFocusChangeListener()
	{

		@Override
		public void onFocusChange(View v, boolean hasFocus)
		{
			if (hasFocus)
			{
				switch (v.getId())
				{
				case R.id.btn_tv:
					if (program_count <= 0 || program_count > 999)
					{
						System.out.println("-------tv focus no number---------");
						btn_tv.setBackgroundResource(R.drawable.tv_buttondown_no);
						tvNumber_view.setVisibility(View.GONE);
					}
					else
					{
						System.out.println("-------tv focus 7---------");
						btn_tv.setBackgroundResource(R.drawable.tv_buttondown);
						displayTVs();
						tvNumber_view.setVisibility(View.VISIBLE);
					}
					break;

				case R.id.btn_apps:
					getAppsNumber();
					if (number_apps <= 0 || number_apps > 999)
					{
						System.out.println("-------apps focus no number---------");
						btn_apps.setBackgroundResource(R.drawable.appdown);
						appsNumber_view.setVisibility(View.GONE);
					}
					else
					{
						System.out.println("-------apps focus 22---------");
						btn_apps.setBackgroundResource(R.drawable.appdown_no);

						displayApps();// ----------------------------

						appsNumber_view.setVisibility(View.VISIBLE);
					}
					break;

				default:
					break;
				}
			}
			else
			{
				switch (v.getId())
				{
				case R.id.btn_tv:
					System.out.println("-------tv no focus ---------");
					btn_tv.setBackgroundResource(R.drawable.tv_button);
					tvNumber_view.setVisibility(View.GONE);
					break;

				case R.id.btn_apps:
					System.out.println("-------apps no focus ---------");
					btn_apps.setBackgroundResource(R.drawable.app);
					appsNumber_view.setVisibility(View.GONE);

				default:
					break;
				}
			}
		}
	};

	/*
	 * 五个按钮点击事件处理
	 */
	OnClickListener clickListener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			switch (v.getId())
			{
			case R.id.btn_tv:
				changeInputSource("mstar.tvsetting.ui");
				ComponentName componentName1 = new ComponentName("mstar.tvsetting.ui",
						"mstar.tvsetting.ui.RootActivity");
				Intent intent1 = new Intent(Intent.ACTION_MAIN);
				intent1.addCategory(Intent.CATEGORY_LAUNCHER);
				intent1.setComponent(componentName1);
				intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				LauncherActivity.this.startActivity(intent1);
				break;

			case R.id.btn_movies:
				changeInputSource("com.mstar.localmm");
				ComponentName componentName2 = new ComponentName("com.jrm.localmm",
						"com.jrm.localmm.ui.main.FileBrowserActivity");
				Intent intent2 = new Intent(Intent.ACTION_MAIN);
				intent2.addCategory(Intent.CATEGORY_LAUNCHER);
				intent2.setComponent(componentName2);
				intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				LauncherActivity.this.startActivity(intent2);
				break;

			case R.id.btn_chrome:
				changeInputSource("com.android.browser");
				ComponentName componentName3 = new ComponentName("com.android.browser",
						"com.android.browser.BrowserActivity");
				Intent intent3 = new Intent(Intent.ACTION_MAIN);
				intent3.addCategory(Intent.CATEGORY_LAUNCHER);
				intent3.setComponent(componentName3);
				intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				LauncherActivity.this.startActivity(intent3);
				break;

			case R.id.btn_apps:
				ComponentName componentName4 = new ComponentName("com.android.mslauncher",
						"com.android.mslauncher.AllAppListActivity");
				Intent intent4 = new Intent();
				intent4.setComponent(componentName4);
				LauncherActivity.this.startActivity(intent4);
				break;

			case R.id.btn_system:
				changeInputSource("com.android.settings");
				ComponentName componentName5 = new ComponentName("com.android.settings",
						"com.android.settings.MSettings");
				Intent intent5 = new Intent(Intent.ACTION_MAIN);
				intent5.addCategory(Intent.CATEGORY_LAUNCHER);
				intent5.setComponent(componentName5);
				intent5.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				LauncherActivity.this.startActivity(intent5);
				break;

			default:
				break;
			}
		}
	};

	/*
	 * clock
	 */
	class LooperThread extends Thread
	{
		public void run()
		{
			super.run();
			try
			{
				do
				{
					Message m = new Message();
					m.what = LauncherActivity.MSG_CLOCK;
					LauncherActivity.this.mHandler.sendMessage(m);
					Thread.sleep(1000);

				}
				while (LauncherActivity.LooperThread.interrupted() == false);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/*
	 * digital time
	 */
	class LooperThread2 extends Thread
	{
		public void run()
		{
			super.run();
			try
			{
				do
				{
					Thread.sleep(1000);
					Message m = new Message();
					m.what = LauncherActivity.MSG_TIME;
					LauncherActivity.this.mHandler2.sendMessage(m);

				}
				while (LauncherActivity.LooperThread.interrupted() == false);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/*
	 * 数字实现显示转换成图片显示
	 */
	private void displayTime()
	{
		long time = System.currentTimeMillis();
		final Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		mHour = mCalendar.get(Calendar.HOUR_OF_DAY);// 24小时制,HOUR为12小时制
		mMinutes = mCalendar.get(Calendar.MINUTE);
		digitalTime_view.setTypeface(face);

		/*
		 * 对数字时间进行判断，如果首位为0，则空缺
		 */
		if (mHour / 10 == 0)
		{
			digitalTime_view.setText(" " + mHour % 10 + "" + ":" + mMinutes / 10 + "" + mMinutes
					% 10);
		}
		else
		{
			digitalTime_view.setText(mHour / 10 + "" + mHour % 10 + "" + ":" + mMinutes / 10 + ""
					+ mMinutes % 10);
		}
	}

	/*
	 * tv 节目总数view显示
	 */
	private void displayTVs()
	{
		tvNumber_view.setTypeface(face);
		if (program_count < 10)
		{
			tvNumber_view.setText("0" + String.valueOf(program_count));
		}
		else
		{
			tvNumber_view.setText(String.valueOf(program_count));
		}

	}

	/*
	 * 获取apps的数量
	 */
	private int getAppsNumber()
	{
		final PackageManager packageManager = getPackageManager();
		final Intent mIntent = new Intent(Intent.ACTION_MAIN, null);
		mIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		number_apps = packageManager.queryIntentActivities(mIntent, 0).size();

		return number_apps;
	}

	/*
	 * apps 数量view显示
	 */
	private void displayApps()
	{

		appsNumber_view.setTypeface(face);

		if (number_apps < 10)
		{
			appsNumber_view.setText("0" + String.valueOf(number_apps));
		}
		else
		{
			appsNumber_view.setText(String.valueOf(number_apps));
		}
	}

	/*
	 * 远程获取tvsettingui中的tv节目数量
	 */
	private ServiceConnection connection = new ServiceConnection()
	{

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			programCount = null;
			count = 0;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			programCount = ProgramCount.Stub.asInterface(service);
			try
			{
				count = programCount.getProgramCount();
				System.out.println("count = " + count);
				Message message = new Message();
				message.arg1 = count;
				message.what = COUNT;
				tv_hHandler.sendMessage(message);
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
		}
	};

	/*
	 * tv节目数量Handler
	 */
	class TV_Handler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			switch (msg.what)
			{
			case COUNT:
				program_count = msg.arg1;

				/*
				 * 将image_num_init()放在Handler里面，而不是onResume()内，才能实现刷新
				 * 如果放在onResume()里面，则会先运行image_num_init()，再会运行ServiceConnection。
				 * 这样就能保证先拿到数据，再运行image_num_init()方法。
				 */
				LauncherActivity.this.button_init();
				break;

			default:
				break;
			}
		}
	}

	/*
	 * 禁止MLauncher界面的返回键
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			return false;
		}
		return super.onKeyDown(keyCode, event);
	};

	// 设置全屏播放
	private void setFullscale()
	{
		try
		{
			VideoWindowType videoWindowType = new VideoWindowType();
			videoWindowType.height = 0xffff;
			videoWindowType.width = 0xffff;
			videoWindowType.x = 0xffff;
			videoWindowType.y = 0xffff;

			TvManager.getPictureManager().selectWindow(EnumScalerWindow.E_MAIN_WINDOW);
			TvManager.getPictureManager().setDisplayWindow(videoWindowType);
			TvManager.getPictureManager().scaleWindow();
		}
		catch (TvCommonException e)
		{
			e.printStackTrace();
		}
	}

	private void setDefaultWallpaper()
	{

		if (!mWallpaperChecked)
		{
			Drawable wallpaper = peekWallpaper();
			if (wallpaper == null)
			{
				try
				{
					clearWallpaper();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				getWindow().setBackgroundDrawable(new ClippedDrawable(wallpaper));
			}
			mWallpaperChecked = true;
		}
	}

	private class ClippedDrawable extends Drawable
	{
		private final Drawable mWallpaper;

		public ClippedDrawable(Drawable wallpaper)
		{
			mWallpaper = wallpaper;
		}

		@Override
		public void setBounds(int left, int top, int right, int bottom)
		{
			super.setBounds(left, top, right, bottom);
			mWallpaper.setBounds(left, top, left + mWallpaper.getIntrinsicWidth(),
					top + mWallpaper.getIntrinsicHeight());
		}

		public void setAlpha(int alpha)
		{
			mWallpaper.setAlpha(alpha);
		}

		public void setColorFilter(ColorFilter cf)
		{
			mWallpaper.setColorFilter(cf);
		}

		public int getOpacity()
		{
			return mWallpaper.getOpacity();
		}

		@Override
		public void draw(Canvas canvas)
		{
			mWallpaper.draw(canvas);

		}
	}

	private void registerIntentReceivers()
	{
		IntentFilter filter = new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED);
		registerReceiver(mWallpaperReceiver, filter);
	}

	public void changeInputSource(String packName)
	{

		if (packName != null)
		{

			if (packName.contentEquals("mstar.tvsetting.ui")
					|| packName.contentEquals("mstar.factorymenu.ui")
					|| packName.contentEquals("com.tvos.pip")
					|| packName.contentEquals("com.mstar.tvsetting.hotkey")
					|| packName.contentEquals("com.android.settings"))
			{

			}
			else
			{
				ITvServiceServer tvService = ITvServiceServer.Stub.asInterface(ServiceManager
						.checkService(Context.TV_SERVICE));
				if (tvService == null)
				{

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
	}

	/*
	 * 壁纸设置的广播
	 */
	private class WallpaperIntentReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			updateWallpaperVisibility(true);
		}
	}

	private void startWallpaper()
	{
		final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
		startActivity(Intent.createChooser(pickWallpaper, getString(R.string.wallpaper)));
	}

	void updateWallpaperVisibility(boolean visible)
	{
		int wpflags = visible ? WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER : 0;
		int curflags = getWindow().getAttributes().flags
				& WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
		if (wpflags != curflags)
		{
			getWindow().setFlags(wpflags, WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
		}
	}

	private void ClearTVManager_RestorSTR()
	{
		try
		{
			TvManager tvmanager = TvManager.getListenerHandle();
			tvmanager.finalizeAllManager();
		}
		catch (Throwable e1)
		{
			e1.printStackTrace();
		}
	}

	private void SetPropertyForSTR(String value)
	{
		IWindowManager winService = IWindowManager.Stub.asInterface(ServiceManager
				.checkService(Context.WINDOW_SERVICE));
		if (winService == null)
		{

		}
		else
		{
			try
			{
				winService.setSystemProperties("mstar.str.suspending", value);
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
		}
	}

	// handle启动获取温度线程，得到温度数据
	class MyHandler extends Handler
	{

		public MyHandler()
		{
		}

		@Override
		public void dispatchMessage(Message msg)
		{
			super.dispatchMessage(msg);

			switch (msg.what)
			{
			case LauncherActivity.MSG_Weather1:
				mWeatherThread = new WeatherThread();
				mWeatherThread.start();
				break;

			case LauncherActivity.MSG_Weather2:
				Bundle b = msg.getData();
				weatherstr = b.getString("textStr");
				System.out.println("==============================" + weatherstr);
				temperatrue_view.setText(weatherstr);// ---------------------weatherstr
				int temp = Integer.parseInt(weatherstr);
				if (temp < 0)
				{
					minus_view.setVisibility(View.VISIBLE);
				}
				else
				{
					minus_view.setVisibility(View.GONE);
				}

				System.out.println("**********************");
				break;

			default:
				break;

			}
			super.handleMessage(msg);
		}

	}

	/*
	 * 线程获取温度
	 */
	class WeatherThread extends Thread
	{
		public void run()
		{
			super.run();
			try
			{
				do
				{
					Thread.sleep(1000);
					URL url = new URL("http://weather.yahooapis.com/forecastrss?w=2122265&u=f");

					getWeather();

				}
				while (LauncherActivity.WeatherThread.interrupted() == false);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/*
	 * 通过SAX方式解析xml
	 */
	public void getWeather() throws MalformedURLException, SAXException,
			ParserConfigurationException
	{
		InputStream input = null;
		URL url = new URL("http://weather.yahooapis.com/forecastrss?w=2122265&u=f");
		try
		{
			input = url.openStream();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		SAXParserFactory factory = SAXParserFactory.newInstance();

		factory.setNamespaceAware(false);
		SAXParser parser = factory.newSAXParser();

		XMLReader reader = parser.getXMLReader();
		try
		{
			parser.parse(input, new YahooHandler());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/*
	 * 得到温度
	 */
	public class YahooHandler extends DefaultHandler
	{

		public void startElement(String uri, String localName, String qName, Attributes attributes)

		throws SAXException
		{
			if ("yweather:condition".equals(qName))
			{
				String temperature = attributes.getValue(2);
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putString("textStr", temperature);
				msg.setData(b);
				msg.what = LauncherActivity.MSG_Weather2;
				LauncherActivity.this.WeatherHandler.sendMessage(msg);
			}
			return;
		}
	}

	/*
	 * 系统关机相关，old
	 */
	public void setSysShutdown()
	{
		bSystemShutdown = true;
	}

}
