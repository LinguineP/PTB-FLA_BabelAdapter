#! /bin/bash

if [[ $# -lt 1 ]] || [[ $# -gt 3 ]]; then
	echo "Usage sudo ./install.sh <username> [primary|follower] [autonomous|]"
	exit 1
fi

username=$1

if [[ $# -eq 2 ]]; then
	type=$2
else
	type=follower
fi

if [[ $# -eq 3 ]]; then
	autonomous=true
else
	autonomous=false
fi

if ! id "$1" >/dev/null 2>&1; then
    	echo "User $1 does not exists"
  	exit 1
fi

if ! test -d /home/$1/sample-applications/Presentation-App; then
	echo "Directory /home/$1/sample-application/Presentation-App does not exists."
	exit 1
fi	

if [[ $type != leader ]] && [[ $type != follower ]]; then
	echo "Usage sudo ./install.sh <username> [primary|follower]"
	exit 1
fi

host=$(hostname)

if ! test -d /home/$1/swarmNimbus-logs; then
	mkdir /home/$1/swarmNimbus-logs
fi

cat swarmNimbus.template | sed "s/<AUTONOMOUS>/$autonomous/g" > swarmNimbus2.tmp
cat swarmNimbus2.tmp | sed "s/<USER>/$1/g" > swarmNimbus1.tmp
cat swarmNimbus1.tmp | sed "s/<HOST>/$host/g" > swarmNimbus.tmp

if [[ $type == leader ]]; then
	cat swarmNimbus.tmp | sed "s/<DISCOVERY>/HyParView.Contact=none/g" > swarmNimbus.service
else
	cat swarmNimbus.tmp | sed "s/<DISCOVERY>//g" > swarmNimbus.service
fi

cat swarmNimbus.service

rm swarmNimbus2.tmp
rm swarmNimbus1.tmp
rm swarmNimbus.tmp

mv swarmNimbus.service /etc/systemd/system/

systemctl daemon-reload

currDir=$(pwd)

cd .. && mvn clean package -U && cd $currDir

systemctl enable swarmNimbus

echo "install complete"
