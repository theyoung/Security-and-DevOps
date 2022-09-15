#!/usr/bin/env bash

echo -n "Enter Splunk Password(Default=12345678): "
read password

echo -n "Mount Directory(Default=/home/): "
read source_dir

echo -n "Time zone(Default=Asia/Seoul): "
read time_zone

echo -n "http Port(Default=8008): "
read port

if [[ -z "${password}" ]]; then
    password="12345678"
fi

if [[ -z "${source_dir}" ]]; then
    source_dir="/home/"
fi

if [[ -z "${time_zone}" ]]; then
    time_zone="Asia/Seoul"
fi

if [[ -z "${port}" ]]; then
    port="8008"
fi

docker run -d -p "${port}":8000 -e "SPLUNK_START_ARGS=--accept-license" -e "SPLUNK_PASSWORD=$password" --mount type=bind,source="${source_dir}",target=/mnt  -v /etc/localtime:/etc/localtime:ro -e TZ="${time_zone}" --name splunk splunk/splunk:latest

# shellcheck disable=SC2181
if [[ $? -eq 0 ]]; then
    echo "Successfully deployed!. You can connect on http://localhost:${port} with id=admin, password=${password}"
else
    echo "Failure deployed!"
fi