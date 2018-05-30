package com.ubtrobot.speech;

import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class IntentAnnotationsLoader {

    private static final HashMap<Class, Map<String, Method>> sClazzIntentMethodsMap =
            new HashMap<>();

    private IntentAnnotationsLoader() {
    }

    public static synchronized Map<String, Method> loadIntentMethods(Class clazz) {
        Map<String, Method> callMethods = sClazzIntentMethodsMap.get(clazz);
        if (callMethods != null) {
            return callMethods;
        }

        callMethods = new HashMap<>();
        sClazzIntentMethodsMap.put(clazz, callMethods);

        StringBuilder callMethodsError = new StringBuilder();
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            Intent annotation = method.getAnnotation(Intent.class);
            if (annotation == null) {
                continue;
            }

            if (TextUtils.isEmpty(annotation.name()) || !annotation.name().startsWith("/")) {
                callMethodsError.append("Method ");
                callMethodsError.append(method.getName());
                callMethodsError.append(
                        " 's @Call annotation has illegal path. Should NOT empty and start with "
                                + "\"/\"\n");
            }

            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1 /*&&
                    parameterTypes[0].equals(Request.class) &&
                    parameterTypes[1].equals(Responder.class)*/) {
                callMethods.put(annotation.name(), method);
                continue;
            }

            callMethodsError.append("Method ");
            callMethodsError.append(method.getName());
            callMethodsError.append(
                    " has unexpected parameter types. Should be (Request request, Responder "
                            + "responder)\n");
        }

        if (callMethodsError.length() > 0) {
            throw new IllegalStateException(callMethodsError.toString());
        }

        return callMethods;
    }
}
