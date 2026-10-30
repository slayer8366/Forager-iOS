# Step 5a revert: prediction, written and pushed before the run

**Edit (one line, `ReadOnlyFile.readFully`, the short-read check of item B row 1):**
`if (count <= 0) throw EOFException()` becomes `if (count <= 0) break`.
`break` rather than deleting the line: with the line deleted, okio's -1 at the end of the file would be added to `n`
and the loop would never end. With `break` the loop still ends within `len` passes.

**Mechanism.** On a cut inside the 200-byte top index, the header read stops short and the rest of the buffer keeps the
fresh `DataBuffers`' zeros. `fileIndex[24]` then reads as 0 (cut 1 or 100) and the length check
(`PhysicalFile.java:121`) fires instead of the `EOFException`.

**Predicted:**
- `jvmTest`: 91 tests, **exactly 1 failure, `FileFailureKindsTest.fk7DataCorruptShort`**, at its first case (cut to 1),
  on the `checkDataFile` comparison: expected `EOFException`, actual
  `file of size 1 too short, should be 112` (0 + 8 + 26 * 4).
- No other test fails: FK 6's files are absent (okio's open throws first), FK 8's copies are full length, and the
  captured tiles are whole, so none of them meets a short read.
- 0 compile errors.
