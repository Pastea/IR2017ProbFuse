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
			mv $file ${file::-3}0.Z
			uncompress ${file::-3}0.Z
			rm ${file::-3}0.Z
		fi
		if [[ $file == *.1Z ]]
		then
			echo "uncompress $file"
			mv $file ${file::-3}1.Z
			uncompress ${file::-3}1.Z
			rm ${file::-3}1.Z
		fi	
		if [[ $file == *.2Z ]]
		then
			echo "uncompress $file"
			mv $file ${file::-3}2.Z
			uncompress ${file::-3}2.Z
			rm ${file::-3}2.Z
		fi		
	fi		
done
