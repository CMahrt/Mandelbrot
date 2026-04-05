package de.cm.mandelproto;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;

public final class I18n {

    private static ResourceBundle bundle = ResourceBundle.getBundle("uiStrings", Locale.getDefault(), I18n.class.getClassLoader());
    private static final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    private I18n() {}

    public static String get(String key) {
        return bundle.getString(key);
    }

    public static void addListener(Runnable l) {
        listeners.add(l);
    }

    public static void removeListener(Runnable l) {
        listeners.remove(l);
    }

    public static void setLocale(Locale locale) {
        ResourceBundle.clearCache(I18n.class.getClassLoader());
        bundle = ResourceBundle.getBundle("uiStrings", locale, I18n.class.getClassLoader());
        listeners.forEach(Runnable::run);
    }
}
