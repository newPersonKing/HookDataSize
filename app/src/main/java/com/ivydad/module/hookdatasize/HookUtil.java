package com.ivydad.module.hookdatasize;

import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.util.ArrayMap;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HookUtil {


    public static void hook() {
        try {
            // 1. 首先我们通过ServiceManager中的sCache获取到原来activity_task对应的IBinder；
            Class serviceManager = Class.forName("android.os.ServiceManager");
            Method getServiceMethod = serviceManager.getMethod("getService", String.class);

            Field sCacheField = serviceManager.getDeclaredField("sCache");
            sCacheField.setAccessible(true);
            Map<String, IBinder> sCache = (Map<String, IBinder>) sCacheField.get(null);
            Map<String, IBinder> sNewCache;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                sNewCache = new ArrayMap<>();
                sNewCache.putAll(sCache);
            } else {
                sNewCache = new HashMap<>(sCache);
            }

            final IBinder activityTaskRemoteBinder = (IBinder) getServiceMethod.invoke(null, "activity_task");

            // 步骤2，步骤3
            // 替换调 activity_task 对应的IBinder 动态代理
            sNewCache.put("activity_task", (IBinder) Proxy.newProxyInstance(serviceManager.getClassLoader(),
                    new Class[]{IBinder.class},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            Log.d("zhy", "activity_task method = " + method.getName() + ", args = " + Arrays.toString(args));
                            if ("transact".equals(method.getName())) {
                                if (args != null && args.length > 1) {
                                    Object arg = args[1];
                                    if (arg instanceof Parcel) {
                                        Parcel parcelArg = (Parcel) arg;
                                        int dataSize = parcelArg.dataSize();
                                        if (dataSize > 300 * 1024) {
                                            // TODO 报警
                                            Log.e("zhy", Log.getStackTraceString(new RuntimeException("[error]TransactionTooLargeException: parcel size exceed 300Kb:" + dataSize)));
                                            if (BuildConfig.DEBUG) {
                                                if (dataSize > 512 * 1024) {
                                                    throw new RuntimeException("[error]TransactionTooLargeException: parcel size exceed 300Kb:" + dataSize);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            return method.invoke(activityTaskRemoteBinder, args);
                        }
                    }));
            sCacheField.set(null, sNewCache);
            Log.d("zhy", "hook success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
