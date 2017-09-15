package com.diagrams.core.debug;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import com.diagrams.lib.AppSupplier;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Stack;

// by haiping
public final class KwExceptionHandler implements UncaughtExceptionHandler {
	private static final String TAG = "KwExceptionHandler";
	
	public static final String CRASH_LOG_FILENAME = "crash.log";
	public static final String CRASH_LOG_FILENAME_SERVICE = "servicecrash.log";
	public static final String CRASH_LOG_FILENAME_EXCEPT = "except.log";
	//易观崩溃日志@chengxiaohan
	public static final String YIGUANCRASH_LOG_FILENAME = "ygcrash.log";
	//艾瑞崩溃日志@chengxiaohan
	public static final String AIRUICRASH_LOG_FILENAME = "ircrash.log";
	//小米崩溃日志
	public static final String XIAOMICRASH_LOG_FILENAME = "xiaomicrash.log";
	
	public static final String SHOWCRASH_LOG_FILENAME = "showcrash.log";
	private static final String VER = "v1";
	private static final String VER_EXT = "_" + VER + ".txt";

	private static final String DMP_EXT = ".dmp";
	
	private static final boolean EASY_READ = false; // 打开的话\n会被替换成\r\n，方便windows同学打开阅读，OOM时候可能没内存操作，全面发布关闭
	
	private static boolean SEND_CRASHLOG = true;
	private static final String crashLogPath = KwDirs.getDir(KwDirs.CRASH);
	private static Stack<String> additionalInfo = new Stack<String>();
	@SuppressWarnings("unused")
	private static byte[] reservedBuffer;
	private static StringBuilder logBuilder = new StringBuilder(2 * 1024);
	
	public static String processName = "";
	public static boolean isSendJniCrash = true; // 如果遍历进程失败，默认发送
	
	public static int currentPage; // 当前在哪个tab页
	public static long currentPageChangeTime = System.currentTimeMillis();
	public static boolean nowPlayingVisible; // 正在播放是否显示
	public static boolean gameVisible; // 游戏界面是否显示
	public static boolean lockScreenVisible; // 锁屏界面是否显示
	public static boolean lowMemory = false;
	public static long topFragmentChangeTime = System.currentTimeMillis();
	
