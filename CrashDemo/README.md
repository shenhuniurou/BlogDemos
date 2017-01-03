## Android捕获崩溃异常
[博客地址](http://shenhuniurou.com/2017/01/03/android-crash-handler)

由于我们写的代码难免会出现一些bug，以及由于测试环境和生产环境差异导致出现的一些异常，在测试过程中没有发现，而在app上线之后会偶然出现的一些bug，以至于app在使用过程中出现ANR，这是个令人蛋疼的现象，app卡死、出现黑屏等，总之当app出现异常时的用户体验不友好，我们开发者需要去捕获这些异常，收集这些异常信息，并且上传到服务器，利于开发人员去解决这些问题，同时，我们还需要给用户一个友好的交互体验。

我也查找了许多捕获崩溃异常的文章来看，但大多都千篇一律，只说了怎么捕获异常，处理异常，并没有对捕获异常后的界面交互做出很好的处理，系统出现ANR时会先卡一段时间（这个时间还有点长）然后弹出一个系统默认的对话框，但是我现在需要的是当出现异常情况时，立马弹出对话框，并且对话框我想自定义界面。

最终实现的效果如图：

<img src="http://upload-images.jianshu.io/upload_images/1159224-cdf0b4169183ecaf.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240" width="40%"  alt="最终效果图" align="center" />

首先需要自定义`Application`，并且在`AndroidManifest.xml`中进行配置
![AndroidManifest.xml.png](http://upload-images.jianshu.io/upload_images/1159224-ae3de39787e5a75d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

还需要自定义一个应用异常捕获类`AppUncaughtExceptionHandler`，它必须得实现`Thread.UncaughtExceptionHandler`接口，另外还需要重写`uncaughtException`方法，去按我们自己的方式来处理异常

在`Application`中我们只需要初始化自定义的异常捕获类即可：

```java
@Override public void onCreate() {
	super.onCreate();
	mInstance = this;
	// 初始化文件目录
	SdcardConfig.getInstance().initSdcard();
	// 捕捉异常
	AppUncaughtExceptionHandler crashHandler = AppUncaughtExceptionHandler.getInstance();
	crashHandler.init(getApplicationContext());
}
```

> 其中文件目录是异常信息保存在sd卡中的目录，还有我们是整个app中全局捕获异常，所以我们自定义的捕获类是单例。

```java
/**
 * 初始化捕获类
 *
 * @param context
 */
public void init(Context context) {
	applicationContext = context.getApplicationContext();
	crashing = false;
	//获取系统默认的UncaughtException处理器
	mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
	//设置该CrashHandler为程序的默认处理器
	Thread.setDefaultUncaughtExceptionHandler(this);
}
```

完成以上过程后，接着需要重写`uncaughtException`方法：

```java
@Override
public void uncaughtException(Thread thread, Throwable ex) {
	if (crashing) {
		return;
	}
	crashing = true;

	// 打印异常信息
	ex.printStackTrace();
	// 我们没有处理异常 并且默认异常处理不为空 则交给系统处理
	if (!handlelException(ex) && mDefaultHandler != null) {
		// 系统处理
		mDefaultHandler.uncaughtException(thread, ex);
	}
	byebye();
}

private void byebye() {
	android.os.Process.killProcess(android.os.Process.myPid());
	System.exit(0);
}
```

既然是我们自己处理异常，所以会先执行`handlelException(ex)`方法：

```java
private boolean handlelException(Throwable ex) {
	if (ex == null) {
		return false;
	}
	try {
		// 异常信息
		String crashReport = getCrashReport(ex);
		// TODO: 上传日志到服务器
		// 保存到sd卡
		saveExceptionToSdcard(crashReport);
		// 提示对话框
		showPatchDialog();
	} catch (Exception e) {
		return false;
	}
	return true;
}
```

获取的异常信息包括系统信息，app版本信息，以及手机制造商信息等：

```java
/**
 * 获取异常信息
 * @param ex
 * @return
 */
private String getCrashReport(Throwable ex) {
	StringBuffer exceptionStr = new StringBuffer();
	PackageInfo pinfo = CrashApplication.getInstance().getLocalPackageInfo();
	if (pinfo != null) {
		if (ex != null) {
			//app版本信息
			exceptionStr.append("App Version：" + pinfo.versionName);
			exceptionStr.append("_" + pinfo.versionCode + "\n");

			//手机系统信息
			exceptionStr.append("OS Version：" + Build.VERSION.RELEASE);
			exceptionStr.append("_");
			exceptionStr.append(Build.VERSION.SDK_INT + "\n");

			//手机制造商
			exceptionStr.append("Vendor: " + Build.MANUFACTURER+ "\n");

			//手机型号
			exceptionStr.append("Model: " + Build.MODEL+ "\n");

			String errorStr = ex.getLocalizedMessage();
			if (TextUtils.isEmpty(errorStr)) {
				errorStr = ex.getMessage();
			}
			if (TextUtils.isEmpty(errorStr)) {
				errorStr = ex.toString();
			}
			exceptionStr.append("Exception: " + errorStr + "\n");
			StackTraceElement[] elements = ex.getStackTrace();
			if (elements != null) {
				for (int i = 0; i < elements.length; i++) {
					exceptionStr.append(elements[i].toString() + "\n");
				}
			}
		} else {
			exceptionStr.append("no exception. Throwable is null\n");
		}
		return exceptionStr.toString();
	} else {
		return "";
	}
}
```

将异常信息保存到sd卡这个我觉得可选吧，但是上传到服务端还是很有必要的：

```java
/**
 * 保存错误报告到sd卡
 * @param errorReason
 */
private void saveExceptionToSdcard(String errorReason) {
	try {
		Log.e("CrashDemo", "AppUncaughtExceptionHandler执行了一次");
		String time = mFormatter.format(new Date());
		String fileName = "Crash-" + time + ".log";
		if (SdcardConfig.getInstance().hasSDCard()) {
			String path = SdcardConfig.LOG_FOLDER;
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(path + fileName);
			fos.write(errorReason.getBytes());
			fos.close();
		}
	} catch (Exception e) {
		Log.e("CrashDemo", "an error occured while writing file..." + e.getMessage());
	}
}
```

保存在sd卡中的异常文件格式：

![异常信息](http://upload-images.jianshu.io/upload_images/1159224-1b1efcc2ad2642cf.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

> 至于上传到服务器，就比较灵活了，可以将整个文件上传，或者上传异常信息的字符串，可以和后端开发人员配合。


因为捕获异常后我要马上关闭掉app即上面的`byebye`方法，是将app整个进程杀死，如果接着要显示提示对话框，则需要在新的任务栈中打开`activity`：

```java
public static Intent newIntent(Context context, String title, String ultimateMessage) {

	Intent intent = new Intent();
	intent.setClass(context, PatchDialogActivity.class);
	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

	intent.putExtra(EXTRA_TITLE, title);
	intent.putExtra(EXTRA_ULTIMATE_MESSAGE, ultimateMessage);
	return intent;
}
```

对话框中给出了重启操作的选项，重启过程的实现：

```java
private void restart() {
	Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	startActivity(intent);
	super.onDestroy();
}
```

源代码地址:[https://github.com/shenhuniurou/BlogDemos/tree/master/CrashDemo](https://github.com/shenhuniurou/BlogDemos/tree/master/CrashDemo)欢迎star。

