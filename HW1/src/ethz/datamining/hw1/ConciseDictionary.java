package ethz.datamining.hw1;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class ConciseDictionary {
	public static final Path INPUT = new Path("hw1-wordcount-out/part-00000");
	public static final Path OUTPUT = new Path("hw1-concise-dict-out");

	private static int lower;
	private static int upper;

	public static class Map extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, Text> {
		@Override
		public void map(LongWritable key, Text value,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			output.collect(new Text("TEST"), new Text("testing"));

			StringTokenizer tokenizer = new StringTokenizer(value.toString());
			String word;
			int count;
			while (tokenizer.hasMoreTokens()) {
				word = tokenizer.nextToken();
				count = Integer.parseInt(tokenizer.nextToken());
				if (count < lower || count > upper) {
					continue;
				}
				// Output the word's first character as the key and the
				// word/count pair as the value
				output.collect(new Text("" + word.charAt(0)), new Text(word
						+ " " + count));
			}
		}
	}

	public static class Reduce extends MapReduceBase implements
			Reducer<Text, Text, Text, Text> {
		@Override
		public void reduce(Text key, Iterator<Text> iter,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			String val = "";
			if (iter.hasNext()) {
				val += iter.next().toString();
				val += iter.hasNext() ? iter.next().toString() : "FALSE";
			} else {
				val += "FALSE";
			}

			output.collect(key, new Text(val));
			// SortedSet<String> sortedWords = new TreeSet<String>(
			// new Comparator<String>() {
			// @Override
			// public int compare(String o1, String o2) {
			// return Integer.compare(getCount(o1), getCount(o2));
			// }
			// });
			// int min = Integer.MAX_VALUE;
			// String val;
			// int count;
			// while (value.hasNext()) {
			// val = value.next().toString();
			// count = getCount(val);
			// if (sortedWords.size() < 20) {
			// sortedWords.add(val);
			// min = getCount(sortedWords.first());
			// } else if (count > min) {
			// sortedWords.remove(sortedWords.first());
			// sortedWords.add(val);
			// min = getCount(sortedWords.first());
			// }
			// }
			//
			// for (String s : sortedWords) {
			// output.collect(new Text(getWord(s)), new IntWritable(
			// getCount(s)));
			// }
		}

		private int getCount(String s) {
			return Integer.parseInt(s.split(" ")[1]);
		}

		private String getWord(String s) {
			return s.split(" ")[0];
		}
	}

	public static void main(String[] args) throws IOException {

		JobConf conf = new JobConf(WordCount.class);
		conf.setJobName("hw1-concise-dict");

		String[] otherArgs = new GenericOptionsParser(conf, args)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err
					.println("Concise Dictionary requires lower/upper bound arguments");
		}
		lower = Integer.parseInt(otherArgs[0]);
		upper = Integer.parseInt(otherArgs[1]);

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(Map.class);
		conf.setCombinerClass(Reduce.class);
		conf.setReducerClass(Reduce.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, INPUT);
		FileOutputFormat.setOutputPath(conf, OUTPUT);

		JobClient.runJob(conf);
	}
}
