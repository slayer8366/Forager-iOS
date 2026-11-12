#!/bin/sh
# usage: 04-api-scan.sh <export root> <out dir>. Import scan of app/src/main, one line per API family:
# family, files, then files per top-level package. The file lists go to <out dir>/04-files-<family>.txt.
# An import scan is an upper/lower bound only: it misses APIs used without an import (Math, String.format).
cd "$1" || exit 1
A=app/src/main/java/com/zynergylabs/forager/app
scan() { name=$1; pat=$2
  grep -rlE "^import ($pat)" "$A" --include=*.kt | sed "s|$A/||" | sort > "$OUT/04-files-$name.txt"
  n=$(wc -l < "$OUT/04-files-$name.txt")
  by=$(awk -F/ '{ if (NF==1) p="(root)"; else if ($1=="ui"||$1=="data") p=$1"/"$2; else p=$1; c[p]++ } END { for (k in c) printf "%s=%d ", k, c[k] }' "$OUT/04-files-$name.txt")
  printf '%s\t%s\t%s\n' "$name" "$n" "$by"
}
OUT=$2
scan maplibre 'org\.maplibre'
scan camerax 'androidx\.camera'
scan camera2 'android\.hardware\.camera2'
scan location 'android\.location'
scan sensors 'android\.hardware\.(Sensor|GeomagneticField)'
scan notifications 'android\.app\.Notification|androidx\.core\.app\.Notification'
scan alarms 'android\.app\.(AlarmManager|PendingIntent)'
scan workmanager 'androidx\.work'
scan room 'androidx\.room|androidx\.sqlite'
scan datastore 'androidx\.datastore'
scan retrofit 'retrofit2'
scan okhttp 'okhttp3|okio'
scan serialization 'kotlinx\.serialization'
scan permissions 'android\.Manifest|androidx\.core\.content\.ContextCompat|android\.content\.pm\.PackageManager'
scan activityresult 'androidx\.activity\.result|androidx\.activity\.compose\.rememberLauncherForActivityResult'
scan intents 'android\.content\.Intent'
scan uri_contentresolver 'android\.net\.Uri|android\.content\.ContentResolver|androidx\.documentfile|android\.provider'
scan fileprovider 'androidx\.core\.content\.FileProvider'
scan context 'android\.content\.Context'
scan log 'android\.util\.Log'
scan graphics_bitmap 'android\.graphics'
scan exif 'androidx\.exifinterface|android\.media'
scan connectivity 'android\.net\.(ConnectivityManager|Network)'
scan os 'android\.os'
scan view_window 'android\.view|android\.widget|androidx\.compose\.ui\.viewinterop|androidx\.core\.view'
scan lifecycle 'androidx\.lifecycle'
scan activity 'androidx\.activity'
scan compose 'androidx\.compose'
scan compose_platform 'androidx\.compose\.ui\.platform\.(LocalContext|LocalView|LocalConfiguration)'
scan material_icons 'androidx\.compose\.material\.icons'
scan r_resources 'com\.zynergylabs\.forager\.app\.R$|androidx\.compose\.ui\.res'
scan java_time 'java\.time'
scan java_io 'java\.io|java\.nio'
scan java_util 'java\.util'
scan java_other 'java\.(text|net|security|math|lang)|javax\.'
scan mgrs 'mil\.nga'
scan btools 'btools\.'
scan any_android 'android\.|androidx\.|org\.maplibre|retrofit2|okhttp3|okio|btools\.|mil\.nga'
scan android_not_compose 'android\.|androidx\.(activity|camera|core|datastore|documentfile|exifinterface|lifecycle|room|sqlite|work)|org\.maplibre|retrofit2|okhttp3|okio|btools\.|mil\.nga'
# files whose only non-Compose platform imports are android.util.Log and/or material icons:
grep -rlE '^import (android\.|androidx\.(activity|camera|core|datastore|documentfile|exifinterface|lifecycle|room|sqlite|work)|org\.maplibre|retrofit2|okhttp3|okio|btools\.|mil\.nga)' "$A" --include=*.kt | while read f; do
  if ! grep -E '^import (android\.|androidx\.(activity|camera|core|datastore|documentfile|exifinterface|lifecycle|room|sqlite|work)|org\.maplibre|retrofit2|okhttp3|okio|btools\.|mil\.nga)' "$f" | grep -qv '^import android\.util\.Log$'; then echo "$f"; fi
done | sed "s|$A/||" | sort > "$OUT/04-files-only_log.txt"
printf 'only_log (subset of android_not_compose bound by android.util.Log alone)\t%s\n' "$(wc -l < "$OUT/04-files-only_log.txt")"
