## 动态加载插件apk中的资源
[博客地址](http://shenhuniurou.com/2016/12/28/dynamic-load-apk)

### 场景

前段时间，产品又提出了新的需求，要把app的主题换成圣诞节的主题，过后再换回来。一种思路就是跟夜间模式那样，准备多套主题资源放在app内的资源文件夹内，切换时调用不用的主题即可，但这样无疑增加了app的包体积，而且如果有新的主题资源包要加进来，用户又得更新整个app，这样的更新方式肯定是不好的，这种情况下我们可以考虑另外一种思路，动态加载资源主题包的apk文件。

先看看最终实现的效果对比：

![1701036.png](http://upload-images.jianshu.io/upload_images/1159224-7c88bd3f373ab577.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



### 原理

动态加载apk有两种方式：

- 一种是将资源主题包的apk安装到手机上再读取apk内的资源，这种方式的原理是将宿主app和插件app设置相同的sharedUserId，这样两个app将会在同一个进程中运行，并可以相互访问内部资源了。
- 一种是不用安装资源apk的方式。其原理是通过DexClassLoader类加载器去加载指定路径下的apk、dex或者jar文件，反射出R类中相应的内部类然后根据资源名来获取我们需要的资源id，然后根据资源id得到对应的图片或者xml文件。

### 实现

无论是哪种方式，我们都需要新的资源包，我们新建一个android工程，把需要更换的新图片和xml资源文件放在这个工程对应的目录下，注意，文件名必需和宿主app内对应的文件名相同，因为后面反射是根据资源名去找资源id。然后将这个工程打包成apk并使用跟宿主app相同的签名文件签名，在app启动的Activity中需要加一个检查是否有新的资源包和是否需要删掉资源包的接口（需要后台人员配合写接口），如果有就下载apk，至于安装apk和不安装apk这两种方式哪种更好，我觉得安装apk这种方式不太友好，即使我们可以做到安装后在桌面上没有启动图标，但还是有一个安装的过程，对用户来说，可能不知道这是什么东西，以为又安装了什么新应用，所以我会使用不安装apk来更新这种方式，这里也还是要记录下安装apk方式是怎么做的。

**准备资源包**

新建工程Skin-Plugin，将要更换的图片或者xml文件放在对应的drawable文件夹内，在AndroidManifest.xml中增加shareUserId，然后打包成apk文件。如果是不需要安装apk的，就不用设置shareUserId了。
> shareUserId这个值可以随意设定，但是必须和宿主app里面的设置为相同才行。

![12081.png](http://upload-images.jianshu.io/upload_images/1159224-01bc777b4fa4fd5a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

我这里只更新几个icon图标和底部tab的selector资源。


![12082.png](http://upload-images.jianshu.io/upload_images/1159224-49cb304901c01406.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


> AndroidManifest配置如上图所示，需要注意的是，让app不在桌面上生成应用图标，需要将启动activity去掉下面的过滤配置：


```xml
<intent-filter>
	<action android:name="android.intent.action.MAIN"/>
	<category android:name="android.intent.category.LAUNCHER"/>
</intent-filter>
```

去掉上述配置后这个工程是无法执行Run操作了，但是不要紧，不影响打包成apk。


**加载安装的apk**

前面说过要提供一个接口下载新的资源包，下载后自动安装，我们在使用这些资源的地方去检查资源apk有没有安装，如果有，就加载资源包中的资源，将检查apk是否安装的方法写到工具类中，这里需要传入资源app的包名。

```java
/**
 * apk是否已安装
 * @param packageName
 * @return true已经安装，false未安装或者已经卸载。
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

检查到安装了插件apk后，需要创建一个插件apk内的上下文对象，因为只有插件apk的上下文对象才能获取到它的Resourece对象，从而通过插件上下文获取资源id。

```java
//获取对应插件中的上下文,通过它可得到插件的Resource??
Context?pluginContext?=?this.createPackageContext(packageName,?CONTEXT_IGNORE_SECURITY?|?CONTEXT_INCLUDE_CODE);??
//获取资源id
int resId = pluginContext.getResources().getIdentifier(......);
```


**加载未安装的apk**

同样的这种方式也要提供一个资源包，用户启动app时在后台静默下载插件apk文件，保存到指定的路径下。我们要加载这个插件，就需要一个插件的类加载器，而不是宿主app的类加载器，这时候只能去手动构建DexClassLoader，再通过类加载器，反射出R类中相应的内部类进而获取我们需要的资源id。

```java
/**
 * 加载apk获得内部资源id
 * @param context
 * @param pluginPath apk路径
 */
public static int getResId(Context context, String pluginPath, String apkPackageName, String resName) {
	try {
		//在应用安装目录下创建一个名为app_dex文件夹目录,如果已经存在则不创建
		File optimizedDirectoryFile = context.getDir("dex", Context.MODE_PRIVATE);
		// 构建插件的DexClassLoader类加载器，参数：
		// 1、包含dex的apk文件或jar文件的路径，
		// 2、apk、jar解压缩生成dex存储的目录，
		// 3、本地library库目录，一般为null，
		// 4、父ClassLoader
		DexClassLoader dexClassLoader = new DexClassLoader(pluginPath, optimizedDirectoryFile.getPath(), null, ClassLoader.getSystemClassLoader());
		//通过使用apk自己的类加载器，反射出R类中相应的内部类进而获取我们需要的资源id
		Class<?> clazz = dexClassLoader.loadClass(apkPackageName + ".R$drawable");
		Field field = clazz.getDeclaredField(resName);//得到名为resName的这张图片字段
		return field.getInt(R.id.class);//得到图片id
	} catch (Exception e) {
		e.printStackTrace();
	}
	return 0;
}
```

>其中第二个参数是插件apk的全路径，文件名必需是带.apk，第三个参数是插件apk的包名，第四个参数是资源名。


```java
/**
 * 获取插件apk的包名
 * @param context
 * @param pluginPath 插件apk的绝对路径
 * @return
 */
public static String getPluginPackagename(Context context, String pluginPath) {
	PackageManager pm = context.getPackageManager();
	PackageInfo pkgInfo = pm.getPackageArchiveInfo(pluginPath, PackageManager.GET_ACTIVITIES);
	if (pkgInfo != null) {
		ApplicationInfo appInfo = pkgInfo.applicationInfo;
		String pkgName = appInfo.packageName;//包名
		return pkgName;
	}
	return null;
}
```

只有资源id还不够，还需要插件apk的Resources对象，因为只有它才能根据资源id获取到对应的资源。

```java
/**
 * 获取对应插件的Resource对象
 * @param context
 * @param pluginPath 插件apk的路径，带apk名
 * @return
 */
public static Resources getPluginResources(Context context, String pluginPath) {
	try {
		AssetManager assetManager = AssetManager.class.newInstance();
		// 反射调用方法addAssetPath(String path)
		Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
		// 将插件Apk文件添加进AssetManager
		addAssetPath.invoke(assetManager, pluginPath);
		// 获取宿主apk的Resources对象
		Resources superRes = context.getResources();
		// 获取插件apk的Resources对象
		Resources mResources = new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
		return mResources;
	} catch (Exception e) {
		e.printStackTrace();
	}
	return null;
}
```

获取插件的Resources对象其实就是用反射的方式调用了`AssetManager`类的`addAssetPath`方法，这个方法的目的是将插件apk里的资源都加载到AssetManager对象中进行管理，然后来构建插件apk的Resources。至于为什么要用反射，看看`addAssetPath`的源码：

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

这里有个注解@hide，表示即使它是public的，但是外界仍然无法访问它的，因为android sdk导出的时候会自动忽略隐藏的api，因此只能通过反射来调用。


```java
// 根据资源名去加载新的资源
String pluginPath = Environment.getExternalStorageDirectory().toString() + "/dynamicload/download/skin-plugin.apk";
if (item.getResName() != null) {
	Drawable drawable = Util.getPluginResources(mContext, pluginPath).getDrawable(Util.getResId(mContext, pluginPath, Util.getPluginPackagename(mContext, pluginPath), item.getResName()));
	imageView.setImageDrawable(drawable);
}
```

至此就完成了动态加载插件apk资源，当我们需要切换回原来的资源时，只需要将资源包删除即可，或者重新构建一个资源包，让用户去下载，由于我们是运行时加载，所以当更换了资源包时，第一次打开只是去下载这个插件资源包，再次打开时才会去加载。


代码下载地址：[https://github.com/shenhuniurou/BlogDemos/tree/master/DynamicLoadDemo](https://github.com/shenhuniurou/BlogDemos/tree/master/DynamicLoadDemo)