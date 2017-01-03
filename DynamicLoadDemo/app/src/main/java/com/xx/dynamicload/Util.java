package com.xx.dynamicload;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by wxx
 * on 2016/12/9.
 */

public class Util {

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

}
