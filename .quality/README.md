# Quality gates

CRAP scores cannot rise, new or changed functions stay under 10, and new dryer pairs fail. Mutation scores cannot drop. Generated ANTLR sources are not scored.

Enable the hook with `git config core.hooksPath .githooks`, or run `scripts/install-git-hooks.sh`. The hook reads `target/site/jacoco/jacoco.xml` from the last test run.

Refresh a baseline with `scripts/quality/refresh-baseline.sh`. Pass `--with-mutation` to include PIT.
