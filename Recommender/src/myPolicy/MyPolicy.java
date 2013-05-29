package myPolicy;

import static java.lang.Math.log;
import static java.lang.Math.sqrt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.ethz.las.bandit.logs.yahoo.Article;
import org.ethz.las.bandit.logs.yahoo.User;
import org.ethz.las.bandit.policies.ContextualBanditPolicy;

public class MyPolicy implements ContextualBanditPolicy<User, Article, Boolean> {
	private static final Double ONE = 1.0;
	private static final Double MAX_UCB = Double.MAX_VALUE;

	private Map<Integer, Double[]> articleFeatures;
	private Map<Integer, Integer> counts;
	private Map<Integer, Double> probs;
	private Map<Integer, Double> ucbs;
	private double iteration = 0;

	// Here you can load the article features.
	public MyPolicy(String articleFilePath) {
		articleFeatures = loadFeatures(new File(articleFilePath));
		initValues(articleFeatures.keySet());
	}

	/**
	 * Load the article feature values and store them to a map
	 * 
	 * @param file
	 *            The file containing the article features
	 * @return Mapping of article IDs to their feature vectors
	 * @throws RuntimeException
	 *             Thrown if there was a problem loading the feature vector file
	 */
	private Map<Integer, Double[]> loadFeatures(File file)
			throws RuntimeException {
		Map<Integer, Double[]> articleMap = new HashMap<Integer, Double[]>();
		BufferedReader br = null;
		String line;
		Integer articleId;
		Double[] articleFeatures = new Double[6];
		Scanner s;

		articleFeatures[0] = ONE;
		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				s = new Scanner(line);
				articleId = s.nextInt();

				// Skip the first feature (it's always 1.0)
				s.nextDouble();

				// Read the rest of the features
				for (int i = 1; i < 6; i++) {
					articleFeatures[i] = s.nextDouble();
				}
				articleMap.put(articleId, articleFeatures);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return articleMap;
	}

	/**
	 * Initialize values for each article
	 * 
	 * @param articleKeys
	 *            The article IDs
	 */
	private void initValues(Set<Integer> articleKeys) {
		int numArticles = articleKeys.size();
		counts = new HashMap<Integer, Integer>(numArticles);
		probs = new HashMap<Integer, Double>(numArticles);
		ucbs = new HashMap<Integer, Double>(numArticles);

		for (Integer article : articleKeys) {
			counts.put(article, 0);
			probs.put(article, 0d);
			ucbs.put(article, MAX_UCB);
		}
	}

	/**
	 * UCB1 ignores the visitor features and solely chooses the article with the
	 * highest UCB value
	 */
	@Override
	public Article getActionToPerform(User visitor,
			List<Article> possibleActions) {
		double maxUCBValue = -1d;
		Article maxUCBArticle = null;

		// Find article with the highest UCB value
		for (Article article : possibleActions) {
			if (ucbs.get(article.getID()) > maxUCBValue) {
				maxUCBArticle = article;
			}
		}
		return maxUCBArticle;
	}

	/**
	 * Update the mean, count, and UCB values for the chosen article
	 */
	@Override
	public void updatePolicy(User c, Article a, Boolean reward) {
		// Increment the article count
		int id = a.getID(), newCount = counts.get(id) + 1;
		counts.put(id, newCount);

		// Update the prob value
		double oldProb = probs.get(id);
		double newProb = oldProb + (reward ? 1 - oldProb : -oldProb) / newCount;

		probs.put(id, newProb);

		// Update the UCB value;
		double newUCB = newProb + sqrt((2 * log(iteration++)) / newCount);
		ucbs.put(id, newUCB);
	}
}
