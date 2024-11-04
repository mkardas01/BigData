-- Tabela dla input_dir3
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


-- Tabela tymczasowa dla input_dir4
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


INSERT OVERWRITE DIRECTORY '${hivevar:output_dir6}'
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.JsonSerDe'
SELECT
    d4.developer_name AS developer_name,
    d3.release_year AS year,
    d3.total_rating_sum / d3.total_app_count AS avg_rate,
    d3.total_app_count AS count_apps,
    d3.total_rating_count AS count_rates
FROM
    data3 d3
        JOIN
    data4 d4
    ON
        d3.developer_id = d4.developer_id
ORDER BY avg_rate DESC
LIMIT 3;
