package com.originos.biometric.imefix;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class CredentialGuard {
    private CredentialGuard() {}

    public static boolean isAlreadyAttached(Object authContainer) {
        if (authContainer == null) {
            return false;
        }
        try {
            Field f = findField(authContainer.getClass(), "mCredentialView");
            if (f == null) {
                return false;
            }
            f.setAccessible(true);
            Object view = f.get(authContainer);
            if (view == null) {
                return false;
            }
            Method m = view.getClass().getMethod("getParent");
            m.setAccessible(true);
            Object parent = m.invoke(view);
            return parent != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Field findField(Class<?> cls, String name) {
        for (Class<?> cur = cls; cur != null; cur = cur.getSuperclass()) {
            try {
                return cur.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }
}
