package vet.inpulse.geolocation.server.test

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder

@State(Scope.Benchmark)
open class BenchmarkRunner {

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun runServiceBenchMark(blackhole: Blackhole) {
        val benchmark = vet.inpulse.geolocation.server.test.Benchmark()
        benchmark.setup()
        benchmark.endpointBenchmark(blackhole)
        benchmark.teardown()
    }
}

fun main() {
    val options = OptionsBuilder()
        .include(BenchmarkRunner::class.java.simpleName)
        .forks(1)
        .build()
    Runner(options).run()
}