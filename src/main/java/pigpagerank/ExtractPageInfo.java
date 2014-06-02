package pigpagerank;

import java.io.IOException;
import java.util.Arrays;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.apache.pig.impl.logicalLayer.schema.Schema.FieldSchema;

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

	@Override
	public Schema outputSchema(Schema input) {
		try {
			Schema tupleSchema = new Schema();
			tupleSchema.add(input.getField(0));
			FieldSchema fieldSchema = new FieldSchema(getSchemaName(getClass()
					.getName().toLowerCase(), input), tupleSchema,
					DataType.TUPLE);
			 return new Schema(fieldSchema);
		} catch (Exception e) {
			return null;
		}
	}
}
