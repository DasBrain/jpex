package pw.dasbrain.jpexs.agent;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarFile;

public class Agent {
	public static void agentmain(String arg, Instrumentation inst) throws Throwable {
		premain(arg, inst);
	}
	
	public static void premain(String arg, Instrumentation inst) throws Throwable {
		// The asm jar is located relative to the agent jar in asm-8.0.1/asm-8.0.1.jar
		var jarConn = (JarURLConnection) Agent.class.getResource("Agent.class").openConnection();
		var asmFile = new File(new URL(jarConn.getJarFileURL(), "asm-8.0.1/asm-8.0.1.jar").toURI());
		try (var jf = new JarFile(asmFile)) {
			inst.appendToSystemClassLoaderSearch(jf);
		};
		
		inst.addTransformer(JPEXSTranformer.INSTANCE);
	}
}
