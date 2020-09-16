package io.jenkins.plugins.http;

import com.google.gson.internal.$Gson$Types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReflectUtil {
    /**
     * 根据指定对象获取其泛型的实际类型
     * 返回的类型为{@link java.io.Serializable}。
     *
     * @param obj
     * @return
     * @see {@link $Gson$Types#canonicalize(Type)}
     */
    public static Type getSuperclassTypeParameter(Object obj) {
        return getSuperclassTypeParameter(obj.getClass());
    }

    /**
     * 根据指定类型获取其泛型的实际类型
     * 返回的类型为{@link java.io.Serializable}。
     *
     * @param subclass
     * @return
     * @see {@link $Gson$Types#canonicalize(Type)}
     */
    public static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        return getParameterUpperBound(0, (ParameterizedType) superclass);
    }

    /**
     * 根据传入的参数化类型获取实际类型
     * 返回在功能上相等但不一定相等的类型。
     * 返回的类型为{@link java.io.Serializable}。
     *
     * @param index
     * @param type
     * @return
     * @see {@link $Gson$Types#canonicalize(Type)}
     */
    public static Type getParameterUpperBound(int index, ParameterizedType type) {
        Type[] types = type.getActualTypeArguments();
        if (index < 0 || index >= types.length) {
            throw new IllegalArgumentException(
                    "Index " + index + " not in range [0," + types.length + ") for " + type);
        }
        return $Gson$Types.canonicalize(types[index]);
    }
}
