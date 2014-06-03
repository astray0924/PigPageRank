package myudfs;

import java.io.IOException;
import java.util.Map;

import org.apache.pig.EvalFunc;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;

public class PageRank extends EvalFunc<Map<String, Tuple>> {
	private static final float jumpingFactor = 0.85f;

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Tuple> exec(Tuple input) throws IOException {
		Map<String, Tuple> graph = (Map<String, Tuple>) input.get(0);

		for (Tuple node : graph.values()) {
			float newScore = getNewScore(node, graph);
			node.set(2, newScore);
		}

		return graph;
	}

	public float getNewScore(Tuple node, Map<String, Tuple> graph)
			throws ExecException {
		int N = graph.size();
		float newScore = ((1 - jumpingFactor) * ((float) 1 / N))
				+ (jumpingFactor) * calculateOuterFactor(node, graph);

		return newScore;
	}

	private float calculateOuterFactor(Tuple node, Map<String, Tuple> graph)
			throws ExecException {
		float score = 0.0f;

		DataBag linksIds = (DataBag) node.get(1);
		for (Tuple link : linksIds) {
			String page_id = (String) link.get(0);

			if (graph.containsKey(page_id)) {
				Tuple nd = graph.get(page_id);
				Float s = (Float) nd.get(2);
				Integer c = (Integer) nd.get(0);
				score += (float) s / c;
			}

		}

		return score;
	}

}
