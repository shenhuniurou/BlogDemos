



![1701036.png](http://upload-images.jianshu.io/upload_images/1159224-7c88bd3f373ab577.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)










![12081.png](http://upload-images.jianshu.io/upload_images/1159224-01bc777b4fa4fd5a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



![12082.png](http://upload-images.jianshu.io/upload_images/1159224-49cb304901c01406.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)




```xml
<intent-filter>
	<action android:name="android.intent.action.MAIN"/>
	<category android:name="android.intent.category.LAUNCHER"/>
</intent-filter>
```





```java
/**
 * @param packageName
 */
public static boolean checkApkInstalled(Context context, String packageName) {
	if (packageName == null || "".equals(packageName)) {
		return false;
	}
	try {
		ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
		return true;
	} catch (PackageManager.NameNotFoundException e) {
		return false;
	}
}
```


```java
Context?pluginContext?=?this.createPackageContext(packageName,?CONTEXT_IGNORE_SECURITY?|?CONTEXT_INCLUDE_CODE);??
int resId = pluginContext.getResources().getIdentifier(......);
```




```java
/**
 * @param context
 */
public static int getResId(Context context, String pluginPath, String apkPackageName, String resName) {
	try {
		File optimizedDirectoryFile = context.getDir("dex", Context.MODE_PRIVATE);
		DexClassLoader dexClassLoader = new DexClassLoader(pluginPath, optimizedDirectoryFile.getPath(), null, ClassLoader.getSystemClassLoader());
		Class<?> clazz = dexClassLoader.loadClass(apkPackageName + ".R$drawable");
	} catch (Exception e) {
		e.printStackTrace();
	}
	return 0;
}
```



```java
/**
 * @param context
 * @return
 */
public static String getPluginPackagename(Context context, String pluginPath) {
	PackageManager pm = context.getPackageManager();
	PackageInfo pkgInfo = pm.getPackageArchiveInfo(pluginPath, PackageManager.GET_ACTIVITIES);
	if (pkgInfo != null) {
		ApplicationInfo appInfo = pkgInfo.applicationInfo;
		return pkgName;
	}
	return null;
}
```


```java
/**
 * @param context
 * @return
 */
public static Resources getPluginResources(Context context, String pluginPath) {
	try {
		AssetManager assetManager = AssetManager.class.newInstance();
		Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
		addAssetPath.invoke(assetManager, pluginPath);
		Resources superRes = context.getResources();
		Resources mResources = new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
		return mResources;
	} catch (Exception e) {
		e.printStackTrace();
	}
	return null;
}
```


```java
/** 
 * Add an additional set of assets to the asset manager.  This can be 
 * either a directory or ZIP file.  Not for use by applications.  Returns 
 * the cookie of the added asset, or 0 on failure. 
 * {@hide} 
 */  
public final int addAssetPath(String path) {  
    int res = addAssetPathNative(path);  
    return res;  
}  
```



```java
String pluginPath = Environment.getExternalStorageDirectory().toString() + "/dynamicload/download/skin-plugin.apk";
if (item.getResName() != null) {
	Drawable drawable = Util.getPluginResources(mContext, pluginPath).getDrawable(Util.getResId(mContext, pluginPath, Util.getPluginPackagename(mContext, pluginPath), item.getResName()));
	imageView.setImageDrawable(drawable);
}
```



