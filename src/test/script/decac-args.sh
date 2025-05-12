#!/bin/bash

RED='\e[31m'
GREEN='\e[32m'
BLUE='\e[34m'
NC='\e[0m'

# navigate to project root
cd "$(dirname "$0")"/../../.. || exit 1
TEST_PATH="src/test/deca/syntax/valid"

# Fonction pour tester et vérifier le code de retour
test_decac() {
    echo "Testing decac with arguments: $@"
    decac "$@" 2>&1 1> /dev/null | while read line; do echo -e "${GREEN} $line ${NC}"; done
    echo -e "-------------------\n"
}

valid_test_decac() {
    echo "-------------------"
    echo -e "Expected: ${GREEN}PASS${NC}"
    test_decac "$@"
}

invalid_test_decac() {
    echo "-------------------"
    echo -e "Expected: ${GREEN}FAIL${NC}"
    test_decac "$@"
}

# Tests basiques
invalid_test_decac
valid_test_decac -b
invalid_test_decac -b -p test.deca

# Tests de compilation séquentielle
valid_test_decac $TEST_PATH/provided/hello.deca 
valid_test_decac $TEST_PATH/provided/hello.deca $TEST_PATH/provided/hello2.deca
valid_test_decac -p $TEST_PATH/provided/hello.deca
valid_test_decac -v $TEST_PATH/provided/hello.deca
valid_test_decac -n $TEST_PATH/provided/hello.deca
valid_test_decac -r 8 $TEST_PATH/provided/hello.deca
valid_test_decac -d $TEST_PATH/provided/hello.deca
valid_test_decac -w $TEST_PATH/provided/hello.deca

# Tests de compilation parallèle
valid_test_decac -P $TEST_PATH/provided/hello2.deca $TEST_PATH/provided/hello2.deca
valid_test_decac -P $(eval echo "$TEST_PATH/parallel_compile/hello"{1..16}".deca")
valid_test_decac -P -d -r 8 $TEST_PATH/provided/hello.deca $TEST_PATH/provided/hello2.deca

# Tests d'erreurs
invalid_test_decac invalid.txt
invalid_test_decac nonexistent.deca
invalid_test_decac -z $TEST_PATH/hello.deca
invalid_test_decac -r 2 $TEST_PATH/hello.deca
invalid_test_decac -p -v $TEST_PATH/hello.deca
