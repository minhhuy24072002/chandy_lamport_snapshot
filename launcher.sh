#!/bin/bash

# Code adapted from posted example by Jordan Frimpter
# Command to grant permission to file to run [RUN THIS]: chmod +x launcher.sh

# Change this to your netid [CHANGE THIS]
netid=your_netid

# Root directory of project [CHANGE THIS]
PROJDIR=/home/013/h/hm/your_netid/Project1

# Directory where the config file is located on your local system [CHANGE THIS]
CONFIGLOCAL=/home/013/h/hm/your_netid/Project1/config.txt

# Directory your compiled classes are in [CHANGE THIS if you move the classes]
BINDIR=$PROJDIR

# Your main project class [CHANGE THIS if you rename the project]
PROG=Main

# extension for hosts [CHANGE THIS if using a different host system (setting to "" should suffice)]
hostExtension="utdallas.edu"

# remove $CONFIGLOCAL if you don't want to give your program the configuration file path as an argument

# loop through hosts, remove comment lines starting with # and $ and any carriage returns
n=0
# remove comments | remove other comments | remove carriage returns
cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" | sed -e "s/\r$//" |
(
    # read the first valid line and collect only the number of hosts
    read i
    echo $i
    ii=$( echo $i| awk '{ print $1 }' )
    echo Hosts: $ii

    # for each host, loop
    while [[ $n -lt $ii ]]
    do
        # read the port number and host address
    read line
    p=$( echo $line | awk '{ print $1 }' )
        host=$( echo $line | awk '{ print $2 }' )

        # add host extension to string if missing from domain name
            if [[ "$host" != *"$hostExtension"* ]];
        then
            host="$host.$hostExtension"
        fi
        echo $host

        # run command [CHANGE THIS if you want a different pattern or use a different software]
        echo "Test print $CONFIGLOCAL"
        runCommand="java -cp $BINDIR $PROG $n $CONFIGLOCAL"

        # issue command
        echo issued command: gnome-terminal -e "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host $runCommand; exec bash" &
gnome-terminal -e "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host $runCommand; exec bash" &
sleep 1

        # increment loop counter
        n=$(( n + 1 ))
    done
)

echo "Launcher complete"