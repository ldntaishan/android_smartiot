package com.aliyun.iot.ilop.demo;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.aliyun.alink.linksdk.tools.ThreadTools;
import com.aliyun.alink.sdk.bone.plugins.config.BoneConfig;
import com.aliyun.iot.aep.component.router.IUrlHandler;
import com.aliyun.iot.aep.component.scan.ScanManager;
import com.aliyun.iot.aep.routerexternal.RouterExternal;
import com.aliyun.iot.aep.sdk.EnvConfigure;
import com.aliyun.iot.aep.sdk.PushManager;
import com.aliyun.iot.aep.sdk.apiclient.IoTAPIClientImpl;
import com.aliyun.iot.aep.sdk.base.delegate.APIGatewaySDKDelegate;
import com.aliyun.iot.aep.sdk.base.delegate.OpenAccountSDKDelegate;
import com.aliyun.iot.aep.sdk.delegate.DownstreamConnectorSDKDelegate;
import com.aliyun.iot.aep.sdk.delegate.RNContainerComponentDelegate;
import com.aliyun.iot.aep.sdk.framework.AApplication;
import com.aliyun.iot.aep.sdk.framework.bundle.BundleManager;
import com.aliyun.iot.aep.sdk.framework.bundle.IBundleRegister;
import com.aliyun.iot.aep.sdk.framework.bundle.PageConfigure;
import com.aliyun.iot.aep.sdk.log.ALog;
import com.aliyun.iot.ilop.ApplicationHelper;
import com.aliyun.iot.ilop.demo.page.device.scan.BoneMobileScanPlugin;
import com.aliyun.iot.ilop.demo.page.main.StartActivity;
import com.aliyun.iot.ilop.page.scan.ScanPageInitHelper;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by wuwang on 2017/10/30.
 */

