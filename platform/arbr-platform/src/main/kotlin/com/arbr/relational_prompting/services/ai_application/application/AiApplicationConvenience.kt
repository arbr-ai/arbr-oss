package com.arbr.relational_prompting.services.ai_application.application

import com.arbr.og.object_model.common.values.SourcedValue
import com.arbr.og.object_model.common.values.collections.*
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono


fun <S1 : SourcedValue<*>, O : SourcedStruct> AiApplication<SourcedStruct1<S1>, O>.invoke(
    s1: S1,
    artifactSink: FluxSink<ApplicationArtifact>
): Mono<O> = invoke(SourcedStruct1(s1), artifactSink)

fun <S1 : SourcedValue<*>, S2 : SourcedValue<*>, O : SourcedStruct> AiApplication<SourcedStruct2<S1, S2>, O>.invoke(
    s1: S1,
    s2: S2,
    artifactSink: FluxSink<ApplicationArtifact>
): Mono<O> = invoke(SourcedStruct2(s1, s2), artifactSink)

fun <S1 : SourcedValue<*>, S2 : SourcedValue<*>, S3 : SourcedValue<*>, O : SourcedStruct> AiApplication<SourcedStruct3<S1, S2, S3>, O>.invoke(
    s1: S1,
    s2: S2,
    s3: S3,
    artifactSink: FluxSink<ApplicationArtifact>
): Mono<O> = invoke(SourcedStruct3(s1, s2, s3), artifactSink)

fun <S1 : SourcedValue<*>, S2 : SourcedValue<*>, S3 : SourcedValue<*>, S4 : SourcedValue<*>, O : SourcedStruct> AiApplication<SourcedStruct4<S1, S2, S3, S4>, O>.invoke(
    s1: S1,
    s2: S2,
    s3: S3,
    s4: S4,
    artifactSink: FluxSink<ApplicationArtifact>
): Mono<O> = invoke(SourcedStruct4(s1, s2, s3, s4), artifactSink)

fun <S1 : SourcedValue<*>, S2 : SourcedValue<*>, S3 : SourcedValue<*>, S4 : SourcedValue<*>, S5 : SourcedValue<*>, O : SourcedStruct> AiApplication<SourcedStruct5<S1, S2, S3, S4, S5>, O>.invoke(
    s1: S1,
    s2: S2,
    s3: S3,
    s4: S4,
    s5: S5,
    artifactSink: FluxSink<ApplicationArtifact>
): Mono<O> = invoke(SourcedStruct5(s1, s2, s3, s4, s5), artifactSink)

fun <S1 : SourcedValue<*>, S2 : SourcedValue<*>, S3 : SourcedValue<*>, S4 : SourcedValue<*>, S5 : SourcedValue<*>, S6 : SourcedValue<*>, O : SourcedStruct> AiApplication<SourcedStruct6<S1, S2, S3, S4, S5, S6>, O>.invoke(
    s1: S1,
    s2: S2,
    s3: S3,
    s4: S4,
    s5: S5,
    s6: S6,
    artifactSink: FluxSink<ApplicationArtifact>
): Mono<O> = invoke(SourcedStruct6(s1, s2, s3, s4, s5, s6), artifactSink)

fun <S1 : SourcedValue<*>, S2 : SourcedValue<*>, S3 : SourcedValue<*>, S4 : SourcedValue<*>, S5 : SourcedValue<*>, S6 : SourcedValue<*>, S7 : SourcedValue<*>, O : SourcedStruct> AiApplication<SourcedStruct7<S1, S2, S3, S4, S5, S6, S7>, O>.invoke(
    s1: S1,
    s2: S2,
    s3: S3,
    s4: S4,
    s5: S5,
    s6: S6,
    s7: S7,
    artifactSink: FluxSink<ApplicationArtifact>
): Mono<O> = invoke(SourcedStruct7(s1, s2, s3, s4, s5, s6, s7), artifactSink)

fun <S1 : SourcedValue<*>, S2 : SourcedValue<*>, S3 : SourcedValue<*>, S4 : SourcedValue<*>, S5 : SourcedValue<*>, S6 : SourcedValue<*>, S7 : SourcedValue<*>, S8 : SourcedValue<*>, O : SourcedStruct> AiApplication<SourcedStruct8<S1, S2, S3, S4, S5, S6, S7, S8>, O>.invoke(
    s1: S1,
    s2: S2,
    s3: S3,
    s4: S4,
    s5: S5,
    s6: S6,
    s7: S7,
    s8: S8,
    artifactSink: FluxSink<ApplicationArtifact>
): Mono<O> = invoke(SourcedStruct8(s1, s2, s3, s4, s5, s6, s7, s8), artifactSink)
