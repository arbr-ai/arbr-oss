package com.arbr.og.object_model.common.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

class ProposedValueStreamTest {

    @Test
    fun `sync proposal updates immediately`() {
        val pvs = ProposedValueStream<Int>(ProposedValueStreamIdentifierBase("", "", "", PropertyKeyRelationship.PARENT),null)
        pvs.proposeUpdate { 0 }
        Assertions.assertEquals(0, pvs.getLatestValue())
    }

    @Test
    fun `proposal after error reverts`() {
        val pvs = ProposedValueStream(ProposedValueStreamIdentifierBase("", "", "", PropertyKeyRelationship.PARENT),0)

        val attemptUpdate = pvs.proposeAsync { prev ->
            Assertions.assertEquals(0, prev)
            Mono.error(Exception())
        }.block()
        Assertions.assertEquals(0, attemptUpdate)

        val actuallyUpdate = pvs.proposeAsync { prev ->
            Assertions.assertEquals(0, prev)
            Mono.just(1)
        }.block()
        Assertions.assertEquals(1, actuallyUpdate)

        Assertions.assertEquals(1, pvs.getLatestValue())
    }

    @Test
    fun `async proposals chain together`() {
        val pvs = ProposedValueStream(ProposedValueStreamIdentifierBase("", "", "", PropertyKeyRelationship.PARENT),0)

        val update1 = pvs.proposeAsync { prev ->
            Assertions.assertEquals(0, prev)
            Mono.just(1)
        }
        val update2 = pvs.proposeAsync { prev ->
            Assertions.assertEquals(1, prev)
            Mono.just(2)
        }

        Assertions.assertEquals(0, pvs.getLatestValue())

        val res1 = update1.block()
        Assertions.assertEquals(1, res1)

        val res2 = update2.block()
        Assertions.assertEquals(2, res2)

        Assertions.assertEquals(2, pvs.getLatestValue())
    }

    @Test
    fun `updates with accepted and rejected proposals`() {
        val pvs = ProposedValueStream<Int>(ProposedValueStreamIdentifierBase("", "", "", PropertyKeyRelationship.PARENT),null)

        @Suppress("ReactiveStreamsUnusedPublisher")
        (Assertions.assertNull(pvs.getCombinedBatchProposal()))

        pvs.proposeUpdate { 1 }

        Assertions.assertEquals(1, pvs.getLatestValue())
        Assertions.assertNull(pvs.getLatestAcceptedValue())

        val prop = pvs.getCombinedBatchProposal()
        Assertions.assertNotNull(prop)
        prop!!.map { proposal ->
            Assertions.assertNull(proposal.acceptedValue)
            Assertions.assertEquals(1, proposal.proposedValue)
            proposal.accept()
        }.block()

        Assertions.assertEquals(1, pvs.getLatestAcceptedValue())

        // Propose and reject

        pvs.proposeUpdate { 2 }

        Assertions.assertEquals(2, pvs.getLatestValue())
        Assertions.assertEquals(1, pvs.getLatestAcceptedValue())

        val prop2 = pvs.getCombinedBatchProposal()
        Assertions.assertNotNull(prop2)
        prop2!!.map { proposal ->
            Assertions.assertEquals(1, proposal.acceptedValue)
            Assertions.assertEquals(2, proposal.proposedValue)
            proposal.reject()
        }.block()

        // No change
        Assertions.assertEquals(2, pvs.getLatestValue())
        Assertions.assertEquals(1, pvs.getLatestAcceptedValue())
    }

    @Test
    fun `updates with batch proposals`() {
        val pvs = ProposedValueStream<Int>(ProposedValueStreamIdentifierBase("", "", "", PropertyKeyRelationship.PARENT),null)

//        val acceptCounter = AtomicInteger(0)
//        pvs.getLatestAcceptedValueFlux().doOnNext {
//            println("Accepted $it")
//            acceptCounter.incrementAndGet()
//        }.subscribe()

//        assertEquals(1, acceptCounter.get())

        @Suppress("ReactiveStreamsUnusedPublisher")
        (Assertions.assertNull(pvs.getCombinedBatchProposal()))

        pvs.proposeAsync {
            Mono.just(1)
        }.block()!!

        Assertions.assertEquals(1, pvs.getLatestValue())
        Assertions.assertNull(pvs.getLatestAcceptedValue())

        pvs.proposeAsync {
            Mono.just(2)
        }.block()!!

        Assertions.assertEquals(2, pvs.getLatestValue())
        Assertions.assertNull(pvs.getLatestAcceptedValue())

//        assertEquals(2, acceptCounter.get()) // Why 2?

        val prop = pvs.getCombinedBatchProposal()
        Assertions.assertNotNull(prop)
        prop!!.map { proposal ->
            Assertions.assertNull(proposal.acceptedValue)
            Assertions.assertEquals(2, proposal.proposedValue)
            proposal.accept()
        }.block()

        Assertions.assertEquals(2, pvs.getLatestAcceptedValue())
//        assertEquals(3, acceptCounter.get())
    }

}