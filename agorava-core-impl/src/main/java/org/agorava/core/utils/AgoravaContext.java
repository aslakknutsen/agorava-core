/*
 * Copyright 2012 Agorava
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.agorava.core.utils;

/**
 * Class containing configuration for Agorava.
 * Static field here are stored here from third parties helper classes.
 *
 * @author Antoine Sabot-Durand
 * @see org.agorava.core.web.CaptureAbsolutePathFilter
 */
public class AgoravaContext {

    /**
     * The complete Web Context path (protocol, server, application context) of this Agorava Instance
     */
    public static String webAbsolutePath = "undefined";

    private static boolean web = false;


    public static boolean isWeb() {
        return web;
    }

    public static void setWeb(boolean web) {
        AgoravaContext.web = web;
    }
}
