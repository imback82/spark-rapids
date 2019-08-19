/*
 * Copyright (c) 2019, NVIDIA CORPORATION.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.rapids.spark

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.col
import org.scalatest.{BeforeAndAfterEach, FunSuite}


class CsvScanSuite extends FunSuite with BeforeAndAfterEach with SparkQueryCompareTestSuite {

  testSparkResultsAreEqual("Test CSV", intsFromCsv) {
    frame => frame.select(col("ints_1"), col("ints_3"), col("ints_5"))
  }

  testSparkResultsAreEqual("Test CSV count", intsFromCsv)(frameCount)

  testSparkResultsAreEqual("Test partitioned CSV", intsFromPartitionedCsv) {
    frame => frame.select(col("partKey"), col("ints_1"), col("ints_3"), col("ints_5"))
  }

  private val smallSplitsConf = new SparkConf().set("spark.sql.files.maxPartitionBytes", "10")

  testSparkResultsAreEqual("Test CSV splits", intsFromCsv, conf=smallSplitsConf) {
    frame => frame.select(col("ints_1"), col("ints_3"), col("ints_5"))
  }

  testSparkResultsAreEqual("Test CSV splits with header", floatCsvDf, conf=smallSplitsConf) {
    frame => frame.select(col("*"))
  }

  testSparkResultsAreEqual("Test partitioned CSV splits", intsFromPartitionedCsv, conf=smallSplitsConf) {
    frame => frame.select(col("partKey"), col("ints_1"), col("ints_3"), col("ints_5"))
  }

  testSparkResultsAreEqual("Test CSV splits with chunks", floatCsvDf, conf= new SparkConf().set(
    "spark.rapids.sql.maxReaderBatchSize", "1")) {
    frame => frame.select(col("floats"))
  }

  testSparkResultsAreEqual("Test CSV count chunked", intsFromCsv, conf= new SparkConf().set(
    "spark.rapids.sql.maxReaderBatchSize", "1"))(frameCount)

  /**
    * Running with an inferred schema results in running things that are not columnar optimized.
    */
  ALLOW_NON_GPU_testSparkResultsAreEqual("Test CSV inferred schema", intsFromCsvInferredSchema) {
    frame => frame.select(col("*"))
  }
}