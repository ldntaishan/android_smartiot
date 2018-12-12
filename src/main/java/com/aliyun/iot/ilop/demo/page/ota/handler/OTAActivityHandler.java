package com.aliyun.iot.ilop.demo.page.ota.handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.aliyun.iot.aep.sdk.framework.AApplication;
import com.aliyun.iot.aep.sdk.log.ALog;
import com.aliyun.iot.ilop.demo.page.ota.OTAConstants;
import com.aliyun.iot.ilop.demo.page.ota.activity.OTAActivity;
import com.aliyun.iot.ilop.demo.page.ota.bean.OTADeviceInfo;
import com.aliyun.iot.ilop.demo.page.ota.bean.OTADeviceSimpleInfo;
import com.aliyun.iot.ilop.demo.page.ota.bean.OTAStatusInfo;
import com.aliyun.iot.ilop.demo.page.ota.business.OTAActivityBusiness;
import com.aliyun.iot.ilop.demo.page.ota.interfaces.IOTAActivity;

/**
 * Created by david on 2018/4/10.
 *
 * @author david
 * @date 2018/04/10
 */
public class OTAActivityHandler extends Handler {
    private static final String TAG = "OTAActivityHandler";
    private IOTAActivity mIActivity;
    private OTAActivityBusiness mBusiness;
    private OTADeviceSimpleInfo mSimpleInfo;

    public OTAActivityHandler(IOTAActivity iOTAActivity) {
        super(Looper.getMainLooper());

        mIActivity = iOTAActivity;
        mBusiness = new OTAActivityBusiness(this);
    }

    /**
     * 刷新数据
     *
     * @param info
     */
    public void refreshData(OTADeviceSimpleInfo info) {
        if (null == mBusiness) {
            return;
        }

        mSimpleInfo = info;

        if (null != info) {
            mBusiness.requestProductInfo(mSimpleInfo.iotId);
        }

        if (null != mIActivity) {
            mIActivity.showLoading();
        }
    }

    /**
     * 请求升级
     *
     * @param
     */
    public void requestUpdate() {
        if (null == mBusiness) {
            return;
        }
        if (!isNetworkAvalible((OTAActivity) mIActivity)) {
            sendEmptyMessage(OTAConstants.OTA_MESSAGE_NETWORK_ERROR);
            return;
        }
        mBusiness.requestUpgrade();
    }


