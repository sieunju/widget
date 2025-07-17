package hmju.widget.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Description : WalletStack View
 * <p>
 * Created by juhongmin on 2025. 7. 17.
 */
public class WalletStackView<T> extends ConstraintLayout {

    private static final String TAG = "WalletStackView";

    public interface Listener<T> {
        View initView(@NonNull T item, @NonNull ViewGroup parent);
    }

    static class WalletData<T> {
        private int index = -1;
        private final T item;

        public WalletData(T item) {
            this.item = item;
        }

        public int getIndex() {
            return index;
        }

        public T getItem() {
            return item;
        }
    }

    record ViewWrapperData<T>(View view, WalletData<T> data) {
    }

    private Listener<T> listener = null;
    private float mSpanStackHeight = dp(30);
    private float mScaleStep = 0.1f;
    private int mThreshold = 200;
    private final List<WalletData<T>> mDataList = new ArrayList<>();
    private final List<ViewWrapperData<T>> mVirtualList = new ArrayList<>();
    private int mCurrentIndex = 0;
    private int mCardHeight = dp(300);
    private int mStackCount = 3;
    private int mCenterX = 0;
    private boolean isAni = false;

    public static void LogD(String msg) {
        Log.d(TAG, msg);
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                (float) value,
                Resources.getSystem().getDisplayMetrics()
        );
    }

    public WalletStackView(Context context) {
        this(context, null);
    }

    public WalletStackView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public WalletStackView(@NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClipToPadding(false);
        setClipChildren(false);
        setStackCount(3);
        post(() -> mCenterX = getWidth() / 2);
    }

    public WalletStackView<T> setListener(Listener<T> l) {
        listener = l;
        return this;
    }

    public WalletStackView<T> setStackCount(int newValue) {
        mStackCount = newValue;
        return this;
    }

    public WalletStackView<T> setCardHeight(int newValue) {
        mCardHeight = newValue;
        return this;
    }

    public WalletStackView<T> setThreshold(int newValue) {
        mThreshold = newValue;
        return this;
    }

    public WalletStackView<T> setScaleStep(float newValue) {
        mScaleStep = newValue;
        return this;
    }

    public WalletStackView<T> setSpanStackHeight(float newHeight) {
        mSpanStackHeight = newHeight;
        return this;
    }

    public void setItems(List<T> list) {
        mDataList.clear();
        for (int i = 0; i < list.size(); i++) {
            T data = list.get(i);
            WalletData<T> item = new WalletData<>(data);
            item.index = i;
            mDataList.add(item);
        }
        mCurrentIndex = mDataList.get(0).getIndex();
    }

    private void setScale(View v, float newScale) {
        v.setScaleX(newScale);
        v.setScaleY(newScale);
    }

    public void startAni() {
        if (listener == null) return;
        for (int i = 0; i < mStackCount; i++) {
            WalletData<T> data = mDataList.get(i);
            View view = listener.initView(data.getItem(), this);
            setTouchDetector(view, data);
            float scale = 1.0f - (i * mScaleStep);
            setScale(view, scale);
            view.setTranslationY((float) mCardHeight / 2);
            mVirtualList.add(new ViewWrapperData<>(view, data));
            addView(view, 0);
            float targetAlpha = 1.0f - (i * 0.2f);
            view.animate().alpha(targetAlpha)
                    .translationY(-mSpanStackHeight * i)
                    .setStartDelay(200L * i) // 0, 200ms, 400ms, ...
                    .setDuration(i == 0 ? 600 : 500) // 첫 번째는 600ms, 나머지는 500ms
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
        }
    }

    private void setTouchDetector(@NonNull View v, final WalletData<T> item) {
        OnTouchListener touchListener = new OnTouchListener() {

            float currentX = 0;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                LogD("onTouch Index " + item.getIndex() + " CurrentIndex " + mCurrentIndex);
                if (item.getIndex() != mCurrentIndex) return false;
                if (isAni) return false;
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    currentX = event.getRawX();
                } else if (action == MotionEvent.ACTION_MOVE) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    float diffX = event.getRawX() - currentX;
                    v.setTranslationX(diffX);

                    // Y값과 회전 추가
                    float progress = Math.abs(diffX) / (mCenterX * 0.5f); // 스와이프 진행도 (0~1)
                    progress = Math.min(progress, 1.0f); // 최대 1.0으로 제한
                    if (progress >= 0.5f) {
                        // 0.5~1.0 구간을 0~1.0으로 정규화
                        float normalizedProgress = (progress - 0.5f) / 0.5f;
                        normalizedProgress = Math.min(normalizedProgress, 1.0f);

                        // Y값: 스와이프할수록 아래로 내려감 (최대 20dp)
                        float translationY = dp(10) * normalizedProgress;
                        v.setTranslationY(translationY);

                        // 회전: 스와이프 방향에 따라 회전 (최대 3도)
                        float rotation = (diffX > 0 ? 1 : -1) * 3 * normalizedProgress;
                        v.setRotation(rotation);
                    } else {
                        // progress가 0.5 미만일 때는 원래 위치로 복원
                        v.setTranslationY(0);
                        v.setRotation(0);
                    }
                    updateBackViewsAnimation(progress);
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                    float x = v.getTranslationX();
                    if (isRemove(x, mThreshold)) {
                        boolean isRightSwipe = x > 0;
                        removeViewWithAnimation(v, isRightSwipe);
                    } else {
                        resetBackViewsAnimation(v);
                    }
                }
                return true;
            }
        };
        v.setOnTouchListener(touchListener);
    }

    private void updateBackViewsAnimation(float progress) {
        for (int i = 1; i < mVirtualList.size(); i++) {
            View backView = mVirtualList.get(i).view();

            // 현재 인덱스에서의 기본값
            float fromScale = 1.0f - (i * mScaleStep);
            float fromTransY = -mSpanStackHeight * i;
            float fromAlpha = 1.0f - (i * 0.2f);

            // 한 단계 위로 올라갔을 때의 값 (i-1 위치)
            float toScale = 1.0f - ((i - 1) * mScaleStep);
            float toTransY = -mSpanStackHeight * (i - 1);
            float toAlpha = 1.0f - ((i - 1) * 0.2f);

            // 보간 계산
            float interpolatedScale = lerp(fromScale, toScale, progress);
            float interpolatedTransY = lerp(fromTransY, toTransY, progress);
            float interpolatedAlpha = lerp(fromAlpha, toAlpha, progress);
            setScale(backView, interpolatedScale);
            backView.setTranslationY(interpolatedTransY);
            backView.setAlpha(interpolatedAlpha);
        }
    }

    private float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }

    private boolean isRemove(float viewX, float centerX) {
        // 우 -> 좌 마이너스
        // 좌 -> 우 플러스
        // 양수 좌 -> 우
        if (viewX > 0) {
            // 좌에서 우로 스와이프
            return centerX - viewX <= 0; // centerX - viewX가 0 이하면 제거
        } else {
            // 우에서 좌로 스와이프
            return centerX + viewX <= 0; // centerX + viewX가 0 이하면 제거
        }
    }

    private void removeViewWithAnimation(View view, boolean isRightSwipe) {
        // 뷰 날리기
        float targetTranslationX = isRightSwipe ? getWidth() : -getWidth();
        float targetTranslationY = dp(20);
        isAni = true;

        view.animate()
                .setDuration(200)
                .translationX(targetTranslationX)
                .translationY(targetTranslationY)
                .alpha(0f)
                .setInterpolator(new FastOutSlowInInterpolator())
                .withEndAction(() -> removeView(view))
                .start();

        // 다음 아이템 인덱스 계산
        int lastIndex = mVirtualList.get(mVirtualList.size() - 1).data().getIndex();
        int findNextIndex = (lastIndex == (mDataList.size() - 1)) ? 0 : lastIndex + 1;

        // 첫 번째 아이템 제거
        mVirtualList.remove(0);
        View newFirstItem = mVirtualList.get(0).view();
        newFirstItem.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .translationY(0)
                .alpha(1.0f)
                .setDuration(200)
                .setInterpolator(new FastOutSlowInInterpolator())
                .start();

        // 중간 뷰들도 한 단계씩 앞으로 이동
        for (int i = 1; i < mVirtualList.size(); i++) {
            View backView = mVirtualList.get(i).view();

            // startAni의 i번째 뷰 설정과 동일하게
            float targetScale = 1.0f - (i * mScaleStep);
            float targetTransY = -mSpanStackHeight * i;
            float targetAlpha = 1.0f - (i * 0.2f);

            backView.animate()
                    .scaleX(targetScale)
                    .scaleY(targetScale)
                    .translationY(targetTransY)
                    .alpha(targetAlpha)
                    .setDuration(200)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
        }

        // 새로운 마지막 뷰 생성
        WalletData<T> item = mDataList.get(findNextIndex);
        View newLastView = listener.initView(item.getItem(), this);
        setTouchDetector(newLastView, item);

        // startAni의 마지막 뷰 설정과 동일하게 (i = mStackCount - 1)
        int lastPosition = mStackCount - 1;
        float lastScale = 1.0f - (lastPosition * mScaleStep);
        float lastTransY = -mSpanStackHeight * lastPosition;
        float lastAlpha = 1.0f - (lastPosition * 0.2f);

        // 초기 상태 설정 (startAni와 동일)
        newLastView.setAlpha(0f);
        newLastView.setTranslationY((float) mCardHeight / 2);
        setScale(newLastView, lastScale);

        addView(newLastView, 0);

        // 최종 위치로 애니메이션 (startAni와 동일)
        newLastView.animate()
                .alpha(lastAlpha)
                .translationY(lastTransY)
                .setDuration(200)
                .setInterpolator(new FastOutSlowInInterpolator())
                .withEndAction(() -> isAni = false)
                .start();
        mVirtualList.add(new ViewWrapperData<>(newLastView, item));
        mCurrentIndex = mVirtualList.get(0).data().getIndex();
    }

    private void resetBackViewsAnimation(View firstView) {
        firstView.animate()
                .setDuration(100)
                .translationX(0)
                .rotation(0)
                .translationY(0)
                .setInterpolator(new FastOutSlowInInterpolator())
                .start();
        // 뒤의 뷰들을 원래 위치로 복원
        for (int i = 1; i < mVirtualList.size(); i++) {
            View backView = mVirtualList.get(i).view();

            float originalScale = 1.0f - (i * mScaleStep);
            float originalTransY = -mSpanStackHeight * i;
            backView.setScaleX(originalScale);
            backView.setScaleY(originalScale);
            backView.setTranslationY(originalTransY);
        }
    }
}

