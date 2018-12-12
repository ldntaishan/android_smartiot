package com.aliyun.iot.ilop.demo.page.ilopmain;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.aliyun.iot.aep.component.router.Router;
import com.aliyun.iot.aep.sdk.apiclient.IoTAPIClient;
import com.aliyun.iot.aep.sdk.apiclient.IoTAPIClientFactory;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTCallback;
import com.aliyun.iot.aep.sdk.apiclient.callback.IoTResponse;
import com.aliyun.iot.aep.sdk.apiclient.emuns.Scheme;
import com.aliyun.iot.aep.sdk.apiclient.request.IoTRequest;
import com.aliyun.iot.aep.sdk.apiclient.request.IoTRequestBuilder;
import com.aliyun.iot.aep.sdk.log.ALog;
import com.aliyun.iot.aep.sdk.login.LoginBusiness;
import com.aliyun.iot.demo.R;
import com.aliyun.iot.ilop.demo.page.bean.DeviceInfoBean;
import com.aliyun.iot.ilop.demo.view.DevicePanelView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeTabFragment extends android.support.v4.app.Fragment {
    private String TAG = HomeTabFragment.class.getSimpleName();
    private Button mIlopMainAddBtn;
    private Button mIlopMainAddBigBtn;
    private Button mIlopMainMenuAddDeviceBtn;
    private Button mIlopMainMenuScanBtn;
    private FrameLayout mIlopMainMenu;
    private FrameLayout mVDevicePanel;
    private FrameLayout mMyDevicePanel;
    private FrameLayout mMyDevicePanelAdd;
    private Handler mHandler = new Handler();
    private int mRegisterCount = 0, mVirtualCount = 0;
    private Bundle mBundle = new Bundle();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hometab_fragment_layout, null);
        mIlopMainAddBtn = (Button) view.findViewById(R.id.ilop_main_add_btn);
        mIlopMainAddBigBtn = (Button) view.findViewById(R.id.ilop_main_add_big_btn);
        mIlopMainMenuScanBtn = (Button) view.findViewById(R.id.ilop_main_menu_scan_btn);
        mIlopMainMenuAddDeviceBtn = (Button) view.findViewById(R.id.ilop_main_menu_add_device_btn);
        mIlopMainMenu = (FrameLayout) view.findViewById(R.id.ilop_main_menu);
        mMyDevicePanel = (FrameLayout) view.findViewById(R.id.my_device_panel);
        mVDevicePanel = (FrameLayout) view.findViewById(R.id.my_vdevice_panel);
        mMyDevicePanelAdd = (FrameLayout) view.findViewById(R.id.my_device_panel_add);

        mIlopMainAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIlopMainMenu.setVisibility(View.VISIBLE);
                mIlopMainMenu.bringToFront();
            }
        });
        mIlopMainAddBigBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Router.getInstance().toUrl(getContext(), "page/ilopadddevice", mBundle);
            }
        });
        mIlopMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIlopMainMenu.setVisibility(View.INVISIBLE);
            }
        });
        mIlopMainMenuAddDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Router.getInstance().toUrl(getContext(), "page/ilopadddevice", mBundle);
            }
        });
        mIlopMainMenuScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Router.getInstance().toUrl(getContext(), "page/scan");
            }
        });

        return view;
    }


    /**
     * 访问后台获取用户的设备列表，将设备列表保存到mHandler
     * 陈丽阳
     */
    private void listByAccount() {
        Map<String, Object> maps = new HashMap<>();
        IoTRequestBuilder builder = new IoTRequestBuilder()
                .setPath("/uc/listBindingByAccount")
                .setScheme(Scheme.HTTPS)
                .setApiVersion("1.0.2")
                .setAuthType("iotAuth")
                .setParams(maps);

        IoTRequest request = builder.build();

        IoTAPIClient ioTAPIClient = new IoTAPIClientFactory().getClient();
        ioTAPIClient.send(request, new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                ALog.d(TAG, "onFailure");
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse response) {
                final int code = response.getCode();
                final String localizeMsg = response.getLocalizedMsg();
                if (code != 200) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), localizeMsg, Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                Object data = response.getData();
                if (data == null) {
                    return;
                }
                if (!(data instanceof JSONObject)) {
                    return;
                }
                try {
                    JSONObject jsonObject = (JSONObject) data;
                    JSONArray jsonArray = jsonObject.getJSONArray("data");

                    List<DeviceInfoBean> deviceInfoBeanList = JSON.parseArray(jsonArray.toString(), DeviceInfoBean.class);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            initDevicePanel(deviceInfoBeanList);
                        }
                    });
                    if (deviceInfoBeanList == null) {
                        return;
                    }
                    //注册虚拟设备
                    Set<String> set = new HashSet<String>();
                    ArrayList<String> deviceStrList = new ArrayList<>();
                    for (DeviceInfoBean deviceInfoBean : deviceInfoBeanList) {
                        set.add(deviceInfoBean.getProductKey());
                        deviceStrList.add(deviceInfoBean.getProductKey() + deviceInfoBean.getDeviceName());
                    }
                    mBundle.putStringArrayList("deviceList", deviceStrList);

                     String[] pks = {"a1AzoSi5TMc", "a1B6cFQldpm", "a1XoFUJWkPr","a1nZ7Kq7AG1"};
                    mRegisterCount = 0;
                    mVirtualCount = 0;
                    for (String pk : pks) {
                        if (set.add(pk)) {
                            mRegisterCount++;
                            registerVirtualDevice(pk);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }

    private void bindVirturalToUser(String pk, String dn) {
        Map<String, Object> maps = new HashMap<>();
        maps.put("productKey", pk);
        maps.put("deviceName", dn);
        IoTRequestBuilder builder = new IoTRequestBuilder()
                .setPath("/thing/virtual/binduser")
                .setApiVersion("1.0.0")
                .setAuthType("iotAuth")
                .setParams(maps);

        IoTRequest request = builder.build();

        IoTAPIClient ioTAPIClient = new IoTAPIClientFactory().getClient();
        ioTAPIClient.send(request, new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                ALog.d(TAG, "onFailure");
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse response) {
                final int code = response.getCode();
                final String localizeMsg = response.getLocalizedMsg();
                mVirtualCount++;
                if (mRegisterCount == mVirtualCount) {
                    listByAccount();
                }
                if (code != 200) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), localizeMsg, Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
            }
        });
    }

    /**
     * 注册虚拟设备
     *
     * @param pk
     */
    private void registerVirtualDevice(String pk) {
        Map<String, Object> maps = new HashMap<>();
        maps.put("productKey", pk);
        IoTRequestBuilder builder = new IoTRequestBuilder()
                .setPath("/thing/virtual/register")
                .setApiVersion("1.0.0")
                .setAuthType("iotAuth")
                .setParams(maps);

        IoTRequest request = builder.build();

        IoTAPIClient ioTAPIClient = new IoTAPIClientFactory().getClient();
        ioTAPIClient.send(request, new IoTCallback() {
            @Override
            public void onFailure(IoTRequest ioTRequest, Exception e) {
                ALog.d(TAG, "onFailure");
            }

            @Override
            public void onResponse(IoTRequest ioTRequest, IoTResponse response) {
                ALog.d(TAG, "onResponse registerVirtualDevice");
                final int code = response.getCode();
                final String localizeMsg = response.getLocalizedMsg();
                if (code != 200) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), localizeMsg, Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                Object data = response.getData();
                if (null != data) {
                    if (data instanceof JSONObject) {
                        try {
                            String dn = ((JSONObject) data).getString("deviceName");
                            String pk = ((JSONObject) data).getString("productKey");
                            bindVirturalToUser(pk, dn);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        });
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    /**
     * 根据访问后台得到的设备列表，初始化设备列表界面
     * 陈丽阳
     * @param deviceInfoBeanList
     */
    private void initDevicePanel(List<DeviceInfoBean> deviceInfoBeanList) {
        if (mMyDevicePanel.getChildCount() > 1) {
            mMyDevicePanel.removeViews(1, mMyDevicePanel.getChildCount() - 1);
        }
        if (mVDevicePanel.getChildCount() > 1) {
            mVDevicePanel.removeViews(1, mVDevicePanel.getChildCount() - 1);
        }

        if (deviceInfoBeanList == null) {
            return;
        }

        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int width = dm.widthPixels;

        int index_my = 0;
        int index_v = 0;
        for (int i = 0; i < deviceInfoBeanList.size(); i++) {
            DeviceInfoBean device = deviceInfoBeanList.get(i);
            DevicePanelView devicePanelView = new DevicePanelView(getContext());

            devicePanelView.setDeviceInfo(device);
            final String title = device.getProductName();
            final String code = device.getProductKey();
            final String iotId = device.getIotId();

            devicePanelView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ALog.d(TAG, "devicePanelView onClick" + code);
                    if (code.equals("a1AzoSi5TMc")) {
                        Intent intent = new Intent(getActivity(), LampsActivity.class);
                        Log.d("HomeTabFragment", iotId);
                        intent.putExtra("iotId", iotId);
                        intent.putExtra("title", title);
                        startActivity(intent);
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putString("iotId", iotId);
                        String url = "link://router/" + code;
                        Router.getInstance().toUrl(getActivity(), url, bundle);
                    }
                }
            });

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((width - 30) / 2,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            if ("VIRTUAL".equalsIgnoreCase(device.getThingType()) || "VIRTUAL_SHADOW".equalsIgnoreCase(device.getThingType())) {
                lp.setMargins(width / 2 * (index_v % 2) + dip2px(getContext(), 5),
                        dip2px(getContext(), 100) * (index_v / 2) + dip2px(getContext(), 26),
                        0, 0);
                index_v++;
                mVDevicePanel.addView(devicePanelView, lp);
            } else {
                lp.setMargins(width / 2 * (index_my % 2) + dip2px(getContext(), 5),
                        dip2px(getContext(), 100) * (index_my / 2) + dip2px(getContext(), 26),
                        0, 0);
                index_my++;
                mMyDevicePanel.addView(devicePanelView, lp);
                mMyDevicePanelAdd.setVisibility(View.GONE);
            }
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            Log.d(TAG, "onActivityResult");
            if (data.getStringExtra("productKey") != null) {
                Bundle bundle = new Bundle();
                bundle.putString("productKey", data.getStringExtra("productKey"));
                bundle.putString("deviceName", data.getStringExtra("deviceName"));
                bundle.putString("token", data.getStringExtra("token"));
                Intent intent = new Intent(getActivity(), HomeTabFragment.class);
                intent.putExtras(bundle);
                this.startActivity(intent);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mIlopMainMenu.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (LoginBusiness.isLogin()) {
            listByAccount();
        }
    }
}
