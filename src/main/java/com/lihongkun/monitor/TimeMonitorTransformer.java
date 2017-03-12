package com.lihongkun.monitor;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.Objects;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

/**
 * 类方法的字节码替换
 * @author Allen Lee
 * @date 2017-03-11
 */
public class TimeMonitorTransformer implements ClassFileTransformer {

	private static final String START_TIME = "\nlong startTime = System.currentTimeMillis();\n";
	private static final String END_TIME = "\nlong endTime = System.currentTimeMillis();\n";
	private static final String METHOD_RUTURN_VALUE_VAR = "__lihongkun_monitor_result";
	private static final String EMPTY = "";
	
	private String classNameKeyword;
	
	public TimeMonitorTransformer(String classNameKeyword){
		this.classNameKeyword = classNameKeyword;
	}

	public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		
		className = className.replace("/", ".");
		CtClass ctClass = null;
		try {
			//使用全称,用于取得字节码类
			ctClass = ClassPool.getDefault().get(className);
			
			if(Objects.equals(classNameKeyword, EMPTY)||(!Objects.equals(classNameKeyword, EMPTY)&&className.indexOf(classNameKeyword)!=-1)){
				//所有方法
				CtMethod[] ctMethods = ctClass.getDeclaredMethods();
				
				for(CtMethod ctMethod:ctMethods){
					transformMethod(ctMethod, ctClass);   
				}
			}
			
			return ctClass.toBytecode();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void transformMethod(CtMethod ctMethod,CtClass ctClass) throws Exception{
		
		if((ctMethod.getModifiers()&Modifier.ABSTRACT)>0){
			return;
		}
		
		String methodName = ctMethod.getName();
		String monitorStr = "\nSystem.out.println(\"method " + ctMethod.getLongName() + " cost:\" +(endTime - startTime) +\"ms.\");";
		String newMethodName = methodName + "$impl";
		
		ctMethod.setName(newMethodName);
		CtMethod newMethod = CtNewMethod.copy(ctMethod,methodName, ctClass, null);//创建新的方法，复制原来的方法 ，名字为原来的名字
		
		StringBuilder bodyStr = new StringBuilder();
		bodyStr.append("{");
		
		//返回类型
		CtClass returnType = ctMethod.getReturnType();
		
		//是否需要返回
		boolean hasReturnValue = (CtClass.voidType != returnType);
		
		if (hasReturnValue) {
            String returnClass = returnType.getName();
            bodyStr.append("\n").append(returnClass + " " + METHOD_RUTURN_VALUE_VAR + ";");
        }
		
		
		bodyStr.append(START_TIME);
		
		if (hasReturnValue) {
			bodyStr.append("\n").append(METHOD_RUTURN_VALUE_VAR + " = ($r)" + newMethodName + "($$);");
        } else {
        	bodyStr.append("\n").append(newMethodName + "($$);");
        }
		
		bodyStr.append(END_TIME);
		bodyStr.append(monitorStr);
		
		if (hasReturnValue) {
			bodyStr.append("\n").append("return " + METHOD_RUTURN_VALUE_VAR+" ;");
        }
		
		bodyStr.append("}");

		newMethod.setBody(bodyStr.toString());//替换新方法   
		ctClass.addMethod(newMethod);//增加新方法   
	}

}
