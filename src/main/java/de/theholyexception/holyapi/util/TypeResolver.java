package de.theholyexception.holyapi.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import sun.misc.Unsafe;

public final class TypeResolver {
    private static final Map<Class<?>, Reference<Map<TypeVariable<?>, Type>>> TYPE_VARIABLE_CACHE = Collections.synchronizedMap(new WeakHashMap());
    private static volatile boolean CACHE_ENABLED = true;
    private static boolean RESOLVES_LAMBDAS;
    private static Object JAVA_LANG_ACCESS;
    private static Method GET_CONSTANT_POOL;
    private static Method GET_CONSTANT_POOL_SIZE;
    private static Method GET_CONSTANT_POOL_METHOD_AT;
    private static final Map<String, Method> OBJECT_METHODS = new HashMap();
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS;
    private static final Double JAVA_VERSION = Double.parseDouble(System.getProperty("java.specification.version", "0"));

    private TypeResolver() {
    }

    public static void enableCache() {
        CACHE_ENABLED = true;
    }

    public static void disableCache() {
        TYPE_VARIABLE_CACHE.clear();
        CACHE_ENABLED = false;
    }

    /**
     * Resolves a raw argument from a generic type using the given subtype as context.
     *
     * @param type the generic type to resolve the raw argument from
     * @param subType the subtype to use as context for resolving the raw argument
     * @param <T> type parameter for the generic type
     * @param <S> type parameter for the subtype
     * @return the raw argument as a Class object, or Unknown.class if resolution fails
     * @throws IllegalArgumentException if resolution fails or if there are multiple arguments
     */
    public static <T, S extends T> Class<?> resolveRawArgument(Class<T> type, Class<S> subType) {
        return resolveRawArgument(resolveGenericType(type, subType), subType);
    }

    /**
     * Resolves a raw argument from a generic type.
     *
     * @param genericType the generic type to resolve the raw argument from
     * @param subType the subtype to use as context for resolving the raw argument
     * @return the raw argument as a Class object, or Unknown.class if resolution fails
     * @throws IllegalArgumentException if resolution fails or if there are multiple arguments
     */
    public static Class<?> resolveRawArgument(Type genericType, Class<?> subType) {
        Class<?>[] arguments = resolveRawArguments(genericType, subType);
        if (arguments == null) {
            return Unknown.class;
        } else if (arguments.length != 1) {
            throw new IllegalArgumentException("Expected 1 argument for generic type " + genericType + " but found " + arguments.length);
        } else {
            return arguments[0];
        }
    }

    public static <T, S extends T> Class<?>[] resolveRawArguments(Class<T> type, Class<S> subType) {
        return resolveRawArguments(resolveGenericType(type, subType), subType);
    }

    public static <T, S extends T> Type reify(Class<T> type, Class<S> context) {
        return reify(resolveGenericType(type, context), getTypeVariableMap(context, (Class)null));
    }

    public static Type reify(Type type, Class<?> context) {
        return reify(type, getTypeVariableMap(context, (Class)null));
    }

    public static Type reify(Type type) {
        return reify((Type)type, (Map)(new HashMap(0)));
    }

    /**
     * Resolves the raw arguments from a generic type, given a subtype as context.
     *
     * @param genericType the generic type to resolve the raw arguments from
     * @param subType the subtype to use as context for resolving the raw arguments
     * @return the raw arguments as a Class[] array, or null if resolution fails
     * @throws IllegalArgumentException if resolution fails or if there are multiple arguments
     */
    public static Class<?>[] resolveRawArguments(Type genericType, Class<?> subType) {
        Class<?>[] result = null;
        Class<?> functionalInterface = null;
        if (RESOLVES_LAMBDAS && subType.isSynthetic()) {
            Class<?> fi = genericType instanceof ParameterizedType && ((ParameterizedType)genericType).getRawType() instanceof Class ? (Class)((ParameterizedType)genericType).getRawType() : (genericType instanceof Class ? (Class)genericType : null);
            if (fi != null && fi.isInterface()) {
                functionalInterface = fi;
            }
        }

        if (genericType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType)genericType;
            Type[] arguments = paramType.getActualTypeArguments();
            result = new Class[arguments.length];

            for(int i = 0; i < arguments.length; ++i) {
                result[i] = resolveRawClass(arguments[i], subType, functionalInterface);
            }
        } else if (genericType instanceof TypeVariable) {
            result = new Class[]{resolveRawClass(genericType, subType, functionalInterface)};
        } else if (genericType instanceof Class) {
            TypeVariable<?>[] typeParams = ((Class)genericType).getTypeParameters();
            result = new Class[typeParams.length];

            for(int i = 0; i < typeParams.length; ++i) {
                result[i] = resolveRawClass(typeParams[i], subType, functionalInterface);
            }
        }

