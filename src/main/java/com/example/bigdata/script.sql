CREATE EXTERNAL TABLE IF NOT EXISTS data3 (
    developer_id STRING,
    release_year INT,
    total_rating_sum DOUBLE,
    total_rating_count INT,
    total_app_count INT
)
    ROW FORMAT DELIMITED
        FIELDS TERMINATED BY ';'
    STORED AS TEXTFILE
    LOCATION '${hivevar:input_dir3}';


CREATE EXTERNAL TABLE IF NOT EXISTS data4 (
    developer_name STRING,
    developer_website STRING,
    developer_email STRING,
    developer_id STRING
)
    ROW FORMAT DELIMITED
        FIELDS TERMINATED BY '\u0001'
    STORED AS TEXTFILE
    LOCATION '${hivevar:input_dir4}';

CREATE EXTERNAL TABLE IF NOT EXISTS developer_summary (
    year INT,
    developer_name STRING,
    avg_rate DOUBLE,
    count_rates INT,
    count_apps INT
)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.JsonSerDe'
STORED AS TEXTFILE
LOCATION '${hivevar:output_dir6}';

INSERT OVERWRITE TABLE developer_summary
SELECT
    year,
    developer_name,
    avg_rate,
    count_rates,
    count_apps
FROM (
         SELECT
             d3.release_year AS year,
             d4.developer_name,
             d3.total_rating_sum / d3.total_app_count AS avg_rate,
             d3.total_rating_count AS count_rates,
             d3.total_app_count AS count_apps,
             ROW_NUMBER() OVER (PARTITION BY d3.release_year ORDER BY d3.total_rating_sum / d3.total_app_count DESC, d3.total_rating_count DESC, d3.total_app_count DESC) AS rank
         FROM
             data3 d3
                 JOIN
             data4 d4 ON d3.developer_id = d4.developer_id
     ) ranked_developers
WHERE
    rank <= 3
ORDER BY
    year,
    avg_rate DESC,
    count_rates DESC,
    count_apps DESC;


SELECT *
FROM developer_summary
ORDER BY year, avg_rate DESC, count_rates DESC, count_apps DESC;

