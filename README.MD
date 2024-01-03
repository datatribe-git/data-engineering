
### What is Deequ?
Deequ is a library built on top of Apache Spark for defining "unit tests for data", which measure data quality not only in the small datasets but also at scale for large datasets. 
It's built on top of Apache Spark and uses Spark's distributed computation engine to define data quality checks. 
It's a declarative API combining common quality constraints with the user-defined validation code, which enables the "unit tests" for data. 
Essentially, it's a series of data quality checks that can run within the ETL pipeline - to ensure the data quality is maintained throughout the data pipeline. The tool was developed by Amazon and is now open source.

### Dimensions of Data Quality
Deequ defines below dimensions of data quality:
- **Completeness**: The data should not have missing values. It refers to the degree to which an entity includes data required to describe a real-world object.
- **Consistency**: It refers to the degree to which a set of semantic rules are violated by the data.
- **Intra-relation constraints**: These define the range of admissible values for a column in a table, or the allowed combinations of values across columns in a table, or the allowed combinations of values across tables and also specific data types.
- **Accuracy**: Refers to the correctness of data, measured in two ways:
    - **Syntactic**: Represents the degree to which the data conforms to the defined schema.
    - **Semantic**: Compares the data to the real-world object it represents.
    - For eg., a column that stores the age of a person should not have a value greater than 100 - is considered as a semantic constraint whereas a column considering a person's age as a string is considered as a syntactic constraint.

### Deequ Approach
Deequ platform efficiently executes the resulting constraint validation workload by translating it to aggregation queries on Apache Spark.
The platform supports the incremental validation of data quality on growing datasets, and leverages machine learning, 
e.g., for enhancing constraint suggestions, for estimating the ‘predictability’ of a column, and for detecting anomalies in historic data quality time series.
What's more, the platform provides a domain-specific language for defining constraints, and a set of built-in data quality checks.
The declarative API allows users to define checks for their datasets - which results in error or warnings during execution. This means we can control the ETL flow to downstream systems based on the data quality checks. 
This is quite effective because at the time of creating the data pipeline, we can define the data quality checks and the pipeline will fail if the data quality checks fail. And, we never know when the source systems behave over the period of the time. 
Having these quality checks embedded in the pipeline will help in the data operations in the long run.

### How does a constraint look like?
| Constraint | Arguments        | Semantic Type                                                            |
| --- |------------------|--------------------------------------------------------------------------|
| isComplete | column name      | Check that there are no missing values in a column                       |
| hasCompleteness | column name, udf | custom validation - checks for the fraction of missing values in a column |
| isUnique | column name      | Check that there are no duplicates in a column                           |

### General Syntax
```scala
var checks = Array()

val yourDataFrame = spark.read.parquet("s3://your-bucket/your-data")

// TODO: Do we have any checkLevel as in Info?
 
checks += Check(Level.Error) // We have 2 levels of checks - Error and Warning. Error will fail the pipeline if the check fails. Warning will only log the error.
.isComplete("column_name")
.isUnique("column_name")
.isLessThan("column_name", 100) 
 
val verificationResult: VerificationResult = VerificationSuite()
    .onData(yourDataFrame)
    .addChecks(checks)
    .run()
```
Here you could see how to define the data quality checks in the pipeline and the pipeline will fail if the data quality checks fail.
We have used few constraints like isComplete, isUnique, isLessThan, which will produce an error if the data quality check fails. 
It literally means that the pipeline will fail if any of the constraints fall into this category. Let's see an example.

### Deequ Example
```scala

val spotifyAlbumsDF = spark.read.parquet("s3://your-bucket/your-data").filter("year = 2010")

val spotifyAlbumsVerificationResults = VerificationSuite()
.onData(spotifyAlbumsDF)
.addCheck(
  Check(CheckLevel.Error, "UnitTest")
  .hasSize(_ == 3410)
).run()

val resultsForAllConstraints = spotifyAlbumsVerificationResults.checkResults
    .flatMap { case (_, checkResult) => checkResult.constraintResults }

  resultsForAllConstraints
    .filter { _.status != ConstraintStatus.Success }
    .foreach { result => println(s"${result.constraint}: ${result.message.get}") }
 
```
Here in this simple example, we are reading the data from S3 and filtering the data for the year 2010. 
Then we are defining a constraint to check the size of the data. In this case, we are expecting the size of the data to be 3410, but the actual size of the data is 3409.
So, the pipeline will fail with the below error message:
```
We found errors in the data:

SizeConstraint(Size(None)): Value: 3409 does not meet the constraint requirement!
import com.amazon.deequ.constraints.ConstraintStatus   
```
This is a hypothetical example of setting a constraint on table size unless it's a dimension or a static lookup table, but you could see how the pipeline fails if the data quality check fails.

### Versioning
Deequ is available in Maven Central and Spark Packages. One needs to look at the compatibility matrix to find the right version of Deequ for the Spark version.

|Deequ Version|Spark Version| Scala Version | Java Version  |
|---|---|---------------| ------------- |
|2.0.1|3.2.1| 2.12.10       |1.8 |
|2.0.6|3.4.1| 2.12.10       |1.8|

More details can be found [here](https://repo1.maven.org/maven2/com/amazon/deequ/deequ/).
If these details are missing or incompatible with the pom.xml or build.sbt, then the build will fail.
Similarly, cluster configuration on databricks should be compatible with the spark and scala versions only, while the java version can be different.

### Profiling
1. definition and code, , how it does that?
### Partitioning 
1. definition and code, how it does that?
### Anomaly detection // Optional
### What happens after that
1. Each code line explanation
2. Verification Result explanation
### Incremental computation // Optional

### Limitations
Deequ is not a data quality tool, it's a data quality library. It's not a tool like Informatica or Talend, which can be used to profile the data and generate reports.
It should be considered, indeed more of a data quality library than a standalone data quality tool.
It is not primarily focused on data profiling or generating reports in the same way as Informatica or Talend. 
Deequ, on the other hand, is more developer-centric and integrates with Spark for scalable and distributed data processing.
Deequ also does not have a UI, which means it's not a tool for business users. It is only meant for developers and data engineers.
Perhaps, the biggest limitation of Deequ is that it does not support all the data sources. It only supports Spark data sources. 
The data source needs to be brought in as a spark dataframe and then only the data quality checks can be applied.
Since it is heavily Apache Spark based, its not a tool for Snowflake. Java dependencies are not compatible with Snowflake, JVM for Scala needs Java 11.x whereas Deequ is still compatible with Java 8, so there are not much of a possibility - but need to double-check!

### Conclusion
1. If the ETL pipelines are built on top of Spark, then Deequ comes in place as a handy tool to define the data quality checks, which gets embedded in the pipeline. 
2. It's declarative API style makes it easy to define the data quality checks and the pipeline will fail if the data quality checks fail.
3. It's a great engineering project, leveraging Spark's distributed computation engine to pass the data quality checks/constraints in a optimized SparkSQL query. The query execution engines are generally faster in terms of implementation.
4. This tool also gives recommendations for every column in the dataset - based on the machine learning model trained on the large datasets. This gives some sort of certainty on the data quality checks defined.
5. It's focussed heavily on the developer community, it's not a tool for business users, unless they write the spark code to define the data quality checks.
