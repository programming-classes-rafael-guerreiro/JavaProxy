package ca.btc.proxy;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

public class Application {
	public static void main(String[] args) {
		final Optional<MyCustomInterface> instance = buildProxyInstance();

		final String returnedFromMethod1 = instance.get().someMethod1("Hello proxy.");
		System.out.println("returnedFromMethod1: \"" + returnedFromMethod1 + "\"\n\n");

		final Integer returnedFromMethod2 = instance.get().someMethod2(123456);
		System.out.println("returnedFromMethod2: " + returnedFromMethod2);
	}

	private static Optional<MyCustomInterface> buildProxyInstance() {
		final ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setInterfaces(new Class[] { MyCustomInterface.class });

		proxyFactory.setFilter(new MethodFilter() {
			public boolean isHandled(Method m) {
				// ignore finalize()
				return !m.getName().equals("finalize");
			}
		});

		final Class fakeImplementation = proxyFactory.createClass();

		final MethodHandler methodHandler = new MethodHandler() {
			public Object invoke(Object self, Method m, Method proceed, Object[] args) {
				System.out.println("##### Invoking method \"" + m.getName() + "\" with arguments: " + Arrays.toString(args));

				final Class<?> returnType = m.getReturnType();
				if (returnType.isAssignableFrom(String.class)) // return type of the method is a String
					return "Proxy returned a String.";

				// If it's not a String, it's an Integer.
				return 42;
			}
		};

		try {
			MyCustomInterface instance = (MyCustomInterface) fakeImplementation.newInstance();
			((Proxy) instance).setHandler(methodHandler);
			return Optional.of(instance);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return Optional.empty();
	}
}

interface MyCustomInterface {
	String someMethod1(String argument);

	Integer someMethod2(Integer argument);
}
