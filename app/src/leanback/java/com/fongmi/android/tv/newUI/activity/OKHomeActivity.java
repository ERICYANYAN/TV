package com.fongmi.android.tv.newUI.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

import com.android.cast.dlna.dmr.DLNARendererService;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.databinding.OkActivityHomeBinding;
import com.fongmi.android.tv.model.SiteViewModel;
import com.fongmi.android.tv.newUI.fragment.OKMineFragment;
import com.fongmi.android.tv.server.Server;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.newUI.view.OKHomeTabLayout;
import com.fongmi.android.tv.newUI.fragment.OKHomeVodFragment;
import com.fongmi.android.tv.newUI.fragment.OKTestFragment;
import com.fongmi.android.tv.utils.Tbs;
import com.github.catvod.crawler.SpiderDebug;

public class OKHomeActivity extends BaseActivity {

    private static final String SAVED_CURRENT_TAB = "current_tab";

    private OkActivityHomeBinding mBinding;
    private OKHomeTabLayout mTabLayout;
    
    // 四个主要的 fragment
    private OKTestFragment mSearchFragment;
    private OKTestFragment mRecommendFragment;
    private OKMineFragment mMineFragment;
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
        mViewModel.homeContent();

    }

    private void initFragments() {
        mSearchFragment = OKTestFragment.newInstance("搜索");
        mRecommendFragment = OKTestFragment.newInstance("推荐");
        mMineFragment = OKMineFragment.newInstance(mResult);
        mAllFragment = OKHomeVodFragment.newInstance(mResult.clear());
        
        // 初始化时添加所有Fragment
        androidx.fragment.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.container, mSearchFragment).hide(mSearchFragment);
        ft.add(R.id.container, mRecommendFragment).hide(mRecommendFragment);
        ft.add(R.id.container, mMineFragment).hide(mMineFragment);
        ft.add(R.id.container, mAllFragment).hide(mAllFragment);
        ft.commitAllowingStateLoss();
        
        // 设置初始的当前Fragment
        mCurrentFragment = mRecommendFragment;
    }

    private void initTabLayout() {
        mTabLayout = mBinding.tabLayout;
        mTabLayout.setTabs("搜索", "推荐", "我的", "全部");
        mTabLayout.setOnTabSelectedListener(position -> {
            mCurrentTab = position;
            androidx.fragment.app.Fragment targetFragment;
            switch (position) {
                case 0:
                    targetFragment = mSearchFragment;
                    break;
                case 1:
                    targetFragment = mRecommendFragment;
                    break;
                case 2:
                    targetFragment = mMineFragment;
                    break;
                case 3:
                    targetFragment = mAllFragment;
                    break;
                default:
                    return;
            }
            showFragment(targetFragment);
        });
        mTabLayout.setCurrentTab(mCurrentTab);
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
        mMineFragment = null;
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
