#!/bin/bash

# A bash helper script for committing submodule code before also committing the root repository.
# This script will commit all changes, to stage individual changes -- don't use this script!
# This script is intended to be ran from the root directory of the root project.

git_state () {
	git remote -v update

	LOCAL="$(git rev-parse @)"
	REMOTE="$(git rev-parse @{u})"
	BASE="$(git merge-base @ @{u})"

	if [[ ${LOCAL} = ${REMOTE} ]]; then
		echo "Status: Up-to-date"
		return 0
	elif [[ ${LOCAL} = ${BASE} ]]; then
		echo "Status: Need to pull"
		return 1
	elif [[ ${REMOTE} = ${BASE} ]]; then
		echo "Status: Need to push"
		return 2
	else
		echo "Status: Diverged"
		return -1
	fi
}

commit_submodule_merge () {
		read -p "The remote repo has changes not yet merged, would you like to (m)erge, (c)ontinue, or (a)bort? " -n 1 -r
		echo

		if [[ $REPLY =~ ^[Mm]$ ]]; then
			git pull
			git mergetool
		elif [[ $REPLY =~ ^[Cc]$ ]]; then
			return
		elif [[ $REPLY =~ ^[Aa]$ ]]; then
			exit 1
		esle
			commit_submodule_merge
		fi
}

commit_submodule () {
	if [[ -z "$1" ]]; then
		echo "No submodule path specified. :("
		return
	fi

	if [[ ! -d "$DIR/$1" ]]; then
		echo "Fatal: directory '$DIR/$1' does not exist!"
		exit 1
	fi

	cd "$DIR/$1"
	echo "Now Committing Submodule $1"

	git_state
	STATE=$?
	if [[ "$STATE" -eq "1" ]]; then
		commit_submodule_merge
	elif [[ "$STATE" -eq "-1" ]]; then
		exit 1
	fi

	if [[ -n "$(git status --short)" ]]; then
		git add --all
		git commit -m "$MSG"
		git push
	elif [[ "$STATE" -eq "2" ]]; then
		git push
	else
		echo "Nothing to Commit!"
	fi

	cd "$DIR"
}

if [[ -z "$1" ]]; then
	echo "ERROR: You must specify a commit message!"
	exit 1
fi

git_state
if [[ "$?" -eq "1" ]]; then
	echo "ERROR: The remote repo has changes not yet merged!"
	exit 1
elif [[ "$?" -eq "-1" ]]; then
	exit 1
fi

DIR=`dirname $0`
DIR=`realpath $DIR`
HASH=`git rev-parse --short HEAD`
MSG="$(basename $(pwd)) $HASH: $1"

for MOD in `cat .gitmodules | grep "path = " | cut -d' ' -f3`; do
	commit_submodule "${MOD}"
done

if [[ -z "$(git status --short)" ]]; then
	echo "There are no changes to commit!"
else
	cd "$DIR"
	echo "Root Repository: $(pwd)"

	git add --all
	git commit -m "$1"
	git push

	echo "Finished!"
fi

exit 0
