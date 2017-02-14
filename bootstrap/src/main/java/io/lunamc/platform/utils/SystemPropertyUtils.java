/*
 *  Copyright 2017 LunaMC.io
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.lunamc.platform.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemPropertyUtils {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^\\s}]+)}");

    private SystemPropertyUtils() {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is a utility class and should not be constructed");
    }

    public static String replacePlaceholders(String str) {
        StringBuffer sb = new StringBuffer(str.length());
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(str);
        while (matcher.find()) {
            String value = System.getProperty(matcher.group(1));
            matcher.appendReplacement(sb, value != null ? Matcher.quoteReplacement(value) : "");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
