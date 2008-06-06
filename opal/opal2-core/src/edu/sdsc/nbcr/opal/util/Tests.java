package edu.sdsc.nbcr.opal.util;

import junit.framework.TestSuite;
import junit.framework.Test;

/**
 * TestSuite that runs all the Opal tests
 *
 * @author Sriram Krishnan
 */
public class Tests {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
    System.exit(0);
  }
  
  public static Test suite ( ) {
    TestSuite suite= new TestSuite("All JUnit Tests for Opal2");

    suite.addTest(edu.sdsc.nbcr.opal.state.PackageTest.suite());

    return suite;
  }
}
