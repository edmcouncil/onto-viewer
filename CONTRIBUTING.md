<img src="https://spec.edmcouncil.org/fibo/htmlpages/master/latest/img/logo.66a988fe.png" width="150" align="right"/>

# How to contribute
Since the first release of onto-viewer, [this  repository](https://github.com/edmcouncil/onto-viewer) is considered to be the only official space for the community discussion. So, this repository is where the project community develops the new proposals and discusses changes (via GitHub issues).


If you want to contribute to onto-viewer, you'll need to create a login for [GitHub](https://github.com). Then you can contribute to onto-viewer in two ways. 

The first way is to suggest changes via GitHub issues, e.g., the suggested changes may concern the GUI of application.

The second way is to contribute directly to the code. For that purpose, you'll need to do the following things: 

* Install a git client, e.g., [Sourcetree](https://www.sourcetreeapp.com) from Atlassian
* Make a "fork" of the [onto-viewer](https://github.com/edmcouncil/onto-viewer) repository. 
* Clone your fork to your local repository.
* Submit a Pull Request to the [onto-viewer](https://github.com/edmcouncil/onto-viewer) repository.


# Developer Certificate of Origin (DCO) 

We use [Probot / DCO framework](https://github.com/probot/dco) to enforce the Developer Certificate of Origin (DCO) on Pull Requests. It requires all commit messages to contain the Signed-off-by line with an email address that matches the commit author.

Please read the full text of the [DCO](DCO).

Contributors sign-off that they adhere to these requirements by adding a Signed-off-by line to commit messages.


# Release Management
Release management (corresponding to software versioning in source code management) will follow the widely accepted  [Git's branching model](https://nvie.com/posts/a-successful-git-branching-model/).


## Versioning Policy
Versioning policy rules are the same for all components created in the project. All components should have the same version number (two first sequences) to reflect their proper cooperation. It means that there can be situations in which one component will have version changed, even if there were no code change.

A version number contains three sequences on numbers separated with a "." (dot) sign. A sample of version number is the following: 1.1.2. Rules for sequences and numbers:

* first two sequences reflect the version number of all services, frontend, and apps that should be deployed and run together,
* the first sequence is changed when API is changed,
* the second sequence is changed with every release,
* The last sequence indicates a version with a bug fix in release branch or production.

## Branching Policy
There is one main branch for source code in repository:

* develop – main branch, reflects production version of the application,

Additional branches used in versioning:

* feature branches – derives from develop, name of this branch is based on the name of the feature after feature is done the branch is merged with develop branch,
* release branch – derives from develop, branch is dedicated to the new release, name of the branch comes from release version number (release_v1.1); 
    * release branch is tagged (the version of the release: v1.1.3) and merged to develop branch after production release; 
    * release branch is merged with master branch during production deployment
* bugfix branches – derives from the master branch, used for critical bug fixing (bug found in production version; bugfix_v1.1.3)
