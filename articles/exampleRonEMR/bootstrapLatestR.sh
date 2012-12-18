#!/bin/bash
cp /etc/apt/sources.list ~/sources.listOrig
cat > ~/sources.list <<EOF
deb http://http.us.debian.org/debian    lenny          main contrib non-free
deb http://http.us.debian.org/debian    stable         main contrib non-free
deb http://security.debian.org         lenny/updates  main contrib non-free
deb http://security.debian.org         stable/updates main contrib non-free
deb http://cran.fhcrc.org/bin/linux/debian lenny-cran/
EOF
sudo cp ~/sources.list /etc/apt/sources.list
sudo apt-get update
sudo apt-get -t lenny-cran install --yes --force-yes r-base r-base-dev
R --version
exit 0

