package com.aliyun.iot.aep.sdk.base.delegate;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.alibaba.sdk.android.openaccount.ConfigManager;
import com.alibaba.sdk.android.openaccount.util.ResourceUtils;
import com.aliyun.iot.aep.sdk.EnvConfigure;
import com.aliyun.iot.aep.sdk.apiclient.IoTAPIClientImpl;
import com.aliyun.iot.aep.sdk.apiclient.hook.IoTAuthProvider;
import com.aliyun.iot.aep.sdk.base.delegate.adapter.OALoginAdapter;
import com.aliyun.iot.aep.sdk.credential.IoTCredentialProviderImpl;
import com.aliyun.iot.aep.sdk.credential.IotCredentialManager.IoTCredentialManageImpl;
import com.aliyun.iot.aep.sdk.framework.sdk.SDKConfigure;
import com.aliyun.iot.aep.sdk.framework.sdk.SimpleSDKDelegateImp;
import com.aliyun.iot.aep.sdk.log.ALog;
import com.aliyun.iot.aep.sdk.login.LoginBusiness;
import com.aliyun.iot.demo.R;
import com.aliyun.iot.ilop.demo.page.login3rd.OALoginActivity;
import com.facebook.FacebookSdk;


import java.util.Map;

import static com.facebook.FacebookSdk.APPLICATION_ID_PROPERTY;

/**
 * Created by wuwang on 2017/10/30.
 */

public final class OpenAccountSDKDelegate extends SimpleSDKDelegateImp {

    public final static String ENV_KEY_OPEN_ACCOUNT_HOST = "ENV_KEY_OPEN_ACCOUNT_HOST";

    static final private String TAG = "OpenAccountSDKDelegate";

    private String CONSUMER_KEY = "com.twitter.sdk.android.CONSUMER_KEY";
    private String CONSUMER_SECRET = "com.twitter.sdk.android.CONSUMER_SECRET";

    /* API: ISDKDelegate */

    @Override
    public int init(Application app, SDKConfigure configure, Map<String, String> args) {

        //要在OA初始化前调用
        ConfigManager.getInstance().setGoogleClientId(app.getString(R.string.server_client_id));

        String appId = app.getString(R.string.facebook_app_id);
        FacebookSdk.setApplicationId(appId);
        ConfigManager.getInstance().setFacebookId(appId);

        boolean isDebug = "true".equals(args.get(EnvConfigure.KEY_IS_DEBUG));
        ALog.i(TAG, "init OpenAccount -- isDebug :" + isDebug + " env is:" + args.get(APIGatewaySDKDelegate.ENV_KEY_API_CLIENT_API_ENV));
//        initUT(app, configure, args.get(EnvConfigure.KEY_APPKEY), isDebug);
        String env = args == null ? "" : args.get(APIGatewaySDKDelegate.ENV_KEY_API_CLIENT_API_ENV);
        String host = args == null ? "" : args.get(ENV_KEY_OPEN_ACCOUNT_HOST);

        //使用系统默认OA
        OALoginAdapter loginAdapter = new OALoginAdapter(app);
        loginAdapter.setDefaultOAHost(host);
        loginAdapter.setDefaultLoginClass(OALoginActivity.class);
        loginAdapter.init(env, "114d");
        LoginBusiness.init(app, loginAdapter, env);

        IoTCredentialManageImpl.init(args.get(EnvConfigure.KEY_APPKEY));
        IoTAuthProvider provider = new IoTCredentialProviderImpl(IoTCredentialManageImpl.getInstance(app));
        IoTAPIClientImpl.getInstance().registerIoTAuthProvider("iotAuth", provider);

        return 0;
    }
}
