package tools.vitruv.methodologisttemplate.vsum.mafds;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Performance test class to measure execution times of tests in
 * DampingRatioTest and TotalMassTest.
 * This class runs the individual test methods from both test classes
 * and measures their execution times to analyze performance characteristics.
 * 
 * @author Claus Hammann
 */
public class PerformanceTest {

    private DampingRatioTest dampingRatioTestInstance;
    private TotalMassTest totalMassTestInstance;

    @Test
    @DisplayName("Performance Test: Measure DampingRatio and TotalMass Test Execution Times")
    void measureDampingRatioAndTotalMassTestPerformance(@TempDir Path tempDir) {
        // Initialize static resources needed for the tests
        // Only TotalMassTest has a setup method
        TotalMassTest.setup();

        dampingRatioTestInstance = new DampingRatioTest();
        totalMassTestInstance = new TotalMassTest();

        List<TestResult> results = new ArrayList<>();

        // Test 1: DampingRatio - Add Damper System
        System.out.println(
                "Running performance tests for DampingRatioTest and TotalMassTest...\n");

        TestResult result1 = measureTestExecution(
                "DampingRatio - addDamperSystemTest",
                () -> dampingRatioTestInstance.addDamperSystemTest(tempDir.resolve("test1")));
        results.add(result1);

        // Test 2: DampingRatio - Add Damper System with Uncertainty
        TestResult result2 = measureTestExecution(
                "DampingRatio - addDamperSystemWithUncertaintyTest",
                () -> dampingRatioTestInstance.addDamperSystemWithUncertaintyTest(tempDir.resolve("test2")));
        results.add(result2);

        // Test 3: DampingRatio - Add Damper System with Uncertainty and StoEx
        TestResult result3 = measureTestExecution(
                "DampingRatio - addDamperSystemWithUncertaintyAndStoExTest",
                () -> dampingRatioTestInstance.addDamperSystemWithUncertaintyAndStoExTest(tempDir.resolve("test3")));
        results.add(result3);

        // Test 4: TotalMass - Add Damper System
        TestResult result4 = measureTestExecution(
                "TotalMass - addDamperSystemTest",
                () -> totalMassTestInstance.addDamperSystemTest(tempDir.resolve("test4")));
        results.add(result4);

        // Test 5: TotalMass - Add Damper System with Uncertainty
        TestResult result5 = measureTestExecution(
                "TotalMass - addDamperSystemWithUncertaintyTest",
                () -> totalMassTestInstance.addDamperSystemWithUncertaintyTest(tempDir.resolve("test5")));
        results.add(result5);

        // Test 6: TotalMass - Add Damper System with Uncertainty and StoEx
        TestResult result6 = measureTestExecution(
                "TotalMass - addDamperSystemWithUncertaintyAndStoExTest",
                () -> totalMassTestInstance.addDamperSystemWithUncertaintyAndStoExTest(tempDir.resolve("test6")));
        results.add(result6);

        // Print comprehensive results
        printPerformanceResults(results);

        // Run multiple iterations for more accurate measurements
        System.out.println("\n" + "=".repeat(70));
        System.out.println("RUNNING MULTIPLE ITERATIONS FOR STATISTICAL ANALYSIS");
        System.out.println("=".repeat(70));

        runMultipleIterations(tempDir, 100);
    }

    private TestResult measureTestExecution(String testName, Runnable testMethod) {
        // System.out.println("Measuring: " + testName);

        long startTime = System.nanoTime();
        long startMemory = getUsedMemory();

        try {
            testMethod.run();

            long endTime = System.nanoTime();
            long endMemory = getUsedMemory();

            long executionTimeNanos = endTime - startTime;
            long memoryUsed = endMemory - startMemory;

            TestResult result = new TestResult(testName, executionTimeNanos, memoryUsed, true);

            // System.out.println(" ✓ Completed in " + formatTime(executionTimeNanos));
            // System.out.println(" Memory change: " + formatMemory(memoryUsed));
            // System.out.println();

            return result;

        } catch (Exception e) {
            long endTime = System.nanoTime();
            long executionTimeNanos = endTime - startTime;

            // System.out.println(" ✗ Failed after " + formatTime(executionTimeNanos));
            // System.out.println(" Error: " + e.getMessage());
            // System.out.println();

            return new TestResult(testName, executionTimeNanos, 0, false);
        }
    }

