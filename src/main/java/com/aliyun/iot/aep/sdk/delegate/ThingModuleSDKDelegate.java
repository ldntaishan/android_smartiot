package com.aliyun.iot.aep.sdk.delegate;

import android.app.Application;

import com.aliyun.alink.linksdk.tmp.TmpSdk;
import com.aliyun.alink.linksdk.tmp.api.TmpInitConfig;
import com.aliyun.alink.linksdk.tmp.extbone.BoneThing;
import com.aliyun.alink.sdk.jsbridge.BonePluginRegistry;
import com.aliyun.iot.aep.sdk.framework.sdk.SDKConfigure;
import com.aliyun.iot.aep.sdk.framework.sdk.SimpleSDKDelegateImp;

import java.util.Map;

/**
 * @author guikong on 18/4/7.
 */

public class ThingModuleSDKDelegate extends SimpleSDKDelegateImp {

    @Override
    public int init(Application application, SDKConfigure sdkConfigure, Map<String, String> map) {

        TmpSdk.init(application, new TmpInitConfig(TmpInitConfig.DAILY));
        TmpSdk.getDeviceManager().discoverDevices(null, true, 5000, null);

        BonePluginRegistry.register("BoneThing", BoneThing.class);

        return 0;
    }
}
