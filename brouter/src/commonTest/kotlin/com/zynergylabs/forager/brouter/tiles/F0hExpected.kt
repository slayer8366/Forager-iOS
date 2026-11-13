package com.zynergylabs.forager.brouter.tiles

/**
 * Step 10, session 48 (RECORD -139, decision 2): stratum F0h's 8 rows of brouter/src/jvmTest/resources/e3/expected-1b.tsv,
 * as [NtExpected] holds NT's: id, then kind, nodes, cost, distance, nodeDigest, heightsDigest (as [EngineRun.Outcome]),
 * then the stored tupleDigest (every F0h row is `exact`). No row holds a position: the file stores none and the ends are not
 * here (they are written at run time, as the other groups'); `Step10CommonDigestTest` holds this constant to the file.
 */
internal object F0hExpected {
    val ROWS: List<NtExpected.Row> = listOf(
        NtExpected.Row("F0h-inside-across-the-degree-line", EngineRun.Outcome("route", 226, 5302, 5047, "9ca48d05a90f4dcd", "602d4109bb916eca"), "45ed7bcd65f49b1a"),
        NtExpected.Row("F0h-inside-short", EngineRun.Outcome("route", 167, 2889, 2573, "80ed7bc76cd49b14", "7dd5bc2f418a942e"), "ad0126fcb075b6b4"),
        NtExpected.Row("F0h-inside-long", EngineRun.Outcome("route", 306, 7405, 6713, "7d242f8dd2c428b4", "81ca705bd6652037"), "83e16f2859107fd2"),
        NtExpected.Row("F0h-edge-north-a", EngineRun.Outcome("route", 485, 11142, 10253, "80b91e6cdecbca6b", "44ac5ed74f690531"), "cb65b8767e45d6b6"),
        NtExpected.Row("F0h-edge-north-b", EngineRun.Outcome("route", 522, 12137, 11107, "9a6e1ba76433eb26", "3bf34cc4b7d132bb"), "91c3accab94ad9e1"),
        NtExpected.Row("F0h-detour-west", EngineRun.Outcome("route", 445, 9274, 7619, "8e7413e4588b005a", "78f521e1799fa575"), "1dfde04b19101cd6"),
        NtExpected.Row("F0h-outside-west-island", EngineRun.Outcome("route", 737, 15203, 13997, "724a6e49733ac0d2", "144b2f26f2ecec83"), "3630e4560afa4924"),
        NtExpected.Row("F0h-outside-west-moved", EngineRun.Outcome("route", 742, 15494, 13908, "3b0c5f82536d5789", "ee68cd97a2dfa1b5"), "3830e4264cbd7434"),
    )
}
