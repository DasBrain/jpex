package pw.dasbrain.jpexs.agent;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import static java.lang.invoke.MethodType.methodType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldModifierIntercept {
	
	public static int modifiers = 0;
	
	public static final Field MODIFIER_FIELD_SURROGATE;
	
	public static final MethodHandle SHOULD_INTERCEPT_GET_FIELD;
	public static final MethodHandle MODIFIER_FIELD_SURROGGATE_CONSTANT;
	
	public static final MethodHandle SHOULD_INTERCEPT_FIELD_SET;
	public static final MethodHandle INTERCEPT_FIELD_SET;
	public static final MethodHandle INTERCEPT_FIELD_SET_INT;
	
	static {
		try {
			MODIFIER_FIELD_SURROGATE = FieldModifierIntercept.class.getDeclaredField("modifiers");
			MODIFIER_FIELD_SURROGATE.setAccessible(true);
			
			Lookup local = MethodHandles.lookup();
			
			SHOULD_INTERCEPT_GET_FIELD = local.findStatic(FieldModifierIntercept.class,
					"shouldInterceptGetField",
					methodType(boolean.class, Class.class, String.class));
			
			MODIFIER_FIELD_SURROGGATE_CONSTANT = MethodHandles.dropArguments(
					MethodHandles.constant(Field.class, MODIFIER_FIELD_SURROGATE),
					0, Class.class, String.class);
			
			SHOULD_INTERCEPT_FIELD_SET = local.findStatic(FieldModifierIntercept.class, "shouldInterceptFieldSet",
					methodType(boolean.class, Field.class, Object.class));
			INTERCEPT_FIELD_SET = local.findStatic(FieldModifierIntercept.class, "interceptFieldSet",
					methodType(void.class, Field.class, Object.class, Object.class));
			INTERCEPT_FIELD_SET_INT = local.findStatic(FieldModifierIntercept.class, "interceptFieldSetInt",
					methodType(void.class, Field.class, Object.class, int.class));
			
		} catch (ReflectiveOperationException roe) {
			throw new Error(roe);
		}
	}
	public static CallSite bootstrap(Lookup l, String name, MethodType mt, MethodHandle original) throws Throwable {
		switch (name) {
			case "getDeclaredField" -> {
				var cond = MethodHandles.guardWithTest(SHOULD_INTERCEPT_GET_FIELD, MODIFIER_FIELD_SURROGGATE_CONSTANT, original);
				return new ConstantCallSite(cond);
			}
			case "set" -> {
				var cond = MethodHandles.guardWithTest(SHOULD_INTERCEPT_FIELD_SET, INTERCEPT_FIELD_SET, original);
				return new ConstantCallSite(cond);
			}
			case "setInt" -> {
				var cond = MethodHandles.guardWithTest(SHOULD_INTERCEPT_FIELD_SET, INTERCEPT_FIELD_SET_INT, original);
				return new ConstantCallSite(cond);
			}
		}
		return null;
	}
	
	@SuppressWarnings("unused")
	private static boolean shouldInterceptGetField(Class<?> target, String name) {
		return target == Field.class && name.equals("modifiers");
	}
	
	@SuppressWarnings("unused")
	private static boolean shouldInterceptFieldSet(Field f, Object target) {
		return f == MODIFIER_FIELD_SURROGATE || Modifier.isPrivate(f.getModifiers());
	}
	
	@SuppressWarnings("unused")
	private static void interceptFieldSet(Field f, Object target, Object value) throws IllegalArgumentException, ReflectiveOperationException {
		if (f == MODIFIER_FIELD_SURROGATE) {
			System.out.println(target);
			return;
		}
		f.setAccessible(true);
		f.set(target, value);
	}
	
	@SuppressWarnings("unused")
	private static void interceptFieldSetInt(Field f, Object target, int value) {
		if (f == MODIFIER_FIELD_SURROGATE) {
			System.out.println(target);
		}
	}
}
