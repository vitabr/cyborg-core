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

package com.nu.art.cyborg.core.dataModels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by TacB0sS on 22-Jun 2015.
 */
@SuppressWarnings("unchecked")
public class ListDataModel<Item>
	extends DataModel<Item> {

	private final Class<? extends Item>[] itemsType;

	private ArrayList<Item> items = new ArrayList<>();

	public ListDataModel(Class<? extends Item>... itemsType) {
		this.itemsType = itemsType;
	}

	public final int indexOf(Item item) {
		return items.indexOf(item);
	}

	public final void add(Item... items) {
		addAll(Arrays.asList(items));
	}

	public final void addAll(Collection<Item> items) {
		int size = this.items.size();
		this.items.addAll(items);
		if (adapter != null)
			adapter.onItemRangeInserted(size, items.size());
	}

	public final void removeItems(Item... items) {
		removeItems(Arrays.asList(items));
	}

	public final void removeItems(List<Item> items) {
		int position = -1;
		if (items.size() == 1)
			position = getPositionByItem(items.get(0));

		boolean removed = this.items.removeAll(items);
		if (position >= 0) {
			if (adapter != null)
				adapter.onItemRemoved(position);
			return;
		}

		if (!removed)
			return;

		// TODO: can add a calculation of minimal range..
		if (adapter != null)
			adapter.onDataSetChanged();
	}

	@Override
	public int getItemTypesCount() {
		return itemsType.length;
	}

	@Override
	public int getPositionForItem(Item item) {
		return items.indexOf(item);
	}

	@Override
	public int getItemTypeByPosition(int position) {
		Item item = getItemForPosition(position);
		if (item == null)
			return 0;

		return getItemTypeByItem(item);
	}

	protected int getItemTypeByItem(Item item) {
		for (int i = 0; i < itemsType.length; i++) {
			if (item.getClass() == itemsType[i])
				return i;
		}
		return 0;
	}

	@Override
	public Item getItemForPosition(int position) {
		return items.get(position % items.size());
	}

	@Override
	public int getRealItemsCount() {
		return items.size();
	}

	@Override
	public int getItemsCount() {
		return cyclic && items.size() > 0 ? Integer.MAX_VALUE : items.size();
	}

	@Override
	public void renderItem(Item item) {
		int position = getPositionByItem(item);
		if (position == -1)
			return;

		renderItemAtPosition(position);
	}

	private int getPositionByItem(Item item) {
		return items.indexOf(item);
	}

	@Override
	public void renderItemAtPosition(int position) {
		if (adapter != null)
			adapter.onItemAtPositionChanged(position);
	}

	public final void notifyDataSetChanged() {
		if (adapter != null)
			adapter.onDataSetChanged();
	}

	public final void setItems(Item... items) {
		this.items.clear();
		add(items);
	}

	public final void clear() {
		items.clear();
		notifyDataSetChanged();
	}
}
