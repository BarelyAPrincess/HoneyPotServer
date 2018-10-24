# Setup and Building

### Prerequisites

* Java 8

_Honey Pot Server_ is tested and compiled using the [Oracle JDK](http://oracle.com/technetwork/java/javase/downloads). For best experience, we recommend you do the same, otherwise, the [OpenJDK](http://openjdk.java.net/) should also work. _Honey Pot Server_ is untested with Java 9.

### Building This Repository

Git Branches:

1. **master-dev** \(default\) is where the most unstable and untested code is located, this includes new features and substantial code changes.
2. **incubating** is where the most bleeding edge code is located before it become released as stable; once the version is deemed stable, changes are then backported to the `master-dev` branch.
3. **stable** is where the stable code is located. With each stable release, a merged pull request will be made on this repository.

Pull requests that fix security and app breaking bugs should be made against the `incubating` branch \(if relevant\). All others must be made against the `master-dev`branch.

```text
git clone --recursive https://github.com/BarelyAPrincess/HoneyPotServer
cd HoneyPotServer
```

### Using Gradle

_Honey Pot Server_ is easily built using Gradle. If you are new to Gradle, just execute the `./gradlew build` \(or `grandle.bat build` for Windows\) script and you should find a fresh build in the `build/dest` directory.

Keep in mind that you might encounter compilation errors if building from the `master-dev` branch.

### Using IntelliJ IDEA

Coming Soon



