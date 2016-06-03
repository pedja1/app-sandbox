package rs.pedjaapps.appsandbox;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.af.jutils.Alert;
import com.jaredrummler.apkparser.ApkParser;
import com.jaredrummler.apkparser.model.AndroidComponent;
import com.jaredrummler.apkparser.model.AndroidManifest;
import com.jaredrummler.apkparser.model.ApkMeta;
import com.jaredrummler.apkparser.model.IntentFilter;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class ParserActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Uri uri = intent.getData();

        File apkFile = new File(uri.getPath());

        ApkParser apkParser = ApkParser.create(apkFile);
        try
        {
            ApkMeta apkMeta = apkParser.getApkMeta();
            int apkMinSdk = Integer.parseInt(apkMeta.minSdkVersion);
            int thisMinSdk = Build.VERSION.SDK_INT;

            if (thisMinSdk < apkMinSdk)
            {
                exit(getString(R.string.invalid_sdk, apkMinSdk, thisMinSdk));
                return;
            }

            AndroidManifest manifest = apkParser.getAndroidManifest();
            List<AndroidComponent> activities = manifest.activities;

            //find main activity
            for (AndroidComponent activity : activities)
            {
                for (IntentFilter filter : activity.intentFilters)
                {
                    boolean actionFound = false;
                    boolean categoryFound = false;
                    for (String action : filter.actions)
                    {
                        if ("android.intent.action.MAIN".equals(action))
                        {
                            actionFound = true;
                            break;
                        }
                    }
                    for (String category : filter.categories)
                    {
                        if ("android.intent.category.LAUNCHER".equals(category))
                        {
                            categoryFound = true;
                            break;
                        }
                    }
                    if (actionFound && categoryFound)
                    {
                        startActivity(new Intent(this, HostActivity.class).putExtra(HostActivity.INTENT_EXTRA_APK_FILE, apkFile).putExtra(HostActivity.INTENT_EXTRA_ACTIVITY, activity.name));
                    }
                }
            }
            exit(getString(R.string.launcher_activity_not_found));
        }
        catch (IOException | NumberFormatException | ParseException e)
        {
            if (BuildConfig.DEBUG) e.printStackTrace();
            exit(getString(R.string.failed_to_read_apk));
        }
        finish();
    }

    private void exit(String string)
    {
        Alert.showToast(this, string);
    }
}
