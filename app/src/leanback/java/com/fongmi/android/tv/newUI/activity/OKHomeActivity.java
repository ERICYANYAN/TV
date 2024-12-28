package com.fongmi.android.tv.newUI.activity;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

import com.android.cast.dlna.dmr.DLNARendererService;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.databinding.OkActivityHomeBinding;
import com.fongmi.android.tv.model.SiteViewModel;
import com.fongmi.android.tv.server.Server;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.newUI.view.OKHomeTabLayout;
import com.fongmi.android.tv.newUI.fragment.OKHomeVodFragment;
import com.fongmi.android.tv.newUI.fragment.OKTestFragment;
import com.fongmi.android.tv.utils.Tbs;

public class OKHomeActivity extends BaseActivity {

    private OkActivityHomeBinding mBinding;
    private OKHomeTabLayout mTabLayout;
    
    // 四个主要的 fragment
    private OKTestFragment mSearchFragment;
    private OKTestFragment mRecommendFragment;
    private OKTestFragment mMineFragment;
    private OKHomeVodFragment mAllFragment;
    private SiteViewModel mViewModel;
    private Result mResult;

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
            initFragments();
            initTabLayout();
        });
        mViewModel.homeContent();

    }

    private void initFragments() {
        mSearchFragment = OKTestFragment.newInstance("搜索");
        mRecommendFragment = OKTestFragment.newInstance("推荐");
        mMineFragment = OKTestFragment.newInstance("我的");
        mAllFragment = OKHomeVodFragment.newInstance(mResult.clear());
    }

    private void initTabLayout() {
        mTabLayout = mBinding.tabLayout;
        mTabLayout.setTabs("搜索", "推荐", "我的", "全部");
        mTabLayout.setOnTabSelectedListener(position -> {
            switch (position) {
                case 0:
                    showFragment(mSearchFragment);
                    break;
                case 1:
                    showFragment(mRecommendFragment);
                    break;
                case 2:
                    showFragment(mMineFragment);
                    break;
                case 3:
                    showFragment(mAllFragment);
                    break;
            }
        });
        mTabLayout.setCurrentTab(1); // 默认选中"推荐"
    }

    private void showFragment(androidx.fragment.app.Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSearchFragment = null;
        mRecommendFragment = null;
        mMineFragment = null;
        mAllFragment = null;
    }
}
