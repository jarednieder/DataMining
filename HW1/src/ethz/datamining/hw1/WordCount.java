package ethz.datamining.hw1;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class WordCount {
	public static final Path INPUT = new Path("hw1-in");
	public static final Path OUTPUT = new Path("hw1-wordcount-out");
	public static final String REGEX = "([a-zA-Z]+)";
	public static final Pattern PATTERN = Pattern.compile(REGEX);

	public static class Map extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, IntWritable> {
		public static final IntWritable ONE = new IntWritable(1);
		
		@Override
		public void map(LongWritable docKey, Text doc,
				OutputCollector<Text, IntWritable> output, Reporter reporter)
				throws IOException {
			Text word = new Text();
			String docStr = doc.toString();
			Matcher m = PATTERN.matcher(docStr);
			while (m.find()) {
				word.set(m.group(1).toLowerCase());
				output.collect(word, ONE);
			}
		}
	}

	public static class Reduce extends MapReduceBase implements
			Reducer<Text, IntWritable, Text, IntWritable> {
		@Override
		public void reduce(Text word, Iterator<IntWritable> counts,
				OutputCollector<Text, IntWritable> output, Reporter reporter)
				throws IOException {
			int sum = 0;
			while (counts.hasNext()) {
				sum += counts.next().get();
			}
			output.collect(word, new IntWritable(sum));
		}
	}

	public static void main(String[] args) throws IOException {
		JobConf conf = new JobConf(WordCount.class);
		conf.setJobName("hw1-word-count");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(IntWritable.class);

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