    /**
     * 判断网络情况
     *
     * @param context 上下文
     * @return false 表示没有网络 true 表示有网络
     */
    public static boolean isNetworkAvalible(Context context) {
        // 获得网络状态管理器
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        } else {
            // 建立网络数组
            NetworkInfo net_info = connectivityManager.getActiveNetworkInfo();

            if (net_info != null && net_info.isConnected()) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        if (null == mIActivity) {
            return;
        }

        if (null == mBusiness) {
            return;
        }

        if (null == mSimpleInfo) {
            return;
        }

        if (OTAConstants.MINE_MESSAGE_RESPONSE_OTA_DEVICE_INFO_SUCCESS == msg.what) {
            //设备详情获取成功
            OTADeviceInfo info = (OTADeviceInfo) msg.obj;

            if (null == info) {
                ALog.e(TAG, "info is null");
                return;
            }

            if (null != info.otaFirmwareDTO) {
                String newVersion = info.otaFirmwareDTO.version;

                String newVersionTime = "";
                if (!TextUtils.isEmpty(info.otaFirmwareDTO.timestamp)) {
                    try {
                        Date date = new Date(Long.valueOf(info.otaFirmwareDTO.timestamp));
                        Locale locale = AApplication.getInstance().getResources().getConfiguration().locale;
                        newVersionTime = new SimpleDateFormat("YYYY/MM/dd", locale).format(date);
                    } catch (Exception e) {
                        ALog.e(TAG, "format new version date error" + e);
                    }
                }
                mIActivity.showTips(newVersion + " " + newVersionTime);

                String currentVersion = info.otaFirmwareDTO.currentVersion;

                String currentVersionTime = "";
                if (!TextUtils.isEmpty(info.otaFirmwareDTO.currentTimestamp)) {
                    try {
                        Date date = new Date(Long.valueOf(info.otaFirmwareDTO.currentTimestamp));
                        Locale locale = AApplication.getInstance().getResources().getConfiguration().locale;
                        currentVersionTime = new SimpleDateFormat("YYYY/MM/dd", locale).format(date);
                    } catch (Exception e) {
                        ALog.e(TAG, "format current date error" + e);
                    }
                }
                if (null != info.otaUpgradeDTO) {
                    mIActivity.showCurrentVersion(currentVersion + " " + currentVersionTime);
                }
            }

            if (null != info.otaUpgradeDTO) {
                int status = Integer.valueOf(info.otaUpgradeDTO.upgradeStatus);
                mIActivity.showUpgradeStatus(status);
            }

            mIActivity.showLoaded(null);
        } else if (OTAConstants.MINE_MESSAGE_RESPONSE_OTA_DEVICE_PRODUCT_INFO_SUCCESS == msg.what) {
            //产品详情获取成功
            String netType = "";
            if (null != msg.obj) {
                netType = msg.obj.toString();
            }
            Log.e(TAG, "handleMessage: " + netType);
            mBusiness.generateOTAManager(this, mSimpleInfo.iotId, netType);
            mBusiness.requestDeviceInfo();
        } else if (OTAConstants.MINE_MESSAGE_RESPONSE_OTA_UPGRADE_SUCCESS == msg.what) {
            //升级请求成功
            mIActivity.showUpgradeStatus(OTAConstants.OTA_STATUS_LOADING);
        } else if (OTAConstants.MINE_MESSAGE_RESPONSE_OTA_DEVICE_STATUS_SUCCESS == msg.what) {
            try {
                if (null != msg.obj) {
                    OTAStatusInfo info = (OTAStatusInfo) msg.obj;
                    int status = Integer.parseInt(info.upgradeStatus);
                    mIActivity.showUpgradeStatus(status);
                }
            } catch (Exception e) {
                ALog.e(TAG, "get status error", e);
            }
        } else if (OTAConstants.OTA_MESSAGE_RESQUEST_ERROR == msg.what
                || OTAConstants.MINE_MESSAGE_RESPONSE_OTA_DEVICE_PRODUCT_INFO_FAILED == msg.what
                || OTAConstants.MINE_MESSAGE_RESPONSE_OTA_DEVICE_INFO_FAILED == msg.what) {
            //请求失败(产品详情获取、设备详情获取)
            mIActivity.showLoaded(null);
            mIActivity.showLoadError();
        } else if (OTAConstants.MINE_MESSAGE_RESPONSE_OTA_UPGRADE_FAILED == msg.what) {
            //升级请求失败
            if (null != msg.obj) {
                mIActivity.showLoaded(msg.obj.toString());
            }
            mIActivity.showUpgradeStatus(OTAConstants.OTA_STATUS_FAILURE);
        } else if (OTAConstants.OTA_MESSAGE_NETWORK_ERROR == msg.what) {
            //请求失败(网络错误)
            mIActivity.showNoNetToast();
        }

    }

    public void destroy() {
        removeMessages(OTAConstants.MINE_MESSAGE_RESPONSE_OTA_DEVICE_INFO_FAILED);
        removeMessages(OTAConstants.MINE_MESSAGE_RESPONSE_OTA_DEVICE_INFO_SUCCESS);
        removeMessages(OTAConstants.MINE_MESSAGE_RESPONSE_OTA_DEVICE_STATUS_SUCCESS);
        removeMessages(OTAConstants.MINE_MESSAGE_RESPONSE_OTA_DEVICE_PRODUCT_INFO_SUCCESS);
        removeMessages(OTAConstants.MINE_MESSAGE_RESPONSE_OTA_DEVICE_PRODUCT_INFO_FAILED);
        removeMessages(OTAConstants.MINE_MESSAGE_RESPONSE_OTA_UPGRADE_SUCCESS);
        removeMessages(OTAConstants.MINE_MESSAGE_RESPONSE_OTA_UPGRADE_FAILED);
        removeMessages(OTAConstants.MINE_MESSAGE_RESPONSE_OTA_UPGRADE_STATUS);
        removeMessages(OTAConstants.OTA_MESSAGE_NETWORK_ERROR);

        if (null != mBusiness) {
            mBusiness.destroy();
            mBusiness = null;
        }
        mIActivity = null;
    }
}
