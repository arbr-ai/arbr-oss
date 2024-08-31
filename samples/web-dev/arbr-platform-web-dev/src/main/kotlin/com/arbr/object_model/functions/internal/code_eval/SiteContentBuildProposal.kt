package com.arbr.object_model.functions.internal.code_eval

import com.arbr.platform.object_graph.file_system.VolumeState

interface SiteContentBuildProposal {
    val projectName: String
    val volumeState: VolumeState
    val pageHref: String
}