# time-monitor
javaagent tool implements by javassit,print the execution time of a method on the run time

运行时监控方法执行时间.  
使用方式  java -javaagent:time-monitor.jar=[监控类的关键词] -jar [执行的jar包].jar


#实现方式
1.使用java 的 premain  
2.使用javassist执行Class的字节码替换,即运行时修改了类的实现,植入方法的运行时间监控逻辑.
