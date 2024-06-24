package com.arbr.platform.ml.logging

class LossTracker {
    enum class Source {
        INIT,
        GRID,
        CVXL,
        GRAD,
    }

    private var lossValue = 1.0
    private var source: Source = Source.INIT

    @Synchronized
    fun set(source: Source, value: Double) {
        this.source = source
        lossValue = value
    }

    fun getMdcKey(): String {
        return getMdcKey(source)
    }

    @Synchronized
    override fun toString(): String {
        val innerValue = lossValue
        return String.format("%.06f <- ${source.name}", innerValue)
    }

    companion object {
        private fun getMdcKey(source: Source): String {
            return "training_loss_${source.name.lowercase()}"
        }
    }
}
