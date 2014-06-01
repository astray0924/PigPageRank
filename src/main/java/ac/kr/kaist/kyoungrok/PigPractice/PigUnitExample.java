package ac.kr.kaist.kyoungrok.PigPractice;

import java.io.IOException;

import org.apache.pig.pigunit.Cluster;
import org.apache.pig.pigunit.PigTest;
import org.apache.pig.tools.pigscript.parser.ParseException;
import org.junit.Test;

//java/example/PigUnitExample.java 
public class PigUnitExample {
	private PigTest test;
	private static Cluster cluster;

	@Test
	public void testDataInFile() throws ParseException, IOException {
		// System.out.println(System.getProperty("user.dir"));

		// Construct an instance of PigTest that will use the script //
		// pigunit.pig.
		test = new PigTest("./script/pigunit.pig");
		// Specify our expected output. The format is a string for each line. //
		// In this particular case we expect only one line of output.
		String[] output = { "(0.27305267014925455)" };
		// Run the test and check that the output matches our expectation. //
		// The "avgdiv" tells PigUnit what alias to check the output value //
		// against. It inserts a store for that alias and then checks the //
		// contents of the stored file against output.
		// test.assertOutput("avgdiv", output);
	}

	@Test
	public void testTextInput() throws ParseException, IOException {
		test = new PigTest("../pigunit.pig");
		// Rather than read from a file, generate synthetic input. // Format is
		// one record per line, tab-separated.
		String[] input = { "NYSE\tCPO\t2009-12-30\t0.14",
				"NYSE\tCPO\t2009-01-06\t0.14", "NYSE\tCCS\t2009-10-28\t0.414",
				"NYSE\tCCS\t2009-01-28\t0.414", "NYSE\tCIF\t2009-12-09\t0.029", };
		String[] output = { "(0.22739999999999996)" };
		// Run the example script using the input we constructed // rather than
		// loading whatever the load statement says. // "divs" is the alias to
		// override with the input data. // As with the previous example,
		// "avgdiv" is the alias // to test against the value(s) in output.
		// test.assertOutput("divs", input, "avgdiv", output);
	}
}