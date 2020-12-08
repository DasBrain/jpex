package pw.dasbrain.jpexs.agent;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;

public class FieldModifierInterceptTransformer extends ClassVisitor {
	
	public FieldModifierInterceptTransformer(ClassVisitor parent) {
		super(ASM8, parent);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor,
			String signature, String[] exceptions) {
		var parent = super.visitMethod(access, name, descriptor, signature, exceptions);
		return new FieldModifierMethodVisitor(parent);
	}
	
}

class FieldModifierMethodVisitor extends MethodVisitor {
	
	private static final Handle BOOTSTRAP = new Handle(H_INVOKESTATIC,
			"pw/dasbrain/jpexs/agent/FieldModifierIntercept", "bootstrap",
			MethodType.methodType(CallSite.class, Lookup.class, String.class,
					MethodType.class, MethodHandle.class).descriptorString(),
			false);
	
	public FieldModifierMethodVisitor(MethodVisitor parent) {
		super(ASM8, parent);
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String descriptor,
			boolean isInterface) {
		boolean replace = false;
		switch (opcode) {
			case INVOKEVIRTUAL -> {
				switch (owner) {
					case "java/lang/Class" -> {
						if (name.equals("getDeclaredField")) {
							replace = true;
						}
					}
					case ("java/lang/reflect/Field") -> {
						switch (name) {
							case "set", "setInt" -> {
								replace = true;
							}
						}
					}
					
				}
			}
		}
		if (replace) {
			var hndl = new Handle(H_INVOKEVIRTUAL, owner, name, descriptor, isInterface);
			var newDesc = MethodTypeDesc.ofDescriptor(descriptor)
					.insertParameterTypes(0, ClassDesc.ofDescriptor("L" + owner + ";"))
					.descriptorString();
			super.visitInvokeDynamicInsn(name, newDesc, BOOTSTRAP, hndl);
			return;
		}
		super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
	}
}