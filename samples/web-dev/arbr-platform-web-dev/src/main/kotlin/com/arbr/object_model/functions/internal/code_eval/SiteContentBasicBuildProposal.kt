package com.arbr.object_model.functions.internal.code_eval

import com.arbr.og_engine.file_system.VolumeState

data class SiteContentBasicBuildProposal(
    override val projectName: String,
    override val volumeState: VolumeState,
    override val pageHref: String,
): SiteContentBuildProposal