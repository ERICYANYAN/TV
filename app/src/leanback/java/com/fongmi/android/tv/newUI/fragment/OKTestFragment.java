package com.fongmi.android.tv.newUI.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.databinding.OkFragmentTestBinding;
import com.fongmi.android.tv.ui.base.BaseFragment;
import com.fongmi.android.tv.utils.Notify;

public class OKTestFragment extends BaseFragment {

    private OkFragmentTestBinding mBinding;

    
    private String message;

    public static OKTestFragment newInstance(String message) {
        OKTestFragment fragment = new OKTestFragment();
        fragment.message = message;
        return fragment;
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = OkFragmentTestBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {

        Notify.show(message);

        // 设置按钮焦点属性
        mBinding.btn1.setFocusable(true);
        mBinding.btn2.setFocusable(true);
        mBinding.btn3.setFocusable(true);
        mBinding.btn4.setFocusable(true);
        mBinding.btn5.setFocusable(true);
        initEvent();

        // 设置按钮焦点顺序
        mBinding.btn1.setNextFocusDownId(mBinding.btn2.getId());
        mBinding.btn2.setNextFocusDownId(mBinding.btn3.getId());
        mBinding.btn3.setNextFocusDownId(mBinding.btn4.getId());
        mBinding.btn4.setNextFocusDownId(mBinding.btn5.getId());
        mBinding.btn5.setNextFocusUpId(mBinding.btn4.getId());
        mBinding.btn4.setNextFocusUpId(mBinding.btn3.getId());
        mBinding.btn3.setNextFocusUpId(mBinding.btn2.getId());
        mBinding.btn2.setNextFocusUpId(mBinding.btn1.getId());

    }

    protected void initEvent() {
        // 设置按钮点击事件
        mBinding.btn1.setOnClickListener(v -> onClick(1));
        mBinding.btn2.setOnClickListener(v -> onClick(2));
        mBinding.btn3.setOnClickListener(v -> onClick(3));
        mBinding.btn4.setOnClickListener(v -> onClick(4));
        mBinding.btn5.setOnClickListener(v -> onClick(5));

        // 设置焦点监听
        setFocusListener(mBinding.btn1);
        setFocusListener(mBinding.btn2);
        setFocusListener(mBinding.btn3);
        setFocusListener(mBinding.btn4);
        setFocusListener(mBinding.btn5);
    }

    private void setFocusListener(View view) {
        view.setOnFocusChangeListener((v, hasFocus) -> {
            float scale = hasFocus ? 1.1f : 1.0f;
            v.animate().scaleX(scale).scaleY(scale).setDuration(200).start();
            v.setSelected(hasFocus);
        });
    }

    private void onClick(int position) {
        Notify.show(""+position);

    }
} 