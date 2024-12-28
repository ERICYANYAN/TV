package com.fongmi.android.tv.newUI.fragment;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ItemBridgeAdapter;
import androidx.leanback.widget.ListRow;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.Product;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.History;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.bean.Style;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.FragmentHomeBinding;
import com.fongmi.android.tv.ui.activity.CollectActivity;
import com.fongmi.android.tv.ui.activity.VideoActivity;
import com.fongmi.android.tv.ui.base.BaseFragment;
import com.fongmi.android.tv.ui.custom.CustomRowPresenter;
import com.fongmi.android.tv.ui.custom.CustomSelector;
import com.fongmi.android.tv.ui.presenter.HeaderPresenter;
import com.fongmi.android.tv.ui.presenter.HistoryPresenter;
import com.fongmi.android.tv.ui.presenter.ProgressPresenter;
import com.fongmi.android.tv.ui.presenter.VodPresenter;
import com.fongmi.android.tv.utils.ResUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.collect.Lists;

import java.util.List;


// 我的页面Fragment，包含历史记录和推荐内容
public class OKRecommendFragment extends BaseFragment implements VodPresenter.OnClickListener, HistoryPresenter.OnClickListener {

    // 布局绑定
    public FragmentHomeBinding mBinding;

    // 历史记录相关适配器和Presenter
    private ArrayObjectAdapter mHistoryAdapter;
    public HistoryPresenter mHistoryPresenter;
    private ArrayObjectAdapter mAdapter;
    
    // 存储API返回的结果数据
    public Result mResult;

    // 创建Fragment实例，传入API结果数据
    public static OKRecommendFragment newInstance(Result mResult) {
        OKRecommendFragment fragment =  new OKRecommendFragment();
        fragment.mResult = mResult;
        return  fragment;
    }

    // 获取当前首页站点配置
    private Site getHome() {
        return VodConfig.get().getHome();
    }

    // 初始化视图
    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentHomeBinding.inflate(inflater, container, false);
    }

    // 初始化视图
    @Override
    protected void initView() {
        mBinding.progressLayout.showProgress();
        setRecyclerView();    // 设置RecyclerView
        setAdapter();         // 设置适配器
        addVideo(mResult);    // 添加视频数据
        mBinding.progressLayout.showContent();
    }

    // 初始化数据
    @Override
    protected void initData() {
        getHistory(false);
    }

    // 设置RecyclerView，配置各种Presenter
    private void setRecyclerView() {
        CustomSelector selector = new CustomSelector();
        selector.addPresenter(Integer.class, new HeaderPresenter());
        selector.addPresenter(String.class, new ProgressPresenter());
        selector.addPresenter(ListRow.class, new CustomRowPresenter(16), VodPresenter.class);
        selector.addPresenter(ListRow.class, new CustomRowPresenter(16), HistoryPresenter.class);
        mBinding.recycler.setAdapter(new ItemBridgeAdapter(mAdapter = new ArrayObjectAdapter(selector)));
        mBinding.recycler.setVerticalSpacing(ResUtil.dp2px(16));
    }

    // 设置适配器，添加历史记录行和推荐行
    private void setAdapter() {
        if (Setting.isHomeHistory()) mAdapter.add(R.string.home_history);
        mAdapter.add(R.string.home_recommend);
        mHistoryAdapter = new ArrayObjectAdapter(mHistoryPresenter = new HistoryPresenter(this));
    }

    // 添加视频内容
    private void addVideo(Result result) {
        int index = getRecommendIndex();
        if (mAdapter.size() > index) mAdapter.removeItems(index, mAdapter.size() - index);
        Style style = result.getStyle(getHome().getStyle());
        for (List<Vod> items : Lists.partition(result.getList(), Product.getColumn(style))) {
            ArrayObjectAdapter adapter = new ArrayObjectAdapter(new VodPresenter(this, style));
            adapter.setItems(items, null);
            mAdapter.add(new ListRow(adapter));
        }
    }


    // 获取并显示历史记录
    public void getHistory(boolean renew) {
        int historyIndex = getHistoryIndex();
        int recommendIndex = getRecommendIndex();
        if (historyIndex == -1) {
            if (!Setting.isHomeHistory()) return;
            int historyStringIndex = recommendIndex - 1;
            historyStringIndex = historyStringIndex < 0 ? 0 : historyStringIndex;
            mAdapter.add(historyStringIndex, R.string.home_history);
        }
        if (!Setting.isHomeHistory()) {
            mAdapter.removeItems(historyIndex - 1, 2);
            return;
        }
        historyIndex = getHistoryIndex();
        recommendIndex = getRecommendIndex();
        List<History> items = History.get();
        boolean exist = recommendIndex - historyIndex == 2;
        if (renew) mHistoryAdapter = new ArrayObjectAdapter(mHistoryPresenter = new HistoryPresenter(this));
        if ((items.isEmpty() && exist) || (renew && exist)) mAdapter.removeItems(historyIndex, 1);
        if ((items.size() > 0 && !exist) || (renew && exist)) mAdapter.add(historyIndex, new ListRow(mHistoryAdapter));
        mHistoryAdapter.setItems(items, null);
    }

    // 设置历史记录删除模式
    public void setHistoryDelete(boolean delete) {
        mHistoryPresenter.setDelete(delete);
        mHistoryAdapter.notifyArrayItemRangeChanged(0, mHistoryAdapter.size());
    }

    // 清空历史记录
    private void clearHistory() {
        mAdapter.removeItems(getHistoryIndex(), 1);
        History.delete(VodConfig.getCid());
        mHistoryPresenter.setDelete(false);
        mHistoryAdapter.clear();
    }

    // 获取各个部分在适配器中的位置索引
    private int getHistoryIndex() {
        for (int i = 0; i < mAdapter.size(); i++) if (mAdapter.get(i).equals(R.string.home_history)) return i + 1;
        return -1;
    }

    private int getRecommendIndex() {
        for (int i = 0; i < mAdapter.size(); i++) if (mAdapter.get(i).equals(R.string.home_recommend)) return i + 1;
        return -1;
    }

    // 历史记录项点击事件处理
    @Override
    public void onItemClick(History item) {
        VideoActivity.start(getActivity(), item.getSiteKey(), item.getVodId(), item.getVodName(), item.getVodPic());
    }

    // 历史记录删除事件处理
    @Override
    public void onItemDelete(History item) {
        mHistoryAdapter.remove(item.delete());
        if (mHistoryAdapter.size() > 0) return;
        mAdapter.removeItems(getHistoryIndex(), 1);
        mHistoryPresenter.setDelete(false);
    }

    // 历史记录长按事件处理
    @Override
    public boolean onLongClick() {
        if (mHistoryPresenter.isDelete()) {
            new MaterialAlertDialogBuilder(getActivity()).setTitle(R.string.dialog_delete_record).setMessage(R.string.dialog_delete_history).setNegativeButton(R.string.dialog_negative, null).setPositiveButton(R.string.dialog_positive, (dialog, which) -> clearHistory()).show();
        } else {
            setHistoryDelete(true);
        }
        return true;
    }

    // 视频项点击事件处理
    @Override
    public void onItemClick(Vod item) {
        if (getHome().isIndexs()) CollectActivity.start(getActivity(), item.getVodName());
        else VideoActivity.start(getActivity(), item.getVodId(), item.getVodName(), item.getVodPic());
    }

    // 视频项长按事件处理
    @Override
    public boolean onLongClick(Vod item) {
        CollectActivity.start(getActivity(), item.getVodName());
        return true;
    }
}
