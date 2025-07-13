package hmju.widget.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.text.StaticLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Description : Rolling Number Animation TextView
 * <p>
 * Created by juhongmin on 2025. 7. 13.
 */
public class RollingAmountViewJava extends ConstraintLayout {

    private float defaultTextSize = 30f;
    private long currentAmount = 0L;
    private float amountTextSize = defaultTextSize;
    private int amountTextSideSpan = 0;
    private int amountTextStyle = View.NO_ID;
    private int amountTextColor = Color.BLACK;

    private LinearLayout amountRootView;
    private TextView tvTemp;
    private Handler handler;

    public RollingAmountViewJava(@NonNull Context context) {
        this(context, null);
    }

    public RollingAmountViewJava(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RollingAmountViewJava(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        handler = new Handler(Looper.getMainLooper());
        initAmountRootView();
        addView(amountRootView);
        tvTemp = initTempTextView();
    }

    private void initAmountRootView() {
        amountRootView = new LinearLayout(getContext());
        amountRootView.setGravity(Gravity.CENTER);
        amountRootView.setOrientation(LinearLayout.HORIZONTAL);

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        layoutParams.topToTop = LayoutParams.PARENT_ID;
        layoutParams.endToEnd = LayoutParams.PARENT_ID;
        layoutParams.bottomToBottom = LayoutParams.PARENT_ID;
        amountRootView.setLayoutParams(layoutParams);
        amountRootView.setClipToPadding(false);
    }

    /**
     * SetAmount
     *
     * @param amount 금액
     */
    public void setAmount(long amount) {
        amountTextSize = defaultTextSize;
        tvTemp = initTempTextView();
        amountRootView.removeAllViews();

        String numberStr = NumberFormat.getNumberInstance().format(amount);
        calculateTextSize(numberStr);

        List<Integer> amountArr = new ArrayList<>();
        for (char c : numberStr.toCharArray()) {
            if (c == ',') {
                amountArr.add(-1);
            } else if (c == '-') {
                amountArr.add(-2);
            } else {
                amountArr.add(Character.getNumericValue(c));
            }
        }

        long delay = 0L;
        for (int i = 0; i < amountArr.size(); i++) {
            int digits = amountArr.get(i);

            if (digits == -1 || digits == -2) {
                TextView view = initDigitsTextView(0);
                view.setText(digits == -1 ? "," : "-");
                view.setAlpha(0f);
                view.setTranslationY(50f);
                amountRootView.addView(view);

                view.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(delay)
                        .start();
                delay += 50;
            } else {
                SingleItemRecyclerView view = new SingleItemRecyclerView(getContext());
                view.setAdapter(new DigitsAdapter());
                view.setNestedScrollingEnabled(false);
                view.setClipToPadding(false);
                view.setAlpha(0F);
                new LinearSnapHelper().attachToRecyclerView(view);
                amountRootView.addView(view);

                CustomSmoothScroller scroller = new CustomSmoothScroller(currentAmount < amount);
                final int targetPosition = digits;
                final SingleItemRecyclerView finalView = view;

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finalView.animate()
                                .alpha(1f)
                                .setDuration(50)
                                .start();
                        scroller.setTargetPosition(targetPosition);
                        if (finalView.getLayoutManager() != null) {
                            finalView.getLayoutManager().startSmoothScroll(scroller);
                        }
                    }
                }, delay);
                delay += 50;
            }
        }
        currentAmount = amount;
    }

    public long getAmount() {
        return currentAmount;
    }

    public RollingAmountViewJava setDefaultTextSize(float newTextSize) {
        defaultTextSize = newTextSize;
        return this;
    }

    public RollingAmountViewJava setAmountTextSideSpan(int newSpan) {
        amountTextSideSpan = newSpan;
        return this;
    }

    public RollingAmountViewJava setAmountTextColor(int newColor) {
        amountTextColor = newColor;
        return this;
    }

    public RollingAmountViewJava setTextStyle(@StyleRes int newStyle) {
        amountTextStyle = newStyle;
        return this;
    }

    /**
     * AutoTextSize 계산하는 함수
     */
    private void calculateTextSize(String newText) {
        boolean isEnd = false;
        int availableWidth = getWidth() - (getPaddingLeft() + getPaddingRight());

        while (!isEnd) {
            if (tvTemp == null) break;

            StaticLayout layout = StaticLayout.Builder.obtain(
                            newText,
                            0,
                            newText.length(),
                            tvTemp.getPaint(),
                            availableWidth
                    ).setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0.0f, 1.0f)
                    .setIncludePad(false)
                    .build();

            if (layout.getLineCount() > 1) {
                amountTextSize -= 1F;
            } else {
                isEnd = true;
            }
            tvTemp = initTempTextView();
        }
    }

    private class CustomSmoothScroller extends LinearSmoothScroller {

        private final boolean isUp;

        public CustomSmoothScroller(boolean isUp) {
            super(getContext());
            this.isUp = isUp;
        }

        @Override
        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            return 1200f / displayMetrics.densityDpi;
        }

        @Override
        protected int calculateTimeForScrolling(int dx) {
            int time = super.calculateTimeForScrolling(dx);
            return Math.min(time, 2000); // 최대 2초로 제한
        }

        @Override
        protected int calculateTimeForDeceleration(int dx) {
            return (int) (calculateTimeForScrolling(dx) * 0.8f);
        }

        @Override
        public int getVerticalSnapPreference() {
            return isUp ? SNAP_TO_START : SNAP_TO_END;
        }
    }

    private TextView initDigitsTextView(int sidePadding) {
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        textView.setGravity(Gravity.CENTER);

        if (amountTextStyle != View.NO_ID) {
            TextViewCompat.setTextAppearance(textView, amountTextStyle);
        } else {
            textView.setTextColor(amountTextColor);
            textView.setTypeface(null, Typeface.BOLD);
        }

        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, amountTextSize);
        textView.setPadding(sidePadding, 0, sidePadding, 0);
        textView.setIncludeFontPadding(false);

        return textView;
    }

    private TextView initTempTextView() {
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        textView.setGravity(Gravity.RIGHT);

        if (amountTextStyle != View.NO_ID) {
            TextViewCompat.setTextAppearance(textView, amountTextStyle);
        } else {
            textView.setTextColor(amountTextColor);
            textView.setTypeface(null, Typeface.BOLD);
        }

        textView.setVisibility(View.INVISIBLE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, amountTextSize);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setIncludeFontPadding(false);

        return textView;
    }

    private class DigitsAdapter extends RecyclerView.Adapter<DigitsAdapter.DigitsViewHolder> {
        private final List<Integer> dataList;

        public DigitsAdapter() {
            dataList = new ArrayList<>();
            for (int i = 0; i <= 9; i++) {
                dataList.add(i);
            }
        }

        @NonNull
        @Override
        public DigitsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DigitsViewHolder(initDigitsTextView(amountTextSideSpan));
        }

        @Override
        public void onBindViewHolder(@NonNull DigitsViewHolder holder, int position) {
            holder.tv.setText(String.valueOf(dataList.get(position)));
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        private static class DigitsViewHolder extends RecyclerView.ViewHolder {
            TextView tv;

            public DigitsViewHolder(@NonNull TextView itemView) {
                super(itemView);
                this.tv = itemView;
            }
        }
    }

    private static class SingleItemRecyclerView extends RecyclerView {
        private int itemHeight = 0;

        public SingleItemRecyclerView(@NonNull Context context) {
            this(context, null);
        }

        public SingleItemRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public SingleItemRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            setLayoutManager(new LinearLayoutManager(context));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            // 아이템 높이 계산
            if (itemHeight == 0 && getAdapter() != null && getAdapter().getItemCount() > 0) {
                RecyclerView.LayoutManager layoutManager = getLayoutManager();
                if (layoutManager != null) {
                    RecyclerView.ViewHolder viewHolder = getAdapter().createViewHolder(this, 0);
                    getAdapter().onBindViewHolder(viewHolder, 0);

                    // 아이템 뷰 측정
                    View itemView = viewHolder.itemView;
                    itemView.measure(
                            MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                    );
                    itemHeight = itemView.getMeasuredHeight();
                }
            }

            // 높이를 아이템 하나의 높이로 제한
            if (itemHeight > 0) {
                int newHeight = itemHeight + getPaddingTop() + getPaddingBottom();
                setMeasuredDimension(getMeasuredWidth(), newHeight);
            }
        }
    }
}
