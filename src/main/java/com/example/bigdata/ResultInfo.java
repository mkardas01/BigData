package com.example.bigdata;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ResultInfo implements Writable {
    private DoubleWritable totalSum; //Przechowuje całkowitą sumę ocen dla wszystkich aplikacji danego dewelopera w danym roku.
    private IntWritable totalCount; //Przechowuje łączną liczbę ocen dla wszystkich aplikacji danego dewelopera w danym roku.

    public ResultInfo() {
        this.totalSum = new DoubleWritable(0.0);
        this.totalCount = new IntWritable(0);
    }

    public void set(double totalSum, int totalCount) {
        this.totalSum.set(totalSum);
        this.totalCount.set(totalCount);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        totalSum.write(out);
        totalCount.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        totalSum.readFields(in);
        totalCount.readFields(in);
    }
}
