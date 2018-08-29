/*
 * Copyright 2000-2017 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.demo.dashboard;

import com.vaadin.demo.dashboard.data.DataProvider;
import com.vaadin.demo.dashboard.data.dummy.DummyDataProvider;
import com.vaadin.demo.dashboard.event.DashboardEventBus;
import com.vaadin.mpr.MprUI;
import com.vaadin.server.VaadinRequest;

public class DashUI extends MprUI {
    private final DataProvider dataProvider = new DummyDataProvider();
    private final DashboardEventBus dashboardEventbus = new DashboardEventBus();

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        super.init(vaadinRequest);

    }

    public static DataProvider getDataProvider() {
        return ((DashUI) getCurrent()).dataProvider;

    }

    public static DashboardEventBus getDashboardEventbus() {
        return ((DashUI) getCurrent()).dashboardEventbus;
    }
}
