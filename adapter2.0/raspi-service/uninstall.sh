#! /bin/bash

if ! test -f /etc/systemd/system/swarmNimbus.service; then
	echo "Service 'swarmNimbus' is not installed."
	exit 1
fi	

systemctl stop swarmNimbus

systemctl disable swarmNimbus

rm /etc/systemd/system/swarmNimbus.service

systemctl daemon-reload

echo "uninstall complete"
