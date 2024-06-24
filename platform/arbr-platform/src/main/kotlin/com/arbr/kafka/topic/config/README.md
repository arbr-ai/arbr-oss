# Kafka Streaming API for state updates

Kafka lets us easily pass messages between entities in our backend, and includes streaming APIs.

It does not provide a system for maintaining aggregated state as a byproduct of differential updates.

To reflect workflow state to the frontend, we need to ingest workflow state updates, apply them in order, store them in the database with an associated indicator of the offset, and stream updates to the frontend whenever it is requested.

Importantly, we need to navigate the following cases:
* The API server(s) serving the stream(s) to the frontend may be different than the server ingesting incremental updates
* There may be multiple clients requesting streams[
* The client may disconnect, then later reconnect, expecting no loss o]()f accuracy

## API

Roughly:
- POST /workflows
    
    <- id
- GET /workflows/{id}/events

  -> hash?
  
  <- stream: [Sum dM[h+1] ... dM[t], dM[t+1], dM[t+2], ...]

In other words, the client should keep track of the hash value associated with the latest model version, as provided by the backend with each update, as well as the aggregated client-side model. Then, if the stream disconnects, such as if the user leaves the page, the client may request a new event stream starting from that hash.

The backend would then combine updates from immediately after the hash point until the present, send the aggregated difference as the first stream element, then continue with live updates.

As a special case, the initial stream request would have no hash, so the backend would serve the aggregation from t0 to the current time, then continue on to stream live updates.

A familiar system with similar behavior is git fetch.

## Components

### Differential Model

An API resource constructed by the sum of not-necessarily-commutative edit operations. It has a dual representation as a combined state and a sequence of diffs.

### Reducer

For a particular type of model and its differential operations, an actor which combines state with a diff to achieve the next state.

In the case the state is tracked via a hash, the reducer should also produce the next has as a result of applying the diff. The hash need not be a pure function of the result state, but a given `(state, hash, diff)` input triple should produce a consistent next state and next hash.

### Sink

A sink for a differential model is a special kind of consumer which both consumes live updates and which presents a query API for forwarding differential message streams after a given tracking hash.
