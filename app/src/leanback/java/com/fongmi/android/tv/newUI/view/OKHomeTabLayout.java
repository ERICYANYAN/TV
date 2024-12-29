package com.fongmi.android.tv.newUI.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.catvod.crawler.SpiderDebug;

import java.util.ArrayList;

public class OKHomeTabLayout extends HorizontalScrollView {

    private final LinearLayout container;
    private final Paint indicatorPaint;
    private OnTabSelectedListener listener;
    private int selectedPosition = -1;

    public OKHomeTabLayout(@NonNull Context context) {
        this(context, null);
    }

    public OKHomeTabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setHorizontalScrollBarEnabled(false);
        setFillViewport(true);
        
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        addView(container);

        indicatorPaint = new Paint();
        indicatorPaint.setAntiAlias(true);
        indicatorPaint.setColor(ResUtil.getColor(R.color.white));

        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
    }

    public void setTabs(int index, String... titles) {

        container.removeAllViews();
        for (int i = 0; i < titles.length; i++) {
            TextView tab = createTab(titles[i],i==index);
            tab.setTag(i);
            container.addView(tab);
        }
        
        // 添加post延迟，确保视图已经完成布局
        post(() -> {
            View selectedTab = container.getChildAt(index);
            if (selectedTab != null) {
                // 手动触发选中效果
                selectTab(selectedTab);
                // 请求焦点
                selectedTab.requestFocus();
                // 滚动到可见位置
                int scrollX = (selectedTab.getLeft() - (getWidth() - selectedTab.getWidth()) / 2);
                smoothScrollTo(scrollX, 0);
            }
        });
    }

    @SuppressLint("NewApi")
    private TextView createTab(String title, boolean isFocus) {
        TextView tab = new TextView(getContext());
        tab.setText(title);
        tab.setTextSize(16);
        tab.setPadding(ResUtil.dp2px(16), ResUtil.dp2px(8), ResUtil.dp2px(16), ResUtil.dp2px(8));
        tab.setFocusable(true);
        tab.setFocusedByDefault(isFocus);
        tab.setBackgroundResource(R.drawable.ok_home_tab_item_bg);
        tab.setTextColor(ResUtil.getColor(R.color.grey_700));
        tab.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        
        // 修改焦点变化监听器，只处理高亮效果
        tab.setOnFocusChangeListener((v, hasFocus) -> {
            TextView textView = (TextView) v;
            if (hasFocus) {
                textView.setTextColor(ResUtil.getColor(R.color.black));
                if ((int)v.getTag() != selectedPosition) {
                    selectTab(v);
                }
            } else {
                textView.setTextColor(ResUtil.getColor(R.color.grey_700));
            }
        });
        return tab;
    }

    private void selectTab(View view) {
        int position = (int) view.getTag();
        if (position == selectedPosition) return;
        
        // 更新选中状态
        for (int i = 0; i < container.getChildCount(); i++) {
            TextView tab = (TextView) container.getChildAt(i);
            // 如果当前tab没有焦点，则设置对应的颜色
            if (!tab.hasFocus()) {
                tab.setTextColor(i == position ? ResUtil.getColor(R.color.white) : ResUtil.getColor(R.color.grey_700));
            }
        }

        // 更新指示器位置
        view.post(() -> {
            indicatorLeft = view.getLeft();
            indicatorRight = view.getRight();
            invalidate();
        });

        selectedPosition = position;
        if (listener != null) listener.onTabSelected(position);
    }

    private View getSelectedView(){
       return (TextView) container.getChildAt(selectedPosition);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            View focused = findFocus();
            if (focused != null) {
                int position = (int) focused.getTag();
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT && position > 0) {
                    container.getChildAt(position - 1).requestFocus();
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT && position < container.getChildCount() - 1) {
                    container.getChildAt(position + 1).requestFocus();
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void setOnTabSelectedListener(OnTabSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (this.hasFocus()) {
            super.addFocusables(views, direction, focusableMode);
        } else {
            View selectedView = getSelectedView();
            if (selectedView != null) {
                views.add(0,selectedView);
            } else {
                super.addFocusables(views, direction, focusableMode);
            }
        }
    }

    public interface OnTabSelectedListener {
        void onTabSelected(int position);
    }





} 