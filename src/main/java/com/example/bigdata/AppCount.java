package com.example.bigdata;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class AppCount implements WritableComparable<AppCount> {
    private DoubleWritable rateSum; // suma ocen
    private IntWritable rateCount; // liczba ocen
    private IntWritable appNumberCount; // liczba aplikacji

    public AppCount() {
        this.rateSum = new DoubleWritable(0.0);
        this.rateCount = new IntWritable(0);
        this.appNumberCount = new IntWritable(1);
    }

    public void set(DoubleWritable sum, IntWritable count, IntWritable appCount) {
        this.rateSum = sum;
        this.rateCount = count;
        this.appNumberCount = appCount;
    }

    public void set(DoubleWritable sum, IntWritable count) {
        this.rateSum = (sum);
        this.rateCount = (count);
        this.appNumberCount = new IntWritable(1);
    }

    public DoubleWritable getRateSum() {
        return rateSum;
    }

    public IntWritable getRateCount() {
        return rateCount;
    }

    public IntWritable getAppNumberCount() {
        return appNumberCount;
    }

    public void addAppCount(AppCount appCount) {
        set(new DoubleWritable(this.rateSum.get() + appCount.getRateSum().get()),
                new IntWritable(this.rateCount.get() + appCount.getRateCount().get()),
                new IntWritable(this.appNumberCount.get() + appCount.getAppNumberCount().get()));
    }

    @Override
    public void write(DataOutput out) throws IOException {
        rateSum.write(out);
        rateCount.write(out);
        appNumberCount.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        rateSum.readFields(in);
        rateCount.readFields(in);
        appNumberCount.readFields(in);
    }

    @Override
    public int compareTo(AppCount appCount) {

        int comparison = this.rateSum.compareTo(appCount.rateSum);

        if (comparison != 0) {
            return comparison;
        }

        return this.rateCount.compareTo(appCount.rateCount);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        AppCount appCount = (AppCount) obj;

        return rateSum.equals(appCount.rateSum) &&
                rateCount.equals(appCount.rateCount) &&
                appNumberCount.equals(appCount.appNumberCount);
    }

    @Override
    public int hashCode() {
        int result = rateSum.hashCode();
        result = 31 * result + rateCount.hashCode();
        result = 31 * result + appNumberCount.hashCode();
        return result;
    }


}

