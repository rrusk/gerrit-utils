#Gerrit Code Review - Quick Start

##SSH key generation

###Please don’t generate new keys if you already have a valid keypair! They will be overwritten!

  user@host:~$ ssh-keygen -t rsa
  Generating public/private rsa key pair.
  Enter file in which to save the key (/home/user/.ssh/id_rsa):
  Created directory '/home/user/.ssh'.
  Enter passphrase (empty for no passphrase):
  Enter same passphrase again:
  Your identification has been saved in /home/user/.ssh/id_rsa.
  Your public key has been saved in /home/user/.ssh/id_rsa.pub.
  The key fingerprint is:
  00:11:22:00:11:22:00:11:44:00:11:22:00:11:22:99 user@host
  The key's randomart image is:
  +--[ RSA 2048]----+
  |     ..+.*=+oo.*E|
  |      u.OoB.. . +|
  |       ..*.      |
  |       o         |
  |      . S ..     |
  |                 |
  |                 |
  |          ..     |
  |                 |
  +-----------------+
  user@host:~$

##Registering your key in Gerrit

Open a browser and enter the url of the Gerrit server:
http://gerrit.seng.uvic.ca:8080.  You will have to be on the UVic
network to access this URL.

Register a new account in Gerrit through the web interface with the
email address of your choice.  The authentication type is OpenID so a
Google or Yahoo OpenID will work.

Once signed in, you find a little wizard to get you started. The
wizard helps you fill out:

* Real name (visible name in Gerrit)
* Register your email (it must be confirmed later)
* Select a username with which to communicate with Gerrit over ssh+git
  (be sure to press the select button after entering your username
  into the dialog box)
* The server will ask you for an RSA public key. That’s the key we
  generated above, and it’s time to make sure that Gerrit knows about
  our new key and can identify us by it.

  user@host:~$ cat .ssh/id_rsa.pub
  ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA1bidOd8LAp7Vp95M1b9z+LGO96OEWzdAgBPfZPq05jUh
  jw0mIdUuvg5lhwswnNsvmnFhGbsUoXZui6jdXj7xPUWOD8feX2NNEjTAEeX7DXOhnozNAkk/Z98WUV2B
  xUBqhRi8vhVmaCM8E+JkHzAc+7/HVYBTuPUS7lYPby5w95gs3zVxrX8d1++IXg/u/F/47zUxhdaELMw2
  deD8XLhrNPx2FQ83FxrjnVvEKQJyD2OoqxbC2KcUGYJ/3fhiupn/YpnZsl5+6mfQuZRJEoZ/FH2n4DEH
  wzgBBBagBr0ZZCEkl74s4KFZp6JJw/ZSjMRXsXXXWvwcTpaUEDii708HGw== John Doe@MACHINE
  user@host:~$

###Important
Please take note of the extra line-breaks introduced in the key above
for formatting purposes. Please be sure to copy and paste your key
without line-breaks.

Copy the string starting with ssh-rsa to your clipboard and then paste
it into the box for RSA keys. Make absolutely sure no extra spaces or
line feeds are entered in the middle of the RSA string.

##Verify that the ssh connection works for you.

  user@host:~$ ssh user@gerrit.seng.uvic.ca -p 29418
  The authenticity of host '[gerrit.seng.uvic.ca]:29418 ([127.0.0.1]:29418)' can't be established.
  RSA key fingerprint is db:07:3d:c2:94:25:b5:8d:ac:bc:b5:9e:2f:95:5f:4a.
  Are you sure you want to continue connecting (yes/no)? yes
  Warning: Permanently added '[gerrit.seng.uvic.ca]:29418' (RSA) to the list of known hosts.

  ****    Welcome to Gerrit Code Review    ****

  Hi user, you have successfully connected over SSH.

  Unfortunately, interactive shells are disabled.
  To clone a hosted Git repository, use:

  git clone ssh://user@gerrit.seng.uvic.ca:29418/REPOSITORY_NAME.git

  user@host:~$

##Project creation

###Clone Oscar McMaster RELEASE_12_1
Download a local clone of the OSCAR repository and move into it

  user@host:~$ git clone ssh://user@host:29418/seng371/oscar
  Cloning into oscar...
  remote: Counting objects: 2, done
  remote: Finding sources: 100% (2/2)
  remote: Total 2 (delta 0), reused 0 (delta 0)
  user@host:~$ cd oscar
  user@host:~/oscar$ git checkout RELEASE_12_1
  user@host:~/oscar$ git status
  user@host:~/oscar$

### Create your own project branch
Make a branch for your teams development.  Use your NetlinkID as the branch name.
  user@host:~/oscar$ git branch <branch>
  user@host:~/oscar$ git checkout <branch>

### Then make a change to it and upload it as a reviewable change in Gerrit.

  user@host:~/oscar$ date > testfile.txt
  user@host:~/oscar$ git add testfile.txt
  user@host:~/oscar$ git commit -m "My pretty test commit"
  [master ff643a5] My pretty test commit
   1 files changed, 1 insertions(+), 0 deletions(-)
   create mode 100644 testfile.txt
  user@host:~/oscar$

### Make sure that you have the change-id commit hook installed

  Subsitute your username for "user"

  user@host:~/oscar$ gitdir=$(git rev-parse --git-dir); scp -p -P 29418 user@gerrit.seng.uvic.ca:hooks/commit-msg ${gitdir}/hooks/

####Usually when you push to a remote git, you push to the reference
    '/refs/heads/branch', but when working with Gerrit you have to
    push to a virtual branch representing "code review before
    submission to branch". This virtual name space is known as
    /refs/for/<branch>

  user@host:~/oscar$ git push origin HEAD:refs/for/<branch>
  Counting objects: 4, done.
  Writing objects: 100% (3/3), 293 bytes, done.
  Total 3 (delta 0), reused 0 (delta 0)
  remote:
  remote: New Changes:
  remote:   http://gerrit.seng.uvic.ca:8080/1
  remote:
  To ssh://user@gerrit.seng.uvic.ca:29418/oscar
   * [new branch]      HEAD -> refs/for/<branch>
  user@host:~/oscar$

You should now be able to access your change by browsing to the http
URL suggested by your output message.

####To make Gerrit push for review easier, set up some Git aliases
  git config remote.review.pushurl ssh://gerrit.seng.uvic.ca:29418/seng371/oscar.git
  git config remote.review.push refs/heads/*:refs/for/*
  git push review # this will push your current branch up for review

