package com.fongmi.android.tv.newUI.view;

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

public class OKHomeTabLayout extends HorizontalScrollView {

    private final LinearLayout container;
    private final Paint indicatorPaint;
    private final RectF indicatorRect;
    private OnTabSelectedListener listener;
    private float indicatorLeft;
    private float indicatorRight;
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
        
        indicatorRect = new RectF();
    }

    public void setTabs(String... titles) {
        container.removeAllViews();
        for (int i = 0; i < titles.length; i++) {
            TextView tab = createTab(titles[i]);
            tab.setTag(i);
            container.addView(tab);
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
        
        // 修改点击监听器，只在点击时切换选中状态
        tab.setOnClickListener(v -> {
            if ((int)v.getTag() != selectedPosition) {
                selectTab(v);
            }
        });
        
        // 修改焦点变化监听器，只处理高亮效果
        tab.setOnFocusChangeListener((v, hasFocus) -> {
            TextView textView = (TextView) v;
            if (hasFocus) {
                // 仅高亮显示，不切换选中状态
                textView.setTextColor(ResUtil.getColor(R.color.white));
                // 滚动到可见位置
                int scrollX = (v.getLeft() - (getWidth() - v.getWidth()) / 2);
                smoothScrollTo(scrollX, 0);
            } else {
                // 失去焦点时，根据是否是选中项来决定颜色
                textView.setTextColor((int)v.getTag() == selectedPosition ? 
                    ResUtil.getColor(R.color.white) : 
                    ResUtil.getColor(R.color.grey_700));
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
                selectTab(view);
                view.requestFocus();
            }
        }
    }

    public String getCurrentTabName() {
        // 获取当前选中的tab的名称，需要把 view 转换为 TextView
        TextView textView = (TextView) container.getChildAt(selectedPosition);
        return textView.getText().toString();
    }

    public interface OnTabSelectedListener {
        void onTabSelected(int position);
    }
} 