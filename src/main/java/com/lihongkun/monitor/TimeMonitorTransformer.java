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
 * �෽�����ֽ����滻
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
			//ʹ��ȫ��,����ȡ���ֽ�����
			ctClass = ClassPool.getDefault().get(className);
			
			if(Objects.equals(classNameKeyword, EMPTY)||(!Objects.equals(classNameKeyword, EMPTY)&&className.indexOf(classNameKeyword)!=-1)){
				//���з���
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
		CtMethod newMethod = CtNewMethod.copy(ctMethod,methodName, ctClass, null);//�����µķ���������ԭ���ķ��� ������Ϊԭ��������
		
		StringBuilder bodyStr = new StringBuilder();
		bodyStr.append("{");
		
		//��������
		CtClass returnType = ctMethod.getReturnType();
		
		//�Ƿ���Ҫ����
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

		newMethod.setBody(bodyStr.toString());//�滻�·���   
		ctClass.addMethod(newMethod);//�����·���   
	}

}
