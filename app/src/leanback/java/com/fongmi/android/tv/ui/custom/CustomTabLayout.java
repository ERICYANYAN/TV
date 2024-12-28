package com.fongmi.android.tv.ui.custom;

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

public class CustomTabLayout extends HorizontalScrollView {

    private final LinearLayout container;
    private final Paint indicatorPaint;
    private final RectF indicatorRect;
    private OnTabSelectedListener listener;
    private float indicatorLeft;
    private float indicatorRight;
    private int selectedPosition;

    public CustomTabLayout(@NonNull Context context) {
        this(context, null);
    }

    public CustomTabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
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
        
        indicatorRect = new RectF();
    }

    public void setTabs(String... titles) {
        container.removeAllViews();
        for (int i = 0; i < titles.length; i++) {
            TextView tab = createTab(titles[i]);
            tab.setTag(i);
            container.addView(tab);
            if (i == 0) selectTab(tab);
        }
    }

    private TextView createTab(String title) {
        TextView tab = new TextView(getContext());
        tab.setText(title);
        tab.setTextSize(16);
        tab.setPadding(ResUtil.dp2px(16), ResUtil.dp2px(8), ResUtil.dp2px(16), ResUtil.dp2px(8));
        tab.setFocusable(true);
        tab.setTextColor(ResUtil.getColor(R.color.grey_700));
        tab.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tab.setOnClickListener(v -> selectTab(v));
        tab.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) selectTab(v);
        });
        return tab;
    }

    private void selectTab(View view) {
        int position = (int) view.getTag();
        if (position == selectedPosition) return;
        
        // 更新选中状态
        for (int i = 0; i < container.getChildCount(); i++) {
            TextView tab = (TextView) container.getChildAt(i);
            tab.setTextColor(i == position ? ResUtil.getColor(R.color.white) : ResUtil.getColor(R.color.grey_700));
        }

        // 更新指示器位置
        view.post(() -> {
            indicatorLeft = view.getLeft();
            indicatorRight = view.getRight();
            invalidate();
        });

        // 滚动到可见位置
        int scrollX = (view.getLeft() - (getWidth() - view.getWidth()) / 2);
        smoothScrollTo(scrollX, 0);

        selectedPosition = position;
        if (listener != null) listener.onTabSelected(position);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制底部指示器
        float indicatorHeight = ResUtil.dp2px(2);
        indicatorRect.left = indicatorLeft;
        indicatorRect.right = indicatorRight;
        indicatorRect.top = getHeight() - indicatorHeight;
        indicatorRect.bottom = getHeight();
        canvas.drawRect(indicatorRect, indicatorPaint);
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

    public void setCurrentTab(int i) {
        if (i >= 0 && i < container.getChildCount()) {
            View view = container.getChildAt(i);
            if (view != null) {
                view.requestFocus();
            }
        }
    }

    public interface OnTabSelectedListener {
        void onTabSelected(int position);
    }
} 