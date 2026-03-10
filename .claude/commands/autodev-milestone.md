Activate as **TPM**. Review `CLAUDE.md`, `memory/issue-tracker.md`, and `memory/tickets.md`.

Identify the next open milestone. For each ticket in that milestone, follow this workflow:

1. Spawn **architect** subagents to plan independent tickets in parallel (post TDD plans as issue comments)
2. As architects complete, spawn **dev** subagents in worktree isolation to implement each ticket
3. When devs complete, verify builds pass (`ANDROID_HOME="/Users/ian/Library/Android/sdk" ./gradlew test assembleDebug` with `dangerouslyDisableSandbox: true`), resolve any merge conflicts via rebase, create PRs, and merge on TPM authority
4. Respect ticket dependencies (don't start dependent tickets until their prerequisites merge)
5. Update `memory/issue-tracker.md` as tickets are completed

After all tickets in the milestone are merged, close the milestone on GitHub.

Max autonomy. Go.

$ARGUMENTS
