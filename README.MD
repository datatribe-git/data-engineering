
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

### Profiling
1. definition and code, , how it does that?
### Partitioning 
1. definition and code, how it does that?
### Anomaly detection // Optional
### What happens after that
1. Each code line explanation
2. Verification Result explanation
### Incremental computation // Optional
### Conclusion