package com.zynergylabs.forager.brouter.tiles

/**
 * Step 10, session 49 (RECORD -140, decision 1): stratum PM5's 55 rows of brouter/src/jvmTest/resources/e3/expected-1b.tsv
 * (memory class 5, the same draw sequence as ML5; every row a route), as [NtExpected] holds NT's: id, then kind, nodes, cost, distance, nodeDigest, heightsDigest (as
 * [EngineRun.Outcome]), then the stored tupleDigest (every PM5 row is `exact`). No row holds a position: the file stores
 * none and [NtExpected.Row]'s fields are an id, a kind, Ints and digests (E1); `Step10CommonDigestTest` holds this constant
 * equal to the file's rows on the JVM.
 */
internal object Pm5Expected {
    val ROWS: List<NtExpected.Row> = listOf(
        NtExpected.Row("M5-0010", EngineRun.Outcome("route", 968, 47337, 35123, "40d26f35156bc2b9", "375d64a69686ee"), "9c8defec6042ca62"),
        NtExpected.Row("M5-0025", EngineRun.Outcome("route", 985, 63760, 36515, "8850b8599eda8a9f", "fb905af06721bb39"), "f27e3cd8c7d0f6c9"),
        NtExpected.Row("M5-0042", EngineRun.Outcome("route", 2885, 129958, 88112, "5992be91faef4730", "ecf844f8fd38e0c6"), "144347618bb1e0ce"),
        NtExpected.Row("M5-0062", EngineRun.Outcome("route", 3537, 178600, 124146, "882ce0de83118b6a", "b4c90442a70d28ac"), "689267c9485332ed"),
        NtExpected.Row("M5-0063", EngineRun.Outcome("route", 1207, 55135, 33877, "8c68223ff5c6e58b", "4f8e69143900af5c"), "da329cf58407a255"),
        NtExpected.Row("M5-0109", EngineRun.Outcome("route", 4680, 151087, 106555, "94c686e051ddff19", "c5f3f79ac71614f3"), "42ac2855027958e4"),
        NtExpected.Row("M5-0115", EngineRun.Outcome("route", 3141, 157173, 112076, "ac0f37ddc5d55b71", "9db2134d29fd7729"), "14b5dfb576830f91"),
        NtExpected.Row("M5-0134", EngineRun.Outcome("route", 4610, 164503, 114871, "61b893c15e2c8f2c", "a73e8e25849b9374"), "c36c2cefa2a862b3"),
        NtExpected.Row("M5-0140", EngineRun.Outcome("route", 2682, 195160, 110447, "44226f2dec8d08d6", "17741520ed02d3fd"), "9f50f21986eecf7e"),
        NtExpected.Row("M5-0173", EngineRun.Outcome("route", 4738, 147202, 108955, "43544c989fd29329", "b62566514a1cf459"), "fa0eebf78756bffa"),
        NtExpected.Row("M5-0179", EngineRun.Outcome("route", 4507, 198192, 138568, "42ff1ad351d5efa0", "15dbb175fa15353a"), "2615545d7703ed23"),
        NtExpected.Row("M5-0211", EngineRun.Outcome("route", 1617, 55332, 38906, "4aa6d75e245919a1", "328ee1af35a92b12"), "5da33e5a60818f40"),
        NtExpected.Row("M5-0218", EngineRun.Outcome("route", 1621, 92544, 69154, "cbb94d9274dc29d8", "4560465b1501a465"), "d115192727fb430e"),
        NtExpected.Row("M5-0251", EngineRun.Outcome("route", 2330, 127855, 90280, "3ea3840590a886eb", "9c89ff5852d568b2"), "732c1df514b6eeaf"),
        NtExpected.Row("M5-0303", EngineRun.Outcome("route", 2483, 93747, 71568, "e8fe2b374cc6a330", "7f182c39316815a0"), "4b0f677bfad48321"),
        NtExpected.Row("M5-0304", EngineRun.Outcome("route", 2029, 96223, 75984, "936cf286a39f7292", "239ff750a0cdcca9"), "1fc400b5a8f2146e"),
        NtExpected.Row("M5-0340", EngineRun.Outcome("route", 1560, 64329, 46471, "24f0caf5a2f64d44", "62c00119baaf865a"), "4c0b63e9623843b4"),
        NtExpected.Row("M5-0369", EngineRun.Outcome("route", 3465, 146236, 105878, "6de51c1f1d415e8c", "bc69daf9833730be"), "7c40d361cec9b854"),
        NtExpected.Row("M5-0373", EngineRun.Outcome("route", 2645, 114558, 75141, "b9e7abbbfef937bc", "c442ce0a27721e85"), "ec65bd12331bcd7f"),
        NtExpected.Row("M5-0402", EngineRun.Outcome("route", 3100, 152706, 99865, "b68ed4959eb9814e", "e7a2012685e3f605"), "ff5e910e8d4f5dfc"),
        NtExpected.Row("M5-0407", EngineRun.Outcome("route", 2349, 82611, 62470, "172f98d9b6c6b2dd", "71ff585f495fc60f"), "27cae3be0c32d013"),
        NtExpected.Row("M5-0428", EngineRun.Outcome("route", 2707, 75967, 57660, "ce81fa5aaa820a98", "611b897049466ab9"), "fe8e64a05fdc978d"),
        NtExpected.Row("M5-0433", EngineRun.Outcome("route", 3723, 178589, 121906, "477183077b28c449", "4511c2f0cc26efe1"), "cf200097eeed5eaf"),
        NtExpected.Row("M5-0449", EngineRun.Outcome("route", 2305, 61123, 48172, "6cc55152c3ad62dd", "7c51c42317438377"), "633756282b9fbd5e"),
        NtExpected.Row("M5-0465", EngineRun.Outcome("route", 3413, 143963, 94127, "1a49da93f70a3982", "aee78bab98d7ebd8"), "c4a697072d80d59e"),
        NtExpected.Row("M5-0492", EngineRun.Outcome("route", 1245, 56463, 39278, "5a986a8be6a62a91", "7b1bf46a51e9a68e"), "9430e30c34bccf55"),
        NtExpected.Row("M5-0497", EngineRun.Outcome("route", 2712, 94998, 73837, "f4e3a3ef9a7f10db", "7e819f6ef00192d"), "b65260e3685c5041"),
        NtExpected.Row("M5-0506", EngineRun.Outcome("route", 5164, 129970, 98424, "ac7c9c5eece484d8", "23621cca5f8d4c6f"), "ff0548f7ef85ed17"),
        NtExpected.Row("M5-0513", EngineRun.Outcome("route", 1752, 60520, 45901, "c912b62882c57b6f", "a1c032e443acdd27"), "e3498ed1a73838f6"),
        NtExpected.Row("M5-0518", EngineRun.Outcome("route", 1111, 55281, 33746, "38cdb68543f40474", "189dfb2b5772301f"), "75de22b38b80fd94"),
        NtExpected.Row("M5-0561", EngineRun.Outcome("route", 3331, 131790, 98564, "3009669361a937e0", "9c86fafa68ff817a"), "34bf273fd8d6eeb4"),
        NtExpected.Row("M5-0567", EngineRun.Outcome("route", 3318, 219224, 139848, "9bcdb703e7f4d529", "e6c7fc5dbe26d637"), "ee1c889dda8506fc"),
        NtExpected.Row("M5-0588", EngineRun.Outcome("route", 2147, 76730, 59316, "be710fa0f2a27205", "92f702903e30ca00"), "a2ceddf5c116fd56"),
        NtExpected.Row("M5-0602", EngineRun.Outcome("route", 1495, 53017, 40248, "8a9677d0b06aa8a0", "908a762a9ee13b89"), "d6a56e48a023f00c"),
        NtExpected.Row("M5-0617", EngineRun.Outcome("route", 1352, 36865, 29952, "e7fcd6bf8fc90855", "2524d4ec9f1635c2"), "70ab5fb049a541dd"),
        NtExpected.Row("M5-0630", EngineRun.Outcome("route", 3951, 139936, 101642, "abeb24813fa6da04", "92839346eed0905e"), "44e9be3b19c8a508"),
        NtExpected.Row("M5-0648", EngineRun.Outcome("route", 1898, 65478, 47357, "6685e6a0525a7854", "bcbe546bcaaa3254"), "e53ed592798fa063"),
        NtExpected.Row("M5-0721", EngineRun.Outcome("route", 4042, 195902, 138326, "48bb030a2066764a", "7a294f984c23c504"), "2b4dc917adcc7521"),
        NtExpected.Row("M5-0751", EngineRun.Outcome("route", 1629, 67436, 47955, "b1ca4ee89691c967", "b37033b7a4d8f368"), "1e8bbcf4ba829c5c"),
        NtExpected.Row("M5-0753", EngineRun.Outcome("route", 4601, 220019, 141487, "3fe67254c9d5315e", "259a6b703565ef91"), "5dcc67d1109eee55"),
        NtExpected.Row("M5-0835", EngineRun.Outcome("route", 2917, 111732, 82628, "3b447f347dde9b16", "de8d99bd0a4abfc3"), "17ccec64ae46a110"),
        NtExpected.Row("M5-0928", EngineRun.Outcome("route", 2210, 75357, 56883, "923e6b67a9dcc60f", "15cb03743ae68d24"), "8eec2868b7258dd2"),
        NtExpected.Row("M5-0937", EngineRun.Outcome("route", 3523, 119117, 92469, "819fb1e1f6da81f5", "bb2bcb9d36874cfc"), "cb429f3f8d8f13eb"),
        NtExpected.Row("M5-0942", EngineRun.Outcome("route", 3821, 136332, 98539, "5dc921a6bfc7b095", "f8c51b1cb3c04e2f"), "d87bbd3ee194d0b"),
        NtExpected.Row("M5-0954", EngineRun.Outcome("route", 842, 30026, 23295, "4b28e8b535cef871", "69bc13e2f042540"), "b24d27706df8c81a"),
        NtExpected.Row("M5-0960", EngineRun.Outcome("route", 1237, 41699, 29552, "36d258f169250966", "3ea254ef4511c251"), "1832feb385a6bb8a"),
        NtExpected.Row("M5-1012", EngineRun.Outcome("route", 1054, 54572, 38550, "34c15c19cc4bfdc4", "bcfeb8cd4938ab36"), "dd64eba46dca28b1"),
        NtExpected.Row("M5-1014", EngineRun.Outcome("route", 1291, 63038, 46318, "863a91d33296ac3", "b5242c52670d26c9"), "d73a05268d6e7fd1"),
        NtExpected.Row("M5-1027", EngineRun.Outcome("route", 1872, 63483, 43246, "49f4335d95b0d45", "2cab802b06ae3db5"), "e8fe4ee1bf95875a"),
        NtExpected.Row("M5-1029", EngineRun.Outcome("route", 2739, 165122, 119511, "352a8649eeb613d4", "3a0b9b6bc2fa8050"), "3771a4b64f4f92a"),
        NtExpected.Row("M5-1071", EngineRun.Outcome("route", 2064, 152058, 79647, "6269c5db4e1b82e9", "a965f6ed033ee3fd"), "10cd4dd2c5616a90"),
        NtExpected.Row("M5-1085", EngineRun.Outcome("route", 3072, 132074, 96947, "7a9d1138a0d278b1", "a46234013dca0f85"), "63b89297611bab5e"),
        NtExpected.Row("M5-1136", EngineRun.Outcome("route", 3003, 134155, 90017, "a3f4c6b7f1a3e370", "bf352169dea8445f"), "d4bad63b296592ad"),
        NtExpected.Row("M5-1146", EngineRun.Outcome("route", 1835, 65998, 47481, "207323104cf4b7e6", "21ee8bd5e4f3f3dc"), "9fd45b645d15e223"),
        NtExpected.Row("M5-1180", EngineRun.Outcome("route", 1860, 61202, 48149, "d263521be41ebfbf", "475dd501495198d9"), "7484935617364767"),
    )
}
