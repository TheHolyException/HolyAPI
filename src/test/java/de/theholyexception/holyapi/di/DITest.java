package de.theholyexception.holyapi.di;

import org.junit.jupiter.api.*;

import java.lang.ref.WeakReference;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DITest {


	@Test
	@Order(1)
	void simpleDI() {
		AdvancedDIContainer container = new AdvancedDIContainer();

		container.register(DITestClass1.class, DITestClass1.class);
		container.resolve(DITestClass1.class);
		DITestClass2 t = container.resolve(DITestClass2.class);
		container.register(DITestClass2.class, t);

		assert t.testClass1 != null;
	}

	@Test
	@Order(2)
	void circularDI() {

		ComplexDIContainer container = new ComplexDIContainer()
			.setResolveCircularDependencies(true);
		container.register(DITestClass3.class);
		container.register(DITestClass4.class);
		DITestClass3 t3 = container.resolve(DITestClass3.class);
		DITestClass4 t4 = container.resolve(DITestClass4.class);

		System.out.println(t3);
		System.out.println(t4);
		System.out.println(t3.testClass4);
		System.out.println(t4.testClass3);
		System.out.println(t4.testClass3.testClass4);

		assert t3.testClass4 == t4 && t4.testClass3 == t3;
	}

	@Test
	@Order(3)
	void circularDIConstructorFieldMix() {

		ComplexDIContainer container = new ComplexDIContainer()
			.setResolveCircularDependencies(true)
			.setConstructorInjection(true);
		container.register(DITestConstructor1.class);
		container.register(DITestConstructor2.class);
		DITestConstructor1 t1 = container.resolve(DITestConstructor1.class);
		DITestConstructor2 t2 = container.resolve(DITestConstructor2.class);

		System.out.println(t1);
		System.out.println(t2);
		System.out.println(t2.testClass1);

		assert t2.testClass1 == t1;
	}

	@Test
	@Order(4)
	void existingInstance() {
		ComplexDIContainer container = new ComplexDIContainer()
			.setResolveCircularDependencies(true)
			.setConstructorInjection(true);

		container.register(DITestClass1.class, DITestClass1.class);
		container.resolve(DITestClass1.class);

		DITestClass2 t = new DITestClass2();
		container.injectFields(t);

		assert t.testClass1 != null;
	}

	@Test
	@Order(5)
	void privateConstructor() {
		ComplexDIContainer container = new ComplexDIContainer()
			.setResolveCircularDependencies(true);
		container.register(DITestClass1.class, new DITestClass1());
		DITestPrivate1 instance = container.resolve(DITestPrivate1.class);

		assert instance.testClass1 != null;
	}

	@Test
	@Order(6)
	void weakReference() {
		ComplexDIContainer container = new ComplexDIContainer(true).setResolveCircularDependencies(true);
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
		ComplexDIContainer container = new ComplexDIContainer().setResolveCircularDependencies(true);
		DITestClass1 tc1 = new DITestClass1();
		WeakReference<DITestClass1> wr1 = new WeakReference<>(tc1);
		container.register(tc1);
		tc1 = null;
		System.gc();
		assert wr1.get() != null;
	}

}
