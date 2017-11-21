#!/bin/bash
find . -print0 | while IFS= read -r -d '' file
do 
	echo "$file"
	if [[ $file == *.Z ]]
	then
		echo "uncompress $file"
		uncompress $file
	else
		if [[ $file == *.0Z ]]
		then
			echo "uncompress $file"
			s=$(echo ${file##*/}| cut -d '.' -f 1)
			cat ${file::-3}.* | uncompress > ${file::-3}.txt
		fi		
	fi		
done
