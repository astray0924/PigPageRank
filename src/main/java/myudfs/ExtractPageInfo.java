package myudfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.apache.pig.impl.logicalLayer.schema.Schema.FieldSchema;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.common.base.Joiner;

public class ExtractPageInfo extends EvalFunc<Tuple> {
	private WikiDumpParser parser;

	public ExtractPageInfo() {
		this.parser = new WikiDumpParser();
	}

	@Override
	public Tuple exec(Tuple input) throws IOException {
		if (input == null || input.size() == 0 || input.get(0) == null) {
			return null;
		} else {
			parser.parse((String) input.get(0));

			if (parser.getNs() == 0
					&& StringUtils.isAlphanumericSpace(parser.getTitle())) {
				// Page Infos
				Integer id = parser.getId();
				String title = parser.getTitle();
				List<String> outLinks = parser.getSanitizedLinks();
				Integer outLinksCount = outLinks.size();

				// Output
				Tuple output = TupleFactory.getInstance().newTuple(4);
				output.set(0, id);
				output.set(1, title);
				Joiner joiner = Joiner.on("|").skipNulls();
				output.set(2, joiner.join(outLinks));
				output.set(3, outLinksCount);

				return output;
			} else {
				return null;
			}

		}

	}

	@Override
	public Schema outputSchema(Schema input) {
		try {
			Schema tupleSchema = new Schema();

			tupleSchema.add(new FieldSchema("id", DataType.INTEGER));
			tupleSchema.add(new FieldSchema("title", DataType.CHARARRAY));
			tupleSchema.add(new FieldSchema("outlinks", DataType.CHARARRAY));
			tupleSchema.add(new FieldSchema("outlinks_count", DataType.INTEGER));

			// return new Schema(new FieldSchema(getSchemaName(this.getClass()
			// .getName().toLowerCase(), input), tupleSchema,
			// DataType.TUPLE));
			return new Schema(new FieldSchema(getSchemaName(getClass()
					.getName().toLowerCase(), input), tupleSchema,
					DataType.TUPLE));
		} catch (Exception e) {
			return null;
		}
	}

	private class WikiDumpParser {
		private final Pattern linkPattern = Pattern
				.compile("\\[\\[(.*?)\\]\\]");
		private final Pattern redirectLinkPattern = Pattern
				.compile("#REDIRECT \\[\\[(.*?)\\]\\]");

		private String page;
		private Document dom;
		private int id;
		private int ns;
		private String text;
		private String title;
		private List<String> links;

		public WikiDumpParser() {
			this.page = "";
			this.dom = new Document("");
			this.id = -1;
			this.ns = -1;
			this.text = "";
			this.title = "";
			this.links = new ArrayList<String>();
		}

		public void parse(String page) {
			this.page = page;
			this.dom = Jsoup.parse(page);
			this.id = extractId();
			this.ns = extractNs();
			this.text = extractText();
			this.title = extractTitle();
			this.links = extractLinks();
		}

		private int extractNs() {
			int ns = -1;

			try {
				Element nsTag = dom.getElementsByTag("ns").get(0);
				ns = Integer.parseInt(nsTag.textNodes().get(0).text());
			} catch (IndexOutOfBoundsException e) {
				// e.printStackTrace();
			} catch (NumberFormatException ne) {
				// ne.printStackTrace();
			}

			return ns;
		}

		private int extractId() {
			int id = -1;

			try {
				Element idTag = dom.getElementsByTag("id").get(0);
				id = Integer.parseInt(idTag.textNodes().get(0).text());
			} catch (IndexOutOfBoundsException e) {
				// e.printStackTrace();
			} catch (NumberFormatException ne) {
				// ne.printStackTrace();
			}

			return id;
		}

		private String extractText() {
			String text = "";

			try {
				Element textTag = dom.getElementsByTag("text").get(0);
				text = textTag.textNodes().get(0).text();
			} catch (IndexOutOfBoundsException e) {
				// e.printStackTrace();
			} catch (NumberFormatException ne) {
				// ne.printStackTrace();
			}

			return text;
		}

		private String extractTitle() {
			String title = "";

			try {
				Element titleTag = dom.getElementsByTag("title").get(0);
				title = titleTag.textNodes().get(0).text().trim();
			} catch (IndexOutOfBoundsException e) {
				// e.printStackTrace();
			} catch (NumberFormatException ne) {
				// ne.printStackTrace();
			}

			return title;
		}

		private List<String> extractLinks() {
			List<String> links = new ArrayList<String>();

			// 먼저 redirect link를 모두 공백으로 대체
			String text = getText();
			Matcher matcher = redirectLinkPattern.matcher(text);
			String pageWithoutRedirects = matcher.replaceAll(" ");

			// 그 다음 일반 link를 추출
			matcher = linkPattern.matcher(pageWithoutRedirects);
			while (matcher.find()) {
				links.add(matcher.group(1));
			}

			return links;
		}

		public int getNs() {
			return this.ns;
		}

		public int getId() {
			return this.id;
		}

		public String getText() {
			return this.text;
		}

		public String getTitle() {
			return this.title;
		}

		public List<String> getRawLinks() {
			return this.links;
		}

		public List<String> getSanitizedLinks() {
			return sanitizeLinks(this.links);
		}

		public List<String> sanitizeLinks(List<String> links) {
			List<String> sanitizedLinks = new ArrayList<String>();

			for (String link : links) {
				// 만약 link가 비어있으면 무시
				// 혹은 '.', '/', ':'가 포함되어 있으면 마찬가지로 무시한다
				link = link.trim();
				// if (link.equals("") || link.contains(".") ||
				// link.contains("/")
				// || link.contains(":") || link.contains("-")) {
				// continue;
				// }
				if (!StringUtils.isAlphanumericSpace(link)) {
					continue;
				}

				// 만약 '#'로 시작하면 같은 페이지 내에서의 참조이므로 무시한다
				if (link.startsWith("#")) {
					continue;
				}

				String sLink = "";
				try {
					// 먼저 '|'로 분리해서 첫번째 부분만 취한다.
					sLink = link.split("\\|")[0];

					// 다시 '#'로 분리해서 첫번째 부분만 취한다
					sLink = sLink.split("#")[0];
				} catch (IndexOutOfBoundsException e) {
					continue;
				}

				// 앞뒤 공백을 제거한다
				sLink = sLink.trim();

				// 첫번째 문자만 소문자화한다
				// try {
				// sLink = Character.toLowerCase(sLink.charAt(0))
				// + sLink.substring(1);
				// } catch (IndexOutOfBoundsException e) {
				// if (sLink.isEmpty()) {
				// continue;
				// }
				//
				// // System.err.println(link);
				// sLink =
				// String.valueOf(Character.toLowerCase(sLink.charAt(0)));
				// }

				// 리스트에 추가
				sanitizedLinks.add(sLink);
			}

			return sanitizedLinks;
		}

	}
}
