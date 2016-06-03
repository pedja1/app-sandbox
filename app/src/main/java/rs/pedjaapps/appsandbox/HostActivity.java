package rs.pedjaapps.appsandbox;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * Created by pedja on 3.6.16. 09.55.
 * This class is part of the app-sandbox
 * Copyright © 2016 ${OWNER}
 */
public class HostActivity extends Activity
{
    public static final String INTENT_EXTRA_APK_FILE = "apk_file";
    public static final String INTENT_EXTRA_ACTIVITY = "activity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        TextView test = new TextView(this);
        test.setText("Test");
        test.setTextColor(Color.WHITE);
        setContentView(test);

        Intent intent = getIntent();
        File apkFile = (File) intent.getSerializableExtra(INTENT_EXTRA_APK_FILE);
        String activityName = intent.getStringExtra(INTENT_EXTRA_ACTIVITY);

        Context baseContext = getBaseContext();
        Object mMainThread = getField("mMainThread");
        Instrumentation mInstrumentation = (Instrumentation) getField("mInstrumentation");
        IBinder mToken = (IBinder) getField("mToken");
        int mIdent = (int) getField("mIdent");
        Application mApplication = getApplication();
        Intent mIntent = getIntent();
        ActivityInfo mActivityInfo = (ActivityInfo) getField("mActivityInfo");
        CharSequence mTitle = getTitle();
        Activity mParent = getParent();
        String mEmbeddedID = (String) getField("mEmbeddedID");
        Object mLastNonConfigurationInstances = getField("mLastNonConfigurationInstances");
        Configuration mCurrentConfig = (Configuration) getField("mCurrentConfig");
        String mReferrer = (String) getField("mReferrer");
        Object mVoiceInteractor = getField("mVoiceInteractor");

        try
        {
            Class mMainThreadClass = Class.forName("android.app.ActivityThread");
            Class mLastNonConfigurationInstancesClass = Class.forName("android.app.Activity$NonConfigurationInstances");
            Class mVoiceInteractorClass = Class.forName("com.android.internal.app.IVoiceInteractor");

            File optimizedDexOutputPath = getDir("outdex", 0);
            DexClassLoader dexLoader = new DexClassLoader(apkFile.getAbsolutePath(), optimizedDexOutputPath.getAbsolutePath(), null, ClassLoader.getSystemClassLoader().getParent());

            Class<?> requestedActivityClass = dexLoader.loadClass(activityName);
            Class<?> activityClass = Activity.class;
            Activity activity = (Activity) requestedActivityClass.newInstance();
            Method attach = activityClass.getDeclaredMethod("attach", Context.class, mMainThreadClass, Instrumentation.class,
                    IBinder.class, int.class, Application.class, Intent.class, ActivityInfo.class, CharSequence.class,
                    Activity.class, String.class, mLastNonConfigurationInstancesClass, Configuration.class,/*, String.class,*/ mVoiceInteractorClass);
            attach.setAccessible(true);
            attach.invoke(activity, baseContext, mMainThread, mInstrumentation,
                    mToken, mIdent, mApplication, mIntent, mActivityInfo, mTitle, mParent, mEmbeddedID, mLastNonConfigurationInstances, mCurrentConfig,/*, mReferrer,*/ mVoiceInteractor);
            Method onCreate = activityClass.getDeclaredMethod("onCreate", Bundle.class);
            onCreate.setAccessible(true);
            onCreate.invoke(activity, savedInstanceState);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    private Object getField(String fieldName)
    {
        try
        {
            Field field = Activity.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(this);
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
