package pw.dasbrain.jpexs.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public enum JPEXSTranformer implements ClassFileTransformer {
	
	INSTANCE;
	
	@Override
	public byte[] transform(Module module, ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		
		ClassReader cr = new ClassReader(classfileBuffer);
		ClassWriter cw = new ClassWriter(cr, 0);
		ClassVisitor cv = cw;
		
		switch (className) {
			case "com/jpexs/decompiler/flash/configuration/Configuration" -> {
				cv = new ConfigurationVisitor(new FieldModifierInterceptTransformer(cv));
			}
			case "com/jpexs/helpers/Helper" -> {
				cv = new FieldModifierInterceptTransformer(cv);
			}
		}
		
		if (cv == cw) {
			return null;
		}
		cr.accept(cv, 0);
		return cw.toByteArray();
	}
	
}
