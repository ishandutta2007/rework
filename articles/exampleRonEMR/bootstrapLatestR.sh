#!/bin/bash

# For more information about what this script is doing, see http://cran.fhcrc.org/bin/linux/debian/

cp /etc/apt/sources.list ~/sources.listOrig
cp /etc/apt/sources.list ~/
cat >> ~/sources.list <<EOF
deb http://cran.fhcrc.org/bin/linux/debian squeeze-cran/
EOF
sudo cp ~/sources.list /etc/apt/sources.list
sudo apt-key adv --keyserver subkeys.pgp.net --recv-key 381BA480
sudo apt-get update
sudo apt-get install --yes r-base
R --version
exit 0

