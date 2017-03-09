#!/bin/bash
i=0
while IFS='' read -r line || [[ -n "$line" ]]; do
	i=$(($i+1))
    #printf "$line\n"
done < "$1"
echo $i
