package pigpagerank;

import java.io.IOException;
import java.util.Arrays;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

public class ExtractPageInfo extends EvalFunc<Tuple> {

	@Override
	public Tuple exec(Tuple input) throws IOException {
		if (input == null || input.size() == 0 || input.get(0) == null) {
			return null;
		} else {
			return TupleFactory.getInstance().newTuple(
					Arrays.asList((String) input.get(0)));
		}

	}
}
