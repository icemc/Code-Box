package com.wordpress.icemc.gsmcodes.utilities;

/**
 * Distinguish different kinds of app start
 * first start ever ({@link #FIRST_TIME})
 * First start in this version ({@link #FIRST_TIME_VERSION})
 * Normal app start ({@link #NORMAL})
 *
 * @author  iceMc
 * inspired by
 * @author williscool and schnatterer
 */
public enum AppStartStatus {
    FIRST_TIME, FIRST_TIME_VERSION, NORMAL
}