package com.aliyun.iot.ilop.demo.page.main;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.Toast;

import com.aliyun.iot.aep.component.router.Router;
import com.aliyun.iot.aep.sdk.framework.AActivity;
import com.aliyun.iot.aep.sdk.login.ILoginCallback;
import com.aliyun.iot.aep.sdk.login.LoginBusiness;
import com.aliyun.iot.demo.R;


/**
 * Created by feijie.xfj on 17/11/27.
 */
//launcher页面不可以singleTask`
public class StartActivity extends AActivity {
    private static final String TAG = "StartActivity";

    private CountDownTimer countDownTimer;
    private Handler mH = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
        countDownTimer = new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (LoginBusiness.isLogin()) {
                    Router.getInstance().toUrl(StartActivity.this, "page/ilopmain");
                    finish();
                } else {
                    LoginBusiness.login(new ILoginCallback() {
                        @Override
                        public void onLoginSuccess() {
                            mH.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Router.getInstance().toUrl(StartActivity.this, "page/ilopmain");
                                }
                            }, 0);

                        }


                        @Override
                        public void onLoginFailed(int i, String s) {
                            Toast.makeText(getApplicationContext(), "登录失败 :" + s, Toast.LENGTH_SHORT).show();
                        }
                    });
                    finish();
                }

            }
        };
        countDownTimer.start();

    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = null;
        super.onDestroy();
    }
}
