/*
 * cyborg-core is an extendable  module based framework for Android.
 *
 * Copyright (C) 2017  Adam van der Kruk aka TacB0sS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nu.art.cyborg.core;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.nu.art.cyborg.annotations.Restorable;
import com.nu.art.cyborg.core.CyborgAdapter.CyborgRecyclerAdapter;
import com.nu.art.cyborg.modules.AttributeModule;
import com.nu.art.cyborg.modules.AttributeModule.AttributesSetter;
import com.nu.art.reflection.annotations.ReflectiveInitialization;
import com.nu.art.cyborg.R;

/**
 * Enhanced Recycler with item click events, xml layouting parameters.
 */
@Restorable
public class CyborgRecycler
		extends RecyclerView {

	public interface OnRecyclerItemClickListener {

		void onRecyclerItemClicked(RecyclerView parentView, View view, int position);
	}

	public interface OnRecyclerItemLongClickListener {

		boolean onRecyclerItemLongClicked(RecyclerView parentView, View view, int position);
	}

	public static class SpacesItemDecoration
			extends ItemDecoration {

		private int vertical;

		private int horizontal;

		public SpacesItemDecoration() { }

		@Override
		public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
			outRect.left = horizontal;
			outRect.right = horizontal;
			outRect.bottom = vertical;
			outRect.top = vertical;
		}

		final ItemDecoration set(int vertical, int horizontal) {
			this.vertical = vertical;
			this.horizontal = horizontal;
			return this;
		}
	}

	public class CyborgGridLayoutManager
			extends GridLayoutManager {

		public CyborgGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
			super(context, attrs, defStyleAttr, defStyleRes);
		}

		public CyborgGridLayoutManager(Context context, int spanCount) {
			super(context, spanCount);
		}

		public CyborgGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
			super(context, spanCount, orientation, reverseLayout);
		}

		@Override
		public boolean supportsPredictiveItemAnimations() {
			Adapter adapter = getAdapter();
			return (adapter != null && adapter instanceof CyborgRecyclerAdapter && ((CyborgRecyclerAdapter) adapter).isAutoAnimate()) || super.supportsPredictiveItemAnimations();
		}
	}

	private CyborgGridLayoutManager layoutManager;

	public OnRecyclerItemClickListener recyclerItemListener;

	public OnRecyclerItemLongClickListener recyclerItemLongListener;

	private SpacesItemDecoration itemDecoration = new SpacesItemDecoration();

	@Restorable
	private int landscapeColumnsCount = 1;

	@Restorable
	private int portraitColumnsCount = 1;

	@Restorable
	private int layoutOrientation;

	@Restorable
	private int verticalSpacing = 2;

	@Restorable
	private int horizontalSpacing = 2;

	public CyborgRecycler(Context context) {
		super(context);
		init(context, null, -1);
	}

	public CyborgRecycler(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, -1);
	}

	public CyborgRecycler(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private void init(Context context, AttributeSet attrs, int defStyle) {
		CyborgBuilder.getModule(isInEditMode() ? context : null, AttributeModule.class).setAttributes(context, attrs, this);
		addItemDecoration(itemDecoration);
	}

	@Override
	public void setAdapter(Adapter adapter) {
		super.setAdapter(adapter);
	}

	public View getViewForPosition(int position) {
		return layoutManager.findViewByPosition(position);
	}

	public final void setAdapter(CyborgAdapter adapter) {
		super.setAdapter(adapter.getRecyclerAdapter(this));
	}

	@SuppressWarnings("unused")
	public void setRecyclerItemClickListener(OnRecyclerItemClickListener recyclerItemListener) {
		this.recyclerItemListener = recyclerItemListener;
	}

	@SuppressWarnings("unused")
	public void setRecyclerItemLongClickListener(OnRecyclerItemLongClickListener recyclerItemLongListener) {
		this.recyclerItemLongListener = recyclerItemLongListener;
	}

	public int getHorizontalSpacing() {
		return horizontalSpacing;
	}

	public int getLandscapeColumnsCount() {
		return landscapeColumnsCount;
	}

	public int getLayoutOrientation() {
		return layoutOrientation;
	}

	public int getPortraitColumnsCount() {
		return portraitColumnsCount;
	}

	public int getVerticalSpacing() {
		return verticalSpacing;
	}

	public void onConfigurationChanged(Configuration newConfig) {
		invalidateLayoutManager();
	}

	public final void invalidateLayoutManager() {
		int orientation = getContext().getResources().getConfiguration().orientation;

		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			if (layoutManager == null) {
				layoutManager = new CyborgGridLayoutManager(getContext(), landscapeColumnsCount, layoutOrientation, false);
				setLayoutManager(layoutManager);
			}
			layoutManager.setSpanCount(landscapeColumnsCount);
		} else {
			if (layoutManager == null) {
				layoutManager = new CyborgGridLayoutManager(getContext(), landscapeColumnsCount, layoutOrientation, false);
				setLayoutManager(layoutManager);
			}
			layoutManager.setSpanCount(portraitColumnsCount);
		}
		layoutManager.setOrientation(layoutOrientation);

		if (layoutOrientation == LinearLayoutManager.VERTICAL)
			itemDecoration.set(verticalSpacing, horizontalSpacing);
		else
			itemDecoration.set(horizontalSpacing, verticalSpacing);
	}

	//	@Override
	//	protected void onAttachedToWindow() {
	//		super.onAttachedToWindow();
	//		invalidateLayoutManager();
	//	}

	public void setLandscapeColumnsCount(int landscapeColumnsCount) {
		this.landscapeColumnsCount = landscapeColumnsCount;
		if (this.landscapeColumnsCount < 1)
			this.landscapeColumnsCount = 1;
	}

	public void setPortraitColumnsCount(int portraitColumnsCount) {
		this.portraitColumnsCount = portraitColumnsCount;
		if (this.portraitColumnsCount < 1)
			this.portraitColumnsCount = 1;
	}

	public void setVerticalSpacing(int verticalSpacing) {
		this.verticalSpacing = verticalSpacing;
	}

	public void setHorizontalSpacing(int horizontalSpacing) {
		this.horizontalSpacing = horizontalSpacing;
	}

	public void setLayoutOrientation(int layoutOrientation) {
		this.layoutOrientation = layoutOrientation;
	}

	/**
	 * Setting the xml attributes onto a {@link CyborgRecycler} instance.
	 */
	@ReflectiveInitialization
	public static class CyborgRecyclerSetter
			extends AttributesSetter<CyborgRecycler> {

		private static int[] ids = {
				R.styleable.Recycler_orientation,
				R.styleable.Recycler_horizontalSpacing,
				R.styleable.Recycler_verticalSpacing,
				R.styleable.Recycler_landscapeColumnsCount,
				R.styleable.Recycler_portraitColumnsCount
		};

		private CyborgRecyclerSetter() {
			super(CyborgRecycler.class, R.styleable.Recycler, ids);
		}

		@Override
		protected void setAttribute(CyborgRecycler instance, TypedArray a, int attr) {
			if (attr == R.styleable.Recycler_orientation) {
				int margin = a.getInt(attr, 0);
				instance.setLayoutOrientation(margin == 0 ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL);
				return;
			}
			if (attr == R.styleable.Recycler_horizontalSpacing) {
				int horizontalSpacing = a.getDimensionPixelSize(attr, 0);
				instance.setHorizontalSpacing(horizontalSpacing);
				return;
			}
			if (attr == R.styleable.Recycler_verticalSpacing) {
				int verticalSpacing = a.getDimensionPixelSize(attr, 0);
				instance.setVerticalSpacing(verticalSpacing);
				return;
			}
			if (attr == R.styleable.Recycler_landscapeColumnsCount) {
				int columnsCount = a.getInt(attr, 1);
				instance.setLandscapeColumnsCount(columnsCount);
				return;
			}
			if (attr == R.styleable.Recycler_portraitColumnsCount) {
				int columnsCount = a.getInt(attr, 1);
				instance.setPortraitColumnsCount(columnsCount);
			}
		}

		@Override
		protected void onSettingCompleted(CyborgRecycler instance) {
			instance.invalidateLayoutManager();
		}
	}
}
