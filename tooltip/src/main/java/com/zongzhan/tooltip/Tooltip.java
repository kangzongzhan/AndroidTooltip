package com.zongzhan.tooltip;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

public class Tooltip {
    private boolean isShowing = false;
    private TooltipListener listener;
    private TooltipContainer container;

    private Tooltip() {
    }

    private void setContainer(TooltipContainer container) {
        this.container = container;
        container.tooltip = this;
    }

    public void show() {
        container.show();
    }

    public void dismiss() {
        container.dismiss();
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void setListener(TooltipListener listener) {
        this.listener = listener;
    }

    public enum Position {
        ABOVE, BELOW, LEFT, RIGHT
    }

    public enum Align {
        CENTER, TOP, RIGHT, BOTTOM, LEFT
    }

    private static class TooltipParams {
        Context context;
        ViewGroup parent;
        View targetView;
        View toolTipView;
        @LayoutRes int toolTipLayout;
        Position position = Position.ABOVE;
        Align align = Align.CENTER;
        boolean outSideClose = true;
        boolean inSideClose = false;
        boolean inSideConsumeTouchEvent = true;
        boolean outSideConsumeTouchEvent = true;
        int animationDuration = 200;
        int xOffset = 0;
        int yOffset = 0;
    }

    public static class Builder {
        private TooltipParams params;
        public Builder() {
            params = new TooltipParams();
        }
        public Builder target(View targetView) {
            this.params.targetView = targetView;
            this.params.context = targetView.getContext();
            return this;
        }
        public Builder position(Position position) {
            this.params.position = position;
            return this;
        }
        public Builder align(Align align) {
            this.params.align = align;
            return this;
        }
        public Builder offset(int xOffset, int yOffset) {
            this.params.xOffset = xOffset;
            this.params.yOffset = yOffset;
            return this;
        }

        public Builder toolTipView(View toolTipView) {
            this.params.toolTipLayout = -1;
            this.params.toolTipView = toolTipView;
            return this;
        }

        public Builder toolTipView(@LayoutRes int toolTipLayout) {
            this.params.toolTipView = null;
            this.params.toolTipLayout = toolTipLayout;
            return this;
        }

        public Builder showInto(ViewGroup parent) {
            this.params.parent = parent;
            return this;
        }

        public Builder consumeTouchEvent(boolean inside, boolean outside) {
            this.params.inSideConsumeTouchEvent = inside;
            this.params.outSideConsumeTouchEvent = outside;
            return this;
        }
        public Builder closeWhenTouch(boolean inside, boolean outside) {
            this.params.inSideClose = inside;
            this.params.outSideClose = outside;
            return this;
        }
        public Builder animationDuration(int duration) {
            this.params.animationDuration = duration;
            return this;
        }
        public Tooltip build() {
            if (this.params.targetView == null) {
                throw new RuntimeException("No target is set");
            }
            if (this.params.toolTipLayout != -1 && this.params.toolTipView == null) {
                this.params.toolTipView = LayoutInflater.from(this.params.context).inflate(this.params.toolTipLayout, null);
            }
            if (this.params.parent == null) {
                if(params.context instanceof Activity) {
                    Activity activity = (Activity) params.context;
                    this.params.parent = (ViewGroup) activity.getWindow().getDecorView();
                } else {
                    throw new RuntimeException("In non Activity, you have to set a parent explicitly");
                }
            }
            TooltipContainer tooltipContainer = new TooltipContainer(this.params);
            Tooltip tooltip = new Tooltip();
            tooltip.setContainer(tooltipContainer);
            return tooltip;
        }
    }

    public interface TooltipListener {
        void onShow();
        void onDismiss();
        void onTouch();
    }

    private static class TooltipContainer extends FrameLayout implements ViewTreeObserver.OnPreDrawListener {

        private TooltipParams params;
        private Tooltip tooltip;

        public TooltipContainer(TooltipParams params) {
            super(params.context);
            this.params = params;
            this.addView(this.params.toolTipView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            if (touchInContentView(event)) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && tooltip.listener != null) {
                    tooltip.listener.onTouch();
                }
                if (params.inSideClose)
                    dismiss();
                return params.inSideConsumeTouchEvent;
            }
            if (!touchInContentView(event)) {
                if (params.outSideClose)
                    dismiss();
                return params.outSideConsumeTouchEvent;
            }
            return super.onTouchEvent(event);
        }

        private void show() {
            params.parent.addView(this, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            tooltip.isShowing = true;
            setAlpha(0f);
            requestLayout();
            animate()
                    .alpha(1f)
                    .setInterpolator(new DecelerateInterpolator())
                    .setDuration(params.animationDuration)
                    .start();
            if (tooltip.listener != null) {
                tooltip.listener.onShow();
            }
        }

        private void dismiss() {
            if (!tooltip.isShowing) {
                return;
            }
            if (tooltip.listener != null) {
                tooltip.listener.onDismiss();
            }
            tooltip.isShowing = false;
            animate().setInterpolator(new DecelerateInterpolator())
                    .alpha(0f)
                    .setDuration(params.animationDuration)
                    .withEndAction(() -> {
                        ViewGroup parent = (ViewGroup) getParent();
                        if (parent != null)
                            parent.removeView(TooltipContainer.this);
                    }).start();
        }

        private boolean touchInContentView(MotionEvent event) {
            float x = event.getRawX();
            float y = event.getRawY();
            int[] contentViewCoords = new int[2];
            params.toolTipView.getLocationOnScreen(contentViewCoords);
            boolean xHit = x > contentViewCoords[0] && x < contentViewCoords[0] + params.toolTipView.getWidth();
            boolean yHit = y > contentViewCoords[1] && y < contentViewCoords[1] + params.toolTipView.getHeight();
            return xHit && yHit;
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            getViewTreeObserver().addOnPreDrawListener(this);
        }

        @Override
        public boolean onPreDraw() {
            if (params.targetView == null || params.toolTipView.getTranslationX() != 0 || params.toolTipView.getTranslationY() != 0)
                return true;

            int[] translations = getTranslations();
            translations[0] += params.xOffset;
            translations[1] += params.yOffset;
            params.toolTipView.setTranslationX(translations[0]);
            params.toolTipView.setTranslationY(translations[1]);
            return true;
        }

        private int[] getTranslations() {

            int[] targetViewCoords = new int[2];
            params.targetView.getLocationOnScreen(targetViewCoords);
            int[] contentViewCoords = new int[2];
            params.toolTipView.getLocationOnScreen(contentViewCoords);

            int[] translations = new int[2];
            switch (params.position) {
                case ABOVE:
                    switch (params.align) {
                        case LEFT:
                            translations[0] = targetViewCoords[0] - contentViewCoords[0];
                            break;
                        case RIGHT:
                            translations[0] = targetViewCoords[0] - contentViewCoords[0] + params.targetView.getWidth() - params.toolTipView.getWidth();
                            break;
                        default:
                            translations[0] = targetViewCoords[0] - contentViewCoords[0] - params.toolTipView.getWidth() / 2 + params.targetView.getWidth() / 2;
                            break;
                    }
                    translations[1] = targetViewCoords[1] - contentViewCoords[1] - params.toolTipView.getHeight();
                    break;
                case BELOW:
                    switch (params.align) {
                        case LEFT:
                            translations[0] = targetViewCoords[0] - contentViewCoords[0];
                            break;
                        case RIGHT:
                            translations[0] = targetViewCoords[0] - contentViewCoords[0] + params.targetView.getWidth() - params.toolTipView.getWidth();
                            break;
                        default:
                            translations[0] = targetViewCoords[0] - contentViewCoords[0] - params.toolTipView.getWidth() / 2 + params.targetView.getWidth() / 2;
                            break;
                    }
                    translations[1] = targetViewCoords[1] - contentViewCoords[1] + params.targetView.getHeight();
                    break;
                case LEFT:
                    translations[0] = targetViewCoords[0] - contentViewCoords[0]
                            - params.toolTipView.getWidth();
                    switch (params.align) {
                        case TOP:
                            translations[1] = targetViewCoords[1] - contentViewCoords[1];
                            break;
                        case BOTTOM:
                            translations[1] = targetViewCoords[1] - contentViewCoords[1] + params.targetView.getHeight() - params.toolTipView.getHeight();
                            break;
                        default:
                            translations[1] = targetViewCoords[1] - contentViewCoords[1] + params.targetView.getHeight() / 2 - params.toolTipView.getHeight() / 2;
                            break;
                    }
                    break;
                case RIGHT:
                    translations[0] = targetViewCoords[0] - contentViewCoords[0] + params.targetView.getWidth();
                    switch (params.align) {
                        case TOP:
                            translations[1] = targetViewCoords[1] - contentViewCoords[1];
                            break;
                        case BOTTOM:
                            translations[1] = targetViewCoords[1] - contentViewCoords[1] + params.targetView.getHeight() - params.toolTipView.getHeight();
                            break;
                        default:
                            translations[1] = targetViewCoords[1] - contentViewCoords[1] + params.targetView.getHeight() / 2 - params.toolTipView.getHeight() / 2;
                            break;
                    }
                    break;
            }
            return translations;
        }
    }
}
