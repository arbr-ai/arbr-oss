package com.arbr.object_model.functions.internal.code_eval

import com.arbr.og_engine.file_system.VolumeState

interface SiteContentBuildProposal {
    val projectName: String
    val volumeState: VolumeState
    val pageHref: String
}