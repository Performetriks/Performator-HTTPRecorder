package com.performetriks.performator.httprecorder.injection;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;

/***************************************************************************
 * 
 * Copyright Owner: Performetriks GmbH, Switzerland
 * License: MIT License
 * 
 * @author Reto Scheiwiller
 * 
 ***************************************************************************/
public class InjectionAgent {

	/*****************************************************************************
	 * 
	 *****************************************************************************/
	public static void agentmain(String args, Instrumentation instr) {
		InjectionAgent.log("INFO", "execute agentmain()...");
		
		premain(args, instr);
	}
	
	


	/*****************************************************************************
	 * 
	 *****************************************************************************/
	public static void premain(String args, Instrumentation instr) {
			
		InjectionAgent.log("INFO", "Add Bytecode Transformer");
		//redefineGuavaHostAndPort(instr);
		instr.addTransformer(new BytecodeTransformer());
	}

//	/*****************************************************************************
//	 * 
//	 *****************************************************************************/
//	private static void redefineGuavaHostAndPort(Instrumentation instr) {
//		
//		InjectionAgent.log("INFO", "Add method HostAndPort.getHostText();");
//		
//		String className = "com.google.common.net.HostAndPort";
//		
//		try {
//	        ClassPool pool = ClassPool.getDefault();
//	        pool.insertClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
//	        // IMPORTANT: class must already exist in JVM
//	        Class<?> loadedClass = Class.forName(className);
//	
//	        CtClass transformedClass = pool.get(className);
//	
//			//----------------------------------
//			// Create Method
//			CtMethod methodGetHostText = CtNewMethod.make(
//		            "public String getHostText(String arg) { return getHost(); }",
//		            transformedClass
//		        );
//	
//			transformedClass.addMethod(methodGetHostText);
//
//	        // convert to bytecode
//	        byte[] byteCode = transformedClass.toBytecode();
//	
//	        // redefine class
//	        ClassDefinition def = new ClassDefinition(loadedClass, byteCode);
//	        instr.redefineClasses(def);
//	
//	        transformedClass.detach();
//		} catch (Exception e) {
//			InjectionAgent.log("ERROR", "Error while transforming class: "+className, e);
//			
//		}
//	}


	/*****************************************************************************
	 * 
	 *****************************************************************************/
	public static void log(String level, String message) {
		System.out.println("["+level+"] InjectionAgent: "+message);
	}
	
	/*****************************************************************************
	 * 
	 *****************************************************************************/
	public static void log(String level, String message, Throwable e) {
		
		StringBuffer errorBuffer = new StringBuffer(e.toString());
		
		for(StackTraceElement s : e.getStackTrace()) {
			errorBuffer.append("\n"+s.toString());
		}
		
		message += errorBuffer.toString();
		
		InjectionAgent.log(level, message);
	}

}
