# 5. Root Safety, Rollback Protection, and Open Governance

* Status: Accepted
* Date: 2026-03-06

## Context
For advanced users with rooted devices or custom ROMs, optional system-level patches can enhance compatibility. However, modifying system partitions carries the risk of bootloops or bricking if not handled with extreme care.

## Decision
1. **Privileged Module Safety Safeguards (`hcs-privileged`)**:
   - Every file modification must create a full local backup before writing changes.
   - SHA-256 hashes must be verified before and after applying patches.
   - A dry-run / simulation mode must be supported.
   - 1-click clean rollback must be available at all times.
   - A bootloop guard must prevent persistent system failures.
2. **Open Governance**:
   - Maintain clear `CONTRIBUTING.md` and `SECURITY.md` policies for community security disclosures.

## Consequences
- Rooted users can safely test optional system patches with guaranteed rollback.
- Zero risk of unrecoverable system corruption.
