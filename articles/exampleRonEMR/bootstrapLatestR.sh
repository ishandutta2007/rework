#!/bin/bash

# For more information about what this script is doing, see http://cran.fhcrc.org/bin/linux/debian/

cp /etc/apt/sources.list ~/sources.listOrig
cat > ~/sources.list <<EOF
deb http://cran.fhcrc.org/bin/linux/debian squeeze-cran/
EOF
cat ~/sources.listOrig >> ~/sources.list
sudo cp ~/sources.list /etc/apt/sources.list
sudo apt-key adv --keyserver subkeys.pgp.net --recv-key 381BA480
sudo apt-get update
sudo apt-get -t squeeze-cran install --yes r-base r-base-dev
R --version
exit 0

