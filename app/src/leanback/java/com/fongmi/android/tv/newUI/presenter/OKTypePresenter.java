package com.fongmi.android.tv.newUI.presenter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.leanback.widget.Presenter;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Class;
import com.fongmi.android.tv.databinding.OkAdapterTypeBinding;
import com.fongmi.android.tv.utils.ResUtil;

public class OKTypePresenter extends Presenter {

    private final OnFilterClickListener mListener;

    public OKTypePresenter(OnFilterClickListener listener) {
        this.mListener = listener;
    }

    public interface OnFilterClickListener {

        void onFilterItemClick(Class item);

    }

    @Override
    public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(OkAdapterTypeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object object) {
        Class item = (Class) object;
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.binding.text.setText(item.getTypeName());
        holder.binding.text.setCompoundDrawablePadding(ResUtil.dp2px(4));
        holder.binding.text.setCompoundDrawablesWithIntrinsicBounds(0, 0, getIcon(item), 0);
        setOnClickListener(holder, view -> mListener.onFilterItemClick(item));
    }

    @Override
    public void onUnbindViewHolder(@NonNull Presenter.ViewHolder viewHolder) {

    }

    private int getIcon(Class item) {
        return item.getFilter() == null ? 0 : item.getFilter() ? R.drawable.ic_vod_filter_off : R.drawable.ic_vod_filter_on;
    }

    public static class ViewHolder extends Presenter.ViewHolder {

        private final OkAdapterTypeBinding binding;

        public ViewHolder(@NonNull OkAdapterTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}