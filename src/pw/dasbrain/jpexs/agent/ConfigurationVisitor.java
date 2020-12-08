package pw.dasbrain.jpexs.agent;

import static org.objectweb.asm.Opcodes.*;

import java.lang.invoke.MethodType;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

public class ConfigurationVisitor extends ClassVisitor {
	
	public ConfigurationVisitor(ClassVisitor parent) {
		super(ASM8, parent);
	}
	
	@Override
	public FieldVisitor visitField(int access, String name, String descriptor,
			String signature, Object value) {
		return super.visitField(access & ~ACC_FINAL, name, descriptor, signature,
				value);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor,
			String signature, String[] exceptions) {
		var parent = super.visitMethod(access, name, descriptor, signature, exceptions);
		return new ConfigurationMethodVisistor(parent);
	}
	
}

class ConfigurationMethodVisistor extends MethodVisitor {
	public ConfigurationMethodVisistor(MethodVisitor parent) {
		super(ASM8, parent);
	}
	
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String descriptor,
			boolean isInterface) {
		switch (opcode) {
			case INVOKESPECIAL -> {
				if (owner.equals("java/lang/Error") && 
						descriptor.equals(MethodType.methodType(void.class, String.class)
								.toMethodDescriptorString())) {
					super.visitVarInsn(ALOAD, 0);
					super.visitMethodInsn(INVOKESPECIAL, "java/lang/Error", "<init>",
							"(Ljava/lang/String;Ljava/lang/Throwable;)V", false);
					return;
				}
			}
		}
		super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
	}
}
