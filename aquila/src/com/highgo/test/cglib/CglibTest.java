package com.highgo.test.cglib;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Cglib������
 * 
 * @author cuiran
 * @version 1.0
 */
public class CglibTest {
	public static void main(String[] args) throws ClassNotFoundException {
		
		
		
		Class.forName("java.lang.Byte");
		
		
		// �������Ա����
		HashMap<String, Class<?>> propertyMap = new HashMap<>();
		propertyMap.put("id", Class.forName("java.lang.Integer"));
		propertyMap.put("name", Class.forName("java.lang.String"));
		propertyMap.put("address", Class.forName("java.lang.String"));
		
		// ���ɶ�̬ Bean
		CglibBeanUtil bean = new CglibBeanUtil(propertyMap);
		
		// �� Bean ����ֵ
		bean.setValue("id", new Integer(123));
		bean.setValue("name", "454");
		bean.setValue("address", "789");
		
		// �� Bean �л�ȡֵ����Ȼ�˻��ֵ�������� Object
		System.out.println("  >> id      = " + bean.getValue("id"));
		System.out.println("  >> name    = " + bean.getValue("name"));
		System.out.println("  >> address = " + bean.getValue("address"));
		
		
		System.out.println(bean.getClass());
		
		// ���bean��ʵ��
		Object object = bean.getObject();
		
		// ͨ������鿴���з�����
		Class<?> clazz = object.getClass();
		Method[] methods = clazz.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			System.out.println(methods[i].getName());
		}
	}
}