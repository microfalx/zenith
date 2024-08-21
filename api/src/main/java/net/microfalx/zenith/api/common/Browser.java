package net.microfalx.zenith.api.common;

import net.microfalx.lang.EnumUtils;

/**
 * An enum which identifies the browser.
 */
public enum Browser {

    /**
     * The Mozilla Firefox browser (https://www.mozilla.org/en-US/firefox/).
     */
    FIREFOX,

    /**
     * The Google Chrome browser (https://www.google.com/chrome/).
     */
    CHROME,

    /**
     * An unknown type of browser.
     */
    OTHER;

    /**
     * Returns a browser based on its name.
     *
     * @param name the name of the browser
     * @return a non-null enum
     */
    public static Browser from(String name) {
        return EnumUtils.fromName(Browser.class, name, Browser.OTHER);
    }
}
