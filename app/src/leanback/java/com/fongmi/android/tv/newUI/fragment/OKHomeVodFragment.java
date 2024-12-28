package com.fongmi.android.tv.newUI.fragment;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.leanback.widget.OnChildViewHolderSelectedListener;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Class;
import com.fongmi.android.tv.bean.Filter;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.databinding.ActivityVodBinding;
import com.fongmi.android.tv.ui.fragment.VodFragment;
import com.fongmi.android.tv.ui.presenter.TypePresenter;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.catvod.utils.Prefers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OKHomeVodFragment extends Fragment implements TypePresenter.OnClickListener {

    private ActivityVodBinding mBinding;
    private ArrayObjectAdapter mAdapter;
    private PageAdapter mPageAdapter;
    private boolean coolDown;
    private View mOldView;
    private String mKey;
    private Result mResult;


    public static OKHomeVodFragment newInstance(Result result) {
        return newInstance(VodConfig.get().getHome().getKey(), result);
    }

    public static OKHomeVodFragment newInstance(String key, Result result) {
        OKHomeVodFragment fragment = new OKHomeVodFragment();
        Bundle args = new Bundle();
        args.putString("key", key);
        args.putParcelable("result", result);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mKey = getArguments().getString("key");
        mResult = getArguments().getParcelable("result");
        for (Map.Entry<String, List<Filter>> entry : mResult.getFilters().entrySet()) {
            Prefers.put("filter_" + mKey + "_" + entry.getKey(), App.gson().toJson(entry.getValue()));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = ActivityVodBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initEvent();
    }

    private void initView() {
        setRecyclerView();
        setTypes();
        setPager();
    }

    private void initEvent() {
        mBinding.pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mBinding.recycler.setSelectedPosition(position);
            }
        });
        mBinding.recycler.addOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
            @Override
            public void onChildViewHolderSelected(@NonNull RecyclerView parent, @Nullable RecyclerView.ViewHolder child, int position, int subposition) {
                onChildSelected(child);
            }
        });
    }

    private List<Filter> getFilter(String typeId) {
        return Filter.arrayFrom(Prefers.getString("filter_" + mKey + "_" + typeId));
    }

    private Site getSite() {
        return VodConfig.get().getSite(mKey);
    }

    private void setRecyclerView() {
        mBinding.recycler.setHorizontalSpacing(ResUtil.dp2px(16));
        mBinding.recycler.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mBinding.recycler.setAdapter(new ItemBridgeAdapter(mAdapter = new ArrayObjectAdapter(new TypePresenter(this))));
    }

    private List<Class> getTypes(Result result) {
        List<Class> items = new ArrayList<>();
        for (String cate : getSite().getCategories()) {
            for (Class item : result.getTypes()) {
                if (cate.equals(item.getTypeName())) items.add(item);
            }
        }
        return items;
    }

    private void setTypes() {
        mResult.setTypes(getTypes(mResult));
        for (Class item : mResult.getTypes()) {
            item.setFilters(getFilter(item.getTypeId()));
        }
        mAdapter.setItems(mResult.getTypes(), null);
    }

    private void setPager() {
        mBinding.pager.setAdapter(mPageAdapter = new PageAdapter(getChildFragmentManager()));
    }

    private void onChildSelected(@Nullable RecyclerView.ViewHolder child) {
        if (mOldView != null) mOldView.setActivated(false);
        if (child == null) return;
        mOldView = child.itemView;
        mOldView.setActivated(true);
        App.post(mRunnable, 100);
    }

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mBinding.pager.setCurrentItem(mBinding.recycler.getSelectedPosition());
        }
    };

    private void updateFilter(Class item) {
        if (item.getFilter() == null) return;
        getFragment().toggleFilter(item.toggleFilter());
        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
    }

    private VodFragment getFragment() {
        return (VodFragment) mPageAdapter.instantiateItem(mBinding.pager, mBinding.pager.getCurrentItem());
    }

    private void setCoolDown() {
        App.post(() -> coolDown = false, 2000);
        coolDown = true;
    }

    @Override
    public void onItemClick(Class item) {
        updateFilter(item);
    }

    @Override
    public boolean onItemLongClick(Class item) {
        return true;
    }

    @Override
    public void onRefresh(Class item) {
        getFragment().onRefresh();
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            updateFilter((Class) mAdapter.get(mBinding.pager.getCurrentItem()));
            return true;
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.isLongPress() && getFragment().goRoot()) {
                setCoolDown();
                return true;
            }
            onBackPressed();
            return true;
        }
        return false;
    }

    public void onBackPressed() {
        Class item = (Class) mAdapter.get(mBinding.pager.getCurrentItem());
        if (item.getFilter() != null && item.getFilter()) {
            updateFilter(item);
        } else if (getFragment().canBack()) {
            getFragment().goBack();
        }
    }

    class PageAdapter extends FragmentStatePagerAdapter {

        public PageAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Class type = (Class) mAdapter.get(position);
            return VodFragment.newInstance(mKey, type.getTypeId(), type.getStyle(), type.getExtend(false), "1".equals(type.getTypeFlag()));
        }

        @Override
        public int getCount() {
            return mAdapter.size();
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        }
    }
}