public class DemoApplication extends AApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        // Push SDK 需要在主进程和子进程都初始化
        PushManager.getInstance().init(this);

        // 其他 SDK, 仅在 主进程上初始化
        String packageName = this.getPackageName();
        if (!packageName.equals(ThreadTools.getProcessName(this, android.os.Process.myPid()))) {
            return;
        }
        // 设置日志级别
        ALog.setLevel(ALog.LEVEL_DEBUG);
        com.aliyun.alink.linksdk.tools.ALog.setLevel(com.aliyun.alink.linksdk.tools.ALog.LEVEL_DEBUG);

        // set env for sdks .begin
        // api gateway
        EnvConfigure.putEnvArg(APIGatewaySDKDelegate.ENV_KEY_API_CLIENT_API_ENV, "RELEASE");
        EnvConfigure.putEnvArg(APIGatewaySDKDelegate.ENV_KEY_API_CLIENT_DEFAULT_HOST, "api.link.aliyun.com");
        // 海外版初始化
        // EnvConfigure.putEnvArg(APIGatewaySDKDelegate.ENV_KEY_API_CLIENT_DEFAULT_HOST, "api-iot.ap-southeast-1.aliyuncs.com");
        // OA
        EnvConfigure.putEnvArg(OpenAccountSDKDelegate.ENV_KEY_OPEN_ACCOUNT_HOST, null);
        // 海外版初始化
        // EnvConfigure.putEnvArg(OpenAccountSDKDelegate.ENV_KEY_OPEN_ACCOUNT_HOST, "sgp-sdk.openaccount.aliyun.com");
        // MQTT
        EnvConfigure.putEnvArg(DownstreamConnectorSDKDelegate.ENV_KEY_MQTT_HOST, null);
        EnvConfigure.putEnvArg(DownstreamConnectorSDKDelegate.ENV_KEY_MQTT_AUTO_HOST, "false");
        // 海外版初始化
        // EnvConfigure.putEnvArg(DownstreamConnectorSDKDelegate.ENV_KEY_MQTT_AUTO_HOST, "true");
        EnvConfigure.putEnvArg(DownstreamConnectorSDKDelegate.ENV_KEY_MQTT_CHECK_ROOT_CRT, "true");
        // set env for sdks .end

        EnvConfigure.putEnvArg(RNContainerComponentDelegate.KEY_RN_CONTAINER_PLUGIN_ENV, "test");
        EnvConfigure.putEnvArg(EnvConfigure.KEY_LANGUAGE, "zh-CN");

        // the key set from sp that need to be put into AConfigure.envArgs
        HashSet spKeySet = new HashSet();
        EnvConfigure.init(this, spKeySet);

        new ApplicationHelper().onCreate(this);

        /* 加载Native页面 */
        BundleManager.init(this, new IBundleRegister() {
            @Override
            public void registerPage(Application application, PageConfigure configure) {
                if (null == configure || null == configure.navigationConfigures)
                    return;

                ArrayList<String> nativeUrls = new ArrayList<>();
                ArrayList<PageConfigure.NavigationConfigure> configures = new ArrayList<>();

                PageConfigure.NavigationConfigure deepCopyItem = null;
                for (PageConfigure.NavigationConfigure item : configure.navigationConfigures) {
                    if (null == item.navigationCode || item.navigationCode.isEmpty() || null == item.navigationIntentUrl || item.navigationIntentUrl.isEmpty())
                        continue;

                    deepCopyItem = new PageConfigure.NavigationConfigure();
                    deepCopyItem.navigationCode = item.navigationCode;
                    deepCopyItem.navigationIntentUrl = item.navigationIntentUrl;
                    deepCopyItem.navigationIntentAction = item.navigationIntentAction;
                    deepCopyItem.navigationIntentCategory = item.navigationIntentCategory;

                    configures.add(deepCopyItem);

                    nativeUrls.add(deepCopyItem.navigationIntentUrl);

                    ALog.d("BundleManager", "register-native-page: " + item.navigationCode + ", " + item.navigationIntentUrl);

                    RouterExternal.getInstance().registerNativeCodeUrl(deepCopyItem.navigationCode, deepCopyItem.navigationIntentUrl);
                    RouterExternal.getInstance().registerNativePages(nativeUrls, new NativeUrlHandler(deepCopyItem));
                }
            }
        });

        // 支持扫码调试
        ScanManager.getInstance().registerPlugin("boneMobile", new BoneMobileScanPlugin());

        //初始化pagescan页面的router配置
        ScanPageInitHelper.initPageScanRouterConfig();

        //初始化之后，可以改变显示语言
        //switchLanguage("en-US");

    }
    /**
     * help class
     */
    static final private class NativeUrlHandler implements IUrlHandler {

        private final String TAG = "ApplicationHelper$NativeUrlHandler";

        private final PageConfigure.NavigationConfigure navigationConfigure;

        NativeUrlHandler(PageConfigure.NavigationConfigure configures) {
            this.navigationConfigure = configures;
        }

        @Override
        public void onUrlHandle(Context context, String url, Bundle bundle, boolean startActForResult, int reqCode) {
            ALog.d(TAG, "onUrlHandle: url: " + url);
            if (null == context || null == url || url.isEmpty())
                return;

            /* prepare the intent */
            Intent intent = new Intent();
            intent.setData(Uri.parse(url));

            if (null != this.navigationConfigure.navigationIntentAction)
                intent.setAction(this.navigationConfigure.navigationIntentAction);
            if (null != this.navigationConfigure.navigationIntentCategory)
                intent.addCategory(this.navigationConfigure.navigationIntentCategory);

            if (Build.VERSION.SDK_INT >= 26) {//解决android8.0路由冲突问题，将intent行为限制在本应用内
                intent.setPackage(context.getPackageName());
            }

            /* start the navigated activity */
            ALog.d(TAG, "startActivity(): url: " + this.navigationConfigure.navigationIntentUrl + ", startActForResult: " + startActForResult + ", reqCode: " + reqCode);
            this.startActivity(context, intent, bundle, startActForResult, reqCode);
        }

        private void startActivity(Context context, Intent intent, Bundle bundle, boolean startActForResult, int reqCode) {
            if (null == context || null == intent)
                return;


            if (null != bundle) {
                intent.putExtras(bundle);
            }
            /* startActivityForResult() 场景，只能被 Activity 调用 */
            if (startActForResult) {
                if (false == (context instanceof Activity))
                    return;

                ((Activity) context).startActivityForResult(intent, reqCode);

                return;
            }

            /* startActivity 被 Application 调用时的处理 */
            if (context instanceof Application) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
            /* startActivity 被 Activity、Service 调用时的处理 */
            else if (context instanceof Activity || context instanceof Service) {
                context.startActivity(intent);
            }
            /* startActivity 被其他组件调用时的处理 */
            else {
                // 暂不支持
            }
        }
    }


    /**
     * 改变语言，当前仅支持 “zh-CN”, "en-US" 两个选项
     * @param locale: “zh-CN” 或 "en-US"
     */
    private void switchLanguage(String locale) {
        IoTAPIClientImpl.getInstance().setLanguage(locale); // 全局配置，设置后立即起效
        // 当前仅支持 “zh-CN”, "en-US" 两个选项

        // 容器更改语言
        BoneConfig.set("language", locale);

        // APIClient更改语言后，push通道重新绑定即可更改push语言
        PushManager.getInstance().bindUser();

    }

}
