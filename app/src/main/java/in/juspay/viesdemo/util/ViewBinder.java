package in.juspay.viesdemo.util;

import android.app.Activity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

public class ViewBinder {
    private static void bindAll(Activity context, Object object) {
        Field[] fields = object.getClass().getDeclaredFields();

        for (Field field : fields) {
            Binding binding = field.getAnnotation(Binding.class);

            if (binding == null) {
                continue;
            }

            String name = binding.value();

            if (name.equals("[unassigned]")) {
                name = field.getName();
            }

            int resId = context.getResources().getIdentifier(name, "id", context.getPackageName());
            Object view = context.findViewById(resId);

            try {
                field.setAccessible(true);
                field.set(object, view);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static void bindAll(Activity context) {
        bindAll(context, context);
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Binding {
        String value() default "[unassigned]";
    }
}
