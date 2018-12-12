package com.aliyun.iot.ilop.demo.page.ota.executor;

import com.aliyun.iot.ilop.demo.page.ota.business.listener.IOTAQueryStatusCallback;
import com.aliyun.iot.ilop.demo.page.ota.business.listener.IOTAStartUpgradeCallback;
import com.aliyun.iot.ilop.demo.page.ota.business.listener.IOTAStopUpgradeCallback;
import com.aliyun.iot.ilop.demo.page.ota.interfaces.IOTAExecutor;
import com.aliyun.iot.ilop.demo.page.ota.interfaces.IOTAStatusChangeListener;

/**
 * Created by david on 2018/4/13.
 *
 * @author david
 * @date 2018/04/13
 *
 * 蓝牙 ota
 */
public class BreezeOTAExecutor implements IOTAExecutor {
    public BreezeOTAExecutor(IOTAStatusChangeListener listener) {

    }

    @Override
    public void queryOTAStatus(String iotId, IOTAQueryStatusCallback callback) {

    }

    @Override
    public void startUpgrade(String iotId, IOTAStartUpgradeCallback callback) {

    }

    @Override
    public void stopUpgrade(String iotId, IOTAStopUpgradeCallback callback) {

    }

    @Override
    public void destroy() {

    }
}
