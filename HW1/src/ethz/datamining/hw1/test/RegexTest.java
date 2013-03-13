package ethz.datamining.hw1.test;

import java.util.HashMap;
import java.util.TreeSet;
import java.util.regex.Matcher;

import org.junit.Assert;
import org.junit.Test;

import ethz.datamining.hw1.WordCount;

public class RegexTest extends Assert {
	private static final String TEST_STR = "\"This is a (very, very) short ’book’. It is only 2 sentences long.\"";

	@Test
	public void test() {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		Matcher m = WordCount.PATTERN.matcher(TEST_STR);
		while (m.find()) {
			addToMap(map, m.group(1).toLowerCase());
		}

		for (String s : new TreeSet<String>(map.keySet())) {
			System.out.println(s + " | " + map.get(s));
		}
	}

	private void addToMap(HashMap<String, Integer> map, String s) {
		if (map.containsKey(s)) {
			map.put(s, map.get(s) + 1);
		} else {
			map.put(s, 1);
		}
	}
}
