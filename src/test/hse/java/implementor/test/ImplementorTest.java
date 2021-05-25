package hse.java.implementor.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;

public class ImplementorTest extends AbstractImplementorTest {
    /*
     * Here you can use for tests following methods:
     *    - checkInterfaceImplementationFromFolder
     *    - checkInterfaceImplementationFromStandardLibrary
     *    - checkAbstractClassImplementationFromFolder
     *    - checkAbstractClassImplementationFromStandardLibrary
     *
     * In each method you should use FQN.
     *
     * You can test implementor on any class/interface, that lays down
     *   in your main module (src/main/java).
     */

    public ImplementorTest() throws Exception {
        super();
    }

    // Uncomment, if you want to cleanup your implementor output directory (tmp)
    @AfterAll
    static void cleanUp() {
        deleteFolderContent(new File(OUTPUT_DIRECTORY), false);
    }

    @Test
    public void implementComparable() {
        Assertions.assertTimeout(
                Duration.ofSeconds(5),
                () -> checkInterfaceImplementationFromStandardLibrary("java.lang.Comparable")
        );
    }

    @Test
    public void implementIterable() {
        Assertions.assertTimeout(
                Duration.ofSeconds(5),
                () -> checkInterfaceImplementationFromStandardLibrary("java.lang.Iterable")
        );
    }


    @Test
    public void implementAbstractSet() {
        Assertions.assertTimeout(
                Duration.ofSeconds(5),
                () -> checkAbstractClassImplementationFromStandardLibrary("java.util.AbstractSet")
        );
    }

    @Test
    public void implementAbstractQueue() {
        Assertions.assertTimeout(
                Duration.ofSeconds(5),
                () -> checkAbstractClassImplementationFromStandardLibrary("java.util.AbstractQueue")
        );
    }



    @Test
    public void implementMyInterface() {
        Assertions.assertTimeout(
                Duration.ofSeconds(5),
                () -> checkInterfaceImplementationFromFolder("study.MyInterface")
        );
    }

    @Test
    public void implementSomeInterface() {
        Assertions.assertTimeout(
                Duration.ofSeconds(5),
                () -> checkInterfaceImplementationFromFolder("study.SomeInterface")
        );
    }

    @Test
    public void implementNoPackageInterface() {
        Assertions.assertTimeout(
                Duration.ofSeconds(5),
                () -> checkInterfaceImplementationFromFolder("NoPackageInterface")
        );
    }

    @Test
    public void implementImplementor() {
        Assertions.assertTimeout(
                Duration.ofSeconds(5),
                () -> checkInterfaceImplementationFromFolder("ru.hse.java.implementor.Implementor")
        );
    }

    @Test
    public void implementClassB() {
        Assertions.assertTimeout(
                Duration.ofSeconds(5),
                () -> checkAbstractClassImplementationFromFolder("study.inherit.ClassB")
        );
    }


    @Test
    public void implementMyClass() {
        Assertions.assertTimeout(
                Duration.ofSeconds(5),
                () -> checkAbstractClassImplementationFromFolder("study.MyClass")
        );
    }

    @Test
    public void implementAbstractClass() {
        Assertions.assertTimeout(
                Duration.ofSeconds(5),
                () -> checkAbstractClassImplementationFromFolder("study.AbstractClass")
        );
    }



}
