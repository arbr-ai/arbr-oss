# model-suite

Context-agnostic definitions of predictive models as parametric programs over more primitive data structures.

## Dependency Scope

Model definitions will need reactor, primitives from the `ml` module, and possibly definitions from `alignable` and `parsers`.

Other 3rd-party: slf4j, jackson.

Spring, Kafka, and any other networking are all out of scope.

Models should always remain parametric to their weights and allow a more gnostic consumer to manage loading of weights in context.

Primitives that might be reused in a number of places should live in a more basic repo. Only specific model definitions should be exposed.

Models should have a small and simple interface.

## List of Models

- LinearTreeIndentPredictor
- DocumentDiffAlignmentPredictor
- SimpleTextDiffAlignmentPredictor

## Roadmap

- Integrate tensor-typed inputs and outputs
