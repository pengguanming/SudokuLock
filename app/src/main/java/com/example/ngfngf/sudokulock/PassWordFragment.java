package com.example.ngfngf.sudokulock;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ngfngf.sudokulock.view.LockPatternView;


public class PassWordFragment extends Fragment implements LockPatternView.OnPatterChangeListener {
    public static final String TYPE_SETTING = "Setting";
    public static final String TYPE_CHECK = "check";
    private static final String ARG_TYPE = "type";

    private TextView mTvTip;
    private LockPatternView mLockPatternView;
    private LinearLayout mLayout;
    private Button mBtnSubmit;
    private String passWord;

    public static PassWordFragment newInstance(String typeStr) {
        PassWordFragment fragment = new PassWordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, typeStr);
        fragment.setArguments(args);
        return fragment;
    }

    public PassWordFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_pass_word, container, false);
        mLockPatternView = (LockPatternView) contentView.findViewById(R.id.fg_lockpatternView);
        mTvTip = (TextView) contentView.findViewById(R.id.fg_tv_tip);
        mLayout = (LinearLayout) contentView.findViewById(R.id.fg__btn_layout);
        mBtnSubmit = (Button) contentView.findViewById(R.id.fg_btn_submit);
        //设置密码
        if (getArguments() != null) {
            if (TYPE_SETTING.equals(getArguments().getString(ARG_TYPE))) {
                mLayout.setVisibility(View.VISIBLE);
            }
        }
        mBtnSubmit = (Button) contentView.findViewById(R.id.fg_btn_submit);
        mBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getActivity().getSharedPreferences("sp", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sp.edit();
                edit.putString("passWord", passWord);
                edit.commit();
                getActivity().finish();
                Log.d("JJY", "onClick: " + passWord);
            }
        });
        mLockPatternView.setPatterChangeListener(this);
        return contentView;
    }

    @Override
    public void onPatterChange(String passWord) {
        this.passWord=passWord;
        if (!TextUtils.isEmpty(passWord)) {
            mTvTip.setText(passWord);
            //密码检查
            if (getArguments() != null) {
                if (TYPE_CHECK.equals(getArguments().getString(ARG_TYPE))) {
                    SharedPreferences sp = getActivity().getSharedPreferences("sp", Context.MODE_PRIVATE);
                    //密码正确
                    if (passWord.equals(sp.getString("passWord", ""))) {
                        getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
                        getActivity().finish();
                    } else {//密码错误
                        mTvTip.setText("密码错误");
                        //重置图案
                        mLockPatternView.resetPoint();
                    }
                }
            }
        } else {
            mTvTip.setText("至少5个图案以上");
        }
    }

    @Override
    public void onPatterStart(Boolean isStart) {
        mTvTip.setText("请绘制图案");
    }
}
