package com.aliyun.iot.ilop.demo.page.ilopmain;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.iot.aep.component.scan.ScanManager;
import com.aliyun.iot.ilop.demo.page.device.activity.BindAndUseActivity;
import com.aliyun.iot.ilop.demo.page.main.StartActivity;
import com.aliyun.iot.ilop.demo.page.device.scan.AddDeviceScanPlugin;
import com.aliyun.iot.ilop.demo.utils.FloatWindowHelper;
import com.aliyun.iot.aep.sdk.login.LoginBusiness;
import com.aliyun.iot.demo.R;

import java.util.Arrays;

public class MainActivity extends FragmentActivity {
    private String TAG = HomeTabFragment.class.getSimpleName();

    private MyFragmentTabLayout fragmentTabHost;

    private Class fragmentClass[] = {HomeTabFragment.class, DebugTabFragment.class, MyAccountTabFragment.class};
    private String textViewArray[] = {"首页", "调试", "我的"};
    private Integer drawables[] = {R.drawable.tab_home_btn, R.drawable.tab_view_btn, R.drawable.tab_my_btn};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FloatWindowHelper helper = FloatWindowHelper.getInstance(getApplication());
        if (helper != null) {
            helper.setNeedShowFloatWindowFlag(true);
        }
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        fragmentTabHost = (MyFragmentTabLayout) findViewById(R.id.tab_layout);
        fragmentTabHost.init(getSupportFragmentManager())
                .setFragmentTabLayoutAdapter(new DefaultFragmentTabAdapter(Arrays.asList(fragmentClass), Arrays.asList(textViewArray), Arrays.asList(drawables)) {
                    @Override
                    public View createView(int pos) {
                        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.tab_item, null);
                        ImageView imageView = (ImageView) view.findViewById(R.id.img);
                        imageView.setImageResource(drawables[pos]);
                        TextView textView = (TextView) view.findViewById(R.id.tab_text);
                        textView.setText(textViewArray[pos]);
                        return view;
                    }

                    @Override
                    public void onClick(int pos) {
                    }
                }).creat();

        //扫码添加设备 注册
        ScanManager.getInstance().registerPlugin(AddDeviceScanPlugin.NAME, new AddDeviceScanPlugin(this));
    }

    @Override
    protected void onDestroy() {
        //需要取消注册，否则会造成内存泄露
        ScanManager.getInstance().unRegisterPlugin(AddDeviceScanPlugin.NAME);
        //退出首页不显示浮窗
        FloatWindowHelper helper = FloatWindowHelper.getInstance(getApplication());
        if (helper != null) {
            helper.setNeedShowFloatWindowFlag(false);
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!LoginBusiness.isLogin()) {
            Intent intent = new Intent(getApplicationContext(), StartActivity.class);
            startActivity(intent);
            finish();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            Log.d(TAG, "onActivityResult");
            if (data != null && data.getStringExtra("productKey") != null) {
                Bundle bundle = new Bundle();
                bundle.putString("productKey", data.getStringExtra("productKey"));
                bundle.putString("deviceName", data.getStringExtra("deviceName"));
                bundle.putString("token", data.getStringExtra("token"));
                Intent intent = new Intent(this, BindAndUseActivity.class);
                intent.putExtras(bundle);
                this.startActivity(intent);
            }
        }
    }
}
