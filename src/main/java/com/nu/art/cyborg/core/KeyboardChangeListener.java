package com.nu.art.cyborg.core;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.core.abs.Cyborg;
import com.nu.art.cyborg.core.interfaces.ActivityLifeCycleImpl;

public class KeyboardChangeListener {

	public interface OnKeyboardVisibilityListener {

		void onVisibilityChanged(boolean visible);
	}

	private boolean enabled;

	private final Activity activity;

	private final Cyborg cyborg;

	private OnGlobalLayoutListener layoutChangeListener;

	public KeyboardChangeListener(Cyborg cyborg, Activity activity) {
		this.cyborg = cyborg;
		this.activity = activity;
	}

	final void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (enabled && layoutChangeListener == null) {
			addActivityLifecycleListener();
		}
	}

	private void addActivityLifecycleListener() {
		activity.getApplication().registerActivityLifecycleCallbacks(new ActivityLifeCycleImpl() {
			@Override
			public void onActivityResumed(Activity activity) {
				if (KeyboardChangeListener.this.activity != activity)
					return;

				if (!enabled)
					return;

				addLayoutChangeListener();
			}

			@Override
			public void onActivityPaused(Activity activity) {
				if (KeyboardChangeListener.this.activity != activity)
					return;

				removeLayoutChangeListener();
			}

			@Override
			public void onActivityDestroyed(Activity activity) {
				if (KeyboardChangeListener.this.activity != activity)
					return;

				removeLayoutChangeListener();
			}
		});
	}

	private void removeLayoutChangeListener() {
		final View activityRootView = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);

		ViewTreeObserver viewTreeObserver = activityRootView.getViewTreeObserver();
		if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
			viewTreeObserver.removeOnGlobalLayoutListener(layoutChangeListener);
		} else
			viewTreeObserver.removeGlobalOnLayoutListener(layoutChangeListener);
	}

	private void addLayoutChangeListener() {
		final View activityRootView = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);

		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(layoutChangeListener = new OnGlobalLayoutListener() {

			private final int DefaultKeyboardDP = 100;

			// From @nathanielwolf answer...  Lollipop includes button bar in the root. Add height of button bar (48dp) to maxDiff
			private final int EstimatedKeyboardDP = DefaultKeyboardDP + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 48 : 0);

			private final Rect r = new Rect();

			private boolean wasOpened;

			@Override
			public void onGlobalLayout() {
				int estimatedKeyboardHeight = cyborg.dpToPx(EstimatedKeyboardDP);

				activityRootView.getWindowVisibleDisplayFrame(r);
				int heightDiff = activityRootView.getRootView().getHeight() - (r.bottom - r.top);
				final boolean isShown = heightDiff >= estimatedKeyboardHeight;

				if (isShown == wasOpened)
					return;

				wasOpened = isShown;
				cyborg.dispatchEvent("Keyboard visibility changed: " + isShown, OnKeyboardVisibilityListener.class, new Processor<OnKeyboardVisibilityListener>() {
					@Override
					public void process(OnKeyboardVisibilityListener listener) {
						listener.onVisibilityChanged(isShown);
					}
				});
			}
		});
	}
}
