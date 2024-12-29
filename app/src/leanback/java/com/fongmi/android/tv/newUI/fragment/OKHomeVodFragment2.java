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
import com.fongmi.android.tv.databinding.OkHomeVodFragmentBinding;
import com.fongmi.android.tv.newUI.presenter.OKTypePresenter;
import com.fongmi.android.tv.ui.fragment.VodFragment;
import com.fongmi.android.tv.ui.presenter.TypePresenter;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.Prefers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OKHomeVodFragment2 extends Fragment implements OKTypePresenter.OnClickListener {

    private OkHomeVodFragmentBinding mBinding;
    private ArrayObjectAdapter mAdapter;
    private View mOldView;
    private String mKey;
    private Result mResult;


    public static OKHomeVodFragment2 newInstance(Result result) {
        return newInstance(VodConfig.get().getHome().getKey(), result);
    }

    public static OKHomeVodFragment2 newInstance(String key, Result result) {
        OKHomeVodFragment2 fragment = new OKHomeVodFragment2();
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
        log("mkey" + mKey);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = OkHomeVodFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.filterContainer.setHorizontalSpacing(ResUtil.dp2px(16));
        mBinding.filterContainer.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        OKTypePresenter typePresenter = new OKTypePresenter(this);
        mAdapter = new ArrayObjectAdapter(typePresenter);
        mBinding.filterContainer.setAdapter(new ItemBridgeAdapter(mAdapter));

//      只获取 site 中有的类型
        mResult.setTypes(getTypes(mResult));
        for (Class item : mResult.getTypes()) {
            item.setFilters(getFilter(item.getTypeId()));
        }
        mAdapter.setItems(mResult.getTypes(), null);

        mBinding.filterContainer.addOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
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


    private List<Class> getTypes(Result result) {
        List<Class> items = new ArrayList<>();
        for (String cate : getSite().getCategories()) {
            for (Class item : result.getTypes()) {
                if (cate.equals(item.getTypeName())) items.add(item);
            }
        }
        return items;
    }


    private void onChildSelected(@Nullable RecyclerView.ViewHolder child) {
        if (mOldView != null) mOldView.setActivated(false);
        if (child == null) return;
        mOldView = child.itemView;
        mOldView.setActivated(true);
    }


    @Override
    public void onItemClick(Class item) {
        log("onItemClick " + item.getTypeName());
    }

    @Override
    public boolean onItemLongClick(Class item) {
        return true;
    }

    @Override
    public void onRefresh(Class item) {
        log("onRefresh " + item.getTypeName());
    }

    private void log(String msg) {
        SpiderDebug.log("### 2 :" + msg);
    }


}