        return result;
    }

    public static Type resolveGenericType(Class<?> type, Type subType) {
        Class rawType;
        if (subType instanceof ParameterizedType) {
            rawType = (Class)((ParameterizedType)subType).getRawType();
        } else {
            rawType = (Class)subType;
        }

        if (type.equals(rawType)) {
            return subType;
        } else {
            Type result;
            if (type.isInterface()) {
                Type[] var4 = rawType.getGenericInterfaces();
                int var5 = var4.length;

                for(int var6 = 0; var6 < var5; ++var6) {
                    Type superInterface = var4[var6];
                    if (superInterface != null && !superInterface.equals(Object.class) && (result = resolveGenericType(type, superInterface)) != null) {
                        return result;
                    }
                }
            }

            Type superClass = rawType.getGenericSuperclass();
            return superClass != null && !superClass.equals(Object.class) && (result = resolveGenericType(type, superClass)) != null ? result : null;
        }
    }

    public static Class<?> resolveRawClass(Type genericType, Class<?> subType) {
        return resolveRawClass(genericType, subType, (Class)null);
    }

    private static Class<?> resolveRawClass(Type genericType, Class<?> subType, Class<?> functionalInterface) {
        if (genericType instanceof Class) {
            return (Class)genericType;
        } else if (genericType instanceof ParameterizedType) {
            return resolveRawClass(((ParameterizedType)genericType).getRawType(), subType, functionalInterface);
        } else if (genericType instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType)genericType;
            Class<?> component = resolveRawClass(arrayType.getGenericComponentType(), subType, functionalInterface);
            return Array.newInstance(component, 0).getClass();
        } else {
            if (genericType instanceof TypeVariable) {
                TypeVariable<?> variable = (TypeVariable)genericType;
                genericType = (Type)getTypeVariableMap(subType, functionalInterface).get(variable);
                genericType = genericType == null ? resolveBound(variable) : resolveRawClass(genericType, subType, functionalInterface);
            }

            return genericType instanceof Class ? (Class)genericType : Unknown.class;
        }
    }

    private static Type reify(Type genericType, Map<TypeVariable<?>, Type> typeVariableTypeMap) {
        if (genericType == null) {
            return null;
        } else {
            return genericType instanceof Class ? genericType : reify(genericType, typeVariableTypeMap, new HashMap());
        }
    }

    private static Type reify(Type genericType, Map<TypeVariable<?>, Type> typeVariableMap, Map<ParameterizedType, ReifiedParameterizedType> partial) {
        if (genericType instanceof Class) {
            return genericType;
        } else {
            Type[] upperBounds;
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType)genericType;
                if (partial.containsKey(parameterizedType)) {
                    ReifiedParameterizedType res = (ReifiedParameterizedType)partial.get(genericType);
                    res.addReifiedTypeArgument(res);
                    return res;
                } else {
                    upperBounds = parameterizedType.getActualTypeArguments();
                    ReifiedParameterizedType result = new ReifiedParameterizedType(parameterizedType);
                    partial.put(parameterizedType, result);
                    Type[] var6 = upperBounds;
                    int var7 = upperBounds.length;

                    for(int var8 = 0; var8 < var7; ++var8) {
                        Type genericTypeArgument = var6[var8];
                        Type reified = reify(genericTypeArgument, typeVariableMap, partial);
                        if (reified != result) {
                            result.addReifiedTypeArgument(reified);
                        }
                    }

                    return result;
                }
            } else {
                Type mapping;
                if (genericType instanceof GenericArrayType) {
                    GenericArrayType genericArrayType = (GenericArrayType)genericType;
                    mapping = genericArrayType.getGenericComponentType();
                    Type reifiedComponentType = reify(genericArrayType.getGenericComponentType(), typeVariableMap, partial);
                    if (mapping == reifiedComponentType) {
                        return mapping;
                    } else if (reifiedComponentType instanceof Class) {
                        return Array.newInstance((Class)reifiedComponentType, 0).getClass();
                    } else {
                        throw new UnsupportedOperationException("Attempted to reify generic array type, whose generic component type could not be reified to some Class<?>. Handling for this case is not implemented");
                    }
                } else if (genericType instanceof TypeVariable) {
                    TypeVariable<?> typeVariable = (TypeVariable)genericType;
                    mapping = (Type)typeVariableMap.get(typeVariable);
                    return mapping != null ? reify(mapping, typeVariableMap, partial) : reify(typeVariable.getBounds()[0], typeVariableMap, partial);
                } else if (genericType instanceof WildcardType) {
                    WildcardType wildcardType = (WildcardType)genericType;
                    upperBounds = wildcardType.getUpperBounds();
                    Type[] lowerBounds = wildcardType.getLowerBounds();
                    if (upperBounds.length == 1 && lowerBounds.length == 0) {
                        return reify(upperBounds[0], typeVariableMap, partial);
                    } else {
                        throw new UnsupportedOperationException("Attempted to reify wildcard type with name '" + wildcardType.getTypeName() + "' which has " + upperBounds.length + " upper bounds and " + lowerBounds.length + " lower bounds. Reification of wildcard types is only supported for the trivial case of exactly 1 upper bound and 0 lower bounds.");
                    }
                } else {
                    throw new UnsupportedOperationException("Reification of type with name '" + genericType.getTypeName() + "' and class name '" + genericType.getClass().getName() + "' is not implemented.");
                }
            }
        }
    }

    private static Map<TypeVariable<?>, Type> getTypeVariableMap(Class<?> targetType, Class<?> functionalInterface) {
        Reference<Map<TypeVariable<?>, Type>> ref = (Reference)TYPE_VARIABLE_CACHE.get(targetType);
        Map<TypeVariable<?>, Type> map = ref != null ? (Map)ref.get() : null;
        if (map == null) {
            map = new HashMap();
            if (functionalInterface != null) {
                populateLambdaArgs(functionalInterface, targetType, (Map)map);
            }

            populateSuperTypeArgs(targetType.getGenericInterfaces(), (Map)map, functionalInterface != null);
            Type genericType = targetType.getGenericSuperclass();

            Class type;
            for(type = targetType.getSuperclass(); type != null && !Object.class.equals(type); type = type.getSuperclass()) {
                if (genericType instanceof ParameterizedType) {
                    populateTypeArgs((ParameterizedType)genericType, (Map)map, false);
                }

                populateSuperTypeArgs(type.getGenericInterfaces(), (Map)map, false);
                genericType = type.getGenericSuperclass();
            }

            for(type = targetType; type.isMemberClass(); type = type.getEnclosingClass()) {
                genericType = type.getGenericSuperclass();
                if (genericType instanceof ParameterizedType) {
                    populateTypeArgs((ParameterizedType)genericType, (Map)map, functionalInterface != null);
                }
            }

            if (CACHE_ENABLED) {
                TYPE_VARIABLE_CACHE.put(targetType, new WeakReference(map));
            }
        }

        return (Map)map;
    }

    private static void populateSuperTypeArgs(Type[] types, Map<TypeVariable<?>, Type> map, boolean depthFirst) {
        Type[] var3 = types;
        int var4 = types.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Type type = var3[var5];
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType)type;
                if (!depthFirst) {
                    populateTypeArgs(parameterizedType, map, depthFirst);
                }

                Type rawType = parameterizedType.getRawType();
                if (rawType instanceof Class) {
                    populateSuperTypeArgs(((Class)rawType).getGenericInterfaces(), map, depthFirst);
                }

                if (depthFirst) {
                    populateTypeArgs(parameterizedType, map, depthFirst);
                }
            } else if (type instanceof Class) {
                populateSuperTypeArgs(((Class)type).getGenericInterfaces(), map, depthFirst);
            }
        }

    }

    private static void populateTypeArgs(ParameterizedType type, Map<TypeVariable<?>, Type> map, boolean depthFirst) {
        if (type.getRawType() instanceof Class) {
            TypeVariable<?>[] typeVariables = ((Class)type.getRawType()).getTypeParameters();
            Type[] typeArguments = type.getActualTypeArguments();
            if (type.getOwnerType() != null) {
                Type owner = type.getOwnerType();
                if (owner instanceof ParameterizedType) {
                    populateTypeArgs((ParameterizedType)owner, map, depthFirst);
                }
            }

            for(int i = 0; i < typeArguments.length; ++i) {
                TypeVariable<?> variable = typeVariables[i];
                Type typeArgument = typeArguments[i];
                if (typeArgument instanceof Class) {
                    map.put(variable, typeArgument);
                } else if (typeArgument instanceof GenericArrayType) {
                    map.put(variable, typeArgument);
                } else if (typeArgument instanceof ParameterizedType) {
                    map.put(variable, typeArgument);
                } else if (typeArgument instanceof TypeVariable) {
                    TypeVariable<?> typeVariableArgument = (TypeVariable)typeArgument;
                    Type resolvedType;
                    if (depthFirst) {
                        resolvedType = (Type)map.get(variable);
                        if (resolvedType != null) {
                            map.put(typeVariableArgument, resolvedType);
                            continue;
                        }
                    }

                    resolvedType = (Type)map.get(typeVariableArgument);
                    if (resolvedType == null) {
                        resolvedType = resolveBound(typeVariableArgument);
                    }

                    map.put(variable, resolvedType);
                }
            }
        }

    }

    public static Type resolveBound(TypeVariable<?> typeVariable) {
        Type[] bounds = typeVariable.getBounds();
        if (bounds.length == 0) {
            return Unknown.class;
        } else {
            Type bound = bounds[0];
            if (bound instanceof TypeVariable) {
                bound = resolveBound((TypeVariable)bound);
            }

            return (Type)(bound == Object.class ? Unknown.class : bound);
        }
    }

    private static void populateLambdaArgs(Class<?> functionalInterface, Class<?> lambdaType, Map<TypeVariable<?>, Type> map) {
        if (RESOLVES_LAMBDAS) {
            Method[] var3 = functionalInterface.getMethods();
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Method m = var3[var5];
                if (!isDefaultMethod(m) && !Modifier.isStatic(m.getModifiers()) && !m.isBridge()) {
                    Method objectMethod = (Method)OBJECT_METHODS.get(m.getName());
                    if (objectMethod == null || !Arrays.equals(m.getTypeParameters(), objectMethod.getTypeParameters())) {
                        Type returnTypeVar = m.getGenericReturnType();
                        Type[] paramTypeVars = m.getGenericParameterTypes();
                        Member member = getMemberRef(lambdaType);
                        if (member == null) {
                            return;
                        }

                        if (returnTypeVar instanceof TypeVariable) {
                            Class<?> returnType = member instanceof Method ? ((Method)member).getReturnType() : ((Constructor)member).getDeclaringClass();
                            returnType = wrapPrimitives(returnType);
                            if (!returnType.equals(Void.class)) {
                                map.put((TypeVariable)returnTypeVar, returnType);
                            }
                        }

                        Class<?>[] arguments = member instanceof Method ? ((Method)member).getParameterTypes() : ((Constructor)member).getParameterTypes();
                        int paramOffset = 0;
                        if (paramTypeVars.length > 0 && paramTypeVars[0] instanceof TypeVariable && paramTypeVars.length == arguments.length + 1) {
                            Class<?> instanceType = member.getDeclaringClass();
                            map.put((TypeVariable)paramTypeVars[0], instanceType);
                            paramOffset = 1;
                        }

                        int argOffset = 0;
                        if (paramTypeVars.length < arguments.length) {
                            argOffset = arguments.length - paramTypeVars.length;
                        }

                        for(int i = 0; i + argOffset < arguments.length; ++i) {
                            if (paramTypeVars[i] instanceof TypeVariable) {
                                map.put((TypeVariable)paramTypeVars[i + paramOffset], wrapPrimitives(arguments[i + argOffset]));
                            }
                        }

                        return;
                    }
                }
            }
        }

    }

    private static boolean isDefaultMethod(Method m) {
        return JAVA_VERSION >= 1.8 && m.isDefault();
    }

    private static Member getMemberRef(Class<?> type) {
        Object constantPool;
        try {
            constantPool = GET_CONSTANT_POOL.invoke(JAVA_LANG_ACCESS, type);
        } catch (Exception var5) {
            return null;
        }

        Member result = null;

        for(int i = getConstantPoolSize(constantPool) - 1; i >= 0; --i) {
            Member member = getConstantPoolMethodAt(constantPool, i);
            if (member != null && (!(member instanceof Constructor) || !member.getDeclaringClass().getName().equals("java.lang.invoke.SerializedLambda")) && !member.getDeclaringClass().isAssignableFrom(type)) {
                result = member;
                if (!(member instanceof Method) || !isAutoBoxingMethod((Method)member)) {
                    break;
                }
            }
        }

        return result;
    }

    private static boolean isAutoBoxingMethod(Method method) {
        Class<?>[] parameters = method.getParameterTypes();
        return method.getName().equals("valueOf") && parameters.length == 1 && parameters[0].isPrimitive() && wrapPrimitives(parameters[0]).equals(method.getDeclaringClass());
    }

    private static Class<?> wrapPrimitives(Class<?> clazz) {
        return clazz.isPrimitive() ? (Class)PRIMITIVE_WRAPPERS.get(clazz) : clazz;
    }

    private static int getConstantPoolSize(Object constantPool) {
        try {
            return (Integer)GET_CONSTANT_POOL_SIZE.invoke(constantPool);
        } catch (Exception var2) {
            return 0;
        }
    }

    private static Member getConstantPoolMethodAt(Object constantPool, int i) {
        try {
            return (Member)GET_CONSTANT_POOL_METHOD_AT.invoke(constantPool, i);
        } catch (Exception var3) {
            return null;
        }
    }

    static {
        try {
            final Unsafe unsafe = (Unsafe)AccessController.doPrivileged(new PrivilegedExceptionAction<Unsafe>() {
                public Unsafe run() throws Exception {
                    Field f = Unsafe.class.getDeclaredField("theUnsafe");
                    f.setAccessible(true);
                    return (Unsafe)f.get((Object)null);
                }
            });
            Class sharedSecretsClass;
            AccessMaker accessSetter;
            Object lookupStaticFieldBase;
            if (JAVA_VERSION < 9.0) {
                sharedSecretsClass = Class.forName("sun.misc.SharedSecrets");
                accessSetter = new AccessMaker() {
                    public void makeAccessible(AccessibleObject accessibleObject) {
                        accessibleObject.setAccessible(true);
                    }
                };
            } else {
                Field overrideField;
                final long overrideFieldOffset;
                if (JAVA_VERSION < 12.0) {
                    try {
                        sharedSecretsClass = Class.forName("jdk.internal.misc.SharedSecrets");
                    } catch (ClassNotFoundException var11) {
                        sharedSecretsClass = Class.forName("jdk.internal.access.SharedSecrets");
                    }

                    overrideField = AccessibleObject.class.getDeclaredField("override");
                    overrideFieldOffset = unsafe.objectFieldOffset(overrideField);
                    accessSetter = new AccessMaker() {
                        public void makeAccessible(AccessibleObject accessibleObject) {
                            unsafe.putBoolean(accessibleObject, overrideFieldOffset, true);
                        }
                    };
                } else {
                    sharedSecretsClass = Class.forName("jdk.internal.access.SharedSecrets");
                    overrideField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
                    overrideFieldOffset = unsafe.staticFieldOffset(overrideField);
                    lookupStaticFieldBase = unsafe.staticFieldBase(overrideField);
                    MethodHandles.Lookup implLookup = (MethodHandles.Lookup)unsafe.getObject(lookupStaticFieldBase, overrideFieldOffset);
                    final MethodHandle overrideSetter = implLookup.findSetter(AccessibleObject.class, "override", Boolean.TYPE);
                    accessSetter = new AccessMaker() {
                        public void makeAccessible(AccessibleObject object) throws Throwable {
                            overrideSetter.invokeWithArguments(object, true);
                        }
                    };
                }
            }

            Method javaLangAccessGetter = sharedSecretsClass.getMethod("getJavaLangAccess");
            accessSetter.makeAccessible(javaLangAccessGetter);
            JAVA_LANG_ACCESS = javaLangAccessGetter.invoke((Object)null);
            GET_CONSTANT_POOL = JAVA_LANG_ACCESS.getClass().getMethod("getConstantPool", Class.class);
            String constantPoolName = JAVA_VERSION < 9.0 ? "sun.reflect.ConstantPool" : "jdk.internal.reflect.ConstantPool";
            Class<?> constantPoolClass = Class.forName(constantPoolName);
            GET_CONSTANT_POOL_SIZE = constantPoolClass.getDeclaredMethod("getSize");
            GET_CONSTANT_POOL_METHOD_AT = constantPoolClass.getDeclaredMethod("getMethodAt", Integer.TYPE);
            accessSetter.makeAccessible(GET_CONSTANT_POOL);
            accessSetter.makeAccessible(GET_CONSTANT_POOL_SIZE);
            accessSetter.makeAccessible(GET_CONSTANT_POOL_METHOD_AT);
            lookupStaticFieldBase = GET_CONSTANT_POOL.invoke(JAVA_LANG_ACCESS, Object.class);
            GET_CONSTANT_POOL_SIZE.invoke(lookupStaticFieldBase);
            Method[] var16 = Object.class.getDeclaredMethods();
            int var17 = var16.length;

            for(int var9 = 0; var9 < var17; ++var9) {
                Method method = var16[var9];
                OBJECT_METHODS.put(method.getName(), method);
            }

            RESOLVES_LAMBDAS = true;
        } catch (Throwable var12) {
        }

        Map<Class<?>, Class<?>> types = new HashMap();
        types.put(Boolean.TYPE, Boolean.class);
        types.put(Byte.TYPE, Byte.class);
        types.put(Character.TYPE, Character.class);
        types.put(Double.TYPE, Double.class);
        types.put(Float.TYPE, Float.class);
        types.put(Integer.TYPE, Integer.class);
        types.put(Long.TYPE, Long.class);
        types.put(Short.TYPE, Short.class);
        types.put(Void.TYPE, Void.class);
        PRIMITIVE_WRAPPERS = Collections.unmodifiableMap(types);
    }

    public static final class Unknown {
        private Unknown() {
        }
    }

    private interface AccessMaker {
        void makeAccessible(AccessibleObject var1) throws Throwable;
    }
}


