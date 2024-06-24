package com.arbr.types.homotopy

import com.arbr.types.homotopy.config.HomotopyConfigLoader

/**
 * Default entrypoint into static Homotopy functionality via loaded configuration
 */
object Homotopy: HomotopyProvider by DefaultHomotopyProvider(
    HomotopyConfigLoader().load()
)
