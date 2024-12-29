package com.fongmi.android.tv.ui.presenter;

import static android.os.Build.VERSION_CODES.R;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.leanback.widget.Presenter;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.AdapterHeaderBinding;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.quickjs.bean.Res;

public class HeaderPresenter extends Presenter {

    @Override
    public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new HeaderPresenter.ViewHolder(AdapterHeaderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object object) {
        HeaderPresenter.ViewHolder holder = (HeaderPresenter.ViewHolder) viewHolder;
        String text = object instanceof String ? object.toString() : ResUtil.getString((int) object);
        holder.binding.text.setText(text);
        if (text == ResUtil.getString(com.fongmi.android.tv.R.string.home_recommend)){
            // 设置marigin top
            holder.binding.text.setPadding(0, ResUtil.dp2px(10), 0, 0);
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
    }

    public static class ViewHolder extends Presenter.ViewHolder {

        private final AdapterHeaderBinding binding;

        public ViewHolder(@NonNull AdapterHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}