package com.example.bigdata;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AppRatingAggregator extends Configured implements Tool {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new AppRatingAggregator(), args);
        System.exit(res);
    }

    public int run(String[] args) throws Exception {
        Job job = Job.getInstance(getConf(), "AppRatingAggregator");
        job.setJarByClass(this.getClass());
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setOutputFormatClass(TextOutputFormat.class);

        job.setMapperClass(AppMapper.class);
        job.setCombinerClass(AppCombiner.class);
        job.setReducerClass(AppReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(AppCount.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(AppCount.class);

        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static class AppMapper extends Mapper<LongWritable, Text, Text, AppCount> {

        private final Text outputKey = new Text();
        private final AppCount appCount = new AppCount();

        public void map(LongWritable offset, Text lineText, Context context) {
            try {
                if (offset.get() != 0) {
                    String line = lineText.toString();
                    String[] fields = line.split("\\u0001");

                    String developerId = fields[21]; // developer_id
                    String releaseDate = fields[13]; // Released
                    String year = getYear(releaseDate);
                    String rating = fields[3]; // Rating
                    String ratingCount = fields[4]; // Rating Count

                    double ratingValue = Double.parseDouble(rating);
                    int countValue = Integer.parseInt(ratingCount);

                    if (countValue >= 1000) {
                        outputKey.set(developerId + ";" + year + ";");
                        appCount.set(new DoubleWritable(ratingValue), new IntWritable(countValue));
                        context.write(outputKey, appCount);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static class AppReducer extends Reducer<Text, AppCount, Text, AppCount> {

        private final AppCount appCount = new AppCount();

        @Override
        public void reduce(Text key, Iterable<AppCount> values, Context context) throws IOException, InterruptedException {
            double totalSum = 0.0;
            int totalCount = 0;
            int totalAppCount = 0;

            for (AppCount val : values) {
                totalSum += val.getRateSum().get();
                totalCount += val.getRateCount().get();
                totalAppCount += val.getAppNumberCount().get();
            }

            appCount.set(new DoubleWritable(totalSum), new IntWritable(totalCount), new IntWritable(totalAppCount));

            context.write(key, appCount);
        }

    }

    public static class AppCombiner extends Reducer<Text, AppCount, Text, AppCount> {

        private final AppCount appCount = new AppCount();

        @Override
        public void reduce(Text key, Iterable<AppCount> values, Context context) throws IOException, InterruptedException {
            appCount.set(new DoubleWritable(0.0d), new IntWritable(0), new IntWritable(0));

            for (AppCount val : values) {
                appCount.addAppCount(val);
            }
            context.write(key, appCount);
        }
    }

    private static String getYear(String releaseDate) throws ParseException {
        Date date = formatter.parse(releaseDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        return String.valueOf(year);
    }
}
