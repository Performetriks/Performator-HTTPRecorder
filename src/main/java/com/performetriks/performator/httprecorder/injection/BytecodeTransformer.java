package com.performetriks.performator.httprecorder.injection;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

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
public class BytecodeTransformer implements ClassFileTransformer {
	
	/*******************************************************************************
	 * 
	 *******************************************************************************/
	@SuppressWarnings("rawtypes")
	public byte[] transform(ClassLoader loader, String className,
			Class classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		
		byte[] byteCode = classfileBuffer;

		//InjectionAgent.log("[INFO] "+className);
		
		System.out.print("x");
		if(className.contains("HttpUtil")
		|| className.contains("Host")) {
			InjectionAgent.log("DEBUG", className);
		}
		
		if (className.equals("net/lightbody/bmp/util/HttpUtil")) {
			
			byteCode = adjustGuavaHostAndPort(loader, className, classfileBuffer, byteCode); 
			return byteCode;
		}

		return byteCode;

	}
	
	/*******************************************************************************
	 * 
	 *******************************************************************************/
	private byte[] adjustGuavaHostAndPort(ClassLoader loader, String className, byte[] classfileBuffer, byte[] byteCode) {
		
		try {
			
			InjectionAgent.log("INFO", "Inject Bytecode into class: "+className);
			
			//----------------------------------
			// Load Class
			ClassPool pool = ClassPool.getDefault();
			pool.insertClassPath(new LoaderClassPath(loader));
			
			CtClass transformedClass = pool.makeClass(new ByteArrayInputStream(classfileBuffer));

			//----------------------------------
			// Debug: Print list of all Methods
			//for(CtMethod method : transformedClass.getDeclaredMethods() ) {
			//	InjectionAgent.log("DEBUG", method.getName());
			//}
			
			//----------------------------------
			// Transform Method: logResponse
			CtMethod methodParseHostHeader = transformedClass.getDeclaredMethod("parseHostHeader");
			
			methodParseHostHeader.setBody("""
					{
						java.util.List hosts = $1.headers().getAll(io.netty.handler.codec.http.HttpHeaders.Names.HOST);
						
				        if (!hosts.isEmpty()) {
				            String hostAndPort = (String) hosts.get(0);
				
				            if ($2) {
				                return hostAndPort;
				            } else {
				                com.google.common.net.HostAndPort parsedHostAndPort = com.google.common.net.HostAndPort.fromString(hostAndPort);
				                return parsedHostAndPort.getHost();
				            }
				        } else {
				            return null;
				        }
			        }
					""");

			//----------------------------------
			// Detach
			byteCode = transformedClass.toBytecode();
			transformedClass.detach();
			
		} catch (Exception e) {
			InjectionAgent.log("ERROR", "Error while transforming class: "+className, e);
			
		}
		
		InjectionAgent.log("INFO", "End Instrumenting class: "+className);
		
		return byteCode;
	}

}
