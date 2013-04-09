package org.ethz.las;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.Path;
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
import org.apache.log4j.Logger;

public class PartitionMatrix {

  protected static final Logger log = Logger.getLogger(PartitionMatrix.class);

  public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {

    // All lines in the input file have the following form: doc_000.sum-hash-3 87
    // WARNING: If you change the output format of the GenerateSignature MapReduce
    // you have to change this regular expression.
    Pattern pattern = Pattern.compile("([a-z0-9_\\.]+)\\s+(\\d+)\\s+(\\d+)");

    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {

      // Read each line and get the fields using the matcher.
      Matcher m = pattern.matcher(value.toString().trim());

      if (m.matches()) {
        String filename = m.group(1);
        int hashFunction = Integer.parseInt(m.group(2));
        long minHash = Long.parseLong(m.group(3));

        // TODO: Output the (document) -> (hash function, min hash).
      }
    }
  }

  public static class Reduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {

    // Prime number that will be used as MOD for all hash functions, i.e. (ax + b) % MOD.
    final int MAX_COEF = (1 << 20);

    // Decide on the number of bands.
    final int NBANDS = XX;

    // Decide on the number of rows inside each band.
    final int NROWS = YY;

    // Seed for the random number generator, used to make sure that
    // all reducers use the same hash functions for same bands.
    final int SEED = 12341;

    // This regular expression matches the output of the mapper.
    Pattern pair = Pattern.compile("(\\d+)\\s+(\\d+)");

    // Here each hash function has an array of coefficients A and an offset B, each of which is smaller than MAX_COEF.
    VectorHashFunction[] hashFunctions = HashFamily.generateVectorHashFamily(MAX_COEF, NROWS, NBANDS, SEED);

    // Reducer calculates the hash for each column of each band, given signature matrix column as input.
    public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {

      // We first read all values for the signature matrix column
      // corresponding to this document.
      long [] signature = new long[NROWS * NBANDS];

      while (values.hasNext()) {
        Matcher m = pair.matcher(values.next().toString());
        if (m.matches()) {
          int hashFunction = Integer.parseInt(m.group(1));
          long minhash = Long.parseLong(m.group(2));
          signature[hashFunction] = minhash;
        }
        else throw new IOException("Line doesn't match the pattern!");
      }

      // TODO: Calculate and output the hash value of each vector for each band.
    }
  }

  public static void main(String[] args) throws Exception {

    JobConf conf = new JobConf(GenerateSignature.class);

    conf.setJobName("PartitionMatrix");

    conf.setOutputKeyClass(Text.class);
    conf.setOutputValueClass(Text.class);

    conf.setMapperClass(Map.class);
    conf.setReducerClass(Reduce.class);

    conf.setInputFormat(TextInputFormat.class);
    conf.setOutputFormat(TextOutputFormat.class);

    FileInputFormat.setInputPaths(conf, new Path(args[0]));
    FileOutputFormat.setOutputPath(conf, new Path(args[1]));

    JobClient.runJob(conf);
  }
}
