![HPSLogo](http://penoaks.com/assets/external/HoneyPotServerLogo.png "Honey Pot Server Logo")

# Introduction
**Honey Pot Server** is a multi-protocol networking server allowing for both dynamic and static content via a powerful subsystem consisting of modular classes, plugins, and built-in Groovy scripting language. Features also include ORM, events, clustering, users, permissions, enforced access policies, and much more. Nearly everything was designed with an extendable concept in mind and can be extended using the provided API. HPS is intended to provide easy-access to the best of the best Java 8 features without the mess and time constants of traditional Java servers. HPS also honors convention over configuration, most everything will work with little to no additional setup, even its libraries are downloaded directly from Maven. We strive to make the code base as easy to understand as possible by leaving out needless interfaces and classes whenever possible.

You can find our official documentation at https://hps-docs.penoaks.com/. It contains tutorials and great advanced details on how to utilize the server the possible from the developers themselves.

# How To Build
HPS is easily built using Gradle. If you are new to Gradle, just execute the `./gradlew build` (or `grandle.bat build` for Windows) script and you should find a fresh build in the `build/dest` directory. Keep in mind that you might encounter compilation errors if building from the bleeding-edge (`master-dev`) branch.

# Coding
Our Gradle Build environment uses the CodeStyle plugin to check coding standards, as follows:

* Please attempt at making your code as easily understandable as possible. Use varibles with human-readable names, like `mergeWithParent` instead of `var2`. We also recommend when possible to use human-readable names for both lambda expressions and generics.
* Leave comments whenever possible. Adding Javadoc is even more appreciated when possible.
* No spaces; use tabs. We like our tabs, sorry. What do you think this is - the dark ages?
* No trailing whitespace.
* Brackets should always be on a new line. Does not impact complition and makes code easier to understand.
* No 80 column limit or 'weird' mid-statement newlines, try to keep your entire statement on one line.

# Pull Request Conventions
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
