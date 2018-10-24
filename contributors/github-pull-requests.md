# GitHub Pull Requests

Thanks for taking the time to read about creating a pull request! There's just a few things you should be aware of before you make one.

#### Please Follow These Points:

* Be sure you're following our code style guidelines.
* If your PR is a work-in-progress, you should put \[WIP\] in the title.
* If your PR updates our API in compliance of IETF RFCs, make sure you title includes the RFC, e.g., "Add Salted Challenge Auth to TCP protocol. \(RFC 5802\)"
* Please be descriptive in your initial PR comment. Address your changes in detail, and explain why you made each change.
* If your PR is intended to improve performance, please include benchmarks or other proof unless improvement is obvious, e.g., `for(;;)` vs. `do...while`.
* PRs that fix bugs are acceptable and encouraged.
* PRs that implement a discreet chunk of a feature instead of the whole feature are acceptable under certain circumstances, eg, implementing cows instead of all of the animals.
* PRs that only change whitespace or other styling issues will not be accepted; please bring this to the attention of the repository maintainers or contributors instead.
* From time to time, we may go through the open PRs and close any that we deem to be inactive. Feel free to re-open any PR that has been closed as a result of this when you work on it again.
  * If you're planning to suspend work on your PR for a period of time \(eg, going on holiday\), feel free to mention this in a comment and we'll take it on board when looking at inactive PRs.
* If multiple people have contributed to the PR, please credit them by @mentioning them in your overall commit message.
* If you don't do it yourself, your PR may be closed and the commits squashed and merged manually by a repository maintainer.

#### Pull Request Conventions:

* The number of commits in a pull request should be kept to a minimum \(squish them into one most of the time - use common sense!\).
* No merges should be included in pull requests unless the pull request's purpose is a merge.
* Pull requests should be tested \(does it compile? AND does it work?\) before submission.
* Any major additions should have documentation ready and provided if applicable \(this is usually the case\). New features should include example snippets.
* Most pull requests should be accompanied by a corresponding GitHub ticket so we can associate commits with GitHub issues \(this is primarily for changelog generation\).

Thanks for contributing!

