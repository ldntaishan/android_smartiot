package com.aliyun.iot.ilop.demo.page.device.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.aliyun.iot.aep.component.router.Router;
import com.aliyun.iot.aep.sdk.framework.AActivity;
import com.aliyun.iot.aep.sdk.log.ALog;
import com.aliyun.iot.demo.R;
import com.aliyun.iot.ilop.demo.page.device.bean.Device;
import com.aliyun.iot.ilop.demo.page.device.bind.DeviceBindBusiness;
import com.aliyun.iot.ilop.demo.page.device.bind.OnBindDeviceCompletedListener;


public class BindAndUseActivity extends AActivity {
    private String TAG = BindAndUseActivity.class.getSimpleName();
    private Button bindAndUseBtn;
    private Button mBackBtn;
    private Handler mHandler = new Handler();
    private DeviceBindBusiness deviceBindBusiness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.bind_and_use_activity);

        deviceBindBusiness = new DeviceBindBusiness();
        mBackBtn = (Button) findViewById(R.id.ilop_bind_back_btn);
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        String pk = "";
        String dn = "";
//        String token = "";
        Bundle data = getIntent().getExtras();

        if (null != data) {
            pk = data.getString("productKey");
            dn = data.getString("deviceName");
//            token = data.getString("token");
        }


        final Device device = new Device();
        device.pk = pk;
        device.dn = dn;

        //查询产品信息
        Log.e(TAG, "onCreate: "+pk+"   "+dn );
        deviceBindBusiness.queryProductInfo(device);


//        final String iotToken = token;
        bindAndUseBtn = (Button) findViewById(R.id.bind_and_use_btn);
        bindAndUseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceBindBusiness.bindDevice(device,new OnBindDeviceCompletedListener() {
                    @Override
                    public void onSuccess(String iotId) {
                        Router.getInstance().toUrl(BindAndUseActivity.this, "page/ilopmain");
                        finish();
                    }

                    @Override
                    public void onFailed(Exception e) {
                        ALog.e("TAG", "bindDevice onFail s = " +e);
                        Toast.makeText(getApplicationContext(), "bindDeviceFailed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(int code, String message, String localizedMsg) {
                        ALog.d("TAG", "onFailure");
                        Toast.makeText(getApplicationContext(), "code = " + code + " msg =" + message, Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });
    }


}
