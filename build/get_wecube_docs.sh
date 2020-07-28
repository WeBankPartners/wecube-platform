#!/bin/bash
wecube_docs_dirname=wecube-docs
wecube_docs_url=https://github.com/WeBankPartners/wecube-docs.git
wecube_docs_branch=gh-pages
# run make doc before make build, to build local docs within portal 
repo_ready=0
# check if repo dir ready
if [ -d "$wecube_docs_dirname" ]; then
    cd "$wecube_docs_dirname"
    test -d ".git" && git status > /dev/null 2>&1
    [[ $? -eq 0 ]] && repo_ready=1 || repo_ready=0
    cd ..
fi
# try to clone repo
if [ $repo_ready -eq 0 ]; then
    git clone "$wecube_docs_url" "$wecube_docs_dirname"
    [[ $? -ne 0 ]] && echo "wecube docs repo clone failed" || repo_ready=1
fi
# try to checkout branch
if [ $repo_ready -eq 1 ]; then
    cd "$wecube_docs_dirname"
    doc_cur_branch=`git symbolic-ref --short -q HEAD`
    if [ "$doc_cur_branch" != "$wecube_docs_branch" ];then
        git checkout -b "$wecube_docs_branch" "origin/$wecube_docs_branch" || git checkout "$wecube_docs_branch"
    fi
    git reset --hard "origin/$wecube_docs_branch"
    git pull
    cd ..
    echo "wecube docs repo is ready"
    exit 0
else
    echo "wecube docs repo is not ready, docs will be empty..."
    exit 1
fi