#!/bin/bash

# A bash helper script for updating all submodules and root project from their remote repository.

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

git_pull_submodule () {
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

git_state
if [[ "$?" -eq "-1" ]]; then
	echo "FATAL: The repo has diverted from the remote repo!"
	exit 1
fi

DIR=`dirname $0`
DIR=`realpath $DIR`

git submodule update --remote --merge

#for MOD in `cat .gitmodules | grep "path = " | cut -d' ' -f3`; do
#	git_pull_submodule "${MOD}"
#done

echo "Finished!"