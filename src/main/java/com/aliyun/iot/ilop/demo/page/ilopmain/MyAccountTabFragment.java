package com.aliyun.iot.ilop.demo.page.ilopmain;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.iot.aep.component.router.Router;
import com.aliyun.iot.ilop.demo.dialog.ASlideDialog;
import com.aliyun.iot.ilop.demo.page.main.StartActivity;
import com.aliyun.iot.aep.sdk.login.ILoginCallback;
import com.aliyun.iot.aep.sdk.login.ILogoutCallback;
import com.aliyun.iot.aep.sdk.login.LoginBusiness;
import com.aliyun.iot.aep.sdk.login.data.UserInfo;
import com.aliyun.iot.demo.R;

public class MyAccountTabFragment extends Fragment {
    private View myInfoView;

    private View myAboutView;

    private View myOTAView;
    private View mMessgae;

    private TextView myUserNameTV;

    // account
    ASlideDialog menuDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.myaccounttab_fragment_layout, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myInfoView = view.findViewById(R.id.my_userinfo);
        myAboutView = view.findViewById(R.id.my_about);
        myUserNameTV = view.findViewById(R.id.my_username_textview);
        myOTAView = view.findViewById(R.id.my_ota);
        mMessgae = view.findViewById(R.id.my_message);

        myAboutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.getInstance().toUrl(getActivity(), "page/about");
            }
        });

        myOTAView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.getInstance().toUrl(getActivity(), "page/ota/list");
            }
        });

        mMessgae.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //调用消息插件

                String code = "link://router/devicenotices";
                Bundle bundle = new Bundle();
                Router.getInstance().toUrlForResult(getActivity(), code, 1, bundle);
            }
        });


        myInfoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!LoginBusiness.isLogin()) {
                    LoginBusiness.login(new ILoginCallback() {
                        @Override
                        public void onLoginSuccess() {
                            myUserNameTV.setText(getUserNick());
                        }

                        @Override
                        public void onLoginFailed(int i, String s) {
                            Toast.makeText(getActivity(), getContext().getString(R.string.account_login_failed), Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                accountShowMenuDialog();
            }
        });
       String userName= getUserNick();
        myUserNameTV.setText(userName);
    }


    private String getUserNick() {
        //设置当前登录用户名
        if (LoginBusiness.isLogin()) {
            UserInfo userInfo = LoginBusiness.getUserInfo();
            String userName = "";
            if (userInfo != null) {

                if (!TextUtils.isEmpty(userInfo.userNick)) {
                    userName = userInfo.userNick;
                } else if (!TextUtils.isEmpty(userInfo.userPhone)) {
                    userName = userInfo.userPhone;
                } else if (!TextUtils.isEmpty(userInfo.userEmail)) {
                    userName = userInfo.userEmail;
                }else{
                    userName = "未获取到用户名";
                }
            }
            return userName;
        }
        return null;
    }

    private void accountShowMenuDialog() {
        if (menuDialog == null) {
            menuDialog = ASlideDialog.newInstance(getContext(), ASlideDialog.Gravity.Bottom, R.layout.menu_dialog);
            menuDialog.findViewById(R.id.menu_logout_textview).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LoginBusiness.logout(new ILogoutCallback() {
                        @Override
                        public void onLogoutSuccess() {
                            Toast.makeText(getActivity(), getContext().getString(R.string.account_logout_success), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity().getApplicationContext(), StartActivity.class);
                            startActivity(intent);
                            if (menuDialog != null) {
                                menuDialog.dismiss();
                            }
                            getActivity().finish();
                        }

                        @Override
                        public void onLogoutFailed(int code, String error) {
                            Toast.makeText(getActivity(), getContext().getString(R.string.account_logout_failed) + error, Toast.LENGTH_SHORT);
                        }
                    });
                    accountHideMenuDialog();
                }
            });
            menuDialog.findViewById(R.id.menu_cancel_textview).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    accountHideMenuDialog();
                }
            });
            menuDialog.setCanceledOnTouchOutside(true);
        }
        //设置当前登录用户名
        ((TextView) menuDialog.findViewById(R.id.menu_name_textview)).setText(getUserNick());
        menuDialog.show();
    }

    private void accountHideMenuDialog() {
        if (menuDialog != null) {
            menuDialog.hide();
        }
    }
}