	static {
		int pid = android.os.Process.myPid();
		try {
			ActivityManager mActivityManager = (ActivityManager) App.getInstance().getSystemService(
					Context.ACTIVITY_SERVICE);
			for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
				if (appProcess.pid == pid) {
					processName = appProcess.processName;
					if ("cn.kuwo.player".equals(processName) || "cn.kuwo.player:service".equals(processName)
							|| "cn.kuwo.player:show".equals(processName)||"cn.kuwo.player:gameweb".equals(processName)
							||"cn.kuwo.player:pushservice".equals(processName)) {
						SEND_CRASHLOG = true;
						isSendJniCrash = true;
					} else {
						SEND_CRASHLOG = false;
						isSendJniCrash = false;
					}
					break;
				}
			}
		} catch (Throwable e) {
		}
	}

	public static void setAdditionalInfo(final String info) {
		if (AppSupplier.IS_FORMAL) {
			return;
		}
		try {
			KwDate date = new KwDate();
			String time = date.toFormatString("yyyy-MM-dd_HH-mm-ss");
			if (EASY_READ) {
				additionalInfo.push(time + info.replaceAll("\n", "\r\n"));
			} else {
				additionalInfo.push(time + info);
			}
		} catch (Throwable e) {
			// 去见上帝
		}
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		reservedBuffer = null; // 请忽略findbugs提示
		if (!AppSupplier.isExiting()) {
			try {
				System.gc();
				//Thread.sleep(1000);
				String logString = KwDebug.throwable2String(ex);
				 //易观服务的崩溃单独存放 @chengxiaohan
				if (logString.contains("MainService") || logString.contains("TimerReceiver") || 
						logString.contains("UtilReceiver") || logString.contains("datatesla")||logString.contains("SystemReceiver")||logString.contains("ShoutDownReceiver")) {
					saveErrorLog(logString, YIGUANCRASH_LOG_FILENAME);
				}else if(logString.contains("com.datac.newspm") || logString.contains("ApplicationBroadcast") || 
						logString.contains("AppFlowBroadcast") || logString.contains("LauncherBroadcast")){
					saveErrorLog(logString, AIRUICRASH_LOG_FILENAME);
				}else if (logString.contains("com.xiaomi")) {
					saveErrorLog(logString, XIAOMICRASH_LOG_FILENAME);
				}else if(logString.contains("cn.kuwo.show")){
					saveErrorLog(logString, SHOWCRASH_LOG_FILENAME);
				}else if(SEND_CRASHLOG){
					if (logString.contains("android.view.SurfaceView.updateWindow")) {
						//不支持动态开屏广告，暂时这样保护了
						ConfMgr.setBoolValue(ConfDef.SEC_AD,ConfDef.KEY_AD_SUPPORT_SURFACEVIEW_KAIPING,false,false);
					}
					PlayMusicImpl.getInstance().realtimeLogPlay("Crash",true);
					if (processName.contains(":service")) {
						saveErrorLog(logString, CRASH_LOG_FILENAME_SERVICE);
					} else {
						saveErrorLog(logString, CRASH_LOG_FILENAME);
					}
				}else{//放到最后
					saveErrorLog(logString, CRASH_LOG_FILENAME_EXCEPT);
				}
			} catch (Throwable e) {
				// amen
			}
		}
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}

	@SuppressWarnings("deprecation")
	public static void saveErrorLog(String log, String fileName) {
		KwDate date = new KwDate();
		String path = null;
		if (TextUtils.isEmpty(fileName)) {
			fileName = date.toFormatString("yyyy-MM-dd_HH-mm-ss") + VER_EXT;
		}
		path = crashLogPath + fileName;

		if (EASY_READ) {
			log = log.replaceAll("\n", "\r\n");
		}
		
		if (AppSupplier.IS_DEBUG) {
			LogMgr.d(TAG, log);
		}
		
		final String splitter;
		if (EASY_READ) {
			splitter = "\r\n";
		} else {
			splitter = "\n";
		}
		
		try {
			logBuilder.append("TIME:").append(date.toDateTimeString());
			logBuilder.append(splitter).append("VERSION:").append(AppSupplier.VERSION_CODE);
			logBuilder.append(splitter).append("INTERVAL_VER:").append(AppInfo.INTERNAL_VERSION);
			logBuilder.append(splitter).append("PACK_TIME:").append(AppInfo.PACK_TIME);
			logBuilder.append(splitter).append("SVN_INFO:").append(AppInfo.SVN_INFO);
			logBuilder.append(splitter).append("UID:").append(AppInfo.getAppUid());
			logBuilder.append(splitter).append("RUN_TIME(s):").append((System.currentTimeMillis() - AppInfo.START_TIME)/1000);
			logBuilder.append(splitter).append("START_TIMES:").append(AppInfo.START_TIMES);
			logBuilder.append(splitter).append("COVER_INSTALL:").append(AppInfo.COVER_INSTALL);
			logBuilder.append(splitter).append("MODEL:").append(Build.MODEL);
			logBuilder.append(splitter).append("PRODUCT:").append(android.os.Build.PRODUCT);
			logBuilder.append(splitter).append("SDK:").append(android.os.Build.VERSION.SDK);
			logBuilder.append(splitter).append("CPU:").append(android.os.Build.CPU_ABI);
			logBuilder.append(splitter).append("FORGROUND:").append(AppInfo.IS_FORGROUND);
			logBuilder.append(splitter).append("IP:").append(AppInfo.CLIENT_IP);
			logBuilder.append(splitter).append("CURRENT_PAGE:").append(currentPage);
			logBuilder.append(splitter).append("CURRENT_PAGE_T(ms):").append(
                    System.currentTimeMillis()-currentPageChangeTime);
			logBuilder.append(splitter).append("CURRENT_FRAGMENT:").append(topFragment);
			logBuilder.append(splitter).append("CURRENT_FRAGMENT_T(ms):").append(
                    System.currentTimeMillis()-topFragmentChangeTime);
			logBuilder.append(splitter).append("NOWPLAY_VISIBLE:").append(nowPlayingVisible);
			logBuilder.append(splitter).append("GAME_VISIBLE:").append(gameVisible);
			logBuilder.append(splitter).append("LOCKSCREEN_VISIBLE:").append(lockScreenVisible);
			logBuilder.append(splitter).append("MAX_MEM:").append(Runtime.getRuntime().maxMemory());
			logBuilder.append(splitter).append("PROCESS:").append(processName);

			logBuilder.append(splitter).append("EXCEPTION:");
			logBuilder.append(splitter).append(log);
		} catch (Throwable e) {
			// ...
		}
		int num = 0;
		while (!additionalInfo.isEmpty()) {
			try {
				logBuilder.append(splitter).append("ADDITIONALINFO ").append(++num).append(":");
				logBuilder.append(splitter).append(additionalInfo.pop());
			} catch (Throwable e) {
				// ...
			}
		}
		logBuilder.append(splitter);
		try {
			FileOutputStream fos = new FileOutputStream(new File(path));
			try {
				IOUtils.writeString(fos, logBuilder.toString());
			} finally {
				fos.close();
			}
			if (AppInfo.IS_DEBUG) {
				LogMgr.d(TAG, "崩溃日志在：" + path);
			} else if (!AppInfo.IS_FORMAL) {
				String saveCrashAsAssertFile = crashLogPath + fileName + VER_EXT;
				KwFileUtils.fileCopy(path, saveCrashAsAssertFile);
				checkSendAssertLog(true);
				Thread.sleep(2000);
			}

			logBuilder.delete(0, logBuilder.length());
		} catch (Throwable e) {
			// OMG，kill me！
		}
	}

	// //// 启动发送之前没发出去的assert日志
	public static void init() {
		//reservedBuffer = new byte[4 * 1024];
		if (!App.isMainProgress()) {
			return;
		}

		if (NetworkStateUtil.isAvaliable()) {
			sendLog(CRASH_LOG_FILENAME, LogDef.LogType.CRASH);
			sendLog(CRASH_LOG_FILENAME_SERVICE, LogDef.LogType.CRASH);
			sendLog(CRASH_LOG_FILENAME_EXCEPT, LogDef.LogType.CRASH_EXCEPT);
			sendLog(SHOWCRASH_LOG_FILENAME, LogDef.LogType.CRASH_XC);

			//第三方崩溃
			sendLog(YIGUANCRASH_LOG_FILENAME, LogDef.LogType.YIGUAN_CRASH);
			sendLog(AIRUICRASH_LOG_FILENAME, LogDef.LogType.AIRUI_CRASH);
			sendLog(XIAOMICRASH_LOG_FILENAME, LogDef.LogType.XIAOMI_CRASH);
			sendJniCrashLog();
		}

		if (!AppInfo.IS_FORMAL) {
			if (NetworkStateUtil.isAvaliable()) {
				MessageManager.getInstance().asyncRun(5000, new MessageManager.Runner() {
					@Override
					public void call() {
						checkSendAssertLog(false);
					}
				});
			} else {
				MessageManager.getInstance().attachMessage(MessageID.OBSERVER_APP, new AppObserver() {
					@Override
					public void IAppObserver_NetworkStateChanged(boolean state, boolean type) {
						if (state) {
							checkSendAssertLog(false);
						}
					}
				});
			}
		}
	}

	private static void sendLog(final String fileName, final LogDef.LogType logType) {
		String path = crashLogPath + fileName;
		if (!KwFileUtils.isExist(path)) {
			return;
		}
		File f = new File(path);
		String log = IOUtils.stringFromFile(f);
		String bakFileName = "crash_sendtime_" + new KwDate().toFormatString("yyyy-MM-dd-HH-mm-ss") + ".txt";
		f.renameTo(new File(KwDirs.getDir(KwDirs.CRASHBAK), bakFileName));
		if (TextUtils.isEmpty(log)) {
			return;
		}
		ServiceLevelLogger.sendLog(logType.name(), log, LogDef.ResourceResult.RESULT_UNKNOWN_ERR);
	}

	private static volatile boolean hasSend;
	private static volatile boolean running;
	public static void checkSendAssertLog(final boolean force) {
		if (!force && hasSend) {
			return;
		}
		if (running) {
			return;
		}
		if (!NetworkStateUtil.isAvaliable()) {
			return;
		}
		hasSend = true;
		running = true;
		KwThreadPool.runThread(KwThreadPool.JobType.NET, new Runnable() {
			@Override
			public void run() {
				try {
					sendAssertLog();
				} catch (Throwable e) {
					// who care
				}
				running = false;
			}
		});
	}

	private static void sendAssertLog() {
		File[] logs=KwFileUtils.getFiles(crashLogPath,new String[]{VER_EXT});
		if (logs==null) {
			return;
		}
		for (File f : logs) {
			try {
				if (sendFile(f)) {
					f.renameTo(new File(KwDirs.getDir(KwDirs.CRASHBAK),"assert"+f.getName()));
					f.delete();
				}
			} catch (Throwable e) {
				// soso
			}
		}
	}

	private static boolean sendFile(final File f) {
		byte[] buffer = IOUtils.bytesFromFile(f);
		if (buffer == null) {
			return false;
		}
		StringBuilder builder = new StringBuilder("http://60.28.200.82:808/uparcrash.lct?");
		builder.append("user=").append(DeviceInfo.DEVICE_ID);
		builder.append("&version=").append(AppInfo.VERSION_CODE);
		builder.append("&src=").append(AppInfo.INSTALL_SOURCE);
		builder.append("&inver=").append(AppInfo.INTERNAL_VERSION);
		HttpSession http = new HttpSession();

		String url = builder.toString();
		HttpResult result = http.post(url, buffer);
		if (!result.ok || result.data == null || !new String(result.data).startsWith("ok")) {
			http = new HttpSession();
			result = http.post(url, buffer);
			if (!result.ok || result.data == null || !new String(result.data).startsWith("ok")) {
				return false;
			}
		}
		return true;
	}

	private static void sendJniCrashLog() {
		final File[] files = KwFileUtils.getFiles(crashLogPath, new String[]{DMP_EXT});
		if (files == null) return;

		KwThreadPool.runThread(KwThreadPool.JobType.NORMAL, new Runnable() {
			@Override
			public void run() {
				for (File f : files) {
					if (!f.isFile() || f.length() <= 0) continue;
					doSendJniCrashLog(f);
				}
			}
		});
	}

	private static void doSendJniCrashLog(File f) {
		try {
		    doSendJniRealLog(f);
		}
		catch (Throwable e) {
			// 此处的实时日志如果异常了，不要影响dmp文件的发送
		}

		KwDate tar = new KwDate();
		tar.fromString("2017-04-30");
		if (new KwDate().before(tar)) {
			sendDmpFile(f);
		}

		String bakFileName = "crash_sendtime_" + new KwDate().toFormatString("yyyy-MM-dd-HH-mm-ss-SSS") + ".dmp";
		f.renameTo(new File(KwDirs.getDir(KwDirs.CRASHBAK), bakFileName));
	}

	@SuppressWarnings("deprecation")
	private static void doSendJniRealLog(final File f) {
		String jniCrashProcess = getProcessNameFromJniCrashFile(f);
		StringBuilder jniContent = new StringBuilder();
		jniContent.append("PROCESS:").append(jniCrashProcess)
			      .append("&MAX_MEM:").append(Runtime.getRuntime().maxMemory())
			      .append("&MODEL:").append(Build.MODEL)
			      .append("&PRODUCT:").append(android.os.Build.PRODUCT)
			      .append("&SDK:").append(android.os.Build.VERSION.SDK)
			      .append("&CPU:").append(android.os.Build.CPU_ABI)
		          .append("&FILENAME:").append(f.getName());

		ServiceLevelLogger.sendLog(LogDef.LogType.JNI_CRASH.name() , jniContent.toString(), LogDef.ResourceResult.RESULT_UNKNOWN_ERR);
		
	}

	private static boolean sendDmpFile(final File f) {
		byte[] buffer = IOUtils.bytesFromFile(f);
		if (buffer == null) {
			return false;
		}
		StringBuilder builder = new StringBuilder("http://60.28.201.13:808/jnicrashv1.lct?");
		builder.append("user=").append(DeviceInfo.DEVICE_ID);
		builder.append("&src=").append(AppInfo.INSTALL_SOURCE);

		String name = f.getName();
		int extPos = name.lastIndexOf('.');
		builder.append("&name=").append(name.substring(0, extPos));

		HttpSession http = new HttpSession();
		
		String url = builder.toString();
		http.setRequestHeader("Content-Type", "application/octet-stream");
		HttpResult result = http.post(url, buffer);
		if (!result.ok || result.data == null || !new String(result.data).startsWith("ok")) {
			http = new HttpSession();
			http.setRequestHeader("Content-Type", "application/octet-stream");
			result = http.post(url, buffer);
			if (!result.ok || result.data == null || !new String(result.data).startsWith("ok")) {
				return false;
			}
		}

		return true;
	}

	private static String getProcessNameFromJniCrashFile(final File f) {
		String name = f.getName();
		if (TextUtils.isEmpty(name)) {
			return "";
		}

		final char sep = '_';
		
		int firstIndex = name.indexOf(sep);
		if (firstIndex == -1) {
			return "";
		}

		int lastIndex = firstIndex;
		int processIndex = lastIndex;

		for (;;) {
			int index = name.indexOf(sep, lastIndex + 1);
			if (index != -1) {
				processIndex = lastIndex;
				lastIndex = index;
			}
			else {
				break;
			}
		}
		
		if (processIndex < lastIndex && processIndex > 0 && lastIndex < name.length()) {
			return name.substring(processIndex + 1, lastIndex);
		}
		
		return "";
	}

	
}
