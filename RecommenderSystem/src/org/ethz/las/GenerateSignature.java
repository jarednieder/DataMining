package org.ethz.las;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.log4j.Logger;

public class GenerateSignature {

	protected static final Logger log = Logger
			.getLogger(GenerateSignature.class);

	/**
	 * Mapper outputs the hash value for each file and hash function.
	 */
	public static class Map extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, LongWritable> {

		// Decide on the number of hash functions.
		final int N_HASH_FUNCTIONS = 32;

		// Documents contain shingles with index in [0, 255].
		final int N_SHINGLES = 256;

		// Prime number that will be used as MOD for all hash functions, i.e.
		// (ax + b) % MOD.
		final int NEXT_LARGER_PRIME = 257;

		// Seed for the random hash function generation (to make sure different
		// Mappers produce the same hash functions.
		final int SEED = 1234567;

		// Generates the hash functions based on the parameters described above.
		final SimpleHashFunction[] hashFunctions = HashFamily
				.generateSimpleHashFamily(N_SHINGLES, NEXT_LARGER_PRIME,
						N_HASH_FUNCTIONS, SEED);

		// We instantiate the key here to avoid instantiating it for every line
		// of the document.
		private Text outputKey = new Text();

		// Main function, implements the mapper. Gets one line as input.
		public void map(LongWritable key, Text value,
				OutputCollector<Text, LongWritable> output, Reporter reporter)
				throws IOException {

			// Gets the name of the file that is currently being mapped.
			String currentFilename = ((FileSplit) reporter.getInputSplit())
					.getPath().getName();

			// Get shingle value from the current line.
			int shingle = Integer.parseInt(value.toString());

			// TODO: Output (document, hash function) -> (hash value) with the
			// goal of calculating Min-hash for this document.
			for (int i = 0; i < hashFunctions.length; i++) {
				outputKey.set(currentFilename + " " + i);
				output.collect(outputKey,
						new LongWritable(hashFunctions[i].hash(shingle)));
			}
		}
	}

	/**
	 * Reducer implements the minhashing.
	 */
	public static class Reduce extends MapReduceBase implements
			Reducer<Text, LongWritable, Text, LongWritable> {

		public void reduce(Text key, Iterator<LongWritable> values,
				OutputCollector<Text, LongWritable> output, Reporter reporter)
				throws IOException {
			// TODO: Calculate and output minhash for document and hash
			// function.
			LongWritable v;
			LongWritable min = values.next();
			while (values.hasNext()) {
				v = values.next();
				if (min.compareTo(v) > 0) {
					min = v;
				}
			}
			output.collect(key, min);
		}
	}

	public static void main(String[] args) throws Exception {

		JobConf conf = new JobConf(GenerateSignature.class);
		conf.setJobName("GenerateSignature");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(LongWritable.class);

		conf.setMapperClass(Map.class);
		conf.setReducerClass(Reduce.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		JobClient.runJob(conf);
	}
}
