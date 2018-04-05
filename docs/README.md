![HPSLogo](http://penoaks.com/assets/external/HoneyPotServerLogo.png "Honey Pot Server Logo")

# Introduction
**Honey Pot Server** is a multi-protocol networking server allowing for both dynamic and static content via a powerful subsystem consisting of modular classes, plugins, and built-in Groovy scripting language. Features also include ORM, events, clustering, users, permissions, enforced access policies, and much more. Nearly everything was designed with an extendable concept in mind and can be extended using the provided API. HPS is intended to provide easy-access to the best of the best Java 8 features without the mess and time constants of traditional Java servers. HPS also honors convention over configuration, most everything will work with little to no additional setup, even its libraries are downloaded directly from Maven. We strive to make the code base as easy to understand as possible by leaving out needless interfaces and classes whenever possible.

You can find our official documentation at https://hps-docs.penoaks.com/. It contains tutorials and advanced features on how to utilize the server directly from the developers.

**NOTICE: HPS is under extensive heavy-development. It is possible that claims made by this README are not yet valid.**

# How To Build

## Prerequisites

 * Java 8

HPS is tested and compiled using the [Oracle JDK](http://oracle.com/technetwork/java/javase/downloads). For best experience, we recommend you do the same, otherwise, the [OpenJDK](http://openjdk.java.net/) should also work. HPS is untested with Java 9.

## Building This Repository

Git Branches:

 1. **master-dev** (default) is where the most unstable and untested code is located, this includes new features and substantial code changes.
 2. **incubating** is where the most bleeding edge code is located before it become released as stable; once the version is deemed stable, changes are then backported to the `master-dev` branch.
 3. **stable** is where the stable code is located. With each stable release, a merged pull request will be made on this repository.

Pull requests that fix security and app breaking bugs should be made against the `incubating` branch (if relevant). All others must be made against the `master-dev` branch.

```bash
git clone --recursive https://github.com/TheAmeliaDeWitt/HoneyPotServer
cd HoneyPotServer
```

## Using Gradle

HPS is easily built using Gradle. If you are new to Gradle, just execute the `./gradlew build` (or `grandle.bat build` for Windows) script and you should find a fresh build in the `build/dest` directory.

Keep in mind that you might encounter compilation errors if building from the `master-dev` branch.

## Using IntelliJ IDEA

Coming Soon

## Contributing

First of all, thank you for your interest in advancing HPS! We always love to see new developers work on the project! You can find all of our resources on how to get started below.

### Coding
Our Gradle Build environment uses the CodeStyle plugin to check coding standards, as follows:

* Please attempt at making your code as easily understandable as possible. Use varibles with human-readable names, like `mergeWithParent` instead of `var2`. We also recommend when possible to use human-readable names for both lambda expressions and generics.
* Leave comments whenever possible. Adding Javadoc is even more appreciated when possible.
* No spaces; use tabs. We like our tabs, sorry. What do you think this is - the dark ages?
* No trailing whitespace.
* Brackets should always be on a new line. Does not impact complition and makes code easier to understand.
* No 80 column limit or 'weird' mid-statement newlines, try to keep your entire statement on one line.

### Pull Request Conventions
* The number of commits in a pull request should be kept to a minimum (squish them into one most of the time - use common sense!).
* No merges should be included in pull requests unless the pull request's purpose is a merge.
* Pull requests should be tested (does it compile? AND does it work?) before submission.
* Any major additions should have documentation ready and provided if applicable (this is usually the case). New features should include example snippets.
* Most pull requests should be accompanied by a corresponding GitHub ticket so we can associate commits with GitHub issues (this is primarily for changelog generation).

# Why Honey Pot Server?
Because Winnie The Pooh loves honey!

# License
**Honey Pot Server** is licensed under the MIT License. If you decide to use our server or use any of our code (In part or whole), PLEASE, we would love to hear about it. We don't require this but it's generally cool to hear what others do with our stuff.

* Copyright (c) 2017 Amelia DeWitt <TheAmeliaDeWitt@gmail.com>
* Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
* All Rights Reserved.
