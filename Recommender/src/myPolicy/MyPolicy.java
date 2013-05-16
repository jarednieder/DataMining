package myPolicy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.ethz.las.bandit.logs.yahoo.Article;
import org.ethz.las.bandit.logs.yahoo.User;
import org.ethz.las.bandit.policies.ContextualBanditPolicy;

public class MyPolicy implements ContextualBanditPolicy<User, Article, Boolean> {
	private static final Double ONE = 1.0;
	private Map<Integer, Double[]> articleFeatures;

	// Here you can load the article features.
	public MyPolicy(String articleFilePath) {
		try {
			articleFeatures = loadFeatures(new File(articleFilePath));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<Integer, Double[]> loadFeatures(File file) throws IOException {
		Map<Integer, Double[]> articleMap = new HashMap<Integer, Double[]>();
		BufferedReader br = null;
		String line;
		String[] lineSplit;
		Integer articleId;
		Double[] articleFeatures = new Double[6];
		articleFeatures[0] = ONE;
		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				lineSplit = line.split(" ");
				articleId = Integer.parseInt(lineSplit[0]);
				for (int i = 2; i < lineSplit.length; i++) {
					articleFeatures[i - 1] = Double.parseDouble(lineSplit[i]);
				}
				articleMap.put(articleId, articleFeatures);
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
		return articleMap;
	}

	@Override
	public Article getActionToPerform(User visitor,
			List<Article> possibleActions) {
		return possibleActions.get(0);
	}

	@Override
	public void updatePolicy(User c, Article a, Boolean reward) {
	}
}
