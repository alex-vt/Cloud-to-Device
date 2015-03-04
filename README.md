##Cloud to Device

An Android app for regular local backups of files from cloud storage services.

###Description

Stores file versions with timestamps and revisions.

Runs periodically in background.

Runs on boot.

###Building and installing

After specifying APP_KEY and APP_SECRET in `MainActivity.java` and `AndroidManifest.xml`, run

    gradlew installDebug