    private void runMultipleIterations(Path tempDir, int iterations) {
        List<List<TestResult>> allResults = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            System.out.println("Iteration " + (i + 1) + "/" + iterations);

            List<TestResult> iterationResults = new ArrayList<>();

            // Create separate temp directories for each iteration
            Path iterationTempDir = tempDir.resolve("iteration" + i);

            // Run each test
            iterationResults.add(measureTestExecution(
                    "DampingRatio - addDamperSystemTest",
                    () -> dampingRatioTestInstance.addDamperSystemTest(iterationTempDir.resolve("test1"))));

            iterationResults.add(measureTestExecution(
                    "DampingRatio - addDamperSystemWithUncertaintyTest",
                    () -> dampingRatioTestInstance
                            .addDamperSystemWithUncertaintyTest(iterationTempDir.resolve("test2"))));

            iterationResults.add(measureTestExecution(
                    "DampingRatio - addDamperSystemWithUncertaintyAndStoExTest",
                    () -> dampingRatioTestInstance
                            .addDamperSystemWithUncertaintyAndStoExTest(iterationTempDir.resolve("test3"))));

            iterationResults.add(measureTestExecution(
                    "TotalMass - addDamperSystemTest",
                    () -> totalMassTestInstance.addDamperSystemTest(iterationTempDir.resolve("test4"))));

            iterationResults.add(measureTestExecution(
                    "TotalMass - addDamperSystemWithUncertaintyTest",
                    () -> totalMassTestInstance
                            .addDamperSystemWithUncertaintyTest(iterationTempDir.resolve("test5"))));

            iterationResults.add(measureTestExecution(
                    "TotalMass - addDamperSystemWithUncertaintyAndStoExTest",
                    () -> totalMassTestInstance.addDamperSystemWithUncertaintyAndStoExTest(
                            iterationTempDir.resolve("test6"))));

            allResults.add(iterationResults);
            System.out.println();
        }

        // Calculate and print statistics
        printStatisticalAnalysis(allResults);
    }

    private void printPerformanceResults(List<TestResult> results) {
        System.out.println("=".repeat(70));
        System.out.println("PERFORMANCE TEST RESULTS");
        System.out.println("=".repeat(70));

        long totalTime = 0;
        for (TestResult result : results) {
            System.out.printf("%-45s | %12s | %10s | %s%n",
                    result.testName,
                    formatTime(result.executionTimeNanos),
                    formatMemory(result.memoryUsed),
                    result.success ? "✓" : "✗");
            if (result.success) {
                totalTime += result.executionTimeNanos;
            }
        }

        System.out.println("-".repeat(70));
        System.out.printf("%-45s | %12s | %10s |%n",
                "TOTAL",
                formatTime(totalTime),
                "");
        System.out.println("=".repeat(70));
    }

    private void printStatisticalAnalysis(List<List<TestResult>> allResults) {
        System.out.println("=".repeat(80));
        System.out.println("STATISTICAL ANALYSIS (Multiple Iterations)");
        System.out.println("=".repeat(80));

        String[] testNames = {
                "DampingRatio - addDamperSystemTest",
                "DampingRatio - addDamperSystemWithUncertaintyTest",
                "DampingRatio - addDamperSystemWithUncertaintyAndStoExTest",
                "TotalMass - addDamperSystemTest",
                "TotalMass - addDamperSystemWithUncertaintyTest",
                "TotalMass - addDamperSystemWithUncertaintyAndStoExTest"
        };

        for (int testIndex = 0; testIndex < testNames.length; testIndex++) {
            List<Long> times = new ArrayList<>();

            for (List<TestResult> iteration : allResults) {
                if (testIndex < iteration.size() && iteration.get(testIndex).success) {
                    times.add(iteration.get(testIndex).executionTimeNanos);
                }
            }

            if (!times.isEmpty()) {
                long min = times.stream().mapToLong(Long::longValue).min().orElse(0);
                long max = times.stream().mapToLong(Long::longValue).max().orElse(0);
                double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);

                System.out.println("\n" + testNames[testIndex] + ":");
                System.out.println(" Minimum: " + formatTime(min));
                System.out.println(" Maximum: " + formatTime(max));
                System.out.println(" Average: " + formatTime((long) avg));
                System.out.println(" Samples: " + times.size());
            }
        }

        System.out.println("\n" + "=".repeat(80));
    }

    private String formatTime(long nanos) {
        if (nanos < 1_000) {
            return nanos + " ns";
        } else if (nanos < 1_000_000) {
            return String.format("%.2f μs", nanos / 1_000.0);
        } else if (nanos < 1_000_000_000) {
            return String.format("%.2f ms", nanos / 1_000_000.0);
        } else {
            return String.format("%.2f s", nanos / 1_000_000_000.0);
        }
    }

    private String formatMemory(long bytes) {
        if (bytes == 0) {
            return "0 B";
        }

        long absBytes = Math.abs(bytes);
        if (absBytes < 1024) {
            return bytes + " B";
        } else if (absBytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * Data class to hold test execution results
     */
    private static class TestResult {
        final String testName;
        final long executionTimeNanos;
        final long memoryUsed;
        final boolean success;

        TestResult(String testName, long executionTimeNanos, long memoryUsed, boolean success) {
            this.testName = testName;
            this.executionTimeNanos = executionTimeNanos;
            this.memoryUsed = memoryUsed;
            this.success = success;
        }
    }
}