class ReifiedParameterizedType implements ParameterizedType {
    private final ParameterizedType original;
    private final Type[] reifiedTypeArguments;
    private final boolean[] loop;
    private int reified = 0;

    ReifiedParameterizedType(ParameterizedType original) {
        this.original = original;
        this.reifiedTypeArguments = new Type[original.getActualTypeArguments().length];
        this.loop = new boolean[original.getActualTypeArguments().length];
    }

    void addReifiedTypeArgument(Type type) {
        if (this.reified < this.reifiedTypeArguments.length) {
            if (type == this) {
                this.loop[this.reified] = true;
            }

            this.reifiedTypeArguments[this.reified++] = type;
        }
    }

    public Type[] getActualTypeArguments() {
        return this.reifiedTypeArguments;
    }

    public Type getRawType() {
        return this.original.getRawType();
    }

    public Type getOwnerType() {
        return this.original.getOwnerType();
    }

    public String toString() {
        Type ownerType = this.getOwnerType();
        Type rawType = this.getRawType();
        Type[] actualTypeArguments = this.getActualTypeArguments();
        StringBuilder sb = new StringBuilder();
        if (ownerType != null) {
            if (ownerType instanceof Class) {
                sb.append(((Class)ownerType).getName());
            } else {
                sb.append(ownerType.toString());
            }

            sb.append("$");
            if (ownerType instanceof ParameterizedType) {
                sb.append(rawType.getTypeName().replace(((ParameterizedType)ownerType).getRawType().getTypeName() + "$", ""));
            } else if (rawType instanceof Class) {
                sb.append(((Class)rawType).getSimpleName());
            } else {
                sb.append(rawType.getTypeName());
            }
        } else {
            sb.append(rawType.getTypeName());
        }

        if (actualTypeArguments != null && actualTypeArguments.length > 0) {
            sb.append("<");

            for(int i = 0; i < actualTypeArguments.length; ++i) {
                if (i != 0) {
                    sb.append(", ");
                }

                Type t = actualTypeArguments[i];
                if (i >= this.reified) {
                    sb.append("?");
                } else if (t == null) {
                    sb.append("null");
                } else if (this.loop[i]) {
                    sb.append("...");
                } else {
                    sb.append(t.getTypeName());
                }
            }

            sb.append(">");
        }

        return sb.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            ReifiedParameterizedType that = (ReifiedParameterizedType)o;
            if (!this.original.equals(that.original)) {
                return false;
            } else if (this.reifiedTypeArguments.length != that.reifiedTypeArguments.length) {
                return false;
            } else {
                for(int i = 0; i < this.reifiedTypeArguments.length; ++i) {
                    if (this.loop[i] != that.loop[i]) {
                        return false;
                    }

                    if (!this.loop[i] && this.reifiedTypeArguments[i] != that.reifiedTypeArguments[i]) {
                        return false;
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.original.hashCode();

        for(int i = 0; i < this.reifiedTypeArguments.length; ++i) {
            if (!this.loop[i] && this.reifiedTypeArguments[i] instanceof ReifiedParameterizedType) {
                result = 31 * result + this.reifiedTypeArguments[i].hashCode();
            }
        }

        return result;
    }
}
