package com.example.bigdata;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

public class AppRatingAggregator extends Configured implements Tool {

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new AppRatingAggregator(), args);
        System.exit(res);
    }

    public int run(String[] args) throws Exception {
        Job job = Job.getInstance(getConf(), "AppRatingAggregator");
        job.setJarByClass(this.getClass());
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setMapperClass(AppRatingMapper.class);
        job.setReducerClass(AppRatingReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(RatingInfo.class);
        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static class AppRatingMapper extends Mapper<LongWritable, Text, Text, RatingInfo> {

        private final Text outputKey = new Text();
        private final RatingInfo ratingInfo = new RatingInfo();

        public void map(LongWritable offset, Text lineText, Context context) {
            try {
                String line = lineText.toString();
                String[] fields = line.split("\u0001");

                String developerId = fields[14]; // developer_id
                String releaseDate = fields[12]; // Released
                String year = releaseDate.split("-")[0]; // Extract year
                String rating = fields[3]; // Rating
                String ratingCount = fields[4]; // Rating Count

                // Convert ratings and counts to numeric types
                double ratingValue = Double.parseDouble(rating);
                int countValue = Integer.parseInt(ratingCount);

                if (countValue >= 1000) { // Filter out apps with < 1000 ratings
                    outputKey.set(developerId + "-" + year);
                    ratingInfo.set(ratingValue, countValue); // sum, count, app count
                    context.write(outputKey, ratingInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class AppRatingReducer extends Reducer<Text, RatingInfo, Text, ResultInfo> {

        private final ResultInfo resultInfo = new ResultInfo();

        @Override
        public void reduce(Text key, Iterable<RatingInfo> values, Context context) throws IOException, InterruptedException {
            double totalSum = 0.0;
            int totalCount = 0;

            for (RatingInfo val : values) {
                totalSum += val.getSum().get();
                totalCount += val.getCount().get();
            }

            resultInfo.set(totalSum, totalCount);
            context.write(key, resultInfo);
        }
    }
}
