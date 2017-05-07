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

package com.nu.art.cyborg.modules;

import com.nu.art.software.core.generics.Processor;
import com.nu.art.cyborg.annotations.ModuleDescriptor;
import com.nu.art.cyborg.core.CyborgModule;
import com.nu.art.cyborg.core.modules.PreferencesModule;
import com.nu.art.cyborg.core.modules.PreferencesModule.IntegerPreference;

/**
 * Created by TacB0sS on 28-Feb 2017.
 */
@ModuleDescriptor(
		usesPermissions = {/*permission.READ_PHONE_STATE*/},
		dependencies = {AppDetailsModule.class})

public class UpgradeModule
		extends CyborgModule {

	public interface OnAppUpgradedListener {

		void onApplicationUpgraded(int previousVersion, int newVersion);
	}

	private IntegerPreference appVersionCode;

	@Override
	protected void init() {
		PreferencesModule preferences = getModule(PreferencesModule.class);
		appVersionCode = preferences.new IntegerPreference("appVersionCode", 0);
		int versionCode = cyborg.getVersionCode();
		int previousVersionCode = this.appVersionCode.get();

		if (versionCode != previousVersionCode) {
			this.appVersionCode.set(versionCode);
			dispatchOnAppUpgraded(previousVersionCode, versionCode);
		}
	}

	private void dispatchOnAppUpgraded(final int previousVersionCode, final int versionCode) {
		dispatchModuleEvent("Application upgraded: " + previousVersionCode + " ==> " + versionCode, OnAppUpgradedListener.class, new Processor<OnAppUpgradedListener>() {

			@Override
			public void process(OnAppUpgradedListener listener) {
				listener.onApplicationUpgraded(previousVersionCode, versionCode);
			}
		});
	}
}
