package com.lihongkun.monitor;

import java.lang.instrument.Instrumentation;

/**
 * Agent 类
 * 
 * 使用方法:运行需要分析的类,
 * 
 * java -javaagent:monitor.jar=classNameKeyword -jar XXX.jar
 * 
 * @author Allen Lee
 * @date 2017-03-11
 */
public class TimeMonitorAgent {
	public static void premain(String agentArgs, Instrumentation inst) {
		inst.addTransformer(new TimeMonitorTransformer(agentArgs));
	}
}
