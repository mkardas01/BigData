package com.example.bigdata;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class RatingInfo implements Writable {
    private DoubleWritable sum;
    private IntWritable count;

    public RatingInfo() {
        this.sum = new DoubleWritable(0.0);
        this.count = new IntWritable(0);
    }

    public void set(double sum, int count) {
        this.sum.set(sum);
        this.count.set(count);
    }

    public DoubleWritable getSum() { return sum; }
    public IntWritable getCount() { return count; }

    @Override
    public void write(DataOutput out) throws IOException {
        sum.write(out);
        count.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        sum.readFields(in);
        count.readFields(in);
    }
}

