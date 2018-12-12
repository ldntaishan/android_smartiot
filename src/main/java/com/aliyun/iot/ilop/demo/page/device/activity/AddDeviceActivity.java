package com.aliyun.iot.ilop.demo.page.device.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.alink.business.devicecenter.api.add.DeviceInfo;
import com.aliyun.alink.business.devicecenter.api.discovery.IDiscoveryListener;
import com.aliyun.alink.business.devicecenter.api.discovery.LocalDeviceMgr;
import com.aliyun.iot.aep.component.router.Router;
import com.aliyun.iot.aep.sdk.apiclient.IoTAPIClient;
import com.aliyun.iot.aep.sdk.apiclient.IoTAPIClientFactory;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTCallback;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTResponse;
import com.aliyun.iot.aep.sdk.apiclient.request.IoTRequest;
import com.aliyun.iot.aep.sdk.apiclient.request.IoTRequestBuilder;
import com.aliyun.iot.aep.sdk.framework.AActivity;
import com.aliyun.iot.aep.sdk.log.ALog;
import com.aliyun.iot.demo.R;
import com.aliyun.iot.ilop.demo.page.device.FilterCallBack;
import com.aliyun.iot.ilop.demo.page.device.bean.FoundDeviceListItem;
import com.aliyun.iot.ilop.demo.page.device.bean.SupportDeviceListItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddDeviceActivity extends AActivity {
    private String TAG = AddDeviceActivity.class.getSimpleName();
    private Button mBackBtn;
    private LinearLayout mSupportDeviceLL;
    private LinearLayout mFoundDeviceLL;
    private FrameLayout mFailMsgPanel;
    private Handler mHandler = new Handler();
    private FrameLayout mLocalDevicePanelFl;
    ArrayList<String> mDeviceList;
//    private static String CODE = "link://plugin/a123kfz2KdRdrfYc";
//    private static String CODE = "link://plugin/a1231HjhvD1Qrihx";
    private static String CODE = "link://router/connectConfig";

    private ArrayList<SupportDeviceListItem> mSupportDeviceListItems;
    private ArrayList<FoundDeviceListItem> mFoundDeviceListItems;
    private ArrayList<FoundDeviceListItem> mFoundDeviceNeedEnrolleeListItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            mDeviceList = bundle.getStringArrayList("deviceList");
        }

        setContentView(R.layout.add_device_activity);
        mBackBtn = (Button) findViewById(R.id.ilop_main_back_btn);
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mLocalDevicePanelFl = (FrameLayout)findViewById(R.id.local_device_panel_fl);
        mFailMsgPanel = (FrameLayout)findViewById(R.id.fail_msg_panel);
        mFoundDeviceLL = (LinearLayout)findViewById(R.id.found_device_ll);
        mFoundDeviceListItems = new ArrayList<>();
        mFoundDeviceNeedEnrolleeListItems = new ArrayList<>();
        mSupportDeviceLL = (LinearLayout)findViewById(R.id.support_device_ll);

        mSupportDeviceListItems = new ArrayList<>();
        getSupportDeviceListFromSever();
    }

    private void fillSupportDeviceLL() {
        View view = null;
        LayoutInflater inflater = getLayoutInflater();
        if (mSupportDeviceListItems.size() == 0){
            // 没有支持的设备
            mFailMsgPanel.setVisibility(View.VISIBLE);
        }else {
            mFailMsgPanel.setVisibility(View.GONE);
        }
        for (int i = 0; i < mSupportDeviceListItems.size(); i++){
            view = inflater.inflate(R.layout.device_listview_item, null);
            ImageView iv_device_icon = (ImageView)view.findViewById(R.id.list_item_device_icon);
            TextView tv_device_name = (TextView)view.findViewById(R.id.list_item_device_name);
            Button btn_device_connect = (Button)view.findViewById(R.id.list_item_device_action);
            iv_device_icon.setImageResource(R.drawable.add_device);
            tv_device_name.setText(mSupportDeviceListItems.get(i).deviceName);
            final String productKey = mSupportDeviceListItems.get(i).productKey;
            btn_device_connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String code = CODE;
                    Bundle bundle = new Bundle();
                    bundle.putString("productKey", productKey);
                    Router.getInstance().toUrlForResult(AddDeviceActivity.this, code, 1, bundle);
                }
            });

            Log.d("TAG", "fillSupportDeviceLL");
            mSupportDeviceLL.addView(view);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && null != data) {
            Log.d("TAG", "onActivityResult");
            if (data.getStringExtra("productKey") != null){
                final String productKey = data.getStringExtra("productKey");
                final String deviceName = data.getStringExtra("deviceName");

                Intent intent = new Intent(getApplicationContext(), BindAndUseActivity.class);
                final Bundle bundle = new Bundle();
                bundle.putString("productKey", productKey);
                bundle.putString("deviceName", deviceName);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    }

    private void fillFoundDeviceLL() {
        mLocalDevicePanelFl.setVisibility(View.VISIBLE);
        mFoundDeviceLL.removeAllViews();
        View view = null;
        LayoutInflater inflater = getLayoutInflater();

        // 需要绑定的设备列表
        for (int i = 0; i < mFoundDeviceListItems.size(); i++){
            view = inflater.inflate(R.layout.device_listview_item, null);
            ImageView iv_device_icon = (ImageView)view.findViewById(R.id.list_item_device_icon);
            TextView tv_device_name = (TextView)view.findViewById(R.id.list_item_device_name);
            Button btn_device_connect = (Button)view.findViewById(R.id.list_item_device_action);
            btn_device_connect.setText("绑定");
            iv_device_icon.setImageResource(R.drawable.add_device);
            tv_device_name.setText(mFoundDeviceListItems.get(i).deviceName);
            final DeviceInfo deviceInfo = mFoundDeviceListItems.get(i).deviceInfo;
            final int index = i;
            btn_device_connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /**
                     * {
                     "awssVer": "xxx", //有就传
                     "productKey": "xxx",
                     "deviceName": "xxx",
                     "regProductKey": "", // 一般待配网设备有 有就透传
                     "regDeviceName": "", // 一般待配网设备有 有就透传
                     "mac": "xxx", // 可能没有  设备端返回就会传   有就透传
                     "token": "xxx", // 可能没有  设备端返回就会传  不需要传到setDevice
                     "provisionStatus": 0, // 0待配 1已配   有就透传
                     "devType": 0, // 0: wifi device, 1: ethernet device, ... 设备端返回就会传 有就透传
                     "bssid": "xxx", // 设备端返回就会传 有就透传
                     "addDeviceFrom": "ROUTER"， //  有就透传 可选值为ROUTER 或“”
                     "linkType": "ForceAliLinkTypeNone"
                     }
                     */
                    String code = CODE;
                    Bundle bundle = new Bundle();
                    bundle.putString("awssVer", deviceInfo.awssVer.toString());
                    bundle.putString("productKey", deviceInfo.productKey);
                    bundle.putString("deviceName", deviceInfo.deviceName);
                    bundle.putString("regProductKey", deviceInfo.regProductKey);
                    bundle.putString("regDeviceName", deviceInfo.regDeviceName);
                    bundle.putString("token", deviceInfo.token);
                    bundle.putString("devType", deviceInfo.devType);
                    bundle.putString("addDeviceFrom", deviceInfo.addDeviceFrom);
                    bundle.putString("linkType", deviceInfo.linkType);
                    Router.getInstance().toUrlForResult(AddDeviceActivity.this, code, 1, bundle);
                }
            });

            mFoundDeviceLL.addView(view);
        }

        // 需要配网的设备列表
        for (int i = 0; i < mFoundDeviceNeedEnrolleeListItems.size(); i++){
            view = inflater.inflate(R.layout.device_listview_item, null);
            ImageView iv_device_icon = (ImageView)view.findViewById(R.id.list_item_device_icon);
            TextView tv_device_name = (TextView)view.findViewById(R.id.list_item_device_name);
            Button btn_device_connect = (Button)view.findViewById(R.id.list_item_device_action);
            btn_device_connect.setText("连接");
            iv_device_icon.setImageResource(R.drawable.add_device);
            tv_device_name.setText(mFoundDeviceNeedEnrolleeListItems.get(i).deviceName);
            final DeviceInfo deviceInfo = mFoundDeviceNeedEnrolleeListItems.get(i).deviceInfo;
            final int index = i;
            btn_device_connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /**
                     * {
                     "awssVer": "xxx", //有就传
                     "productKey": "xxx",
                     "deviceName": "xxx",
                     "regProductKey": "", // 一般待配网设备有 有就透传
                     "regDeviceName": "", // 一般待配网设备有 有就透传
                     "mac": "xxx", // 可能没有  设备端返回就会传   有就透传
                     "token": "xxx", // 可能没有  设备端返回就会传  不需要传到setDevice
                     "provisionStatus": 0, // 0待配 1已配   有就透传
                     "devType": 0, // 0: wifi device, 1: ethernet device, ... 设备端返回就会传 有就透传
                     "bssid": "xxx", // 设备端返回就会传 有就透传
                     "addDeviceFrom": "ROUTER"， //  有就透传 可选值为ROUTER 或“”
                     "linkType": "ForceAliLinkTypeNone"
                     }
                     */
                    String code = CODE;
                    Bundle bundle = new Bundle();
                    if (deviceInfo.awssVer != null){
                        bundle.putString("awssVer", deviceInfo.awssVer.toString());
                    }

                    bundle.putString("productKey", deviceInfo.productKey);
                    bundle.putString("deviceName", deviceInfo.deviceName);
                    bundle.putString("regProductKey", deviceInfo.regProductKey);
                    bundle.putString("regDeviceName", deviceInfo.regDeviceName);
                    bundle.putString("token", deviceInfo.token);
                    bundle.putString("devType", deviceInfo.devType);
                    bundle.putString("addDeviceFrom", deviceInfo.addDeviceFrom);
                    bundle.putString("linkType", deviceInfo.linkType);

                    Router.getInstance().toUrlForResult(AddDeviceActivity.this, code, 1, bundle);
                }
            });

            mFoundDeviceLL.addView(view);
        }
    }

    private void getSupportLocalDeviceNeedBind() {

    }

    private void getSupportDeviceListFromSever() {
        Map<String, Object> maps = new HashMap<>();
        IoTRequestBuilder builder = new IoTRequestBuilder()
                .setPath("/thing/productInfo/getByAppKey")
                .setApiVersion("1.1.1")
                .setAuthType("iotAuth")
                .setParams(maps);

        IoTRequest request = builder.build();

        IoTAPIClient ioTAPIClient = new IoTAPIClientFactory().getClient();
        ioTAPIClient.send(request, new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                ALog.d("TAG", "onFailure");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        fillSupportDeviceLL();
                    }
                });
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                ALog.d("TAG", "onResponse");
                final int code = ioTResponse.getCode();
                final String msg = ioTResponse.getMessage();
                if (code != 200){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "code = " +code + " msg =" + msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                Object data = ioTResponse.getData();
                if (null != data) {
                    if(data instanceof JSONArray){
                        mSupportDeviceListItems = parseSupportDeviceListFromSever((JSONArray) data);

                    }
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        fillSupportDeviceLL();
                    }
                });
            }
        });



    }

    private void filterDevice(String productKey, String deviceName, final FilterCallBack filterCallBack){
        List<Map<String, String>> devices = new ArrayList<>();
        Map<String, String> device = new HashMap<>(2);
        device.put("productKey", productKey);
        device.put("deviceName", deviceName);
        devices.add(device);

        IoTRequest request = new IoTRequestBuilder()
                .setPath("/awss/enrollee/product/filter")
                .setApiVersion("1.0.2")
                .addParam("iotDevices", devices)
                .setAuthType("iotAuth")
                .build();

        new IoTAPIClientFactory().getClient().send(request, new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {

            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse ioTResponse) {
                if (200 != ioTResponse.getCode()) {
                    return;
                }

                if (!(ioTResponse.getData() instanceof JSONArray)) {
                    return;
                }

                JSONArray items = (JSONArray) ioTResponse.getData();
                //有返回数据，表示服务端支持此pk，dn
                if (null != items && items.length() > 0) {
                    ALog.d("JC", "有返回数据，表示服务端支持此pk，dn");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            filterCallBack.onExist();
                        }
                    });
                }else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            filterCallBack.onNotExist();
                        }
                    });
                }
            }
        });

    }


    private ArrayList<SupportDeviceListItem> parseSupportDeviceListFromSever(JSONArray jsonArray) {
        ArrayList<SupportDeviceListItem> arrayList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                SupportDeviceListItem device = new SupportDeviceListItem();
                device.deviceName = jsonObject.getString("name");
                device.productKey = jsonObject.getString("productKey");
                arrayList.add(device);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }

    private boolean isDeviceBound(String deviceId){
        if (mDeviceList == null){
            return false;
        }
        if (mDeviceList.contains(deviceId)){
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 获取之前发现的所有设备
        List<DeviceInfo> list = LocalDeviceMgr.getInstance().getLanDevices();

        mFoundDeviceListItems = new ArrayList<>();
        LocalDeviceMgr.getInstance().startDiscovery(this, new IDiscoveryListener() {
            @Override
            public void onLocalDeviceFound(DeviceInfo deviceInfo) {
                //要绑定 需要在一分钟之内，否则绑定失败，需要重新发现再绑定
                Log.d("TAG", deviceInfo.toString());
                final FoundDeviceListItem deviceListItem = new FoundDeviceListItem();
                deviceListItem.deviceName = deviceInfo.deviceName;
                deviceListItem.productKey = deviceInfo.productKey;
                deviceListItem.deviceInfo = deviceInfo;
                deviceListItem.deviceStatus = FoundDeviceListItem.NEED_BIND;
                if (false == isDeviceBound(deviceInfo.productKey + deviceInfo.deviceName)){
                    filterDevice(deviceInfo.productKey, deviceInfo.deviceName, new FilterCallBack() {
                        @Override
                        public void onExist() {
                            mFoundDeviceListItems.add(deviceListItem);
                            fillFoundDeviceLL();
                        }

                        @Override
                        public void onNotExist() {
                            fillFoundDeviceLL();
                        }
                    });
                }
            }

            @Override
            public void onEnrolleeDeviceFound(List<DeviceInfo> list) {
                Log.d("TAG", list.toString());
                //要配网
                for (DeviceInfo deviceInfo : list) {
                    final FoundDeviceListItem deviceListItem = new FoundDeviceListItem();
                    deviceListItem.deviceStatus = FoundDeviceListItem.NEED_CONNECT;
                    deviceListItem.deviceInfo = deviceInfo;
                    deviceListItem.deviceName = deviceInfo.deviceName;
                    deviceListItem.productKey = deviceInfo.productKey;
                    if (false == isDeviceBound(deviceInfo.productKey + deviceInfo.deviceName)){
                        filterDevice(deviceInfo.productKey, deviceInfo.deviceName, new FilterCallBack() {
                            @Override
                            public void onExist() {
                                mFoundDeviceNeedEnrolleeListItems.add(deviceListItem);
                                fillFoundDeviceLL();
                            }

                            @Override
                            public void onNotExist() {
                                fillFoundDeviceLL();
                            }
                        });
                    }
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocalDevicePanelFl.setVisibility(View.GONE);
        LocalDeviceMgr.getInstance().stopDiscovery();
    }
}
