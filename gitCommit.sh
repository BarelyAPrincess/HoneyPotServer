#!/bin/bash

# A helper script for making commits for the root repository and using the same commit message for each submodule.
# This script will commit all changes, to stage indivigual changes -- don't use this script!

if [ -z "$1" ]; then
	echo "ERROR: You must specify a commit message!"
	exit 1
fi

if [ -z "$(git status --short)" ]; then
	echo "NOTICE: There are no changes to commit!"
else
	DIR=`dirname $0`
	DIR=`realpath $DIR`
	HASH=`git rev-parse --short HEAD`

	cd "$DIR/modules/AmeliaCommonLib"
	echo "Submodule: $(pwd)"

	if [ -n "$(git status --short)" ]; then
		git add --all
		git commit -m "HPS $HASH: $1"
		git push
	else
		echo "NOTICE: No Changes!"
	fi

	cd "$DIR/modules/AmeliaStorageLib"
	echo "Submodule: $(pwd)"

	if [ -n "$(git status --short)" ]; then
		git add --all
		git commit -m "HPS $HASH: $1"
		git push
	else
		echo "NOTICE: No Changes!"
	fi

	cd "$DIR/modules/AmeliaScriptingLib"
	echo "Submodule: $(pwd)"

	if [ -n "$(git status --short)" ]; then
		git add --all
		git commit -m "HPS $HASH: $1"
		git push
	else
		echo "NOTICE: No Changes!"
	fi

	cd "$DIR/modules/AmeliaEventsLib"
	echo "Submodule: $(pwd)"

	if [ -n "$(git status --short)" ]; then
		git add --all
		git commit -m "HPS $HASH: $1"
		git push
	else
		echo "NOTICE: No Changes!"
	fi

	cd "$DIR/modules/AmeliaLogLib"
	echo "Submodule: $(pwd)"

	if [ -n "$(git status --short)" ]; then
		git add --all
		git commit -m "HPS $HASH: $1"
		git push
	else
		echo "NOTICE: No Changes!"
	fi

        cd "$DIR/modules/AmeliaNetworkingLib"
        echo "Submodule: $(pwd)"

        if [ -n "$(git status --short)" ]; then
                git add --all
                git commit -m "HPS $HASH: $1"
                git push
        else
                echo "NOTICE: No Changes!"
        fi

        cd "$DIR/modules/AmeliaUsersLib"
        echo "Submodule: $(pwd)"

        if [ -n "$(git status --short)" ]; then
                git add --all
                git commit -m "HPS $HASH: $1"
                git push
        else
                echo "NOTICE: No Changes!"
        fi

	cd "$DIR"
	echo "Root Repository: $(pwd)"

	git add --all
	git commit -m "$1"
	git push

	echo "MSG: Finished!"
fi

exit 0
