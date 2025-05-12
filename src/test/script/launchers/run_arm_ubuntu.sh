#!/bin/bash

# Vérifier si un fichier d'entrée est fourni
if [ $# -eq 0 ]; then
    echo "Usage: $0 <fichier_assemblage.s>"
    exit 1
fi

# Récupérer le fichier d'entrée
input_file="$1"


# Définir les noms de sortie
output_object="test.o"
output_executable="test"

arm-linux-gnueabi-as -march=armv7-a -mfpu=vfpv3 -o "$output_object" "$input_file"

arm-linux-gnueabi-gcc -o "$output_executable" "$output_object" -march=armv7-a -mfpu=vfpv3 -z noexecstack

qemu-arm -L /usr/arm-linux-gnueabi "./$output_executable"
