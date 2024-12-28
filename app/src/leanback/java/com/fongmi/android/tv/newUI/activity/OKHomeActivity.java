package com.fongmi.android.tv.newUI.activity;

import android.content.Context;
import android.content.Intent;

import androidx.viewbinding.ViewBinding;

import com.android.cast.dlna.dmr.DLNARendererService;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.OkActivityHomeBinding;
import com.fongmi.android.tv.server.Server;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.newUI.view.OKHomeTabLayout;
import com.fongmi.android.tv.newUI.fragment.OKTestFragment;
import com.fongmi.android.tv.utils.Tbs;

public class OKHomeActivity extends BaseActivity {

    public OkActivityHomeBinding mBinding;
    private OKHomeTabLayout mTabLayout;
    private OKTestFragment mOKTestFragment;

    public static void start(Context context) {
        context.startActivity(new Intent(context, OKHomeActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = OkActivityHomeBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        DLNARendererService.Companion.start(this, R.drawable.ic_logo);
        Server.get().start();
        Tbs.init();
        
        // 初始化 TabLayout
        initTabLayout();

    }

    private void initTabLayout() {
        mTabLayout = mBinding.tabLayout;
        mTabLayout.setTabs("搜索", "推荐", "我的", "全部");
        mTabLayout.setOnTabSelectedListener(position -> {
            // 每次切换 tab 都重新加载 TestFragment
            mOKTestFragment = OKTestFragment.newInstance(mTabLayout.getCurrentTabName());
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, mOKTestFragment)
                    .commit();
        });
        mTabLayout.setCurrentTab(1); // 默认选中"推荐"
    }


}
