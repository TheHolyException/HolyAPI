package de.theholyexception.holyapi.di;

import org.junit.jupiter.api.*;

import java.lang.ref.WeakReference;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DITest {


	@Test
	@Order(1)
	void simpleDI() {
		DependencyInjector container = new DependencyInjector();
		container.resolve(DITestClass1.class);
		DITestClass2 t = container.resolve(DITestClass2.class);
		container.register(DITestClass2.class, t);

		assert t.testClass1 != null;
	}

	@Test
	@Order(2)
	void circularDI() {
		DependencyInjector container = new DependencyInjector()
			.setResolveCircularDependencies(true);
		container.register(DITestClass3.class);
		container.register(DITestClass4.class);
		DITestClass3 t3 = container.resolve(DITestClass3.class);
		DITestClass4 t4 = container.resolve(DITestClass4.class);

		assert t3.testClass4 == t4 && t4.testClass3 == t3;
	}

	@Test
	@Order(3)
	void circularDIConstructorFieldMix() {
		DependencyInjector container = new DependencyInjector()
			.setResolveCircularDependencies(true)
			.setConstructorInjection(true);
		container.register(DITestConstructor1.class);
		container.register(DITestConstructor2.class);
		DITestConstructor1 t1 = container.resolve(DITestConstructor1.class);
		DITestConstructor2 t2 = container.resolve(DITestConstructor2.class);

		assert t2.testClass1 == t1;
	}

	@Test
	@Order(4)
	void existingInstance() {
		DependencyInjector container = new DependencyInjector()
			.setResolveCircularDependencies(true)
			.setConstructorInjection(true);

		container.resolve(DITestClass1.class);

		DITestClass2 t = new DITestClass2();
		container.injectFields(t);

		assert t.testClass1 != null;
	}

	@Test
	@Order(5)
	void privateConstructor() {
		DependencyInjector container = new DependencyInjector()
			.setResolveCircularDependencies(true);
		container.register(DITestClass1.class, new DITestClass1());
		DITestPrivate1 instance = container.resolve(DITestPrivate1.class);

		assert instance.testClass1 != null;
	}

	@Test
	@Order(6)
	void weakReference() {
		DependencyInjector container = new DependencyInjector().setResolveCircularDependencies(true);
		DITestClass1 tc1 = new DITestClass1();
		WeakReference<DITestClass1> wr1 = new WeakReference<>(tc1);
		container.register(tc1);
		tc1 = null;
		System.gc();
		assert wr1.get() == null;
	}

	@Test
	@Order(7)
	void weakReference2() {
		DependencyInjector container = new DependencyInjector(false).setResolveCircularDependencies(true);
		DITestClass1 tc1 = new DITestClass1();
		WeakReference<DITestClass1> wr1 = new WeakReference<>(tc1);
		container.register(tc1);
		tc1 = null;
		System.gc();
		assert wr1.get() != null;
	}

	@Test
	@Order(8)
	void unknownInstance() {
		DependencyInjector container = new DependencyInjector();
		DITestClass2 tc2 = container.resolve(DITestClass2.class);
		assert tc2.testClass1 == null;
	}

}
