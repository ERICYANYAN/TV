package com.fongmi.android.tv.newUI.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

import com.android.cast.dlna.dmr.DLNARendererService;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.LiveConfig;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.api.config.WallConfig;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.databinding.OkActivityHomeBinding;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.model.SiteViewModel;
import com.fongmi.android.tv.newUI.fragment.OKKeepFragment;
import com.fongmi.android.tv.newUI.fragment.OKRecommendFragment;
import com.fongmi.android.tv.server.Server;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.newUI.view.OKHomeTabLayout;
import com.fongmi.android.tv.newUI.fragment.OKHomeVodFragment;
import com.fongmi.android.tv.newUI.fragment.OKTestFragment;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.Tbs;
import com.github.catvod.crawler.SpiderDebug;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class OKHomeActivity extends BaseActivity {

    private static final String SAVED_CURRENT_TAB = "current_tab";

    private OkActivityHomeBinding mBinding;
    private OKHomeTabLayout mTabLayout;
    
    // 四个主要的 fragment
    private OKTestFragment mSearchFragment;
    private OKRecommendFragment mRecommendFragment;
    private OKKeepFragment mKeepFragment;
    private OKHomeVodFragment mAllFragment;
    private SiteViewModel mViewModel;
    private Result mResult;
    private int mCurrentTab = 1;
    private androidx.fragment.app.Fragment mCurrentFragment;

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
        setViewModel();

        WallConfig.get().init();
        LiveConfig.get().init().load();
        VodConfig.get().init().load(getCallback(), true);

    }

    private Callback getCallback() {
        return new Callback() {
            @Override
            public void success(String result) {
                Notify.show(result);
            }

            @Override
            public void success() {
                RefreshEvent.video();
            }

            @Override
            public void error(String msg) {
                mResult = Result.empty();
                Notify.show(msg);
            }
        };
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        super.onRefreshEvent(event);
        switch (event.getType()) {
            case VIDEO:
                mViewModel.homeContent();
                break;
        }
    }

    private void setViewModel() {
        mViewModel = new ViewModelProvider(this).get(SiteViewModel.class);
        mViewModel.result.observe(this, result -> {
            mResult = result;
            SpiderDebug.log("### mResult:");
            SpiderDebug.log("### "+mResult.toJson());
            initFragments();
            initTabLayout();
        });
    }

    private void initFragments() {
        mSearchFragment = OKTestFragment.newInstance("搜索");
        mRecommendFragment = OKRecommendFragment.newInstance(Result.fromJson(mResult.toJson()));
        mKeepFragment = OKKeepFragment.newInstance();
        mAllFragment = OKHomeVodFragment.newInstance(mResult.clear());
        
        // 初始化时添加所有Fragment
        androidx.fragment.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.container, mSearchFragment).hide(mSearchFragment);
        ft.add(R.id.container, mRecommendFragment).hide(mRecommendFragment);
        ft.add(R.id.container, mKeepFragment).hide(mKeepFragment);
        ft.add(R.id.container, mAllFragment).hide(mAllFragment);
        ft.commitAllowingStateLoss();
        
    }

    private void initTabLayout() {
        mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText(Html.fromHtml("<b>推荐</b>")));
        mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText(Html.fromHtml("<b>收藏</b>")));
        mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText(Html.fromHtml("<b>全部</b>")));

        mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mCurrentTab = tab.getPosition();
                androidx.fragment.app.Fragment targetFragment;
                switch (mCurrentTab) {
                    case 0:
                        targetFragment = mRecommendFragment;
                        break;
                    case 1:
                        targetFragment = mKeepFragment;
                        break;
                    case 2:
                        targetFragment = mAllFragment;
                        break;
                    default:
                        return;
                }
                showFragment(targetFragment);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // 设置默认选中推荐标签
        mBinding.tabLayout.getTabAt(1).view.requestFocus();
        mBinding.tabLayout.getTabAt(1).select();

    }

    private void showFragment(androidx.fragment.app.Fragment targetFragment) {
        if (mCurrentFragment == targetFragment) return;
        
        androidx.fragment.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mCurrentFragment != null) {
            ft.hide(mCurrentFragment);
        }
        ft.show(targetFragment);
        ft.commitAllowingStateLoss();
        mCurrentFragment = targetFragment;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSearchFragment = null;
        mRecommendFragment = null;
        mKeepFragment = null;
        mAllFragment = null;
        mCurrentFragment = null;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_CURRENT_TAB, mCurrentTab);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentTab = savedInstanceState.getInt(SAVED_CURRENT_TAB, 1);
    }
}
